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
 * Mapa de Iquique con las zonas como marcadores.
 *
 * Usa Google Maps si hay clave y Google Play Services; si no, cae a osmdroid con
 * las teselas de Esri.
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

    /**
     * Arma la pantalla, el filtro de categorías y el mapa.
     *
     * @param savedInstanceState estado guardado de la actividad, o `null`.
     */
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

    /**
     * Decide si se puede usar Google Maps.
     *
     * @return `true` si hay clave configurada y Google Play Services disponible.
     */
    private fun usarGoogle(): Boolean {
        if (BuildConfig.CLAVE_MAPS.isBlank()) return false
        return GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS
    }

    /**
     * Prepara el mapa de Google o el de osmdroid según lo que esté disponible.
     */
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
            findViewById<View>(R.id.creditoMapa).visibility = View.VISIBLE
            configurarOsmdroid()
        }
    }

    /**
     * Se llama cuando Google Maps está listo para dibujar.
     *
     * @param map mapa de Google ya inicializado.
     */
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

    /**
     * Configura osmdroid con las teselas de Esri y el centro en Iquique.
     */
    private fun configurarOsmdroid() {
        val configuracion = Configuration.getInstance()
        configuracion.load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))

        configuracion.userAgentValue = USER_AGENT

        val base = File(cacheDir, "osmdroid")
        configuracion.osmdroidBasePath = base
        configuracion.osmdroidTileCache = File(base, "tiles")

        mapaOsm.setTileSource(Teselas.oscuro)
        mapaOsm.setMultiTouchControls(true)
        mapaOsm.setUseDataConnection(true)
        mapaOsm.controller.setZoom(IquiqueCatalogo.ZOOM_POR_DEFECTO)
        mapaOsm.controller.setCenter(IquiqueCatalogo.CENTRO)
    }

    /**
     * Llena el spinner de categorías y recarga las zonas al cambiar la selección.
     */
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

    /**
     * Carga las zonas de la categoría elegida y las dibuja en el mapa.
     */
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

    /**
     * Dibuja las zonas en el mapa que esté activo.
     *
     * @param zonas zonas que se quieren mostrar.
     */
    private fun dibujarZonas(zonas: List<Zona>) {
        if (googleMap != null) {
            dibujarEnGoogle(zonas)
        } else {
            dibujarEnOsm(zonas)
        }
    }

    /**
     * Dibuja las zonas en Google Maps y ajusta la cámara a sus límites.
     *
     * @param zonas zonas que se quieren mostrar.
     */
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

    /**
     * Dibuja las zonas en el mapa de osmdroid y ajusta la vista a sus límites.
     *
     * @param zonas zonas que se quieren mostrar.
     */
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

    /**
     * Abre el detalle de una zona.
     *
     * @param zona zona que se quiere abrir.
     */
    private fun abrirZona(zona: Zona) {
        startActivity(
            Intent(this, ZonaActivity::class.java)
                .putExtra(ZonaActivity.EXTRA_ZONA_ID, zona.id)
        )
    }

    /**
     * Revisa el permiso de ubicación y, si está concedido, centra el mapa.
     */
    private fun pedirUbicacion() {
        if (!Ubicacion.permisoConcedido(this)) {
            pedirPermiso.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        centrarEnMiUbicacion()
    }

    /**
     * Centra el mapa en la última ubicación conocida del dispositivo.
     */
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

    /**
     * Marca la ubicación del usuario en el mapa de osmdroid.
     *
     * @param latitud latitud de la ubicación.
     * @param longitud longitud de la ubicación.
     */
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

    /**
     * Muestra un mensaje breve sobre el mapa.
     *
     * @param mensaje texto que se muestra.
     */
    private fun avisar(mensaje: String) {
        Snackbar.make(contenedor, mensaje, Snackbar.LENGTH_LONG).show()
    }

    /**
     * Reanuda el mapa de osmdroid cuando la actividad vuelve al frente.
     */
    override fun onResume() {
        super.onResume()
        mapaOsm.onResume()
    }

    /**
     * Pausa el mapa de osmdroid cuando la actividad pasa a segundo plano.
     */
    override fun onPause() {
        super.onPause()
        mapaOsm.onPause()
    }

    companion object {
        private const val USER_AGENT = "ReclamosIquique/1.0 (Android; proyecto escolar)"
    }
}
