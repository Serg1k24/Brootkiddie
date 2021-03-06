package ru.cryhards.brootkiddie.engine.shapes

import android.opengl.GLES30
import ru.cryhards.brootkiddie.engine.util.Shaders
import ru.cryhards.brootkiddie.engine.util.prop.CoordProperty
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * Created with love by luna_koly on 29.10.2017.
 */
class RectanglePlane : Mesh {
    val shaderProgram = Shaders.COLOR_TRANSITION
    val position = CoordProperty()
    val v1 = CoordProperty()
    val v2 = CoordProperty()
    val v3 = CoordProperty()
    val v4 = CoordProperty()

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var drawBuffer: ShortBuffer
    private lateinit var colorBuffer: FloatBuffer
    private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3)
    private val testColor = floatArrayOf(
            0.7f, 0.7f, 0.0f, 1.0f,    // top
            0.7f, 0.0f, 0.7f, 1.0f,   // left
            0.0f, 0.7f, 0.7f, 1.0f,  // right
            1.0f, 1.0f, 1.0f, 1.0f) // extra

    private fun getCoordArray(): FloatArray {
        return floatArrayOf(
                v1.x.value!!, v1.y.value!!, v1.z.value!!,
                v2.x.value!!, v2.y.value!!, v2.z.value!!,
                v3.x.value!!, v3.y.value!!, v3.z.value!!,
                v4.x.value!!, v4.y.value!!, v4.z.value!!
        )
    }

    override fun draw(): Mesh {
        GLES30.glUseProgram(shaderProgram)

        val aPositionHandle = GLES30.glGetAttribLocation(shaderProgram, "aPosition")
        GLES30.glEnableVertexAttribArray(aPositionHandle)
        GLES30.glVertexAttribPointer(aPositionHandle, 3, GLES30.GL_FLOAT, false, 3 * 4, vertexBuffer)

        val aColorHandle = GLES30.glGetAttribLocation(shaderProgram, "aColor")
        GLES30.glEnableVertexAttribArray(aColorHandle)
        GLES30.glVertexAttribPointer(aColorHandle, 4, GLES30.GL_FLOAT, false, 4 * 4, colorBuffer)

        GLES30.glDrawElements(GLES30.GL_TRIANGLES, 6, GLES30.GL_UNSIGNED_SHORT, drawBuffer)
        GLES30.glDisableVertexAttribArray(aPositionHandle)
        return this
    }

    override fun genBuffers(): Mesh {
        var bb = ByteBuffer.allocateDirect(48)  //  4 vertices * 3 coordinates * 4 bytes
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(getCoordArray())
        vertexBuffer.position(0)

        bb = ByteBuffer.allocateDirect(12) // drawOrder.size * 2
        bb.order(ByteOrder.nativeOrder())
        drawBuffer = bb.asShortBuffer()
        drawBuffer.put(drawOrder)
        drawBuffer.position(0)

        bb = ByteBuffer.allocateDirect(64) // color * values per color * 4 bytes
        bb.order(ByteOrder.nativeOrder())
        colorBuffer = bb.asFloatBuffer()
        colorBuffer.put(testColor)
        colorBuffer.position(0)
        return this
    }

    override fun build(floatArrayOf: FloatArray): Mesh {
        v1.x.value = floatArrayOf[0]
        v1.y.value = floatArrayOf[1]
        v1.z.value = floatArrayOf[2]

        v2.x.value = floatArrayOf[3]
        v2.y.value = floatArrayOf[4]
        v2.z.value = floatArrayOf[5]

        v3.x.value = floatArrayOf[6]
        v3.y.value = floatArrayOf[7]
        v3.z.value = floatArrayOf[8]

        v4.x.value = floatArrayOf[9]
        v4.y.value = floatArrayOf[10]
        v4.z.value = floatArrayOf[11]
        return this
    }
}