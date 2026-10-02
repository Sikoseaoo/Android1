package com.example.android1

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat

object Ubicacion {

    fun permisoConcedido(contexto: Context): Boolean =
        ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED

    @Suppress("MissingPermission")
    fun ultima(contexto: Context): Location? {
        if (!permisoConcedido(contexto)) return null

        val gestor = contexto.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val proveedores = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )

        return proveedores
            .filter { gestor.isProviderEnabled(it) }
            .mapNotNull { gestor.getLastKnownLocation(it) }
            .maxByOrNull { it.time }
    }
}