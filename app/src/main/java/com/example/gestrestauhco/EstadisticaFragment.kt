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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class EstadisticaFragment : Fragment() {

    private lateinit var tvWelcome: TextView
    private lateinit var barChart: BarChart
    private val client = OkHttpClient()
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval: Long = 3000L

    private var ultimaTemperatura: Float? = null
    private var ultimaHumedad: Float? = null

    private val apiUrl = "https://gestrestauhco-1.onrender.com/api/sensor"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_estadistica, container, false)

        tvWelcome = view.findViewById(R.id.tvWelcomeEstadistica)
        barChart = view.findViewById(R.id.barChart)

        mostrarBienvenida()
        iniciarActualizacionAutomatica()

        return view
    }

    private fun mostrarBienvenida() {
        val sharedPref = activity?.getSharedPreferences("MyAppPref", AppCompatActivity.MODE_PRIVATE)
        val nombre = sharedPref?.getString("nombre_usuario", "Usuario") ?: "Usuario"
        tvWelcome.text = "BIENVENIDO, $nombre"
    }


    private fun iniciarActualizacionAutomatica() {
        handler.post(object : Runnable {
            override fun run() {
                obtenerUltimoSensor()
                handler.postDelayed(this, updateInterval)
            }
        })
    }

    private fun obtenerUltimoSensor() {
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

                    val temperatura = json.optDouble("temperatura", 0.0).toFloat()
                    val humedad = json.optDouble("humedad", 0.0).toFloat()


                    if (temperatura != ultimaTemperatura || humedad != ultimaHumedad) {
                        ultimaTemperatura = temperatura
                        ultimaHumedad = humedad

                        activity?.runOnUiThread {
                            mostrarGrafico(temperatura, humedad)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun mostrarGrafico(temp: Float, hum: Float) {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, temp))
        entries.add(BarEntry(1f, hum))

        val dataSet = BarDataSet(entries, "Lectura actual del sistema")
        dataSet.colors = listOf(
            resources.getColor(android.R.color.holo_red_light, null),
            resources.getColor(android.R.color.holo_blue_light, null)
        )
        dataSet.valueTextSize = 14f

        val data = BarData(dataSet)
        data.barWidth = 0.5f

        barChart.data = data
        barChart.setFitBars(true)
        barChart.animateY(0)
        barChart.description = Description().apply {
            text = "Temperatura y Humedad actuales"
        }

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(listOf("Temperatura (°C)", "Humedad (%)"))
        xAxis.granularity = 1f
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        barChart.axisRight.isEnabled = false
        barChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }
}
