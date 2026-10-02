package com.example.android1.data

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

object PlayasTarapaca {

    val CENTRO: LatLng = LatLng(-20.2397, -70.1432)

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

    const val ZOOM_POR_DEFECTO = 12f

    const val RELLENO_DP = 48

    fun limites(): LatLngBounds? {
        if (todas.isEmpty()) return null

        val limites = LatLngBounds.Builder()
        for (playa in todas) {
            limites.include(playa.posicion)
        }
        return limites.build()
    }

    fun rellenoPx(densidad: Float): Int = (RELLENO_DP * densidad).toInt()
}