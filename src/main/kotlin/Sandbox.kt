import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL46.*
import org.openartifact.artifact.ApplicationEntry
import org.openartifact.artifact.core.Application
import org.openartifact.artifact.core.Artifact
import org.openartifact.artifact.graphics.choose
import org.openartifact.artifact.graphics.interfaces.IBufferLayout
import org.openartifact.artifact.graphics.interfaces.IIndexBuffer
import org.openartifact.artifact.graphics.interfaces.IVertexBuffer
import org.openartifact.artifact.graphics.interfaces.IShader
import org.openartifact.artifact.graphics.platform.opengl.OpenGLRenderer
import org.openartifact.artifact.graphics.platform.opengl.OpenGLShader
import org.openartifact.artifact.input.KeyConstants.KEY_LEFT_CONTROL
import org.openartifact.artifact.input.KeyConstants.KEY_Q
import org.openartifact.artifact.input.createKeyInputMap
import org.openartifact.artifact.input.with

@ApplicationEntry
@Suppress("unused")
class Sandbox : Application() {

    /**
     * Creates a keyInputMap.
     * @see update for processing.
     */
    private val keyInputMap = createKeyInputMap {
        KEY_LEFT_CONTROL with KEY_Q to { GLFW.glfwSetWindowShouldClose(Artifact.instance.window.handle, true) }
    }

    override fun init() {
        logger.info("Sandbox init")

        renderer = OpenGLRenderer()

        vertexArray = glGenVertexArrays()
        glBindVertexArray(vertexArray)

        val vertices = floatArrayOf(
            - 0.5f, - 0.5f, 0.0f,
            0.5f, - 0.5f, 0.0f,
            0.0f, 0.5f, 0.0f
        )

        val indices = intArrayOf(0, 1, 2)

        vertexBuffer = renderer.choose<IVertexBuffer>().create(vertices)

        indexBuffer = renderer.choose<IIndexBuffer>().create(indices)

        vertexBuffer.apply {
            renderer.choose<IBufferLayout>().create(
                mapOf(
                    Vec3::class to ("a_Position" to false),
                    Vec4::class to ("a_Color" to false),
                )
            )
        }

        val vertexSource = """
            #version 330 core
            
            layout(location = 0) in vec3 a_Position;
            
            out vec3 v_Position;
            
            void main() {
                v_Position = a_Position;
                gl_Position = vec4(a_Position, 1.0);
            }
            
        """.trimIndent()

        val fragmentSource = """
            #version 330 core
            
            layout(location = 0) out vec4 color;
            
            in vec3 v_Position;
            
            void main() {
                color = vec4(v_Position - 0.5 * 0.5 + 1.0, 1.0);
            }
        """.trimIndent()

        @Deprecated("Only supports OpenGL.")
        shader = renderer.choose<IShader>(
            listOf(
                OpenGLShader.ShaderModule(vertexSource, GL_VERTEX_SHADER),
                OpenGLShader.ShaderModule(fragmentSource, GL_FRAGMENT_SHADER),
            )
        ).create()
    }

    override fun update() {
        keyInputMap.process()

        shader !!.bind()

        (renderer as OpenGLRenderer).clearScreenBuffers()

        glBindVertexArray(vertexArray)
        glDrawElements(GL_TRIANGLES, indexBuffer.count, GL_UNSIGNED_INT, 0)
    }

    override fun shutdown() {
        logger.info("Sandbox shutdown")
    }

}