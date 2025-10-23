package com.example.gestrestauhco

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegistrarse = findViewById<TextView>(R.id.tvRegistrarse)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Complete los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            iniciarSesion(email, password)
        }

        tvRegistrarse.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun iniciarSesion(email: String, password: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://gestrestauhco-1.onrender.com/auth/login")
                val conexion = url.openConnection() as HttpURLConnection
                conexion.requestMethod = "POST"
                conexion.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conexion.doOutput = true

                val json = JSONObject()
                json.put("email", email)
                json.put("password", password)

                val output = DataOutputStream(conexion.outputStream)
                output.writeBytes(json.toString())
                output.flush()
                output.close()

                val responseCode = conexion.responseCode
                val responseMessage = conexion.inputStream.bufferedReader().use { it.readText() }

                Log.d("LoginResponse", responseMessage)

                withContext(Dispatchers.Main) {
                    if (responseCode == 200) {
                        val responseJson = JSONObject(responseMessage)

                        // Obtener datos del usuario del JSON
                        val userJson = if (responseJson.has("user")) responseJson.getJSONObject("user") else responseJson

                        val nombreUsuario = if (userJson.has("nombre")) userJson.getString("nombre").trim() else "Usuario"
                        val emailUsuario = if (userJson.has("email")) userJson.getString("email").trim() else "Correo no disponible"

                        // Guardar en SharedPreferences
                        val sharedPref = getSharedPreferences("MyAppPref", MODE_PRIVATE)
                        sharedPref.edit().putString("nombre_usuario", nombreUsuario).apply()
                        sharedPref.edit().putString("email_usuario", emailUsuario).apply()

                        Toast.makeText(this@LoginActivity, "Bienvenido", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Error: $responseMessage", Toast.LENGTH_SHORT).show()
                        Log.e("LoginError", "Código $responseCode, mensaje: $responseMessage")
                    }
                }

                conexion.disconnect()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, " Error: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("LoginException", e.toString())
                }
            }
        }
    }
}
