package ru.cryhards.brootkiddie.engine.scene

import android.opengl.GLES30
import android.opengl.GLES31
import android.opengl.Matrix
import android.util.Log
import ru.cryhards.brootkiddie.engine.util.Environment
import ru.cryhards.brootkiddie.engine.util.MoreMatrix
import ru.cryhards.brootkiddie.engine.util.Shaders
import ru.cryhards.brootkiddie.engine.util.prop.CoordProperty
import ru.cryhards.brootkiddie.engine.util.prop.RotationProperty
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * Created with love by luna_koly on 08.11.2017.
 */
class VertexObject(private val vertices: FloatArray, private val drawOrder: ShortArray) : Mesh {
    val shaderProgram = Shaders.BASIC
    val position = CoordProperty()
    var rotation = RotationProperty()

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var drawBuffer: ShortBuffer
    private lateinit var normalsBuffer: FloatBuffer

    private val testColor = floatArrayOf(0.8f, 0.5f, 0.5f, 1.0f)
    private var normals = FloatArray(drawOrder.size * 2)

    init {
        for (i in 0 until drawOrder.size / 3) {
            var vert = floatArrayOf(
                    vertices[drawOrder[i * 3 + 0].toInt() * 3 + 0],
                    vertices[drawOrder[i * 3 + 0].toInt() * 3 + 1],
                    vertices[drawOrder[i * 3 + 0].toInt() * 3 + 2],

                    vertices[drawOrder[i * 3 + 1].toInt() * 3 + 0],
                    vertices[drawOrder[i * 3 + 1].toInt() * 3 + 1],
                    vertices[drawOrder[i * 3 + 1].toInt() * 3 + 2],

                    vertices[drawOrder[i * 3 + 2].toInt() * 3 + 0],
                    vertices[drawOrder[i * 3 + 2].toInt() * 3 + 1],
                    vertices[drawOrder[i * 3 + 2].toInt() * 3 + 2]
            )
            vert = calcSurfaceNormal(vert)
            normals[i * 6] = vert[0]
            normals[i * 6 + 1] = vert[1]
            normals[i * 6 + 2] = vert[2]

            normals[i * 6 + 3] = vert[0]
            normals[i * 6 + 4] = vert[1]
            normals[i * 6 + 5] = vert[2]
        }

//        normals = floatArrayOf(
//                // Front face
//                0.0f, 0.0f, 1.0f,
//                0.0f, 0.0f, 1.0f,
//                0.0f, 0.0f, 1.0f,
//                0.0f, 0.0f, 1.0f,
//
//                // Back face
//                0.0f, 0.0f, -1.0f,
//                0.0f, 0.0f, -1.0f,
//                0.0f, 0.0f, -1.0f,
//                0.0f, 0.0f, -1.0f,
//
//                // Top face
//                0.0f, 1.0f, 0.0f,
//                0.0f, 1.0f, 0.0f,
//                0.0f, 1.0f, 0.0f,
//                0.0f, 1.0f, 0.0f,
//
//                // Bottom face
//                0.0f, -1.0f, 0.0f,
//                0.0f, -1.0f, 0.0f,
//                0.0f, -1.0f, 0.0f,
//                0.0f, -1.0f, 0.0f,
//
//                // Right face
//                1.0f, 0.0f, 0.0f,
//                1.0f, 0.0f, 0.0f,
//                1.0f, 0.0f, 0.0f,
//                1.0f, 0.0f, 0.0f,
//
//                // Left face
//                -1.0f, 0.0f, 0.0f,
//                -1.0f, 0.0f, 0.0f,
//                -1.0f, 0.0f, 0.0f,
//                -1.0f, 0.0f, 0.0f
//        )
    }

    private fun calcSurfaceNormal(vertices: FloatArray): FloatArray {
        val vec1 = floatArrayOf(
                vertices[3] - vertices[0],
                vertices[4] - vertices[1],
                vertices[5] - vertices[2]
        )
        val vec2 = floatArrayOf(
                vertices[6] - vertices[0],
                vertices[7] - vertices[1],
                vertices[8] - vertices[2]
        )

        val normal = FloatArray(3)
        normal[0] = vec1[1] * vec2[2] - vec2[1] * vec1[2]
        normal[1] = vec1[2] * vec2[0] - vec2[2] * vec1[0]
        normal[2] = vec1[0] * vec2[1] - vec2[0] * vec1[1]
        return normal
    }

