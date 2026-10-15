package com.example.android1

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

/**
 * Las consultas a SQLite y la lectura de sensores no pueden ir en el hilo de la
 * interfaz, asi que se ejecutan aqui y el resultado vuelve al hilo principal.
 */
object Tareas {

    private val executor = Executors.newFixedThreadPool(3)
    private val principal = Handler(Looper.getMainLooper())

    fun <T> consultar(operacion: () -> T, alTerminar: (T) -> Unit) {
        executor.execute {
            val resultado = operacion()
            principal.post { alTerminar(resultado) }
        }
    }

    fun ejecutar(operacion: () -> Unit) {
        executor.execute { operacion() }
    }

    fun cerrar() {
        executor.shutdown()
    }
}