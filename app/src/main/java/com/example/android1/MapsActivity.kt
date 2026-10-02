package com.example.android1

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.android1.data.Playa
import com.example.android1.data.PlayasTarapaca
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import java.io.File

class MapsActivity : AppCompatActivity() {

    private lateinit var mapa: MapView

    private var marcadorUbicacion: Marker? = null

    private val pedirPermiso =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
            if (concedido) {
                centrarEnMiUbicacion()
            } else {
                avisar(getString(R.string.error_permiso_ubicacion))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        configurarOsmdroid()

        mapa = findViewById(R.id.mapa)
        mapa.setTileSource(TileSourceFactory.MAPNIK)
        mapa.setMultiTouchControls(true)
        mapa.setUseDataConnection(true)
        mapa.controller.setZoom(PlayasTarapaca.ZOOM_POR_DEFECTO)
        mapa.controller.setCenter(PlayasTarapaca.CENTRO)

        dibujarPlayas()
        encuadrarPlayas()

        findViewById<FloatingActionButton>(R.id.botonMiUbicacion).setOnClickListener {
            pedirUbicacion()
        }
    }

    private fun configurarOsmdroid() {
        val configuracion = Configuration.getInstance()
        configuracion.load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        configuracion.userAgentValue = packageName

        val base = File(cacheDir, "osmdroid")
        configuracion.osmdroidBasePath = base
        configuracion.osmdroidTileCache = File(base, "tiles")
    }

    private fun dibujarPlayas() {
        for (playa in PlayasTarapaca.todas) {
            val marcador = Marker(mapa)
            marcador.position = playa.posicion
            marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marcador.setTitle(playa.nombre)
            marcador.setSnippet(playa.descripcion)
            marcador.infoWindow = MarkerInfoWindow(R.layout.ventana_playa, mapa)
            marcador.setOnMarkerClickListener { _, _ ->
                abrirEnMapa(playa)
                true
            }
            mapa.overlays.add(marcador)
        }
    }

    private fun abrirEnMapa(playa: Playa) {
        val uri = Uri.parse(
            "geo:${playa.latitud},${playa.longitud}" +
                "?q=${playa.latitud},${playa.longitud}(${Uri.encode(playa.nombre)})"
        )

        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }.onFailure {
            avisar(getString(R.string.error_abrir_mapa, playa.nombre))
        }
    }

    private fun encuadrarPlayas() {
        val limites = PlayasTarapaca.limites() ?: return

        mapa.post {
            runCatching { mapa.zoomToBoundingBox(limites, false, 64) }
                .onFailure { avisar(getString(R.string.error_encuadre)) }
        }
    }

    private fun pedirUbicacion() {
        if (!Ubicacion.permisoConcedido(this)) {
            pedirPermiso.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        centrarEnMiUbicacion()
    }

    private fun centrarEnMiUbicacion() {
        val ubicacion = Ubicacion.ultima(this)

        if (ubicacion == null) {
            avisar(getString(R.string.error_sin_ubicacion))
            return
        }

        val punto = GeoPoint(ubicacion.latitude, ubicacion.longitude)

        if (marcadorUbicacion == null) {
            val marcador = Marker(mapa)
            marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapa.overlays.add(marcador)
            marcadorUbicacion = marcador
        }

        marcadorUbicacion?.apply {
            position = punto
            title = getString(R.string.ubicacion_actual)
        }

        mapa.controller.setZoom(PlayasTarapaca.ZOOM_USUARIO)
        mapa.controller.animateTo(punto)
    }

    private fun avisar(mensaje: String) {
        Snackbar.make(mapa, mensaje, Snackbar.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        mapa.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapa.onPause()
    }
}