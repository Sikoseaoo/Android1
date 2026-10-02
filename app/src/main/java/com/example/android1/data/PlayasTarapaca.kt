package com.example.android1.data

import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint

object PlayasTarapaca {

    val CENTRO: GeoPoint = GeoPoint(-20.2397, -70.1432)

    val todas: List<Playa> = listOf(
        Playa(
            nombre = "Playa Brava",
            latitud = -20.2479147,
            longitud = -70.1394501,
            descripcion = "Playa del centro de Iquique, junto a las esculturas del borde costero.",
        ),
        Playa(
            nombre = "Playa Cavancha",
            latitud = -20.2313775,
            longitud = -70.1469015,
            descripcion = "Playa ancha y tranquila al sur del centro, protegida por los cerros.",
        ),
    )

    const val ZOOM_POR_DEFECTO = 12.0

    const val ZOOM_USUARIO = 15.0

    fun limites(): BoundingBox? {
        if (todas.isEmpty()) return null

        var latitudMin = Double.MAX_VALUE
        var latitudMax = -Double.MAX_VALUE
        var longitudMin = Double.MAX_VALUE
        var longitudMax = -Double.MAX_VALUE

        for (playa in todas) {
            latitudMin = minOf(latitudMin, playa.latitud)
            latitudMax = maxOf(latitudMax, playa.latitud)
            longitudMin = minOf(longitudMin, playa.longitud)
            longitudMax = maxOf(longitudMax, playa.longitud)
        }

        return BoundingBox(latitudMax, longitudMax, latitudMin, longitudMin)
    }
}