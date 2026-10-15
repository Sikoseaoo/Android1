package com.example.android1.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.util.Date

class ReporteDao internal constructor(private val db: SQLiteDatabase) {

    constructor(contexto: Context) : this(
        BaseDeDatosHelper.obtener(contexto).writableDatabase
    )

    fun listar(): List<Reporte> = consultar(null, null, null)

    fun porZona(zonaId: Long): List<Reporte> =
        consultar("${T}.${ContratoIquique.Reporte.ZONA_ID} = ?", arrayOf("$zonaId"), null)

    /**
     * Filtra por zona, categoria y texto libre. Los tres criterios son opcionales y
     * se arman con selectionArgs para no concatenar valores en la consulta.
     */
    fun filtrar(zonaId: Long?, categoria: String?, texto: String?): List<Reporte> {
        val condiciones = mutableListOf<String>()
        val argumentos = mutableListOf<String>()

        if (zonaId != null) {
            condiciones += "${T}.${ContratoIquique.Reporte.ZONA_ID} = ?"
            argumentos += zonaId.toString()
        }
        if (!categoria.isNullOrEmpty()) {
            condiciones += "${T}.${ContratoIquique.Reporte.CATEGORIA} = ?"
            argumentos += categoria
        }
        if (!texto.isNullOrEmpty()) {
            condiciones += "(${T}.${ContratoIquique.Reporte.MENSAJE} LIKE ?" +
                " OR ${T}.${ContratoIquique.Reporte.AUTOR} LIKE ?)"
            argumentos += "%$texto%"
            argumentos += "%$texto%"
        }

        return consultar(
            condiciones.takeIf { it.isNotEmpty() }?.joinToString(" AND "),
            argumentos.toTypedArray(),
            null,
        )
    }

    fun porId(id: Long): Reporte? =
        consultar("${T}.${ContratoIquique.Reporte.ID} = ?", arrayOf("$id"), null).firstOrNull()

    fun insertar(reporte: Reporte): Long =
        db.insert(ContratoIquique.Reporte.TABLA, null, reporte.aValues())

    fun actualizar(reporte: Reporte): Int =
        db.update(
            ContratoIquique.Reporte.TABLA,
            reporte.aValues(),
            "${ContratoIquique.Reporte.ID} = ?",
            arrayOf(reporte.id.toString()),
        )

    fun eliminar(id: Long): Int =
        db.delete(
            ContratoIquique.Reporte.TABLA,
            "${ContratoIquique.Reporte.ID} = ?",
            arrayOf(id.toString()),
        )

    fun eliminarPorZona(zonaId: Long): Int =
        db.delete(
            ContratoIquique.Reporte.TABLA,
            "${ContratoIquique.Reporte.ZONA_ID} = ?",
            arrayOf(zonaId.toString()),
        )

    fun contar(): Int =
        db.rawQuery("SELECT COUNT(*) FROM ${ContratoIquique.Reporte.TABLA}", null)
            .use { if (it.moveToFirst()) it.getInt(0) else 0 }

    fun contarPorCategoria(): Map<String, Int> {
        val consulta = """
            SELECT ${ContratoIquique.Reporte.CATEGORIA}, COUNT(*)
            FROM ${T}
            GROUP BY ${ContratoIquique.Reporte.CATEGORIA}
        """.trimIndent()

        return db.rawQuery(consulta, null).use { cursor ->
            val conteo = mutableMapOf<String, Int>()
            while (cursor.moveToNext()) {
                conteo[cursor.getString(0)] = cursor.getInt(1)
            }
            conteo
        }
    }

    private fun consultar(seleccion: String?, argumentos: Array<String>?, orden: String?): List<Reporte> {
        val consulta = """
            SELECT
                ${ContratoIquique.Reporte.COLUMNAS.joinToString(", ") { "$T.$it" }},
                $Z.${ContratoIquique.Zona.NOMBRE} AS nombre_zona
            FROM $T
            INNER JOIN $Z ON $Z.${ContratoIquique.Zona.ID} = $T.${ContratoIquique.Reporte.ZONA_ID}
            ${seleccion?.let { "WHERE $it" } ?: ""}
            ORDER BY ${orden ?: "${T}.${ContratoIquique.Reporte.FECHA} DESC"}
        """.trimIndent()

        return db.rawQuery(consulta, argumentos).use { cursor ->
            val lista = mutableListOf<Reporte>()
            while (cursor.moveToNext()) lista.add(cursor.aReporte())
            lista
        }
    }

    private fun Cursor.aReporte() = Reporte(
        id = getLong(getColumnIndexOrThrow(ContratoIquique.Reporte.ID)),
        zonaId = getLong(getColumnIndexOrThrow(ContratoIquique.Reporte.ZONA_ID)),
        autor = getString(getColumnIndexOrThrow(ContratoIquique.Reporte.AUTOR)),
        categoria = getString(getColumnIndexOrThrow(ContratoIquique.Reporte.CATEGORIA)),
        mensaje = getString(getColumnIndexOrThrow(ContratoIquique.Reporte.MENSAJE)),
        fecha = Date(getLong(getColumnIndexOrThrow(ContratoIquique.Reporte.FECHA))),
        zonaNombre = getString(getColumnIndexOrThrow("nombre_zona")),
    )

    private fun Reporte.aValues() = ContentValues().apply {
        put(ContratoIquique.Reporte.ZONA_ID, zonaId)
        put(ContratoIquique.Reporte.AUTOR, autor)
        put(ContratoIquique.Reporte.CATEGORIA, categoria)
        put(ContratoIquique.Reporte.MENSAJE, mensaje)
        put(ContratoIquique.Reporte.FECHA, fecha.time)
    }

    private companion object {
        const val T = ContratoIquique.Reporte.TABLA
        const val Z = ContratoIquique.Zona.TABLA
    }
}