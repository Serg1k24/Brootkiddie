package ru.cryhards.brootkiddie.engine.scene

import android.opengl.GLES30
import android.opengl.Matrix
import ru.cryhards.brootkiddie.engine.util.Environment
import ru.cryhards.brootkiddie.engine.util.MoreMatrix
import ru.cryhards.brootkiddie.engine.util.Shaders
import ru.cryhards.brootkiddie.engine.util.prop.CoordProperty
import ru.cryhards.brootkiddie.engine.util.prop.RotationProperty
import java.lang.Math.sqrt
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Created with love by luna_koly on 29.10.2017.
 */
class TrianglePlane : Mesh {
    val shaderProgram = Shaders.BASIC
    val position = CoordProperty()
    var rotation = RotationProperty()
    val v1 = CoordProperty()
    val v2 = CoordProperty()
    val v3 = CoordProperty()
    var surfaceNormal = FloatArray(3)

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var normalsBuffer: FloatBuffer

    private val testColor = floatArrayOf(0.7f, 0.7f, 1.0f, 1.0f)

    override fun draw(environment: Environment): Mesh {
        GLES30.glUseProgram(shaderProgram)

        val aPositionHandle = GLES30.glGetAttribLocation(shaderProgram, "aPosition")
        GLES30.glEnableVertexAttribArray(aPositionHandle)
        GLES30.glVertexAttribPointer(aPositionHandle, 3, GLES30.GL_FLOAT, false, 3 * 4, vertexBuffer)

        val uColorHandle = GLES30.glGetUniformLocation(shaderProgram, "uColor")
        GLES30.glUniform4fv(uColorHandle, 1, testColor, 0)

        val modelMatrix = getMatrix()
        val uMMatrixHandle = GLES30.glGetUniformLocation(shaderProgram, "uMMatrix")
        GLES30.glUniformMatrix4fv(uMMatrixHandle, 1, false, modelMatrix, 0)

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
        GLES30.glVertexAttribPointer(aSurfaceNormalHandle, 3, GLES30.GL_FLOAT, false, 3 * 4, vertexBuffer)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)
        GLES30.glDisableVertexAttribArray(aPositionHandle)
        return this
    }

    override fun genBuffers(): Mesh {
        var bb = ByteBuffer.allocateDirect(36)  //  3 vertices * 3 coordinates * 4 bites
        bb.order(ByteOrder.nativeOrder())

        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(getCoordArray())
        vertexBuffer.position(0)

        bb = ByteBuffer.allocateDirect(surfaceNormal.size * 4) // drawOrder.size * 2
        bb.order(ByteOrder.nativeOrder())
        normalsBuffer = bb.asFloatBuffer()
        normalsBuffer.put(surfaceNormal)
        normalsBuffer.position(0)
        return this
    }

    override fun build(src: FloatArray): Mesh {
        v1.x.value = src[0]
        v1.y.value = src[1]
        v1.z.value = src[2]

        v2.x.value = src[3]
        v2.y.value = src[4]
        v2.z.value = src[5]

        v3.x.value = src[6]
        v3.y.value = src[7]
        v3.z.value = src[8]

        val vec1 = floatArrayOf(
                src[3] - src[0],
                src[4] - src[1],
                src[5] - src[2]
        )
        val vec2 = floatArrayOf(
                src[6] - src[0],
                src[7] - src[1],
                src[8] - src[2]
        )

        surfaceNormal[0] = vec1[1] * vec2[2] - vec2[1] * vec1[2]
        surfaceNormal[1] = vec1[2] * vec2[0] - vec2[2] * vec1[0]
        surfaceNormal[2] = vec1[0] * vec2[1] - vec2[0] * vec1[1]
        return this
    }

    private fun getCoordArray(): FloatArray {
        return floatArrayOf(
                v1.x.value, v1.y.value, v1.z.value,
                v2.x.value, v2.y.value, v2.z.value,
                v3.x.value, v3.y.value, v3.z.value
        )
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