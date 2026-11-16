package com.example.android1.data

import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint

/**
 * Catálogo inicial de zonas de Iquique y valores por defecto del mapa.
 *
 * Se usa al crear la base de datos y para llenar los filtros de categoría.
 */
object IquiqueCatalogo {

    /** Categorías disponibles para las zonas. */
    val CATEGORIAS = listOf("Playa", "Plaza", "Histórico", "Naturaleza", "Comercio")

    /** Zoom que se aplica al abrir el mapa. */
    const val ZOOM_POR_DEFECTO = 12.0

    /** Zoom que se aplica al centrar en la ubicación del usuario. */
    const val ZOOM_USUARIO = 15.0

    /** Centro del mapa sobre Iquique. */
    val CENTRO = GeoPoint(-20.2141, -70.1439)

    /** Zonas que se cargan en la base la primera vez. */
    val ZONAS = listOf(
        Zona(
            id = 0,
            nombre = "Playa Brava",
            categoria = "Playa",
            latitud = -20.2479147,
            longitud = -70.1394501,
            descripcion = "Playa del centro, la más larga de la ciudad y con las esculturas del borde costero.",
        ),
        Zona(
            id = 0,
            nombre = "Playa Cavancha",
            categoria = "Playa",
            latitud = -20.2313775,
            longitud = -70.1469015,
            descripcion = "Playa ancha al sur, protegida por los cerros y muy usada por los surfistas.",
        ),
        Zona(
            id = 0,
            nombre = "Playa La Isla",
            categoria = "Playa",
            latitud = -20.2260000,
            longitud = -70.1560000,
            descripcion = "Tramo tranquilo al sur de la costanera, con menos gente que las playas centrales.",
        ),
        Zona(
            id = 0,
            nombre = "Plaza Prat",
            categoria = "Plaza",
            latitud = -20.2148000,
            longitud = -70.1415000,
            descripcion = "Plaza central de Iquique, hito histórico del puerto y de la historia del país.",
        ),
        Zona(
            id = 0,
            nombre = "Plaza Perú",
            categoria = "Plaza",
            latitud = -20.2200000,
            longitud = -70.1490000,
            descripcion = "Plaza del sector sur, muy usada por las familias de la ciudad.",
        ),
        Zona(
            id = 0,
            nombre = "Mercado de Iquique",
            categoria = "Comercio",
            latitud = -20.2141000,
            longitud = -70.1439000,
            descripcion = "Paseo peatonal con puestos de comida y artesanía típica del norte.",
        ),
        Zona(
            id = 0,
            nombre = "Avenida Argentina",
            categoria = "Comercio",
            latitud = -20.2190000,
            longitud = -70.1440000,
            descripcion = "Principal corredor comercial de la ciudad, con bancos y locales.",
        ),
        Zona(
            id = 0,
            nombre = "Zofiac",
            categoria = "Naturaleza",
            latitud = -20.1897000,
            longitud = -70.1506000,
            descripcion = "Parque de la Zofiac, con areas verdes y mirador hacia la bahía.",
        ),
        Zona(
            id = 0,
            nombre = "Cerro Dragón",
            categoria = "Naturaleza",
            latitud = -20.2440000,
            longitud = -70.1345000,
            descripcion = "Cerro al norte de la ciudad, con rutas de trekking y vista panorámica.",
        ),
        Zona(
            id = 0,
            nombre = "Muelle de Pasajeros",
            categoria = "Histórico",
            latitud = -20.2098000,
            longitud = -70.1462000,
            descripcion = "Muelle del puerto desde donde salen las excursiones marítimas.",
        ),
        Zona(
            id = 0,
            nombre = "Corbeta Esmeralda",
            categoria = "Histórico",
            latitud = -20.2085000,
            longitud = -70.1505000,
            descripcion = "Réplica del buque insignia de Arturo Prat, expuesta a la costanera.",
        ),
        Zona(
            id = 0,
            nombre = "Base Naval",
            categoria = "Histórico",
            latitud = -20.2119000,
            longitud = -70.1546000,
            descripcion = "Base naval de Iquique, con acceso restringido por seguridad.",
        ),
    )
}