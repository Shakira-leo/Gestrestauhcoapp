package com.example.gestrestauhco

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.gestrestauhco.databinding.FragmentDetectorBinding
import com.example.gestrestauhco.ml.Model
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder

class DetectorFragment : Fragment() {

    private lateinit var binding: FragmentDetectorBinding
    private lateinit var labels: List<String>

    // launcher para abrir la cámara
    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            procesarImagen(bitmap)
        } else {
            Toast.makeText(requireContext(), "No se capturó ninguna imagen", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetectorBinding.inflate(inflater, container, false)

        // Cargar las etiquetas del modelo
        labels = loadLabels("labels.txt")

        // Botón para abrir la cámara
        binding.btnEscanear.setOnClickListener {
            takePicture.launch(null)
        }

        return binding.root
    }

    // Procesar imagen capturada por la cámara
    private fun procesarImagen(bitmap: Bitmap) {
        val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val byteBuffer = convertBitmapToByteBuffer(resized)

        try {
            val model = Model.newInstance(requireContext())

            val inputFeature0 =
                TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
            inputFeature0.loadBuffer(byteBuffer)

            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

            model.close()

            val index = outputFeature0.indices.maxByOrNull { outputFeature0[it] } ?: -1

            if (index in labels.indices) {
                val nombrePlato = labels[index]
                binding.txtResultado.text = "🍽️ Plato detectado: $nombrePlato"
                binding.imgPreview.setImageBitmap(bitmap)
            } else {
                binding.txtResultado.text = "❌ No se reconoció ningún plato"
            }

        } catch (e: Exception) {
            binding.txtResultado.text = "Error procesando: ${e.message}"
        }
    }

    // Convierte Bitmap a ByteBuffer para el modelo
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val pixels = IntArray(224 * 224)
        bitmap.getPixels(pixels, 0, 224, 0, 0, 224, 224)

        var pixelIndex = 0
        for (i in 0 until 224) {
            for (j in 0 until 224) {
                val pixelValue = pixels[pixelIndex++]
                byteBuffer.putFloat(((pixelValue shr 16 and 0xFF) / 255.0f))
                byteBuffer.putFloat(((pixelValue shr 8 and 0xFF) / 255.0f))
                byteBuffer.putFloat(((pixelValue and 0xFF) / 255.0f))
            }
        }
        return byteBuffer
    }

    // Cargar las etiquetas desde assets/labels.txt
    private fun loadLabels(fileName: String): List<String> {
        val labelList = mutableListOf<String>()
        try {
            val reader = BufferedReader(InputStreamReader(requireContext().assets.open(fileName)))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                line?.let { labelList.add(it) }
            }
            reader.close()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error cargando labels: ${e.message}", Toast.LENGTH_LONG).show()
        }
        return labelList
    }
}
