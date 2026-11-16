package com.example.android1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Pantalla de inicio con los accesos al mapa, a los reclamos y al perfil.
 */
class MainActivity : AppCompatActivity() {

    /**
     * Arma la pantalla y conecta los tres botones con sus actividades.
     *
     * @param savedInstanceState estado guardado de la actividad, o `null`.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        abrir(R.id.botonMapa, MapsActivity::class.java)
        abrir(R.id.botonReclamos, ReclamosActivity::class.java)
        abrir(R.id.botonPerfil, PerfilActivity::class.java)
    }

    /**
     * Hace que un botón abra otra actividad con un intent explícito.
     *
     * @param id identificador del botón en el layout.
     * @param destino actividad que se abre al tocar el botón.
     */
    private fun abrir(id: Int, destino: Class<*>) {
        findViewById<android.view.View>(id).setOnClickListener {
            startActivity(Intent(this, destino))
        }
    }
}