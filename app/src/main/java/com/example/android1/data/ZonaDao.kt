package com.example.android1.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class ZonaDao internal constructor(private val db: SQLiteDatabase) {

    constructor(contexto: Context) : this(
        BaseDeDatosHelper.obtener(contexto).writableDatabase
    )

    fun listar(): List<Zona> = consultar(null, null)

    fun porCategoria(categoria: String): List<Zona> =
        consultar("${ContratoIquique.Zona.CATEGORIA} = ?", arrayOf(categoria))

    fun porId(id: Long): Zona? = consultar("${ContratoIquique.Zona.ID} = ?", arrayOf("$id")).firstOrNull()

    fun porNombre(nombre: String): Zona? =
        consultar("${ContratoIquique.Zona.NOMBRE} = ?", arrayOf(nombre)).firstOrNull()

    fun insertar(zona: Zona): Long =
        db.insert(ContratoIquique.Zona.TABLA, null, zona.aValues())

    fun actualizar(zona: Zona): Int =
        db.update(
            ContratoIquique.Zona.TABLA,
            zona.aValues(),
            "${ContratoIquique.Zona.ID} = ?",
            arrayOf(zona.id.toString()),
        )

    fun eliminar(id: Long): Int =
        db.delete(
            ContratoIquique.Zona.TABLA,
            "${ContratoIquique.Zona.ID} = ?",
            arrayOf(id.toString()),
        )

    fun contar(): Int =
        db.rawQuery("SELECT COUNT(*) FROM ${ContratoIquique.Zona.TABLA}", null)
            .use { if (it.moveToFirst()) it.getInt(0) else 0 }

    private fun consultar(seleccion: String?, argumentos: Array<String>?): List<Zona> {
        val consulta = """
            SELECT ${ContratoIquique.Zona.COLUMNAS.joinToString(", ")}
            FROM ${ContratoIquique.Zona.TABLA}
            ${seleccion?.let { "WHERE $it" } ?: ""}
            ORDER BY ${ContratoIquique.Zona.NOMBRE} ASC
        """.trimIndent()

        return db.rawQuery(consulta, argumentos).use {
            val lista = mutableListOf<Zona>()
            while (it.moveToNext()) lista.add(it.aZona())
            lista
        }
    }

    private fun Cursor.aZona() = Zona(
        id = getLong(getColumnIndexOrThrow(ContratoIquique.Zona.ID)),
        nombre = getString(getColumnIndexOrThrow(ContratoIquique.Zona.NOMBRE)),
        categoria = getString(getColumnIndexOrThrow(ContratoIquique.Zona.CATEGORIA)),
        latitud = getDouble(getColumnIndexOrThrow(ContratoIquique.Zona.LATITUD)),
        longitud = getDouble(getColumnIndexOrThrow(ContratoIquique.Zona.LONGITUD)),
        descripcion = getString(getColumnIndexOrThrow(ContratoIquique.Zona.DESCRIPCION)),
    )

    private fun Zona.aValues() = ContentValues().apply {
        put(ContratoIquique.Zona.NOMBRE, nombre)
        put(ContratoIquique.Zona.CATEGORIA, categoria)
        put(ContratoIquique.Zona.LATITUD, latitud)
        put(ContratoIquique.Zona.LONGITUD, longitud)
        put(ContratoIquique.Zona.DESCRIPCION, descripcion)
    }
}