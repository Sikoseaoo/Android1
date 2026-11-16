package com.example.android1.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Reclamo que un vecino deja sobre una zona.
 *
 * @property id identificador en la base de datos; 0 si todavía no se guarda.
 * @property zonaId identificador de la zona a la que pertenece el reclamo.
 * @property autor nombre de la persona que escribió el reclamo.
 * @property categoria motivo del reclamo.
 * @property mensaje texto del reclamo.
 * @property fecha fecha y hora en que se creó.
 * @property zonaNombre nombre de la zona; se llena al leer con el join.
 */
data class Reporte(
    val id: Long,
    val zonaId: Long,
    val autor: String,
    val categoria: String,
    val mensaje: String,
    val fecha: Date,
    val zonaNombre: String = "",
) {
    /**
     * Da la fecha en formato legible para la lista.
     *
     * @return la fecha como texto `dd/MM/yyyy HH:mm`.
     */
    val fechaTexto: String get() = FORMATO.format(fecha)

    companion object {
        /** Motivos disponibles al crear un reclamo. */
        val CATEGORIAS = listOf(
            "Contaminacion",
            "Basura",
            "Limpieza",
            "Obras",
            "Servicios",
            "Ruido",
            "Seguridad",
        )

        private val FORMATO = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }
}
