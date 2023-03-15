package io.github.gaming32.kiloengine

import io.github.gaming32.kiloengine.entity.CameraComponent
import io.github.gaming32.kiloengine.loader.SceneLoader
import io.github.gaming32.kiloengine.loader.SceneLoaderImpl
import io.github.gaming32.kiloengine.util.*
import org.joml.*
import org.joml.Math.*
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.nanovg.NanoVGGL2.NVG_ANTIALIAS
import org.lwjgl.nanovg.NanoVGGL2.nvgCreate
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_MULTISAMPLE
import org.lwjgl.opengl.GL30.*
import org.ode4j.math.DVector3
import org.ode4j.ode.DContact.DSurfaceParameters
import org.ode4j.ode.DContactGeomBuffer
import org.ode4j.ode.OdeConstants.*
import org.ode4j.ode.OdeHelper
import kotlin.math.roundToLong

abstract class KiloEngineGame {
    companion object {
        private const val PHYSICS_SPEED = 0.02
        private const val CONTACT_COUNT = 16
        private const val VERTICAL_DAMPING = 0.01
        private const val HORIZONTAL_DAMPING = 0.17
        val SURFACE_PARAMS = DSurfaceParameters().apply {
            mu = 0.0
        }
        val WALL_PARAMS = DSurfaceParameters().apply {
            mu = 3.0
        }
    }

    init {
        OdeHelper.initODE()
    }

    val scene = Scene()
    private val windowSize = Vector2i()
    private val movementInput = Vector3d()
    var wireframe = false
    private var window = 0L
    private var nanovg = 0L
    private var clearParams = GL_DEPTH_BUFFER_BIT
    lateinit var sceneLoader: SceneLoader
        private set

