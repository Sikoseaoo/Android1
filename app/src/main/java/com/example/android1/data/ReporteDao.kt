package com.example.android1.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.util.Date

/**
 * Acceso a la tabla `reporte` de SQLite.
 *
 * @param db base de datos abierta sobre la que se hacen las consultas.
 * @throws android.database.sqlite.SQLiteException si una consulta o escritura falla.
 */
class ReporteDao internal constructor(private val db: SQLiteDatabase) {

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
     * Trae todos los reclamos, del más nuevo al más viejo.
     *
     * @return lista de reclamos; vacía si no hay ninguno.
     * @throws android.database.sqlite.SQLiteException si falla la consulta.
     */
    fun listar(): List<Reporte> = consultar(null, null, null)

    /**
     * Trae los reclamos de una zona.
     *
     * @param zonaId identificador de la zona.
     * @return lista de reclamos de esa zona.
     * @throws android.database.sqlite.SQLiteException si falla la consulta.
     */
    fun porZona(zonaId: Long): List<Reporte> =
        consultar("${T}.${ContratoIquique.Reporte.ZONA_ID} = ?", arrayOf("$zonaId"), null)

    /**
     * Filtra reclamos por zona, categoría y texto libre.
     *
     * Los filtros se pasan como argumentos (`?`), nunca pegados en la consulta.
     *
     * @param zonaId identificador de la zona, o `null` para no filtrar.
     * @param categoria motivo del reclamo, o `null` para no filtrar.
     * @param texto texto que se busca en el mensaje o en el autor, o `null`.
     * @return lista de reclamos que cumplen los filtros.
     * @throws android.database.sqlite.SQLiteException si falla la consulta.
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

    /**
     * Busca un reclamo por su identificador.
     *
     * @param id identificador del reclamo.
     * @return el reclamo encontrado o `null` si no existe.
     * @throws android.database.sqlite.SQLiteException si falla la consulta.
     */
    fun porId(id: Long): Reporte? =
        consultar("${T}.${ContratoIquique.Reporte.ID} = ?", arrayOf("$id"), null).firstOrNull()

    /**
     * Inserta un reclamo nuevo.
     *
     * @param reporte datos del reclamo; el `id` se ignora.
     * @return el identificador que asignó SQLite, o -1 si falló.
     * @throws android.database.sqlite.SQLiteException si la inserción falla.
     */
    fun insertar(reporte: Reporte): Long =
        db.insert(ContratoIquique.Reporte.TABLA, null, reporte.aValues())

    /**
     * Actualiza un reclamo existente.
     *
     * @param reporte reclamo con los datos nuevos; se busca por su `id`.
     * @return cantidad de filas actualizadas.
     * @throws android.database.sqlite.SQLiteException si la actualización falla.
     */
    fun actualizar(reporte: Reporte): Int =
        db.update(
            ContratoIquique.Reporte.TABLA,
            reporte.aValues(),
            "${ContratoIquique.Reporte.ID} = ?",
            arrayOf(reporte.id.toString()),
        )

    /**
     * Elimina un reclamo por su identificador.
     *
     * @param id identificador del reclamo.
     * @return cantidad de filas eliminadas.
     * @throws android.database.sqlite.SQLiteException si el borrado falla.
     */
    fun eliminar(id: Long): Int =
        db.delete(
            ContratoIquique.Reporte.TABLA,
            "${ContratoIquique.Reporte.ID} = ?",
            arrayOf(id.toString()),
        )

    /**
     * Elimina todos los reclamos de una zona.
     *
     * @param zonaId identificador de la zona.
     * @return cantidad de filas eliminadas.
     * @throws android.database.sqlite.SQLiteException si el borrado falla.
     */
    fun eliminarPorZona(zonaId: Long): Int =
        db.delete(
            ContratoIquique.Reporte.TABLA,
            "${ContratoIquique.Reporte.ZONA_ID} = ?",
            arrayOf(zonaId.toString()),
        )

    /**
     * Cuenta los reclamos guardados.
     *
     * @return cantidad de reclamos.
     * @throws android.database.sqlite.SQLiteException si falla la consulta.
     */
    fun contar(): Int =
        db.rawQuery("SELECT COUNT(*) FROM ${ContratoIquique.Reporte.TABLA}", null)
            .use { if (it.moveToFirst()) it.getInt(0) else 0 }

    /**
     * Cuenta los reclamos agrupados por motivo.
     *
     * @return mapa de categoría a cantidad de reclamos.
     * @throws android.database.sqlite.SQLiteException si falla la consulta.
     */
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

    /**
     * Consulta reclamos junto con el nombre de su zona.
     *
     * @param seleccion condición `WHERE` sin la palabra `WHERE`, o `null`.
     * @param argumentos valores que reemplazan los `?` de la selección.
     * @param orden cláusula `ORDER BY`, o `null` para ordenar por fecha.
     * @return lista de reclamos con `zonaNombre` lleno.
     * @throws android.database.sqlite.SQLiteException si falla la consulta.
     */
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

    /**
     * Arma un reclamo a partir de la fila actual del cursor.
     *
     * @return el reclamo leído.
     * @throws IllegalArgumentException si falta alguna columna esperada.
     */
    private fun Cursor.aReporte() = Reporte(
        id = getLong(getColumnIndexOrThrow(ContratoIquique.Reporte.ID)),
        zonaId = getLong(getColumnIndexOrThrow(ContratoIquique.Reporte.ZONA_ID)),
        autor = getString(getColumnIndexOrThrow(ContratoIquique.Reporte.AUTOR)),
        categoria = getString(getColumnIndexOrThrow(ContratoIquique.Reporte.CATEGORIA)),
        mensaje = getString(getColumnIndexOrThrow(ContratoIquique.Reporte.MENSAJE)),
        fecha = Date(getLong(getColumnIndexOrThrow(ContratoIquique.Reporte.FECHA))),
        zonaNombre = getString(getColumnIndexOrThrow("nombre_zona")),
    )

    /**
     * Pasa los datos del reclamo a `ContentValues` para escribir en SQLite.
     *
     * @return los valores listos para insertar o actualizar.
     */
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