    override fun draw(environment: Environment): Mesh {
        GLES30.glUseProgram(shaderProgram)

        val aPositionHandle = GLES30.glGetAttribLocation(shaderProgram, "aPosition")
        GLES30.glEnableVertexAttribArray(aPositionHandle)
        GLES30.glVertexAttribPointer(aPositionHandle, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)

        val uColorHandle = GLES30.glGetUniformLocation(shaderProgram, "uColor")
        GLES30.glUniform4fv(uColorHandle, 1, testColor, 0)

        val modelMatrix = getMatrix()
        val uMMatrixHandle = GLES30.glGetUniformLocation(shaderProgram, "uMMatrix")
        val inverted = modelMatrix.clone()
        Matrix.invertM(inverted, 0, inverted, 0)
        GLES30.glUniformMatrix4fv(uMMatrixHandle, 1, false, inverted, 0)

        val uMVMatrixHandle = GLES30.glGetUniformLocation(shaderProgram, "uMVMatrix")
        GLES30.glUniformMatrix4fv(uMVMatrixHandle, 1, false, environment.mvpMatrix, 0)

        Matrix.multiplyMM(modelMatrix, 0, environment.mvpMatrix, 0, modelMatrix, 0)
        val uMVPMatrixHandle = GLES30.glGetUniformLocation(shaderProgram, "uMVPMatrix")
        GLES30.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, modelMatrix, 0)

        val uAmbientLightHandle = GLES30.glGetUniformLocation(shaderProgram, "uAmbientLight")
        GLES30.glUniform3f(uAmbientLightHandle,
                environment.ambientLight.x.value,
                environment.ambientLight.y.value,
                environment.ambientLight.z.value)

        val uSunlightHandle = GLES30.glGetUniformLocation(shaderProgram, "uSunlight")
        GLES30.glUniform3f(uSunlightHandle,
                environment.sunlight.x.value,
                environment.sunlight.y.value,
                environment.sunlight.z.value)

        val uSunDirectionHandle = GLES30.glGetUniformLocation(shaderProgram, "uSunDirection")
        GLES30.glUniform3f(uSunDirectionHandle,
                environment.sunDirection.x.value,
                environment.sunDirection.y.value,
                environment.sunDirection.z.value)

        val aSurfaceNormalHandle = GLES30.glGetAttribLocation(shaderProgram, "aSurfaceNormal")
        GLES30.glEnableVertexAttribArray(aSurfaceNormalHandle)
        GLES30.glVertexAttribPointer(aSurfaceNormalHandle, 3, GLES30.GL_FLOAT, false, 0, normalsBuffer)

        GLES30.glDrawElements(GLES30.GL_TRIANGLES, drawOrder.size, GLES30.GL_UNSIGNED_SHORT, drawBuffer)
        GLES30.glDisableVertexAttribArray(aPositionHandle)
        return this
    }

    override fun genBuffers(): Mesh {
        var bb = ByteBuffer.allocateDirect(vertices.size * 4)  //  4 vertices * 3 coordinates * 4 bytes
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)

        bb = ByteBuffer.allocateDirect(drawOrder.size * 2) // drawOrder.size * 2
        bb.order(ByteOrder.nativeOrder())
        drawBuffer = bb.asShortBuffer()
        drawBuffer.put(drawOrder)
        drawBuffer.position(0)

        bb = ByteBuffer.allocateDirect(normals.size * 4)
        bb.order(ByteOrder.nativeOrder())
        normalsBuffer = bb.asFloatBuffer()
        normalsBuffer.put(normals)
        normalsBuffer.position(0)
        return this
    }

    override fun build(src: FloatArray): Mesh {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMatrix(): FloatArray {
        val rotationMatrix = MoreMatrix.getLookAroundRotationM(
            rotation.horizontal.value,
            rotation.vertical.value)
        val translationMatrix = MoreMatrix.getTranslationM(
                position.x.value,
                position.y.value,
                position.z.value)

        val modelMatrix = FloatArray(16)
        Matrix.multiplyMM(modelMatrix, 0, translationMatrix, 0, rotationMatrix, 0)
        return modelMatrix
    }
}