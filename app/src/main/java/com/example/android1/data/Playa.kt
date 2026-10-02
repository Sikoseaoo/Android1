package com.example.android1.data

import org.osmdroid.util.GeoPoint

data class Playa(
    val nombre: String,
    val latitud: Double,
    val longitud: Double,
    val descripcion: String,
) {
    val posicion: GeoPoint get() = GeoPoint(latitud, longitud)
}