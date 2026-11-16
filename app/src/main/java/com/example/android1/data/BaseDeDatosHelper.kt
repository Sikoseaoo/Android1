package com.example.android1.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Administra la creación y la migración de la base de datos `iquique.db`.
 *
 * @param contexto contexto de la aplicación con el que se abre la base.
 * @throws android.database.sqlite.SQLiteException si la base no se puede abrir.
 */
class BaseDeDatosHelper(contexto: Context) : SQLiteOpenHelper(
    contexto,
    ContratoIquique.NOMBRE_BASE,
    null,
    ContratoIquique.VERSION,
) {

    /**
     * Crea las tablas, el índice y carga el catálogo inicial de zonas.
     *
     * @param db base de datos recién creada.
     * @throws android.database.sqlite.SQLiteException si una sentencia falla.
     */
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(ContratoIquique.CREAR_ZONA)
        db.execSQL(ContratoIquique.CREAR_REPORTE)
        db.execSQL(ContratoIquique.CREAR_INDICE)

        for (zona in IquiqueCatalogo.ZONAS) {
            ZonaDao(db).insertar(zona)
        }
    }

    /**
     * Recrea el esquema cuando cambia la versión de la base.
     *
     * @param db base de datos que se está migrando.
     * @param anterior versión anterior del esquema.
     * @param nueva versión nueva del esquema.
     * @throws android.database.sqlite.SQLiteException si una sentencia falla.
     */
    override fun onUpgrade(db: SQLiteDatabase, anterior: Int, nueva: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${ContratoIquique.Reporte.TABLA}")
        db.execSQL("DROP TABLE IF EXISTS ${ContratoIquique.Zona.TABLA}")
        onCreate(db)
    }

    companion object {
        @Volatile
        private var instancia: BaseDeDatosHelper? = null

        /**
         * Devuelve la única instancia del helper.
         *
         * @param contexto contexto que se usa la primera vez que se crea.
         * @return la instancia compartida del helper.
         */
        fun obtener(contexto: Context) =
            instancia ?: synchronized(this) {
                instancia ?: BaseDeDatosHelper(contexto.applicationContext).also { instancia = it }
            }
    }
}
