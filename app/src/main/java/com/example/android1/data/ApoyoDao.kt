package com.example.android1.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.util.Date

class ApoyoDao private constructor(private val db: SQLiteDatabase) {

    constructor(contexto: Context) : this(
        BaseDeDatosHelper.obtener(contexto).writableDatabase
    )

    fun listar(): List<Apoyo> = consultar(null, null, null)

    fun porZona(zonaId: Long): List<Apoyo> =
        consultar("${T}.${ContratoIquique.Apoyo.ZONA_ID} = ?", arrayOf("$zonaId"), null)

    /**
     * Filtra por zona, motivo y texto libre. Los tres criterios son opcionales y
     * se arman con selectionArgs para no concatenar valores en la consulta.
     */
    fun filtrar(zonaId: Long?, categoria: String?, texto: String?): List<Apoyo> {
        val condiciones = mutableListOf<String>()
        val argumentos = mutableListOf<String>()

        if (zonaId != null) {
            condiciones += "${T}.${ContratoIquique.Apoyo.ZONA_ID} = ?"
            argumentos += zonaId.toString()
        }
        if (!categoria.isNullOrEmpty()) {
            condiciones += "${T}.${ContratoIquique.Apoyo.CATEGORIA} = ?"
            argumentos += categoria
        }
        if (!texto.isNullOrEmpty()) {
            condiciones += "(${T}.${ContratoIquique.Apoyo.MENSAJE} LIKE ?" +
                " OR ${T}.${ContratoIquique.Apoyo.AUTOR} LIKE ?)"
            argumentos += "%$texto%"
            argumentos += "%$texto%"
        }

        return consultar(condiciones.joinToString(" AND "), argumentos.toTypedArray(), null)
    }

    fun porId(id: Long): Apoyo? = consultar("${ContratoIquique.Apoyo.ID} = ?", arrayOf("$id"), null).firstOrNull()

    fun insertar(apoyo: Apoyo): Long =
        db.insert(ContratoIquique.Apoyo.TABLA, null, apoyo.aValues())

    fun actualizar(apoyo: Apoyo): Int =
        db.update(
            ContratoIquique.Apoyo.TABLA,
            apoyo.aValues(),
            "${ContratoIquique.Apoyo.ID} = ?",
            arrayOf(apoyo.id.toString()),
        )

    fun eliminar(id: Long): Int =
        db.delete(
            ContratoIquique.Apoyo.TABLA,
            "${ContratoIquique.Apoyo.ID} = ?",
            arrayOf(id.toString()),
        )

    fun eliminarPorZona(zonaId: Long): Int =
        db.delete(
            ContratoIquique.Apoyo.TABLA,
            "${ContratoIquique.Apoyo.ZONA_ID} = ?",
            arrayOf(zonaId.toString()),
        )

    fun contar(): Int =
        db.rawQuery("SELECT COUNT(*) FROM ${ContratoIquique.Apoyo.TABLA}", null)
            .use { if (it.moveToFirst()) it.getInt(0) else 0 }

    private fun consultar(seleccion: String?, argumentos: Array<String>?, orden: String?): List<Apoyo> {
        val consulta = """
            SELECT
                ${ContratoIquique.Apoyo.COLUMNAS.joinToString(", ") { "$T.$it" }},
                $Z.${ContratoIquique.Zona.NOMBRE} AS nombre_zona
            FROM $T
            INNER JOIN $Z ON $Z.${ContratoIquique.Zona.ID} = $T.${ContratoIquique.Apoyo.ZONA_ID}
            ${seleccion?.let { "WHERE $it" } ?: ""}
            ORDER BY ${orden ?: "${ContratoIquique.Apoyo.FECHA} DESC"}
        """.trimIndent()

        return db.rawQuery(consulta, argumentos).use { cursor ->
            val lista = mutableListOf<Apoyo>()
            while (cursor.moveToNext()) lista.add(cursor.aApoyo())
            lista
        }
    }

    private fun Cursor.aApoyo() = Apoyo(
        id = getLong(getColumnIndexOrThrow(ContratoIquique.Apoyo.ID)),
        zonaId = getLong(getColumnIndexOrThrow(ContratoIquique.Apoyo.ZONA_ID)),
        autor = getString(getColumnIndexOrThrow(ContratoIquique.Apoyo.AUTOR)),
        categoria = getString(getColumnIndexOrThrow(ContratoIquique.Apoyo.CATEGORIA)),
        mensaje = getString(getColumnIndexOrThrow(ContratoIquique.Apoyo.MENSAJE)),
        fecha = Date(getLong(getColumnIndexOrThrow(ContratoIquique.Apoyo.FECHA))),
        zonaNombre = getString(getColumnIndexOrThrow("nombre_zona")),
    )

    private fun Apoyo.aValues() = ContentValues().apply {
        put(ContratoIquique.Apoyo.ZONA_ID, zonaId)
        put(ContratoIquique.Apoyo.AUTOR, autor)
        put(ContratoIquique.Apoyo.CATEGORIA, categoria)
        put(ContratoIquique.Apoyo.MENSAJE, mensaje)
        put(ContratoIquique.Apoyo.FECHA, fecha.time)
    }

    private companion object {
        const val T = ContratoIquique.Apoyo.TABLA
        const val Z = ContratoIquique.Zona.TABLA
    }
}