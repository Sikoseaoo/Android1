package com.example.android1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val iquique = LatLng(-20.23, -70.14)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(iquique, 14f))
        
        googleMap.addMarker(MarkerOptions().position(LatLng(-20.24, -70.14)).title("Playa Brava"))
        googleMap.addMarker(MarkerOptions().position(LatLng(-20.22, -70.14)).title("Cavancha"))
    }
}
