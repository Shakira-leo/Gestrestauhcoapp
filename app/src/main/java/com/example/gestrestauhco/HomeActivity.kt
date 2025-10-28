package com.example.gestrestauhco

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Mostrar el fragmento de Control al inicio
        replaceFragment(ControlFragment())

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_control -> replaceFragment(ControlFragment())
                R.id.nav_estadistica -> replaceFragment(EstadisticaFragment())
                R.id.nav_configuracion -> replaceFragment(ConfiguracionFragment())
                R.id.nav_detector -> replaceFragment(DetectorFragment())
//                R.id.nav_detector -> replaceFragment(DetectorFragment())
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
