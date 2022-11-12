package io.github.gaming32.fungame

import io.github.gaming32.fungame.obj.ObjLoader
import io.github.gaming32.fungame.util.TextureManager
import io.github.gaming32.fungame.util.gluPerspective
import org.joml.Math.clamp
import org.joml.Math.toRadians
import org.joml.Vector2d
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector3d
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class Application {
    companion object {
        private const val MOUSE_SPEED = 0.5
        private const val MOVE_SPEED = 7.0 // m/s
        private const val JUMP_SPEED = 6.0 // m/s
        private const val DRAG = 25.0
        private const val PHYSICS_SPEED = 0.02
        private const val GRAVITY = -11.0 // m/s/s
        private const val SUN_SPEED = 2 * PI / 120 // r/s
    }

    private val windowSize = Vector2i()
    private val position = Vector3d(0.0, 0.5, -5.0)
    private val motion = Vector3d()
    private val movementInput = Vector3d()
    private val rotation = Vector2f()
    private var wireframe = false
    private var window = 0L
    private lateinit var objLoader: ObjLoader

    fun main() {
        init()
        registerEvents()
        val skybox = objLoader.loadObj("/skybox.obj").toDisplayList()
        val level = objLoader.loadObj("/example.obj")
        val levelList = level.toDisplayList()
        var lastTime = glfwGetTime()
        var lastPhysicsTime = lastTime
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents()
            glClear(GL_DEPTH_BUFFER_BIT or GL_COLOR_BUFFER_BIT)

            val time = glfwGetTime()
            val deltaTime = time - lastTime
            lastTime = time

            if (time - lastPhysicsTime > 1.0) {
                lastPhysicsTime = time - 1.0
            }
            while (time - lastPhysicsTime > PHYSICS_SPEED) {
                val adjustedMovementInput = Vector3d(movementInput).rotateY(
                    toRadians(rotation.y.toDouble())
                )
                motion.x += adjustedMovementInput.x
                motion.z += adjustedMovementInput.z
                if (movementInput.y != 0.0) {
                    motion.y = movementInput.y
                    movementInput.y = 0.0
                }
                motion.x *= DRAG * PHYSICS_SPEED
                motion.z *= DRAG * PHYSICS_SPEED
                motion.y += GRAVITY * PHYSICS_SPEED
                position.x += motion.x * PHYSICS_SPEED
                position.y += motion.y * PHYSICS_SPEED
                position.z += motion.z * PHYSICS_SPEED
//                if (position.y <= 0) {
//                    position.y = 0.0
//                    motion.y = 0.0
//                }
                collide(position, motion, level)
//                println("X: ${position.x} Y: ${position.y} Z: ${position.z} ROT: ${rotation.y}")
                lastPhysicsTime += PHYSICS_SPEED
            }

            glMatrixMode(GL_MODELVIEW)
            glLoadIdentity()
            glRotatef(rotation.x, 1f, 0f, 0f)
            glRotatef(180 - rotation.y, 0f, 1f, 0f)

            glDisable(GL_DEPTH_TEST)
            glDisable(GL_LIGHTING)
            skybox.draw()
            glEnable(GL_DEPTH_TEST)
            glEnable(GL_LIGHTING)

            glTranslatef(-position.x.toFloat(), -position.y.toFloat() - 1.8f, -position.z.toFloat())
            glLightfv(
                GL_LIGHT0, GL_POSITION, floatArrayOf(
                    position.x.toFloat() + 30f * cos(time * SUN_SPEED).toFloat(),
                    position.y.toFloat() + 30f,
                    position.z.toFloat() + 30f * sin(time * SUN_SPEED).toFloat(),
                    0f
                )
            )

            levelList.draw()

            glfwSwapBuffers(window)
        }
        skybox.close()
        levelList.close()
        quit()
    }

    private fun init() {
        GLFWErrorCallback.createPrint().set()
        if (!glfwInit()) {
            throw Exception("Failed to initialize GLFW")
        }

        glfwDefaultWindowHints()

//        val monitor = glfwGetPrimaryMonitor()
//        val videoMode = glfwGetVideoMode(monitor) ?: throw Exception("Could not determine video mode")

        windowSize.set(1280, 720)
        window = glfwCreateWindow(windowSize.x, windowSize.y, "Fun 3D Game", 0, 0)
        glfwMakeContextCurrent(window)
        GL.createCapabilities()

        glfwSwapInterval(1)

        glfwSetWindowSizeCallback(window) { _, width, height ->
            windowSize.set(width, height)
            setViewport()
        }
        setViewport()

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_CULL_FACE)
        glEnable(GL_LIGHTING)

        glClearColor(0f, 0f, 0f, 1f)
        glShadeModel(GL_SMOOTH)
        glEnable(GL_LIGHT0)
        glMaterialfv(GL_FRONT, GL_AMBIENT, floatArrayOf(1f, 1f, 1f, 1f))

        objLoader = ObjLoader(TextureManager.getResource)
    }

    private fun registerEvents() {
        val lastMousePos = Vector2d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
        glfwSetCursorPosCallback(window) { _, x, y ->
            if (
                lastMousePos.x != Double.POSITIVE_INFINITY &&
                glfwGetInputMode(window, GLFW_CURSOR) == GLFW_CURSOR_DISABLED
            ) {
                rotation.y -= ((x - lastMousePos.x) * MOUSE_SPEED).toFloat()
                rotation.x = clamp(-90f, 90f, rotation.x + ((y - lastMousePos.y) * MOUSE_SPEED).toFloat())
            }
            lastMousePos.set(x, y)
        }

        glfwSetMouseButtonCallback(window) { _, button, action, _ ->
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
                    movementInput.z += MOVE_SPEED
                } else {
                    movementInput.z -= MOVE_SPEED
                }
                GLFW_KEY_S -> if (press) {
                    movementInput.z -= MOVE_SPEED
                } else {
                    movementInput.z += MOVE_SPEED
                }
                GLFW_KEY_A -> if (press) {
                    movementInput.x += MOVE_SPEED
                } else {
                    movementInput.x -= MOVE_SPEED
                }
                GLFW_KEY_D -> if (press) {
                    movementInput.x -= MOVE_SPEED
                } else {
                    movementInput.x += MOVE_SPEED
                }
                GLFW_KEY_SPACE -> if (press /* && position.y <= 0 */) {
                    movementInput.y = JUMP_SPEED
                }
                GLFW_KEY_F3 -> if (press) {
                    wireframe = !wireframe
                    glPolygonMode(GL_FRONT_AND_BACK, if (wireframe) GL_LINE else GL_FILL)
                }
            }
        }
    }

    private fun setViewport() {
        glViewport(0, 0, windowSize.x, windowSize.y)

        glMatrixMode(GL_PROJECTION)
        glLoadIdentity()
        gluPerspective(80f, windowSize.x.toFloat() / windowSize.y, 0.1f, 1000f)
//        glOrtho(-5.0, 5.0, -5.0, 5.0, -1.0, 500.0)
    }

    private fun quit() {
        TextureManager.unload()
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)
        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }
}

fun main() = Application().main()
