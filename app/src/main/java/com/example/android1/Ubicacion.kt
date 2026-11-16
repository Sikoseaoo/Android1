package com.example.android1

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task

/**
 * Acceso a la última ubicación conocida del dispositivo.
 */
object Ubicacion {

    private var cliente: FusedLocationProviderClient? = null

    /**
     * Revisa si el usuario ya dio el permiso de ubicación.
     *
     * @param contexto contexto con el que se revisa el permiso.
     * @return `true` si `ACCESS_FINE_LOCATION` está concedido.
     */
    fun permisoConcedido(contexto: Context): Boolean =
        ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * Pide la última ubicación conocida del dispositivo.
     *
     * @param contexto contexto con el que se obtiene el cliente de ubicación.
     * @return tarea que entrega la ubicación, o `null` si no hay una reciente.
     * @throws SecurityException si se llama sin tener el permiso de ubicación.
     */
    @SuppressLint("MissingPermission")
    fun ultima(contexto: Context): Task<Location> = obtenerCliente(contexto).lastLocation

    private fun obtenerCliente(contexto: Context): FusedLocationProviderClient =
        cliente ?: LocationServices.getFusedLocationProviderClient(contexto.applicationContext)
            .also { cliente = it }
}
