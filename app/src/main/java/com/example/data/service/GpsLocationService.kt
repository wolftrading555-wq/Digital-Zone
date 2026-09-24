package com.example.data.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class GpsCoordinates(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedCoordinates: String
        get() = "%.4f° N, %.4f° E".format(latitude, longitude)

    val googleMapsUrl: String
        get() = "https://maps.google.com/?q=$latitude,$longitude"
}

/**
 * Service to capture and manage live GPS coordinates for field sales visits.
 */
class GpsLocationService(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
) {
    fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    /**
     * Attempts to retrieve the latest GPS coordinates using FusedLocationProviderClient.
     * Falls back gracefully if permissions are not granted or location is unavailable.
     */
    suspend fun getCurrentLocation(): GpsCoordinates? {
        if (!hasLocationPermission()) {
            return null
        }

        return try {
            suspendCancellableCoroutine { continuation ->
                val cancellationToken = CancellationTokenSource()

                continuation.invokeOnCancellation {
                    cancellationToken.cancel()
                }

                try {
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationToken.token
                    ).addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            continuation.resume(
                                GpsCoordinates(
                                    latitude = location.latitude,
                                    longitude = location.longitude,
                                    accuracy = location.accuracy,
                                    timestamp = location.time
                                )
                            )
                        } else {
                            // Fallback to lastLocation if getCurrentLocation returns null
                            fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                                if (lastLoc != null) {
                                    continuation.resume(
                                        GpsCoordinates(
                                            latitude = lastLoc.latitude,
                                            longitude = lastLoc.longitude,
                                            accuracy = lastLoc.accuracy,
                                            timestamp = lastLoc.time
                                        )
                                    )
                                } else {
                                    continuation.resume(null)
                                }
                            }.addOnFailureListener {
                                continuation.resume(null)
                            }
                        }
                    }.addOnFailureListener {
                        continuation.resume(null)
                    }
                } catch (e: SecurityException) {
                    continuation.resume(null)
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
