package com.example.gestrestauhco

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity

class EstadisticaFragment : Fragment() {

    private lateinit var tvWelcome: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_estadistica, container, false)
        tvWelcome = view.findViewById(R.id.tvWelcomeEstadistica)

        mostrarBienvenida()

        return view
    }

    private fun mostrarBienvenida() {
        val sharedPref = activity?.getSharedPreferences("MyAppPref", AppCompatActivity.MODE_PRIVATE)
        val nombre = sharedPref?.getString("nombre_usuario", "Usuario") ?: "Usuario"
        tvWelcome.text = "BIENVENIDO, $nombre"
    }
}
