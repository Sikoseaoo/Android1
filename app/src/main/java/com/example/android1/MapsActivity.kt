package com.example.android1

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.android1.data.IquiqueCatalogo
import com.example.android1.data.Zona
import com.example.android1.data.ZonaDao
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import java.io.File

class MapsActivity : AppCompatActivity() {

    private lateinit var mapa: MapView
    private val zonaDao by lazy { ZonaDao(this) }

    private val marcadoresZona = mutableListOf<Marker>()
    private var marcadorUbicacion: Marker? = null
    private var filtroCategoria: Spinner? = null

    private val pedirPermiso =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
            if (concedido) centrarEnMiUbicacion() else avisar(getString(R.string.error_permiso_ubicacion))
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        configurarOsmdroid()

        mapa = findViewById(R.id.mapa)
        mapa.setTileSource(TileSourceFactory.MAPNIK)
        mapa.setMultiTouchControls(true)
        mapa.setUseDataConnection(true)
        mapa.controller.setZoom(IquiqueCatalogo.ZOOM_POR_DEFECTO)
        mapa.controller.setCenter(IquiqueCatalogo.CENTRO)

        configurarFiltro()
        cargarZonas()

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

    private fun cargarZonas() {
        val posicion = filtroCategoria?.selectedItemPosition ?: 0

        Tareas.consultar({
            if (posicion > 0) zonaDao.porCategoria(IquiqueCatalogo.CATEGORIAS[posicion - 1])
            else zonaDao.listar()
        }) { zonas ->
            pintarMarcadores(zonas)
        }
    }

    private fun pintarMarcadores(zonas: List<Zona>) {
        mapa.overlays.removeAll(marcadoresZona)
        marcadoresZona.clear()

        for (zona in zonas) {
            val marcador = Marker(mapa)
            marcador.position = zona.posicion
            marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marcador.setTitle(zona.nombre)
            marcador.setSnippet(zona.descripcion)
            marcador.infoWindow = MarkerInfoWindow(R.layout.ventana_zona, mapa)
            marcador.setOnMarkerClickListener { _, _ ->
                abrirZona(zona)
                true
            }
            mapa.overlays.add(marcador)
            marcadoresZona.add(marcador)
        }

        mapa.invalidate()
        if (zonas.isNotEmpty()) encuadrar(zonas)
    }

    private fun configurarFiltro() {
        val spinner = findViewById<Spinner>(R.id.spinnerCategoriaMapa)
        filtroCategoria = spinner
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf(getString(R.string.filtro_todas_categorias)) + IquiqueCatalogo.CATEGORIAS,
        )
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(padre: AdapterView<*>?, vista: View?, posicion: Int, id: Long) {
                cargarZonas()
            }

            override fun onNothingSelected(padre: AdapterView<*>?) = Unit
        }
    }

    private fun encuadrar(zonas: List<Zona>) {
        val latMin = zonas.minOf { it.latitud }
        val latMax = zonas.maxOf { it.latitud }
        val lonMin = zonas.minOf { it.longitud }
        val lonMax = zonas.maxOf { it.longitud }

        val limites = BoundingBox(latMax, lonMax, latMin, lonMin)

        mapa.post {
            runCatching { mapa.zoomToBoundingBox(limites, false, 64) }
                .onFailure { avisar(getString(R.string.error_encuadre)) }
        }
    }

    private fun abrirZona(zona: Zona) {
        startActivity(
            Intent(this, ZonaActivity::class.java)
                .putExtra(ZonaActivity.EXTRA_ZONA_ID, zona.id)
        )
    }

    private fun pedirUbicacion() {
        if (!Ubicacion.permisoConcedido(this)) {
            pedirPermiso.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        centrarEnMiUbicacion()
    }

    private fun centrarEnMiUbicacion() {
        Ubicacion.ultima(this)
            .addOnSuccessListener { ubicacion ->
                if (ubicacion == null) {
                    avisar(getString(R.string.error_sin_ubicacion))
                    return@addOnSuccessListener
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

                mapa.controller.setZoom(IquiqueCatalogo.ZOOM_USUARIO)
                mapa.controller.animateTo(punto)
                mapa.invalidate()
            }
            .addOnFailureListener {
                avisar(getString(R.string.error_sin_ubicacion))
            }
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