package com.nmm.objectdetectionapp.analysis

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

/**
 * An analyser for processing camera frames to calculate the average luminosity (brightness).
 * This class implements the ImageAnalysis.Analyser interface to receive camera frames,
 * compute the average luminance, and pass that data to a listener. It's useful for
 * applications that need to adjust their behavior based on the current lighting conditions.
 *
 * @param listener A callback that receives the computed luminance value.
 */
class LuminosityAnalyzer(private val listener: (Double) -> Unit) : ImageAnalysis.Analyzer {

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

    override fun analyze(image: ImageProxy) {
        val buffer = image.planes[0].buffer
        val data = buffer.toByteArray()
        val pixels = data.map { it.toInt() and 0xFF }
        val luma = pixels.average()

        listener(luma)

        image.close()
    }
}
