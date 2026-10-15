package com.example.android1.data

import android.provider.BaseColumns

object ContratoIquique {

    const val VERSION = 1
    const val NOMBRE_BASE = "iquique.db"

    /** Zona de Iquique que la comunidad puede apoyar. */
    object Zona : BaseColumns {
        const val ID = BaseColumns._ID
        const val TABLA = "zona"
        const val NOMBRE = "nombre"
        const val CATEGORIA = "categoria"
        const val LATITUD = "latitud"
        const val LONGITUD = "longitud"
        const val DESCRIPCION = "descripcion"

        val COLUMNAS = arrayOf(ID, NOMBRE, CATEGORIA, LATITUD, LONGITUD, DESCRIPCION)
    }

    /** Comentario de apoyo que alguien deja sobre una zona. */
    object Apoyo : BaseColumns {
        const val ID = BaseColumns._ID
        const val TABLA = "apoyo"
        const val ZONA_ID = "zona_id"
        const val AUTOR = "autor"
        const val CATEGORIA = "categoria"
        const val MENSAJE = "mensaje"
        const val FECHA = "fecha"

        val COLUMNAS = arrayOf(ID, ZONA_ID, AUTOR, CATEGORIA, MENSAJE, FECHA)
    }

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

    const val CREAR_APOYO = """
        CREATE TABLE ${Apoyo.TABLA} (
            ${Apoyo.ID}        INTEGER PRIMARY KEY AUTOINCREMENT,
            ${Apoyo.ZONA_ID}   INTEGER NOT NULL,
            ${Apoyo.AUTOR}     TEXT    NOT NULL,
            ${Apoyo.CATEGORIA} TEXT    NOT NULL,
            ${Apoyo.MENSAJE}   TEXT    NOT NULL,
            ${Apoyo.FECHA}     INTEGER NOT NULL,
            FOREIGN KEY (${Apoyo.ZONA_ID}) REFERENCES ${Zona.TABLA}(${Zona.ID})
        )
    """

    const val CREAR_INDICE =
        "CREATE INDEX idx_apoyo_zona ON ${Apoyo.TABLA}(${Apoyo.ZONA_ID})"
}