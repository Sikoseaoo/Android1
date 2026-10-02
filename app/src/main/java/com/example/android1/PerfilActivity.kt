package com.example.android1

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android1.data.PlayasTarapaca
import com.example.android1.data.ReportesAlmacen

class PerfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        findViewById<View>(R.id.botonVolver).setOnClickListener { finish() }

        val nombre = findViewById<TextView>(R.id.perfilNombre)
        val email = findViewById<TextView>(R.id.perfilEmail)
        val region = findViewById<TextView>(R.id.perfilRegion)
        val playas = findViewById<TextView>(R.id.perfilPlayas)
        val reportes = findViewById<TextView>(R.id.perfilReportes)

        nombre.text = getString(R.string.perfil_nombre)
        email.text = getString(R.string.perfil_email)
        region.text = getString(R.string.perfil_region)

        val cantidadPlayas = PlayasTarapaca.todas.size
        val cantidadReportes = ReportesAlmacen.leer(this).size

        playas.text = getString(R.string.perfil_playas_numeradas, cantidadPlayas)
        reportes.text = getString(R.string.perfil_reportes_numerados, cantidadReportes)
    }
}