    fun main() {
        init()
        registerEvents()
        loadInitScene()
        val skybox = scene.skybox?.let {
            withValue(-1, TextureManager::maxMipmap, { TextureManager.maxMipmap = it }) {
                withValue(GL_NEAREST, TextureManager::filter, { TextureManager.filter = it }) {
                    TextureManager.loadAsVirtual(it.down, "skybox/down")
                    TextureManager.loadAsVirtual(it.up, "skybox/up")
                    TextureManager.loadAsVirtual(it.negativeZ, "skybox/negativeZ")
                    TextureManager.loadAsVirtual(it.positiveZ, "skybox/positiveZ")
                    TextureManager.loadAsVirtual(it.negativeX, "skybox/negativeX")
                    TextureManager.loadAsVirtual(it.positiveX, "skybox/positiveX")
                }
            }
            sceneLoader.loadObj("/skybox.obj").toDisplayList()
        }
        var lastTime = glfwGetTime()
        var lastPhysicsTime = lastTime
        var fpsAverage = 0.0
        val contactJointGroup = OdeHelper.createJointGroup()
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents()

            val time = glfwGetTime()
            val deltaTime = time - lastTime
            val fps = 1 / deltaTime
            fpsAverage = 0.95 * fpsAverage + 0.05 * fps
            lastTime = time

            if (time - lastPhysicsTime > 1.0) {
                lastPhysicsTime = time - 1.0
            }
            while (time - lastPhysicsTime > PHYSICS_SPEED) {
                scene.invokeEvent(EventType.PRE_TICK)
                scene.invokeEvent(EventType.HANDLE_MOVEMENT, movementInput)
                movementInput.y = 0.0
                scene.entities.forEach { entity ->
                    entity.body.addForce(
                        entity.body.linearVel.x * -HORIZONTAL_DAMPING / PHYSICS_SPEED,
                        entity.body.linearVel.y * -VERTICAL_DAMPING / PHYSICS_SPEED,
                        entity.body.linearVel.z * -HORIZONTAL_DAMPING / PHYSICS_SPEED
                    )
                }
                val contactBuffer = DContactGeomBuffer(CONTACT_COUNT)
                contactJointGroup.clear()
                scene.space.collide(null) { _, o1, o2 ->
                    repeat(OdeHelper.collide(o1, o2, CONTACT_COUNT, contactBuffer)) {
                        val contact = contactBuffer[it]
                        val e1 = scene.getEntityByBody(contact.g1.body)
                        val e2 = scene.getEntityByBody(contact.g2.body)
                        if (e1 == null || e2 == null) return@repeat
                        var surfaceParams = e1.collideWithEntity(e2, contact, true)
                        val surfaceParams2 = e2.collideWithEntity(e1, contact, false)
                        surfaceParams = surfaceParams ?: surfaceParams2
                        if (surfaceParams == null) return@repeat
                        val joint = OdeHelper.createContactJoint(
                            scene.world,
                            contactJointGroup,
                            DContact(contact, surfaceParams)
                        )
                        joint.attach(contact.g1.body, contact.g2.body)
                    }
                }
                scene.world.quickStep(PHYSICS_SPEED)
                scene.invokeEvent(EventType.TICK)
                lastPhysicsTime += PHYSICS_SPEED
            }

            // 3D Mode
            glEnable(GL_CULL_FACE)

            for (camera in scene.getComponents<CameraComponent>()) {
                val aspect: Float
                if (camera.renderArea != null) {
                    glBindFramebuffer(GL_FRAMEBUFFER, 0)
                    val w = (camera.renderArea.second.x - camera.renderArea.first.x) * windowSize.x
                    val h = (camera.renderArea.second.y - camera.renderArea.first.y) * windowSize.y
                    glViewport(
                        (camera.renderArea.first.x * windowSize.x).toInt(),
                        (camera.renderArea.first.y * windowSize.y).toInt(),
                        w.toInt(), h.toInt()
                    )
                    aspect = w / h
                } else if (camera.textureResolution != null) {
                    glViewport(0, 0, camera.textureResolution.x, camera.textureResolution.y)
                    glBindFramebuffer(GL_FRAMEBUFFER, camera.framebuffer)
                    aspect = camera.textureResolution.x.toFloat() / camera.textureResolution.y
                } else {
                    unreachable()
                }
                glMatrixMode(GL_PROJECTION)
                glLoadIdentity()
                if (camera.fov == null) {
                    glOrtho(
                        camera.orthoRange.first.x,
                        camera.orthoRange.second.x,
                        camera.orthoRange.first.y,
                        camera.orthoRange.second.y,
                        0.01, 1000.0
                    )
                } else {
                    gluPerspective(camera.fov!!, aspect, 0.01f, 1000f)
                }
                glClear(clearParams)

                // Rotate camera
                glMatrixMode(GL_MODELVIEW)
                glLoadIdentity()
                glRotatef(camera.rotation.x, 1f, 0f, 0f)
                glRotatef(camera.rotation.z, 0f, 0f, 1f)
                glRotatef(camera.rotation.y, 0f, 1f, 0f)

                // Skybox
                if (skybox != null) {
                    glDisable(GL_DEPTH_TEST)
                    glDisable(GL_LIGHTING)

                    if (camera.fov != null) {
                        skybox.draw()
                    }

                    glClear(GL_DEPTH_BUFFER_BIT)
                }

                // Scene
                glEnable(GL_DEPTH_TEST)
                val position = DVector3(camera.entity.body.position).add(camera.offset)
                glTranslatef(-position.x.toFloat(), -position.y.toFloat(), -position.z.toFloat())
                scene.sunPosition?.let {
                    glEnable(GL_LIGHTING)
                    glLightfv(
                        GL_LIGHT0, GL_POSITION, floatArrayOf(
                            position.x.toFloat() + it.x,
                            position.y.toFloat() + it.y,
                            position.z.toFloat() + it.z,
                            0f
                        )
                    )
                }
                scene.invokeEvent(EventType.DRAW)
            }

            glBindFramebuffer(GL_FRAMEBUFFER, 0)
            glViewport(0, 0, windowSize.x, windowSize.y)

            // HUD
            glMatrixMode(GL_PROJECTION)
            glLoadIdentity()
            glOrtho(0.0, windowSize.x.toDouble(), windowSize.y.toDouble(), 0.0, 1.0, 3.0)
            glMatrixMode(GL_MODELVIEW)
            glLoadIdentity()
            glTranslatef(0f, 0f, -2f)
            glDisable(GL_LIGHTING)
            glDisable(GL_DEPTH_TEST)

            val widthArray = IntArray(1)
            glfwGetFramebufferSize(window, widthArray, null)
            nvgBeginFrame(
                nanovg,
                windowSize.x.toFloat(), windowSize.y.toFloat(),
                widthArray[0].toFloat() / windowSize.x
            )
            nvgFontFace(nanovg, "minecraftia")
            nvgText(nanovg, 10f, 35f, "FPS: ${fpsAverage.roundToLong()}")
            scene.invokeEvent(EventType.DRAW_UI, nanovg)
            nvgEndFrame(nanovg)

            glfwSwapBuffers(window)
        }
        skybox?.close()
        contactJointGroup.destroy()
        quit()
    }

    private fun init() {
        GLFWErrorCallback.createPrint().set()
        if (!glfwInit()) {
            throw Exception("Failed to initialize GLFW")
        }

        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_SAMPLES, 4)
