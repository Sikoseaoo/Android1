package com.example.android1

import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.MapTileIndex

/**
 * Los teseles oficiales de OpenStreetMap rechazan las peticiones que vienen de
 * una app y CartoDB pide API key, asi que se usan los de Esri, que no
 * requieren registro.
 *
 * Esri entrega imagenes jpeg y en el orden z/y/x, distinto al habitual, por eso
 * la URL se arma a mano en vez de dejar que la arme XYTileSource.
 */
object Teselas {

    private const val ZOOM_MINIMO = 3
    private const val ZOOM_MAXIMO = 19
    private const val TAMANO = 256
    private const val SIN_URL = ""

    private const val ESRI = "https://server.arcgisonline.com/ArcGIS/rest/services/"

    val oscuro = crear("EsriOscuro", "${ESRI}Canvas/World_Dark_Gray_Base/MapServer/tile/")

    val claro = crear("EsriClaro", "${ESRI}World_Topo_Map/MapServer/tile/")

    private fun crear(nombre: String, base: String) = object :
        XYTileSource(nombre, ZOOM_MINIMO, ZOOM_MAXIMO, TAMANO, ".jpg", arrayOf(SIN_URL)) {

        override fun getTileURLString(pMapTileIndex: Long): String {
            val z = MapTileIndex.getZoom(pMapTileIndex)
            val x = MapTileIndex.getX(pMapTileIndex)
            val y = MapTileIndex.getY(pMapTileIndex)
            return "${base}${z}/${y}/${x}.jpg"
        }
    }
}