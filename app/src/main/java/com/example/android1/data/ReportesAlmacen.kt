package com.example.android1.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.Date

object ReportesAlmacen {

    private const val CLAVE = "reportes"
    private const val CAMPO_ID = "id"
    private const val CAMPO_PLAYA = "playa"
    private const val CAMPO_CATEGORIA = "categoria"
    private const val CAMPO_DESCRIPCION = "descripcion"
    private const val CAMPO_FECHA = "fecha"

    val CATEGORIAS = listOf(
        "Contaminacion",
        "Basura",
        "Seguridad",
        "Servicios",
        "Limpieza",
    )

    fun leer(contexto: Context): List<Reporte> {
        val preferencias = contexto.getSharedPreferences(CLAVE, Context.MODE_PRIVATE)
        val texto = preferencias.getString(CLAVE, null) ?: return emptyList()

        return try {
            val arreglo = JSONArray(texto)
            val lista = mutableListOf<Reporte>()
            for (indice in 0 until arreglo.length()) {
                val item = arreglo.getJSONObject(indice)
                lista.add(
                    Reporte(
                        id = item.optLong(CAMPO_ID, 0L),
                        playa = item.optString(CAMPO_PLAYA),
                        categoria = item.optString(CAMPO_CATEGORIA),
                        descripcion = item.optString(CAMPO_DESCRIPCION),
                        fecha = Date(item.optLong(CAMPO_FECHA)),
                    )
                )
            }
            lista
        } catch (error: Exception) {
            emptyList()
        }
    }

    fun guardar(contexto: Context, reportes: List<Reporte>) {
        val arreglo = JSONArray()
        for (reporte in reportes) {
            val item = JSONObject()
            item.put(CAMPO_ID, reporte.id)
            item.put(CAMPO_PLAYA, reporte.playa)
            item.put(CAMPO_CATEGORIA, reporte.categoria)
            item.put(CAMPO_DESCRIPCION, reporte.descripcion)
            item.put(CAMPO_FECHA, reporte.fecha.time)
            arreglo.put(item)
        }

        contexto.getSharedPreferences(CLAVE, Context.MODE_PRIVATE)
            .edit()
            .putString(CLAVE, arreglo.toString())
            .apply()
    }

    fun agregar(contexto: Context, reporte: Reporte): List<Reporte> {
        val actualizado = leer(contexto) + reporte
        guardar(contexto, actualizado)
        return actualizado
    }

    fun siguienteId(contexto: Context): Long {
        val mayor = leer(contexto).maxOfOrNull { it.id } ?: 0L
        return mayor + 1
    }

    fun hoy(hora: Int, minuto: Int): Date {
        val calendario = Calendar.getInstance()
        calendario.set(Calendar.HOUR_OF_DAY, hora)
        calendario.set(Calendar.MINUTE, minuto)
        calendario.set(Calendar.SECOND, 0)
        calendario.set(Calendar.MILLISECOND, 0)
        return calendario.time
    }

    fun conSemilla(context: Context): List<Reporte> {
        val existentes = leer(context)
        if (existentes.isNotEmpty()) return existentes

        val playaBrava = "Playa Brava"
        val playaCavancha = "Playa Cavancha"

        val semilla = listOf(
            Reporte(
                id = 1,
                playa = playaBrava,
                categoria = "Contaminacion",
                descripcion = "Se observa aceite en la orilla norte, cerca del muelle.",
                fecha = hoy(6, 0),
            ),
            Reporte(
                id = 2,
                playa = playaCavancha,
                categoria = "Limpieza",
                descripcion = "Recogida de residuos completada en el acceso principal.",
                fecha = hoy(9, 0),
            ),
        )

        guardar(context, semilla)
        return semilla
    }

    fun porCategoria(reportes: List<Reporte>, categoria: String): List<Reporte> =
        reportes.filter { it.categoria.equals(categoria, ignoreCase = true) }

    fun porPlaya(reportes: List<Reporte>, playa: String): List<Reporte> =
        reportes.filter { it.playa.equals(playa, ignoreCase = true) }
}