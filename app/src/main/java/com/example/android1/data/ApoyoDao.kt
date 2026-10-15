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

    fun porZona(zonaId: Long): List<Apoyo> = consultar("${ContratoIquique.Apoyo.ZONA_ID} = ?", arrayOf("$zonaId"), null)

    fun porCategoria(categoria: String): List<Apoyo> =
        consultar("${ContratoIquique.Apoyo.CATEGORIA} = ?", arrayOf(categoria), null)

    fun buscar(texto: String): List<Apoyo> =
        consultar(
            "${ContratoIquique.Apoyo.MENSAJE} LIKE ? OR ${ContratoIquique.Apoyo.AUTOR} LIKE ?",
            arrayOf("%$texto%", "%$texto%"),
            null,
        )

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

    fun contar(): Int =
        db.rawQuery("SELECT COUNT(*) FROM ${ContratoIquique.Apoyo.TABLA}", null)
            .use { if (it.moveToFirst()) it.getInt(0) else 0 }

    fun contarPorZona(zonaId: Long): Int =
        db.rawQuery(
            "SELECT COUNT(*) FROM ${ContratoIquique.Apoyo.TABLA} WHERE ${ContratoIquique.Apoyo.ZONA_ID} = ?",
            arrayOf(zonaId.toString()),
        ).use { if (it.moveToFirst()) it.getInt(0) else 0 }

    private fun consultar(seleccion: String?, argumentos: Array<String>?, orden: String?): List<Apoyo> {
        val consulta = """
            SELECT
                ${ContratoIquique.Apoyo.COLUMNAS.joinToString(", ")},
                ${ContratoIquique.Zona.TABLA}.${ContratoIquique.Zona.NOMBRE} AS nombre_zona
            FROM ${ContratoIquique.Apoyo.TABLA}
            INNER JOIN ${ContratoIquique.Zona.TABLA}
                ON ${ContratoIquique.Zona.ID} = ${ContratoIquique.Apoyo.ZONA_ID}
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
}