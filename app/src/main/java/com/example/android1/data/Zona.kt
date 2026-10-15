package com.example.android1.data

import org.osmdroid.util.GeoPoint

data class Zona(
    val id: Long,
    val nombre: String,
    val categoria: String,
    val latitud: Double,
    val longitud: Double,
    val descripcion: String,
) {
    val posicion: GeoPoint get() = GeoPoint(latitud, longitud)
}