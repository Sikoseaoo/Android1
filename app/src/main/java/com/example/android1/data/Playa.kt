package com.example.android1.data

import com.google.android.gms.maps.model.LatLng

data class Playa(
    val nombre: String,
    val latitud: Double,
    val longitud: Double,
    val descripcion: String,
) {
    val posicion: LatLng get() = LatLng(latitud, longitud)
}