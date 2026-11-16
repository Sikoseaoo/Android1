package com.example.android1.data

import org.osmdroid.util.GeoPoint

/**
 * Zona o punto de interés de Iquique que se muestra en el mapa.
 *
 * @property id identificador en la base de datos; 0 si todavía no se guarda.
 * @property nombre nombre visible de la zona.
 * @property categoria categoría a la que pertenece la zona.
 * @property latitud coordenada de latitud en grados decimales.
 * @property longitud coordenada de longitud en grados decimales.
 * @property descripcion texto que se muestra al abrir la zona.
 */
data class Zona(
    val id: Long,
    val nombre: String,
    val categoria: String,
    val latitud: Double,
    val longitud: Double,
    val descripcion: String,
) {
    /**
     * Convierte la zona a un punto del mapa de osmdroid.
     *
     * @return [GeoPoint] con la latitud y la longitud de la zona.
     */
    val posicion: GeoPoint get() = GeoPoint(latitud, longitud)
}