//        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE)

//        val monitor = glfwGetPrimaryMonitor()
//        val videoMode = glfwGetVideoMode(monitor) ?: throw Exception("Could not determine video mode")

        windowSize.set(1280, 720)
        window = glfwCreateWindow(windowSize.x, windowSize.y, title, 0, 0)
        glfwMakeContextCurrent(window)
        GL.createCapabilities()
//        GLUtil.setupDebugMessageCallback()

        nanovg = nvgCreate(NVG_ANTIALIAS)
        loadFont(nanovg, "minecraftia")

        glfwSwapInterval(1)

        glfwSetWindowSizeCallback(window) { _, width, height ->
            windowSize.set(width, height)
        }

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_MULTISAMPLE)

        glClearColor(0f, 0f, 0f, 1f)
        glShadeModel(GL_SMOOTH)
        glEnable(GL_LIGHT0)
        glMaterialfv(GL_FRONT, GL_AMBIENT, floatArrayOf(1f, 1f, 1f, 1f))
        glLineWidth(10f)

        sceneLoader = SceneLoaderImpl(Resources::resourceGetter)
    }

    private fun registerEvents() {
        val lastMousePos = Vector2d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
        glfwSetCursorPosCallback(window) { _, x, y ->
            if (
                lastMousePos.x != Double.POSITIVE_INFINITY &&
                glfwGetInputMode(window, GLFW_CURSOR) == GLFW_CURSOR_DISABLED
            ) {
                scene.invokeEvent(EventType.MOUSE_MOVED, MouseMoveEvent(x, y, x - lastMousePos.x, y - lastMousePos.y))
            }
            lastMousePos.set(x, y)
        }

        glfwSetMouseButtonCallback(window) { _, _, action, _ ->
            if (action == GLFW_PRESS) {
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
            }
        }

        glfwSetKeyCallback(window) { _, key, _, action, _ ->
            if (action == GLFW_REPEAT) return@glfwSetKeyCallback
            val press = action == GLFW_PRESS
            if (glfwGetInputMode(window, GLFW_CURSOR) != GLFW_CURSOR_DISABLED) {
                return@glfwSetKeyCallback
            }
            when (key) {
                GLFW_KEY_ESCAPE -> if (press) {
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL)
                    movementInput.x = 0.0
                    movementInput.z = 0.0
                }
                GLFW_KEY_W -> if (press) {
                    movementInput.z++
                } else {
                    movementInput.z--
                }
                GLFW_KEY_S -> if (press) {
                    movementInput.z--
                } else {
                    movementInput.z++
                }
                GLFW_KEY_A -> if (press) {
                    movementInput.x++
                } else {
                    movementInput.x--
                }
                GLFW_KEY_D -> if (press) {
                    movementInput.x--
                } else {
                    movementInput.x++
                }
                GLFW_KEY_SPACE -> if (press) {
                    movementInput.y = 1.0
                }
                GLFW_KEY_F3 -> if (press) {
                    wireframe = !wireframe
                    @Suppress("LiftReturnOrAssignment")
                    if (wireframe) {
                        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
                        glLineWidth(2f)
                        clearParams = GL_DEPTH_BUFFER_BIT or GL_COLOR_BUFFER_BIT
                    } else {
                        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
                        glLineWidth(10f)
                        clearParams = GL_DEPTH_BUFFER_BIT
                    }
                }
            }
        }
    }

    private fun quit() {
        scene.destroy()
        OdeHelper.closeODE()
        TextureManager.unload()
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)
        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }

    abstract val title: String

    abstract fun loadInitScene()
}
