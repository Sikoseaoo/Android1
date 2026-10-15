package com.example.android1.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BaseDeDatosHelper(contexto: Context) : SQLiteOpenHelper(
    contexto,
    ContratoIquique.NOMBRE_BASE,
    null,
    ContratoIquique.VERSION,
) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(ContratoIquique.CREAR_ZONA)
        db.execSQL(ContratoIquique.CREAR_REPORTE)
        db.execSQL(ContratoIquique.CREAR_INDICE)

        for (zona in IquiqueCatalogo.ZONAS) {
            ZonaDao(db).insertar(zona)
        }
    }

    // en la v2 la tabla de apoyos paso a llamarse reporte
    override fun onUpgrade(db: SQLiteDatabase, anterior: Int, nueva: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${ContratoIquique.Reporte.TABLA}")
        db.execSQL("DROP TABLE IF EXISTS ${ContratoIquique.Zona.TABLA}")
        onCreate(db)
    }

    companion object {
        @Volatile
        private var instancia: BaseDeDatosHelper? = null

        fun obtener(contexto: Context) =
            instancia ?: synchronized(this) {
                instancia ?: BaseDeDatosHelper(contexto.applicationContext).also { instancia = it }
            }
    }
}