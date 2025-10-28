package com.example.gestrestauhco

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetectorBinding.inflate(inflater, container, false)

        //  Cargar labels desde el archivo
        labels = loadLabels("labels.txt")

        binding.btnEscanear.setOnClickListener {
            try {
                escanearPlato()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        return binding.root
    }

    private fun escanearPlato() {
        //  imagen desde drawable puedes reemplazar prueba xd
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.sample_plate1)

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
                binding.txtResultado.text = " Plato detectado: $nombrePlato"
            } else {
                binding.txtResultado.text = "No se reconoció ningún plato"
            }

        } catch (e: Exception) {
            binding.txtResultado.text = "❌ Modelo no cargado: ${e.message}"
        }
    }

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

    // Leer las etiquetas del archivo labels.txt en ml
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
