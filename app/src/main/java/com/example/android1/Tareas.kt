package com.example.android1

import android.os.Handler
import android.os.Looper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// las consultas a la base van fuera del hilo de la interfaz
object Tareas {

    private val principal = Handler(Looper.getMainLooper())
    private var executor: ExecutorService? = null

    private val pool: ExecutorService
        get() = executor?.takeIf { !it.isShutdown }
            ?: Executors.newFixedThreadPool(3).also { executor = it }

    fun <T> consultar(operacion: () -> T, alTerminar: (T) -> Unit) {
        pool.execute {
            val resultado = operacion()
            principal.post { alTerminar(resultado) }
        }
    }

    fun ejecutar(operacion: () -> Unit) {
        pool.execute { operacion() }
    }
}