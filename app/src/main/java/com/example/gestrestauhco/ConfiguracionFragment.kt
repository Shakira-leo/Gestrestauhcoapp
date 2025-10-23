package com.example.gestrestauhco

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity

class ConfiguracionFragment : Fragment() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnLogout: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_configuracion, container, false)

        tvWelcome = view.findViewById(R.id.tvWelcome)
        tvEmail = view.findViewById(R.id.tvEmail)
        btnLogout = view.findViewById(R.id.btnLogout)

        mostrarInformacionUsuario()

        btnLogout.setOnClickListener {
            cerrarSesion()
        }

        return view
    }

    private fun mostrarInformacionUsuario() {
        val sharedPref = activity?.getSharedPreferences("MyAppPref", AppCompatActivity.MODE_PRIVATE)
        val nombre = sharedPref?.getString("nombre_usuario", "Usuario") ?: "Usuario"
        val email = sharedPref?.getString("email_usuario", "Correo no disponible") ?: "Correo no disponible"

        tvWelcome.text = "BIENVENIDO, $nombre"
        tvEmail.text = "Correo: $email"
    }

    private fun cerrarSesion() {

        val sharedPref = activity?.getSharedPreferences("MyAppPref", AppCompatActivity.MODE_PRIVATE)
        sharedPref?.edit()?.clear()?.apply()

        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}
