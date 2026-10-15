package com.example.android1.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Reporte(
    val id: Long,
    val zonaId: Long,
    val autor: String,
    val categoria: String,
    val mensaje: String,
    val fecha: Date,
    val zonaNombre: String = "",
) {
    val fechaTexto: String get() = FORMATO.format(fecha)

    companion object {
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