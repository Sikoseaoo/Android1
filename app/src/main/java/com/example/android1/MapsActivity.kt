package com.example.android1

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.android1.data.IquiqueCatalogo
import com.example.android1.data.Zona
import com.example.android1.data.ZonaDao
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File

/**
 * Usa Google Maps cuando hay clave configurada y Google Play Services esta
 * disponible. Si falta cualquiera de las dos cosas, cae a OpenStreetMap para
 * que la app siga mostrando el mapa.
 */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var contenedor: FrameLayout
    private lateinit var mapaOsm: MapView
    private lateinit var etiquetaMapa: TextView
    private lateinit var filtroCategoria: Spinner

    private val zonaDao by lazy { ZonaDao(this) }

    private var googleMap: GoogleMap? = null
    private var marcadoresOsm = mutableListOf<Marker>()
    private var marcadorUbicacion: Marker? = null
    private var pendientes: List<Zona> = emptyList()
    private val zonasPorId = mutableMapOf<String, Zona>()

    private val pedirPermiso =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
            if (concedido) centrarEnMiUbicacion() else avisar(getString(R.string.error_permiso_ubicacion))
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        contenedor = findViewById(R.id.contenedorMapa)
        mapaOsm = findViewById(R.id.mapaOsm)
        etiquetaMapa = findViewById(R.id.etiquetaMapa)

        configurarFiltro()
        prepararMapa()

        findViewById<FloatingActionButton>(R.id.botonMiUbicacion).setOnClickListener {
            pedirUbicacion()
        }
    }

    private fun usarGoogle(): Boolean {
        if (BuildConfig.CLAVE_MAPS.isBlank()) return false
        return GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS
    }

    private fun prepararMapa() {
        if (usarGoogle()) {
            etiquetaMapa.text = getString(R.string.mapa_google)

            val existente = supportFragmentManager.findFragmentById(R.id.contenedorMapa)
            if (existente is SupportMapFragment) return

            val fragmento = SupportMapFragment.newInstance()
            fragmento.getMapAsync(this)
            supportFragmentManager.beginTransaction()
                .replace(R.id.contenedorMapa, fragmento)
                .commit()
            mapaOsm.visibility = View.GONE
        } else {
            etiquetaMapa.text = getString(R.string.mapa_respaldo)
            configurarOsmdroid()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isMapToolbarEnabled = false
        mapaOsm.visibility = View.GONE

        if (pendientes.isNotEmpty()) {
            dibujarEnGoogle(pendientes)
            pendientes = emptyList()
        }
    }

    private fun configurarOsmdroid() {
        val configuracion = Configuration.getInstance()
        configuracion.load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        configuracion.userAgentValue = packageName

        val base = File(cacheDir, "osmdroid")
        configuracion.osmdroidBasePath = base
        configuracion.osmdroidTileCache = File(base, "tiles")

        mapaOsm.setTileSource(TileSourceFactory.MAPNIK)
        mapaOsm.setMultiTouchControls(true)
        mapaOsm.setUseDataConnection(true)
        mapaOsm.controller.setZoom(IquiqueCatalogo.ZOOM_POR_DEFECTO)
        mapaOsm.controller.setCenter(IquiqueCatalogo.CENTRO)
    }

    private fun configurarFiltro() {
        filtroCategoria = findViewById(R.id.spinnerCategoriaMapa)
        filtroCategoria.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf(getString(R.string.filtro_todas_categorias)) + IquiqueCatalogo.CATEGORIAS,
        )
        filtroCategoria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(padre: AdapterView<*>?, vista: View?, posicion: Int, id: Long) {
                cargarZonas()
            }

            override fun onNothingSelected(padre: AdapterView<*>?) = Unit
        }
    }

    private fun cargarZonas() {
        val posicion = filtroCategoria.selectedItemPosition

        Tareas.consultar({
            if (posicion > 0) zonaDao.porCategoria(IquiqueCatalogo.CATEGORIAS[posicion - 1])
            else zonaDao.listar()
        }) { zonas ->
            if (zonas.isEmpty()) return@consultar
            dibujarZonas(zonas)
        }
    }

    private fun dibujarZonas(zonas: List<Zona>) {
        if (googleMap != null) {
            dibujarEnGoogle(zonas)
        } else {
            dibujarEnOsm(zonas)
        }
    }

    private fun dibujarEnGoogle(zonas: List<Zona>) {
        val mapa = googleMap ?: return
        mapa.clear()
        zonasPorId.clear()

        for (zona in zonas) {
            val opciones = MarkerOptions()
                .position(LatLng(zona.latitud, zona.longitud))
                .title(zona.nombre)
                .snippet(zona.descripcion)

            mapa.addMarker(opciones)?.tag = zona.id.toString()
            zonasPorId[zona.id.toString()] = zona
        }

        // En esta version de la libreria el listener va en el mapa, no en el marcador.
        mapa.setOnMarkerClickListener { marcador ->
            val zona = marcador.tag as? String
            if (zona == null) return@setOnMarkerClickListener false

            val encontrada = zonasPorId[zona] ?: return@setOnMarkerClickListener false
            abrirZona(encontrada)
            true
        }

        val limites = LatLngBounds.builder()
            .apply { zonas.forEach { include(LatLng(it.latitud, it.longitud)) } }
            .build()

        mapa.moveCamera(CameraUpdateFactory.newLatLngBounds(limites, 80))
    }

    private fun dibujarEnOsm(zonas: List<Zona>) {
        mapaOsm.overlays.removeAll(marcadoresOsm)
        marcadoresOsm.clear()

        for (zona in zonas) {
            val marcador = Marker(mapaOsm)
            marcador.position = zona.posicion
            marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marcador.setTitle(zona.nombre)
            marcador.setSnippet(zona.descripcion)
            marcador.setOnMarkerClickListener { _, _ ->
                abrirZona(zona)
                true
            }
            mapaOsm.overlays.add(marcador)
            marcadoresOsm.add(marcador)
        }

        val latMin = zonas.minOf { it.latitud }
        val latMax = zonas.maxOf { it.latitud }
        val lonMin = zonas.minOf { it.longitud }
        val lonMax = zonas.maxOf { it.longitud }

        mapaOsm.post {
            runCatching {
                mapaOsm.zoomToBoundingBox(
                    BoundingBox(latMax, lonMax, latMin, lonMin), false, 64
                )
            }.onFailure { avisar(getString(R.string.error_encuadre)) }
        }
        mapaOsm.invalidate()
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

                val punto = LatLng(ubicacion.latitude, ubicacion.longitude)
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(punto, IquiqueCatalogo.ZOOM_USUARIO.toFloat()))

                mostrarEnOsm(ubicacion.latitude, ubicacion.longitude)
            }
            .addOnFailureListener {
                avisar(getString(R.string.error_sin_ubicacion))
            }
    }

    private fun mostrarEnOsm(latitud: Double, longitud: Double) {
        if (googleMap != null) return

        val punto = GeoPoint(latitud, longitud)
        if (marcadorUbicacion == null) {
            val marcador = Marker(mapaOsm)
            marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapaOsm.overlays.add(marcador)
            marcadorUbicacion = marcador
        }
        marcadorUbicacion?.apply {
            position = punto
            title = getString(R.string.ubicacion_actual)
        }
        mapaOsm.controller.setZoom(IquiqueCatalogo.ZOOM_USUARIO)
        mapaOsm.controller.animateTo(punto)
        mapaOsm.invalidate()
    }

    private fun avisar(mensaje: String) {
        Snackbar.make(contenedor, mensaje, Snackbar.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        mapaOsm.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapaOsm.onPause()
    }
}