package com.example.android1.data

import android.provider.BaseColumns

/**
 * Esquema de la base de datos SQLite: nombres de tablas, columnas y sentencias.
 *
 * Centraliza el contrato para que los DAO no repitan nombres de columnas.
 */
object ContratoIquique {

    /** Versión actual del esquema. */
    const val VERSION = 2

    /** Nombre del archivo de la base de datos. */
    const val NOMBRE_BASE = "iquique.db"

    /**
     * Columnas de la tabla `zona`.
     */
    object Zona : BaseColumns {
        const val ID = BaseColumns._ID
        const val TABLA = "zona"
        const val NOMBRE = "nombre"
        const val CATEGORIA = "categoria"
        const val LATITUD = "latitud"
        const val LONGITUD = "longitud"
        const val DESCRIPCION = "descripcion"

        /** Columnas en el orden en que se leen de la base. */
        val COLUMNAS = arrayOf(ID, NOMBRE, CATEGORIA, LATITUD, LONGITUD, DESCRIPCION)
    }

    /**
     * Columnas de la tabla `reporte`.
     */
    object Reporte : BaseColumns {
        const val ID = BaseColumns._ID
        const val TABLA = "reporte"
        const val ZONA_ID = "zona_id"
        const val AUTOR = "autor"
        const val CATEGORIA = "categoria"
        const val MENSAJE = "mensaje"
        const val FECHA = "fecha"

        /** Columnas en el orden en que se leen de la base. */
        val COLUMNAS = arrayOf(ID, ZONA_ID, AUTOR, CATEGORIA, MENSAJE, FECHA)
    }

    /** Sentencia que crea la tabla `zona`. */
    const val CREAR_ZONA = """
        CREATE TABLE ${Zona.TABLA} (
            ${Zona.ID}         INTEGER PRIMARY KEY AUTOINCREMENT,
            ${Zona.NOMBRE}     TEXT    NOT NULL UNIQUE,
            ${Zona.CATEGORIA}  TEXT    NOT NULL,
            ${Zona.LATITUD}    REAL    NOT NULL,
            ${Zona.LONGITUD}   REAL    NOT NULL,
            ${Zona.DESCRIPCION} TEXT   NOT NULL
        )
    """

    /** Sentencia que crea la tabla `reporte` con su clave foránea. */
    const val CREAR_REPORTE = """
        CREATE TABLE ${Reporte.TABLA} (
            ${Reporte.ID}        INTEGER PRIMARY KEY AUTOINCREMENT,
            ${Reporte.ZONA_ID}   INTEGER NOT NULL,
            ${Reporte.AUTOR}     TEXT    NOT NULL,
            ${Reporte.CATEGORIA} TEXT    NOT NULL,
            ${Reporte.MENSAJE}   TEXT    NOT NULL,
            ${Reporte.FECHA}     INTEGER NOT NULL,
            FOREIGN KEY (${Reporte.ZONA_ID}) REFERENCES ${Zona.TABLA}(${Zona.ID})
        )
    """

    /** Índice para acelerar las consultas de reclamos por zona. */
    const val CREAR_INDICE =
        "CREATE INDEX idx_reporte_zona ON ${Reporte.TABLA}(${Reporte.ZONA_ID})"
}
