package com.example.gestrestauhco

import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class ImageClassifier(assetManager: AssetManager, modelPath: String, private val labels: List<String>) {

    private var interpreter: Interpreter

    init {
        val model = loadModelFile(assetManager, modelPath)
        interpreter = Interpreter(model)
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): ByteBuffer {
        val fileDescriptor: AssetFileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Clasifica la imagen y devuelve solo la etiqueta
     */
    fun classify(bitmap: Bitmap): String {
        return classifyWithConfidence(bitmap).first
    }

    /**
     * Clasifica la imagen y devuelve Pair<etiqueta, confianza>
     * Confianza es un valor float entre 0 y 1
     */
    fun classifyWithConfidence(bitmap: Bitmap): Pair<String, Float> {
        val inputSize = 224
        val inputBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        inputBuffer.order(ByteOrder.nativeOrder())

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, false)
        val intValues = IntArray(inputSize * inputSize)
        scaledBitmap.getPixels(intValues, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

        var pixel = 0
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val value = intValues[pixel++]
                inputBuffer.putFloat((value shr 16 and 0xFF) / 255.0f)
                inputBuffer.putFloat((value shr 8 and 0xFF) / 255.0f)
                inputBuffer.putFloat((value and 0xFF) / 255.0f)
            }
        }

        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(inputBuffer, output)

        // Buscar la etiqueta con mayor confianza
        var maxIndex = 0
        var maxConfidence = 0f
        for (i in output[0].indices) {
            if (output[0][i] > maxConfidence) {
                maxConfidence = output[0][i]
                maxIndex = i
            }
        }

        return Pair(labels[maxIndex], maxConfidence)
    }
}
