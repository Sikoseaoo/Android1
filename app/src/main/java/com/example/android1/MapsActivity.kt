package com.example.android1

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.android1.data.Playa
import com.example.android1.data.PlayasTarapaca
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mapa: GoogleMap? = null

    private var playaSeleccionada: Playa? = null

    private val pedirPermisoUbicacion =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
            if (concedido) {
                activarCapaUbicacion()
            } else {
                avisar("Permiso de ubicacion denegado: no se puede mostrar tu posicion.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        (supportFragmentManager.findFragmentById(R.id.mapa) as? SupportMapFragment)
            ?.getMapAsync(this)
            ?: error("No se encontro el fragmento del mapa en activity_maps.xml")

        findViewById<FloatingActionButton>(R.id.botonMiUbicacion).setOnClickListener {
            pedirUbicacion()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapa = googleMap

        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isMapToolbarEnabled = true
        }

        dibujarPlayas(googleMap)
        encuadrarPlayas(googleMap)

        if (tienePermisoUbicacion()) {
            activarCapaUbicacion()
        } else {
            pedirPermisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun dibujarPlayas(googleMap: GoogleMap) {
        for (playa in PlayasTarapaca.todas) {
            googleMap.addMarker(
                MarkerOptions()
                    .position(playa.posicion)
                    .title(playa.nombre)
                    .snippet(playa.descripcion)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
        }

        googleMap.setOnInfoWindowClickListener { marcador ->
            playaSeleccionada = PlayasTarapaca.todas.firstOrNull { it.posicion == marcador.position }
            playaSeleccionada?.let { navegarA(it) }
        }
    }

    private fun encuadrarPlayas(googleMap: GoogleMap) {
        val limites = PlayasTarapaca.limites()

        if (limites == null) {
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    PlayasTarapaca.CENTRO,
                    PlayasTarapaca.ZOOM_POR_DEFECTO,
                )
            )
            return
        }

        val relleno = PlayasTarapaca.rellenoPx(resources.displayMetrics.density)
        val vista = findViewById<View>(R.id.mapa)

        vista.post {
            runCatching {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(limites, relleno))
            }.onFailure {
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        limites.center,
                        PlayasTarapaca.ZOOM_POR_DEFECTO,
                    )
                )
            }
        }
    }

    private fun tienePermisoUbicacion(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    private fun pedirUbicacion() {
        val googleMap = mapa

        if (!tienePermisoUbicacion()) {
            pedirPermisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        activarCapaUbicacion()
        googleMap?.myLocation?.let { ubicacion ->
            val posicion = LatLng(ubicacion.latitude, ubicacion.longitude)
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(posicion, ZOOM_USUARIO)
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun activarCapaUbicacion() {
        mapa?.isMyLocationEnabled = true
    }

    private fun navegarA(playa: Playa) {
        val uriNavegacion = Uri.parse("google.navigation:q=${playa.latitud},${playa.longitud}")
        val uriMapa = Uri.parse(
            "geo:${playa.latitud},${playa.longitud}" +
                "?q=${playa.latitud},${playa.longitud}(${Uri.encode(playa.nombre)})"
        )

        val conNavegacion = Intent(Intent.ACTION_VIEW, uriNavegacion)
            .resolveActivity(packageManager) != null

        val uri = if (conNavegacion) uriNavegacion else uriMapa

        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }.onFailure {
            avisar(getString(R.string.error_navegacion, playa.nombre))
        }
    }

    private fun avisar(mensaje: String) {
        Snackbar.make(findViewById(R.id.mapa), mensaje, Snackbar.LENGTH_LONG).show()
    }

    private companion object {
        const val ZOOM_USUARIO = 15f
    }
}