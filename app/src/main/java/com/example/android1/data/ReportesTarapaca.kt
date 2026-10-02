package com.example.android1.data

import java.util.Calendar
import java.util.Date

object ReportesTarapaca {

    val CATEGORIAS = listOf(
        "Contaminacion",
        "Basura",
        "Seguridad",
        "Servicios",
        "Limpieza",
    )

    val todos: List<Reporte> = listOf(
        Reporte(
            playa = "Playa Brava",
            categoria = "Contaminacion",
            descripcion = "Se observo aceite en la orilla norte, cerca del muelle.",
            fecha = hace(2, 15),
        ),
        Reporte(
            playa = "Playa Cavancha",
            categoria = "Servicios",
            descripcion = "El punto de agua potable esta sin funcionar.",
            fecha = hace(3, 5),
        ),
        Reporte(
            playa = "Playa Cavancha",
            categoria = "Basura",
            descripcion = "Botellas y plasticos acumulados en la zona de picnic.",
            fecha = hace(5, 40),
        ),
        Reporte(
            playa = "Playa Cavancha",
            categoria = "Limpieza",
            descripcion = "Recogida de residuos completada en el acceso principal.",
            fecha = hace(8, 30),
        ),
        Reporte(
            playa = "Playa Brava",
            categoria = "Seguridad",
            descripcion = "Corriente de resaca fuerte en el sector sur.",
            fecha = hace(27, 0),
        ),
        Reporte(
            playa = "Playa Brava",
            categoria = "Accesibilidad",
            descripcion = "La rampa de acceso norte tiene el piso danado.",
            fecha = hace(50, 0),
        ),
    )

    fun porCategoria(categoria: String): List<Reporte> =
        todos.filter { it.categoria.equals(categoria, ignoreCase = true) }

    fun porPlaya(playa: String): List<Reporte> =
        todos.filter { it.playa.equals(playa, ignoreCase = true) }

    private fun hace(horas: Int, minutos: Int): Date {
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.HOUR_OF_DAY, -horas)
        calendario.add(Calendar.MINUTE, -minutos)
        return calendario.time
    }
}
