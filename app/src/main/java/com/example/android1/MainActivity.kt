package com.example.android1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        abrir(R.id.botonPlayas, MapsActivity::class.java)
        abrir(R.id.botonReportes, ReportesActivity::class.java)
        abrir(R.id.botonPerfil, PerfilActivity::class.java)
    }

    private fun abrir(id: Int, destino: Class<*>) {
        findViewById<android.view.View>(id).setOnClickListener {
            startActivity(Intent(this, destino))
        }
    }
}