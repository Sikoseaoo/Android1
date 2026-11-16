package com.example.android1

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android1.data.ReporteDao
import com.example.android1.data.ZonaDao

/**
 * Perfil con los datos del usuario y el resumen de la base de datos.
 */
class PerfilActivity : AppCompatActivity() {

    private val zonaDao by lazy { ZonaDao(this) }
    private val reporteDao by lazy { ReporteDao(this) }

    /**
     * Arma la pantalla y muestra la cantidad de zonas y de reclamos.
     *
     * @param savedInstanceState estado guardado de la actividad, o `null`.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        findViewById<View>(R.id.botonVolver).setOnClickListener { finish() }

        findViewById<TextView>(R.id.perfilNombre).text = getString(R.string.perfil_nombre)
        findViewById<TextView>(R.id.perfilEmail).text = getString(R.string.perfil_email)
        findViewById<TextView>(R.id.perfilRegion).text = getString(R.string.perfil_region)

        Tareas.consultar({ zonaDao.contar() to reporteDao.contar() }) { (zonas, reclamos) ->
            findViewById<TextView>(R.id.perfilZonas).text = zonas.toString()
            findViewById<TextView>(R.id.perfilReclamos).text = reclamos.toString()
        }
    }
}
