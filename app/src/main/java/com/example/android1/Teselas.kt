package com.example.android1

import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.MapTileIndex

/**
 * Fuentes de teselas de mapa basadas en Esri.
 *
 * OpenStreetMap no permite usar sus teselas desde una app, por eso se usa Esri.
 */
object Teselas {

    private const val ZOOM_MINIMO = 3
    private const val ZOOM_MAXIMO = 19
    private const val TAMANO = 256
    private const val SIN_URL = ""

    private const val ESRI = "https://server.arcgisonline.com/ArcGIS/rest/services/"

    /** Teselas oscuras de Esri. */
    val oscuro = crear("EsriOscuro", "${ESRI}Canvas/World_Dark_Gray_Base/MapServer/tile/")

    /** Teselas claras de Esri. */
    val claro = crear("EsriClaro", "${ESRI}World_Topo_Map/MapServer/tile/")

    /**
     * Crea una fuente de teselas que arma la URL con el orden `z/y/x` de Esri.
     *
     * @param nombre nombre interno de la fuente.
     * @param base URL base del servicio de teselas.
     * @return la fuente de teselas configurada.
     */
    private fun crear(nombre: String, base: String) = object :
        XYTileSource(nombre, ZOOM_MINIMO, ZOOM_MAXIMO, TAMANO, ".jpg", arrayOf(SIN_URL)) {

        /**
         * Arma la URL de una tesela.
         *
         * @param pMapTileIndex índice de la tesela con zoom, x e y.
         * @return la URL de la imagen de la tesela.
         */
        override fun getTileURLString(pMapTileIndex: Long): String {
            val z = MapTileIndex.getZoom(pMapTileIndex)
            val x = MapTileIndex.getX(pMapTileIndex)
            val y = MapTileIndex.getY(pMapTileIndex)
            return "${base}${z}/${y}/${x}.jpg"
        }
    }
}
