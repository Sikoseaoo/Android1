package com.example.android1

import android.os.Handler
import android.os.Looper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Ejecuta el trabajo pesado fuera del hilo de la interfaz.
 *
 * Las consultas a SQLite corren en un pool de hilos y el resultado se devuelve
 * al hilo principal para poder tocar las vistas.
 */
object Tareas {

    private val principal = Handler(Looper.getMainLooper())
    private var executor: ExecutorService? = null

    private val pool: ExecutorService
        get() = executor?.takeIf { !it.isShutdown }
            ?: Executors.newFixedThreadPool(3).also { executor = it }

    /**
     * Ejecuta una operación en segundo plano y entrega el resultado en el hilo principal.
     *
     * @param operacion trabajo que se ejecuta en un hilo del pool.
     * @param alTerminar callback que recibe el resultado en el hilo principal.
     * @param T tipo del resultado de la operación.
     */
    fun <T> consultar(operacion: () -> T, alTerminar: (T) -> Unit) {
        pool.execute {
            val resultado = operacion()
            principal.post { alTerminar(resultado) }
        }
    }

    /**
     * Ejecuta una operación en segundo plano sin devolver resultado.
     *
     * @param operacion trabajo que se ejecuta en un hilo del pool.
     */
    fun ejecutar(operacion: () -> Unit) {
        pool.execute { operacion() }
    }
}
