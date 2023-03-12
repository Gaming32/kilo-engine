package io.github.gaming32.kiloengine

import io.github.gaming32.kiloengine.entity.CameraComponent
import io.github.gaming32.kiloengine.entity.PlayerComponent
import io.github.gaming32.kiloengine.loader.LevelLoader
import io.github.gaming32.kiloengine.loader.LevelLoaderImpl
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
import org.ode4j.ode.DBody
import org.ode4j.ode.DContact.DSurfaceParameters
import org.ode4j.ode.DContactGeomBuffer
import org.ode4j.ode.OdeConstants.*
import org.ode4j.ode.OdeHelper
import java.text.DecimalFormat
import kotlin.math.roundToLong

abstract class KiloEngineGame {
    companion object {
        private const val MOUSE_SPEED = 0.25
        private const val MOVE_SPEED = 85.0
        private const val JUMP_SPEED = 500.0
        private const val WALL_JUMP_HORIZONTAL = 1500.0
        private const val WALL_JUMP_VERTICAL = 500.0
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
        private val DEC_FORMAT = DecimalFormat("0.0")
    }

    init {
        OdeHelper.initODE()
    }

    val level = Level()
    private val windowSize = Vector2i()
    private val movementInput = Vector3d()
    lateinit var player: PlayerComponent
        private set
    private lateinit var playerBody: DBody
    var wireframe = false
    private var window = 0L
    private var nanovg = 0L
    private var clearParams = GL_DEPTH_BUFFER_BIT
    lateinit var levelLoader: LevelLoader
        private set

    fun main() {
        init()
        registerEvents()
        val skybox = skyboxTextures?.let {
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
            levelLoader.loadObj("/skybox.obj").toDisplayList()
        } ?: DisplayList.EMPTY
        loadInitLevel()
        player = level.getComponent()
        playerBody = player.entity.body
        var lastTime = glfwGetTime()
        var lastPhysicsTime = lastTime
        var fpsAverage = 0.0
        val contactJointGroup = OdeHelper.createJointGroup()
        val force = DVector3()
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
                level.entities.toList().forEach { it.preTick() }
                val adjustedMovementInput = Vector3d(movementInput).rotateY(
                    toRadians(player.rotation.y.toDouble())
                )
                playerBody.addForce(adjustedMovementInput.x * MOVE_SPEED, 0.0, adjustedMovementInput.z * MOVE_SPEED)
                if (movementInput.y > 0.0 && glfwGetTime() - player.lastJumpCollidedTime <= 0.1) {
                    if (player.jumpNormal.y < 0.95) {
                        playerBody.addForce(
                            0.0,
                            movementInput.y * (WALL_JUMP_VERTICAL - playerBody.linearVel.y),
                            0.0
                        )
                        playerBody.addForce(DVector3(player.jumpNormal).scale(movementInput.y * WALL_JUMP_HORIZONTAL))
                    } else {
                        playerBody.addForce(0.0, movementInput.y * JUMP_SPEED, 0.0)
                    }
                }
                movementInput.y = 0.0
                level.entities.forEach { entity ->
                    entity.body.addForce(
                        entity.body.linearVel.x * -HORIZONTAL_DAMPING / PHYSICS_SPEED,
                        entity.body.linearVel.y * -VERTICAL_DAMPING / PHYSICS_SPEED,
                        entity.body.linearVel.z * -HORIZONTAL_DAMPING / PHYSICS_SPEED
                    )
                }
                force.set(playerBody.force)
                val contactBuffer = DContactGeomBuffer(CONTACT_COUNT)
                contactJointGroup.clear()
                level.space.collide(null) { _, o1, o2 ->
                    repeat(OdeHelper.collide(o1, o2, CONTACT_COUNT, contactBuffer)) {
                        val contact = contactBuffer[it]
                        val e1 = level.getEntityByBody(contact.g1.body)
                        val e2 = level.getEntityByBody(contact.g2.body)
                        if (e1 == null || e2 == null) return@repeat
                        var surfaceParams = e1.collideWithEntity(e2, contact, true)
                        val surfaceParams2 = e2.collideWithEntity(e1, contact, false)
                        surfaceParams = surfaceParams ?: surfaceParams2
                        if (surfaceParams == null) return@repeat
                        val joint = OdeHelper.createContactJoint(
                            level.world,
                            contactJointGroup,
                            DContact(contact, surfaceParams)
                        )
                        joint.attach(contact.g1.body, contact.g2.body)
                    }
                }
                level.world.quickStep(PHYSICS_SPEED)
                level.entities.toList().forEach { it.tick() }
                lastPhysicsTime += PHYSICS_SPEED
            }

