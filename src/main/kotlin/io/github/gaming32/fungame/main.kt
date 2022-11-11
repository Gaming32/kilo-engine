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

class Application {
    companion object {
        private const val MOUSE_SPEED = 0.5
        private const val MOVE_SPEED = 7.0
        private const val JUMP_SPEED = 6.0
        private const val DRAG = 25.0
        private const val PHYSICS_SPEED = 0.02
        private const val GRAVITY = -11.0
    }

    private val windowSize = Vector2i()
    private val position = Vector3d()
    private val motion = Vector3d()
    private val movementInput = Vector3d()
    private val rotation = Vector2f()
    private var window = 0L
    private lateinit var objLoader: ObjLoader

    fun main() {
        init()
        registerEvents()
        val model = objLoader.loadObj("/example.obj").toDisplayList()
        var lastTime = glfwGetTime()
        var lastPhysicsTime = lastTime
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents()
            glClear(GL_DEPTH_BUFFER_BIT or GL_COLOR_BUFFER_BIT)

            val time = glfwGetTime()
            val deltaTime = time - lastTime
            lastTime = time

            while (time - lastPhysicsTime > PHYSICS_SPEED) {
                val adjustedMovementInput = Vector3d(movementInput).rotateY(toRadians(rotation.y.toDouble()))
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
                if (position.y <= 0) {
                    position.y = 0.0
                    motion.y = 0.0
                }
                lastPhysicsTime += PHYSICS_SPEED
            }

            glMatrixMode(GL_MODELVIEW)
            glLoadIdentity()
            glRotatef(rotation.x, 1f, 0f, 0f)
            glRotatef(rotation.y, 0f, 1f, 0f)
            glTranslatef(-position.x.toFloat(), -position.y.toFloat() - 1.8f, position.z.toFloat())

            model.draw()

            glfwSwapBuffers(window)
        }
        model.close()
        quit()
    }

    private fun init() {
        GLFWErrorCallback.createPrint().set()
        if (!glfwInit()) {
            throw Exception("Failed to initialize GLFW")
        }

        glfwDefaultWindowHints()

        val monitor = glfwGetPrimaryMonitor()
        val videoMode = glfwGetVideoMode(monitor) ?: throw Exception("Could not determine video mode")

        windowSize.set(videoMode.width() / 2, videoMode.height() / 2)
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
        glClearColor(0f, 0f, 0f, 1f)

        objLoader = ObjLoader(TextureManager.getResource)
    }

    private fun registerEvents() {
        val lastMousePos = Vector2d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
        glfwSetCursorPosCallback(window) { _, x, y ->
            if (
                lastMousePos.x != Double.POSITIVE_INFINITY &&
                glfwGetInputMode(window, GLFW_CURSOR) == GLFW_CURSOR_DISABLED
            ) {
                rotation.y += ((x - lastMousePos.x) * MOUSE_SPEED).toFloat()
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
            if (press && key == GLFW_KEY_ESCAPE) {
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL)
                movementInput.x = 0.0
                movementInput.z = 0.0
            }
            if (glfwGetInputMode(window, GLFW_CURSOR) != GLFW_CURSOR_DISABLED) {
                return@glfwSetKeyCallback
            }
            when (key) {
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
                    movementInput.x -= MOVE_SPEED
                } else {
                    movementInput.x += MOVE_SPEED
                }
                GLFW_KEY_D -> if (press) {
                    movementInput.x += MOVE_SPEED
                } else {
                    movementInput.x -= MOVE_SPEED
                }
                GLFW_KEY_SPACE -> if (press && position.y <= 0) {
                    movementInput.y = JUMP_SPEED
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
        TextureManager.quit()
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)
        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }
}

fun main() = Application().main()
