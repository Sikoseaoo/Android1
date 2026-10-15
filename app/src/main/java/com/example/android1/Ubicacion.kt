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

object Ubicacion {

    private var cliente: FusedLocationProviderClient? = null

    fun permisoConcedido(contexto: Context): Boolean =
        ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun ultima(contexto: Context): Task<Location> = obtenerCliente(contexto).lastLocation

    private fun obtenerCliente(contexto: Context): FusedLocationProviderClient =
        cliente ?: LocationServices.getFusedLocationProviderClient(contexto.applicationContext)
            .also { cliente = it }
}