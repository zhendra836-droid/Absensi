package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: Long,
    val employeeNip: String,
    val employeeName: String,
    val department: String,
    val dateString: String, // Format: YYYY-MM-DD
    val checkInTimestamp: Long,
    val checkInPhotoPath: String,
    val checkInLatitude: Double,
    val checkInLongitude: Double,
    val checkInAddress: String,
    val checkInDistanceMeters: Float,
    val checkInStatus: String, // "HADIR", "TERLAMBAT", "LUAR_RADIUS"
    val checkOutTimestamp: Long? = null,
    val checkOutPhotoPath: String? = null,
    val checkOutLatitude: Double? = null,
    val checkOutLongitude: Double? = null,
    val checkOutAddress: String? = null,
    val checkOutDistanceMeters: Float? = null,
    val workDurationMinutes: Int = 0,
    val overtimeMinutes: Int = 0,
    val notes: String = ""
)
