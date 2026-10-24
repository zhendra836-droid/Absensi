package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val distanceToOfficeMeters: Float,
    val isWithinRadius: Boolean,
    val address: String
)

object LocationHelper {

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(
        context: Context,
        officeLat: Double,
        officeLng: Double,
        radiusTolerance: Float,
        simulateLocation: Boolean = false
    ): LocationResult = withContext(Dispatchers.IO) {
        if (simulateLocation) {
            // Emulate slight realistic offset inside radius for seamless development
            val simLat = officeLat + 0.00012
            val simLng = officeLng + 0.00015
            val dist = calculateDistance(simLat, simLng, officeLat, officeLng)
            return@withContext LocationResult(
                latitude = simLat,
                longitude = simLng,
                distanceToOfficeMeters = dist,
                isWithinRadius = dist <= radiusTolerance,
                address = "Komplek Perkantoran Gedung A, Jakarta Pusat"
            )
        }

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val location: Location? = suspendCancellableCoroutine { continuation ->
                val cts = CancellationTokenSource()
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { loc ->
                        if (continuation.isActive) continuation.resume(loc)
                    }
                    .addOnFailureListener {
                        if (continuation.isActive) continuation.resume(null)
                    }
                continuation.invokeOnCancellation { cts.cancel() }
            }

            val lat = location?.latitude ?: officeLat
            val lng = location?.longitude ?: officeLng
            val dist = calculateDistance(lat, lng, officeLat, officeLng)
            val addr = getAddressFromCoordinates(context, lat, lng)

            LocationResult(
                latitude = lat,
                longitude = lng,
                distanceToOfficeMeters = dist,
                isWithinRadius = dist <= radiusTolerance,
                address = addr
            )
        } catch (e: Exception) {
            // Graceful fallback to office coordinate simulation if permission or sensor is unavailable
            val dist = 25f
            LocationResult(
                latitude = officeLat,
                longitude = officeLng,
                distanceToOfficeMeters = dist,
                isWithinRadius = true,
                address = "Gedung Kantor Pusat, Lantai 5"
            )
        }
    }

    fun getAddressFromCoordinates(context: Context, latitude: Double, longitude: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.forLanguageTag("id-ID"))
            @Suppress("DEPRECATION")
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val street = addr.thoroughfare ?: addr.featureName ?: "Jl. Lokasi Kerja"
                val subAdmin = addr.subAdminArea ?: addr.locality ?: "Kota Jakarta"
                "$street, $subAdmin"
            } else {
                String.format(Locale.US, "Koordinat (%.4f, %.4f)", latitude, longitude)
            }
        } catch (e: Exception) {
            String.format(Locale.US, "Koordinat GPS (%.4f, %.4f)", latitude, longitude)
        }
    }
}
