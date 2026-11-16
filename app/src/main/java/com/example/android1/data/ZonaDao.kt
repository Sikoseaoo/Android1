package com.example.android1.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

/**
 * Acceso a la tabla `zona` de SQLite.
 *
 * @param db base de datos abierta sobre la que se hacen las consultas.
 * @throws android.database.sqlite.SQLiteException si una consulta o escritura falla.
 */
class ZonaDao internal constructor(private val db: SQLiteDatabase) {

    /**
     * Abre el DAO con la base de datos de la aplicación.
     *
     * @param contexto contexto que se usa para obtener la base de datos.
     * @throws android.database.sqlite.SQLiteException si la base no se puede abrir.
     */
    constructor(contexto: Context) : this(
        BaseDeDatosHelper.obtener(contexto).writableDatabase
    )

    /**
     * Trae todas las zonas ordenadas por nombre.
     *
     * @return lista de zonas; vacía si no hay ninguna.
     * @throws android.database.sqlite.SQLiteException si falla la consulta.
     */
    fun listar(): List<Zona> = consultar(null, null)

    /**
     * Trae las zonas de una categoría.
     *
     * @param categoria categoría que se quiere filtrar.
     * @return lista de zonas de esa categoría.
     * @throws android.database.sqlite.SQLiteException si falla la consulta.
     */
    fun porCategoria(categoria: String): List<Zona> =
        consultar("${ContratoIquique.Zona.CATEGORIA} = ?", arrayOf(categoria))

    /**
     * Busca una zona por su identificador.
     *
     * @param id identificador de la zona.
     * @return la zona encontrada o `null` si no existe.
     * @throws android.database.sqlite.SQLiteException si falla la consulta.
     */
    fun porId(id: Long): Zona? = consultar("${ContratoIquique.Zona.ID} = ?", arrayOf("$id")).firstOrNull()

    /**
     * Busca una zona por su nombre exacto.
     *
     * @param nombre nombre de la zona.
     * @return la zona encontrada o `null` si no existe.
     * @throws android.database.sqlite.SQLiteException si falla la consulta.
     */
    fun porNombre(nombre: String): Zona? =
        consultar("${ContratoIquique.Zona.NOMBRE} = ?", arrayOf(nombre)).firstOrNull()

    /**
     * Inserta una zona nueva.
     *
     * @param zona datos de la zona; el `id` se ignora.
     * @return el identificador que asignó SQLite, o -1 si falló.
     * @throws android.database.sqlite.SQLiteException si la inserción falla.
     */
    fun insertar(zona: Zona): Long =
        db.insert(ContratoIquique.Zona.TABLA, null, zona.aValues())

    /**
     * Actualiza una zona existente.
     *
     * @param zona zona con los datos nuevos; se busca por su `id`.
     * @return cantidad de filas actualizadas.
     * @throws android.database.sqlite.SQLiteException si la actualización falla.
     */
    fun actualizar(zona: Zona): Int =
        db.update(
            ContratoIquique.Zona.TABLA,
            zona.aValues(),
            "${ContratoIquique.Zona.ID} = ?",
            arrayOf(zona.id.toString()),
        )

    /**
     * Elimina una zona por su identificador.
     *
     * @param id identificador de la zona.
     * @return cantidad de filas eliminadas.
     * @throws android.database.sqlite.SQLiteException si el borrado falla.
     */
    fun eliminar(id: Long): Int =
        db.delete(
            ContratoIquique.Zona.TABLA,
            "${ContratoIquique.Zona.ID} = ?",
            arrayOf(id.toString()),
        )

    /**
     * Cuenta las zonas guardadas.
     *
     * @return cantidad de zonas.
     * @throws android.database.sqlite.SQLiteException si falla la consulta.
     */
    fun contar(): Int =
        db.rawQuery("SELECT COUNT(*) FROM ${ContratoIquique.Zona.TABLA}", null)
            .use { if (it.moveToFirst()) it.getInt(0) else 0 }

    /**
     * Consulta zonas con un filtro opcional.
     *
     * @param seleccion condición `WHERE` sin la palabra `WHERE`, o `null`.
     * @param argumentos valores que reemplazan los `?` de la selección.
     * @return lista de zonas ordenadas por nombre.
     * @throws android.database.sqlite.SQLiteException si falla la consulta.
     */
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

    /**
     * Arma una zona a partir de la fila actual del cursor.
     *
     * @return la zona leída.
     * @throws IllegalArgumentException si falta alguna columna esperada.
     */
    private fun Cursor.aZona() = Zona(
        id = getLong(getColumnIndexOrThrow(ContratoIquique.Zona.ID)),
        nombre = getString(getColumnIndexOrThrow(ContratoIquique.Zona.NOMBRE)),
        categoria = getString(getColumnIndexOrThrow(ContratoIquique.Zona.CATEGORIA)),
        latitud = getDouble(getColumnIndexOrThrow(ContratoIquique.Zona.LATITUD)),
        longitud = getDouble(getColumnIndexOrThrow(ContratoIquique.Zona.LONGITUD)),
        descripcion = getString(getColumnIndexOrThrow(ContratoIquique.Zona.DESCRIPCION)),
    )

    /**
     * Pasa los datos de la zona a `ContentValues` para escribir en SQLite.
     *
     * @return los valores listos para insertar o actualizar.
     */
    private fun Zona.aValues() = ContentValues().apply {
        put(ContratoIquique.Zona.NOMBRE, nombre)
        put(ContratoIquique.Zona.CATEGORIA, categoria)
        put(ContratoIquique.Zona.LATITUD, latitud)
        put(ContratoIquique.Zona.LONGITUD, longitud)
        put(ContratoIquique.Zona.DESCRIPCION, descripcion)
    }
}
