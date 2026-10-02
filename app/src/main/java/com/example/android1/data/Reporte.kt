package com.example.android1.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Reporte(
    val id: Long,
    val playa: String,
    val categoria: String,
    val descripcion: String,
    val fecha: Date,
) {
    val fechaTexto: String
        get() = FORMATO.format(fecha)

    private companion object {
        val FORMATO = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }
}