package org.openartifact.sandbox

import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW
import org.openartifact.artifact.core.*
import org.openartifact.artifact.graphics.DataType
import org.openartifact.artifact.graphics.RenderAPI
import org.openartifact.artifact.graphics.choose
import org.openartifact.artifact.graphics.flow.renderFlow
import org.openartifact.artifact.graphics.interfaces.*
import org.openartifact.artifact.graphics.platform.opengl.OpenGLRenderer
import org.openartifact.artifact.graphics.window.WindowConfig
import org.openartifact.artifact.input.KeyConstants.KEY_LEFT_CONTROL
import org.openartifact.artifact.input.KeyConstants.KEY_Q
import org.openartifact.artifact.input.createKeyInputMap
import org.openartifact.artifact.input.with

@ApplicationEntry
@Suppress("unused")
class Sandbox : Application(
    RenderAPI.OpenGL,
    WindowConfig(
        854, 480, "Sandbox"
    )
) {

    private val keyInputMap = createKeyInputMap {
        KEY_LEFT_CONTROL with  KEY_Q to { GLFW.glfwSetWindowShouldClose(Artifact.instance.window.handle, true) }
    }

    private lateinit var rectShader : IShader

    private lateinit var rectVertexArray : IVertexArray
    private lateinit var rectVertexBuffer : IVertexBuffer
    private lateinit var rectIndexBuffer : IIndexBuffer

    private lateinit var projectionMatrix : Mat4

    override fun init() {
        logger.info("Sandbox init")

        renderer = createRenderer()

        // Rectangle
        rectVertexArray = renderer.choose<IVertexArray>().create()

        val vertices = floatArrayOf(
            -1.0f, -1.0f, -1.0f, // 0
            -1.0f, -1.0f,  1.0f, // 1
            -1.0f,  1.0f,  1.0f, // 2
            1.0f,  1.0f, -1.0f, // 3
            -1.0f, -1.0f, -1.0f, // 4
            -1.0f,  1.0f, -1.0f, // 5
            1.0f, -1.0f,  1.0f, // 6
            -1.0f, -1.0f, -1.0f, // 7
            1.0f, -1.0f, -1.0f, // 8
            1.0f,  1.0f, -1.0f, // 9
            1.0f, -1.0f, -1.0f, // 10
            -1.0f, -1.0f, -1.0f, // 11
            -1.0f, -1.0f, -1.0f, // 12
            -1.0f,  1.0f,  1.0f, // 13
            -1.0f,  1.0f, -1.0f, // 14
            1.0f, -1.0f,  1.0f, // 15
            -1.0f, -1.0f,  1.0f, // 16
            -1.0f, -1.0f, -1.0f, // 17
            -1.0f,  1.0f,  1.0f, // 18
            -1.0f, -1.0f,  1.0f, // 19
            1.0f, -1.0f,  1.0f, // 20
            1.0f,  1.0f,  1.0f, // 21
            1.0f, -1.0f, -1.0f, // 22
            1.0f,  1.0f, -1.0f, // 23
            1.0f, -1.0f, -1.0f, // 24
            1.0f,  1.0f,  1.0f, // 25
            1.0f, -1.0f,  1.0f, // 26
            1.0f,  1.0f,  1.0f, // 27
            1.0f,  1.0f, -1.0f, // 28
            -1.0f,  1.0f, -1.0f, // 29
            1.0f,  1.0f,  1.0f, // 30
            -1.0f,  1.0f, -1.0f, // 31
            -1.0f,  1.0f,  1.0f, // 32
            1.0f,  1.0f,  1.0f, // 33
            -1.0f,  1.0f,  1.0f, // 34
            1.0f, -1.0f,  1.0f  // 35
        )

        val indices = intArrayOf(
            0, 1, 2, // Triangle 1
            3, 4, 5, // Triangle 2
            6, 7, 8, // Triangle 3
            9, 10, 11, // Triangle 4
            12, 13, 14, // Triangle 5
            15, 16, 17, // Triangle 6
            18, 19, 20, // Triangle 7
            21, 22, 23, // Triangle 8
            24, 25, 26, // Triangle 9
            27, 28, 29, // Triangle 10
            30, 31, 32, // Triangle 11
            33, 34, 35  // Triangle 12
        )

        val rectLayout = renderer.choose<IBufferLayout>().create(
            mapOf(
                DataType.Vec3 to "a_Position"
            )
        )

        rectVertexBuffer = renderer.choose<IVertexBuffer>().create(vertices, rectLayout)

        rectIndexBuffer = renderer.choose<IIndexBuffer>().create(indices)

        rectVertexArray.addVertexBuffer(rectVertexBuffer)
        rectVertexArray.defineIndexBuffer(rectIndexBuffer)

        val rectangleVertexSource = """
            #version 330 core
            
            layout(location = 0) in vec3 a_Position;

            uniform vec4 u_Color;
            uniform mat4 u_MVP;
            
            out vec4 v_Color;
            out vec3 v_Position;
            
            void main() {
                gl_Position = u_MVP * vec4(a_Position, 1.0);
                v_Color = u_Color;
                v_Position = a_Position;
            }
            
        """.trimIndent()

        val rectangleFragmentSource = """
            #version 330 core
                        
            layout(location = 0) out vec4 color;
                        
            in vec3 v_Position;
                        
            void main() {
                color = vec4(v_Position * 0.5 + 0.5, 1.0);
            }

        """.trimIndent()

        rectShader = renderer.choose<IShader>(
            rectangleVertexSource,
            rectangleFragmentSource
        ).create()


        projectionMatrix = glm.perspective(glm.radians(45f), (windowConfig.width / windowConfig.height).toFloat(), 0.1f, 100f)
    }

    override fun update() {
        keyInputMap.process()

        (renderer as OpenGLRenderer).clearScreenBuffers()

        renderFlow {

            val view = glm.lookAt(
                Vec3(4, 3, 3),
                Vec3(0, 0, 0),
                Vec3(0, 1, 0)
            )

            val model = Mat4(1f)

            val mvp = projectionMatrix * view * model

            directCommit(rectShader) {
                parameterMat4("u_MVP", mvp)
                parameterVec4("u_Color", Vec4(1f, 1f, 1f, 1f))
            }

            commit(rectVertexArray)

            push()
        }
    }

    override fun shutdown() {
        logger.info("Sandbox shutdown")
    }

}
