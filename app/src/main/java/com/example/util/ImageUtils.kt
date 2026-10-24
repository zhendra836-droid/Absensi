package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageUtils {

    /**
     * Watermarks a bitmap with employee name, timestamp, coordinates, address, and status.
     * Returns the File path of the saved JPEG.
     */
    fun saveWatermarkedPhoto(
        context: Context,
        sourceBitmap: Bitmap,
        employeeName: String,
        timestamp: Long,
        latitude: Double,
        longitude: Double,
        address: String,
        type: String // "MASUK" or "PULANG"
    ): String {
        val workingBitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(workingBitmap)
        val width = canvas.width
        val height = canvas.height

        // Dark banner at bottom
        val bannerHeight = (height * 0.22f).toInt().coerceAtLeast(140)
        val bgPaint = Paint().apply {
            color = Color.argb(190, 10, 15, 30)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, (height - bannerHeight).toFloat(), width.toFloat(), height.toFloat(), bgPaint)

        // Accent stripe
        val stripePaint = Paint().apply {
            color = if (type == "MASUK") Color.parseColor("#10B981") else Color.parseColor("#3B82F6")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, (height - bannerHeight).toFloat(), width.toFloat(), (height - bannerHeight + 8).toFloat(), stripePaint)

        // Text paints
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = (bannerHeight * 0.16f).coerceIn(24f, 44f)
            isFakeBoldText = true
        }

        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            textSize = (bannerHeight * 0.12f).coerceIn(18f, 32f)
        }

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = (bannerHeight * 0.10f).coerceIn(16f, 26f)
        }

        val paddingLeft = 32f
        var currentY = (height - bannerHeight) + (bannerHeight * 0.24f)

        // Line 1: Type & Name
        val timeStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
        canvas.drawText("PRESENSI $type: $employeeName", paddingLeft, currentY, titlePaint)

        // Line 2: Waktu
        currentY += (bannerHeight * 0.18f)
        canvas.drawText("Waktu: $timeStr WIB", paddingLeft, currentY, bodyPaint)

        // Line 3: Geotag Koordinat
        currentY += (bannerHeight * 0.18f)
        val coordStr = String.format(Locale.US, "GPS: %.6f, %.6f", latitude, longitude)
        canvas.drawText(coordStr, paddingLeft, currentY, bodyPaint)

        // Line 4: Alamat ringkas
        currentY += (bannerHeight * 0.18f)
        val safeAddress = if (address.length > 55) address.take(52) + "..." else address
        canvas.drawText("Lokasi: $safeAddress", paddingLeft, currentY, subPaint)

        // Save to file
        val filename = "absensi_${type.lowercase()}_${System.currentTimeMillis()}.jpg"
        val outputDir = File(context.filesDir, "attendance_photos").apply { mkdirs() }
        val outputFile = File(outputDir, filename)

        FileOutputStream(outputFile).use { out ->
            workingBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }

        return outputFile.absolutePath
    }

    /**
     * Create placeholder avatar/face bitmap if camera is denied or simulated
     */
    fun createSimulatedFaceBitmap(name: String, actionType: String): Bitmap {
        val width = 480
        val height = 640
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Gradient or soft background
        val bgPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Draw stylized face silhouette
        val facePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#38BDF8")
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawOval(
            width * 0.25f,
            height * 0.2f,
            width * 0.75f,
            height * 0.65f,
            facePaint
        )

        // Eye guides & crosshair
        canvas.drawLine(width * 0.35f, height * 0.38f, width * 0.45f, height * 0.38f, facePaint)
        canvas.drawLine(width * 0.55f, height * 0.38f, width * 0.65f, height * 0.38f, facePaint)
        canvas.drawCircle(width * 0.5f, height * 0.42f, 12f, facePaint)

        // Text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText("FACE VERIFIED", width / 2f, height * 0.12f, textPaint)

        return bitmap
    }

    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
}
