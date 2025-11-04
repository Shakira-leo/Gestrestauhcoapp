package com.example.gestrestauhco

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ControlFragment : Fragment() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvDeteccion: TextView
    private lateinit var tvEstado: TextView
    private lateinit var tvTemperatura: TextView
    private lateinit var tvHumedad: TextView
    private lateinit var tvFecha: TextView

    private val client = OkHttpClient()
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 3000L

    private val apiUrl = "https://gestrestauhco-1.onrender.com/api/sensor"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_control, container, false)

        // Vistas del layout
        tvWelcome = view.findViewById(R.id.tvWelcome)
        tvDeteccion = view.findViewById(R.id.tvDeteccion)
        tvEstado = view.findViewById(R.id.tvEstado)
        tvTemperatura = view.findViewById(R.id.tvTemperatura)
        tvHumedad = view.findViewById(R.id.tvHumedad)
        tvFecha = view.findViewById(R.id.tvFecha)

        mostrarBienvenida()


        startAutoUpdate()

        return view
    }

    private fun mostrarBienvenida() {
        val sharedPref = activity?.getSharedPreferences("MyAppPref", AppCompatActivity.MODE_PRIVATE)
        val nombre = sharedPref?.getString("nombre_usuario", "Usuario") ?: "Usuario"
        tvWelcome.text = "BIENVENIDO, $nombre"
    }

    private fun startAutoUpdate() {
        handler.post(object : Runnable {
            override fun run() {
                obtenerDatosSensor()
                handler.postDelayed(this, updateInterval)
            }
        })
    }

    private fun obtenerDatosSensor() {
        val request = Request.Builder()
            .url(apiUrl)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (!response.isSuccessful || body.isNullOrEmpty()) return

                try {
                    val json = JSONObject(body)

                    val deteccion = json.optString("deteccion", "-")
                    val estado = json.optString("estado", "-")
                    val temperatura = json.optDouble("temperatura", 0.0)
                    val humedad = json.optDouble("humedad", 0.0)
                    val fechaISO = json.optString("fecha", "-")

                    // Formatear fecha a hora local de Perú
                    val fechaFormateada = try {
                        val formatoEntrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                        formatoEntrada.timeZone = TimeZone.getTimeZone("UTC")
                        val fecha = formatoEntrada.parse(fechaISO)
                        val formatoSalida = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                        formatoSalida.timeZone = TimeZone.getTimeZone("America/Lima")
                        formatoSalida.format(fecha!!)
                    } catch (e: Exception) {
                        "-"
                    }

                    activity?.runOnUiThread {
                        tvDeteccion.text = "$deteccion"
                        tvEstado.text = "$estado"
                        tvTemperatura.text = "$temperatura °C"
                        tvHumedad.text = "$humedad %"
                        tvFecha.text = "$fechaFormateada"
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }
}