            // 3D Mode
            glEnable(GL_CULL_FACE)

            for (camera in level.getComponents<CameraComponent>()) {
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
                glDisable(GL_DEPTH_TEST)
                glDisable(GL_LIGHTING)

                if (camera.fov != null) {
                    skybox.draw()
                }

                glClear(GL_DEPTH_BUFFER_BIT)

                // Level
                glEnable(GL_DEPTH_TEST)
                glEnable(GL_LIGHTING)
                val position = DVector3(camera.entity.body.position).add(camera.offset)
                glTranslatef(-position.x.toFloat(), -position.y.toFloat(), -position.z.toFloat())
                glLightfv(
                    GL_LIGHT0, GL_POSITION, floatArrayOf(
                        position.x.toFloat() - 12.9f,
                        position.y.toFloat() + 30f,
                        position.z.toFloat() + 17.1f,
                        0f
                    )
                )
                level.entities.forEach { it.draw() }
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
            nvgText(
                nanovg, 10f, 55f,
                "X/Y/Z: " +
                    "${DEC_FORMAT.format(playerBody.position.x)}/" +
                    "${DEC_FORMAT.format(playerBody.position.y)}/" +
                    DEC_FORMAT.format(playerBody.position.z)
            )
            nvgText(
                nanovg, 10f, 75f,
                "FX/FY/FZ: " +
                    "${DEC_FORMAT.format(force.x)}/" +
                    "${DEC_FORMAT.format(force.y)}/" +
                    DEC_FORMAT.format(force.z)
            )
            nvgText(
                nanovg, 10f, 95f,
                "VX/VY/VZ: " +
                    "${DEC_FORMAT.format(playerBody.linearVel.x)}/" +
                    "${DEC_FORMAT.format(playerBody.linearVel.y)}/" +
                    DEC_FORMAT.format(playerBody.linearVel.z)
            )
            nvgText(
                nanovg, 10f, 115f,
                "RY/RX: " +
                    "${DEC_FORMAT.format(player.rotation.y)}/" +
                    DEC_FORMAT.format(player.rotation.x)
            )
            nvgEndFrame(nanovg)

            glfwSwapBuffers(window)
        }
        skybox.close()
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

        levelLoader = LevelLoaderImpl(TextureManager::resourceGetter)
    }

    private fun registerEvents() {
        val lastMousePos = Vector2d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
        glfwSetCursorPosCallback(window) { _, x, y ->
            if (
                lastMousePos.x != Double.POSITIVE_INFINITY &&
                glfwGetInputMode(window, GLFW_CURSOR) == GLFW_CURSOR_DISABLED
            ) {
                player.rotation.y = normalizeDegrees(
                    player.rotation.y - ((x - lastMousePos.x) * MOUSE_SPEED).toFloat()
                )
                player.rotation.x = clamp(
                    -90f, 90f,
                    player.rotation.x + ((y - lastMousePos.y) * MOUSE_SPEED).toFloat()
                )
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
        level.destroy()
        OdeHelper.closeODE()
        TextureManager.unload()
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)
        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }

    open val skyboxTextures: SkyboxTextures? = null

    abstract val title: String

    abstract fun loadInitLevel()
}
