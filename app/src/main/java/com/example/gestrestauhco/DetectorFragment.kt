package com.example.gestrestauhco
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class DetectorFragment : Fragment() {
    private lateinit var tvWelcome: TextView
    private lateinit var classifier: ImageClassifier
    private val labels = listOf(
        "Alfajor","Anticuchos", "Arroz con Leche",
        "Broaster", "Ceviche", "Chaufa", "Chicha de Jora", "Chicha Morada",
        "Crema Volteada","Juane", "Papa ala Huancaina",
        "Salchipapa", "Tallarin Verde"
    )

    private lateinit var imgPreview: ImageView
    private lateinit var txtResultado: TextView
    private lateinit var btnEscanear: Button

    private val REQUEST_IMAGE_CAPTURE = 1001
    private var ultimaFoto: Bitmap? = null

    private val UMBRAL_CONFIANZA = 0.6f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detector, container, false)


        tvWelcome = view.findViewById(R.id.tvWelcome)
        imgPreview = view.findViewById(R.id.imgPreview)
        txtResultado = view.findViewById(R.id.txtResultado)
        btnEscanear = view.findViewById(R.id.btnEscanear)

        // Cargar modelo TFLite
        classifier = ImageClassifier(requireContext().assets, "modelo_comida_peruana.tflite", labels)

        // Inicialmente no mostrar imagen ni resultado
        imgPreview.setImageDrawable(null)
        txtResultado.text = "Presiona el botón para escanear"

        // Abrir cámara
        btnEscanear.setOnClickListener { abrirCamara() }
        mostrarBienvenida()
        return view
    }
    private fun mostrarBienvenida() {
        val sharedPref = activity?.getSharedPreferences("MyAppPref", AppCompatActivity.MODE_PRIVATE)
        val nombre = sharedPref?.getString("nombre_usuario", "Usuario") ?: "Usuario"
        tvWelcome.text = "BIENVENIDO, $nombre"
    }
    private fun abrirCamara() {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            txtResultado.text = " No se pudo acceder a la cámara"
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val photo = data?.extras?.get("data") as? Bitmap
            if (photo != null) {

                ultimaFoto?.recycle()
                ultimaFoto = photo

                imgPreview.setImageBitmap(photo)

                val result = classifier.classifyWithConfidence(photo)
                if (result.second >= UMBRAL_CONFIANZA) {
                    val porcentaje = (result.second * 100).toInt()
                    txtResultado.text = "${result.first} ($porcentaje%)"
                } else {
                    txtResultado.text = "No existe en la base de datos"
                }
            } else {
                txtResultado.text = "No se capturó imagen"
            }
        }
    }

    override fun onPause() {
        super.onPause()
        cerrarCamaraSegura()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cerrarCamaraSegura()
        ultimaFoto?.recycle()
        ultimaFoto = null
    }

    private fun cerrarCamaraSegura() {
        try {
            val cameraService = requireActivity().getSystemService(android.hardware.camera2.CameraManager::class.java)
            for (id in cameraService.cameraIdList) {
                try {
                    cameraService.setTorchMode(id, false)
                } catch (_: Exception) { }
            }
        } catch (_: Exception) { }
    }
}
