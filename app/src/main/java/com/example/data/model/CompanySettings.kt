package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "company_settings")
data class CompanySettings(
    @PrimaryKey
    val id: Int = 1,
    val companyName: String = "PT. RAZMI MINORI SEJAHTERA",
    val officeName: String = "Kantor Pusat Jakarta",
    val officeLatitude: Double = -6.2088,
    val officeLongitude: Double = 106.8456,
    val radiusToleranceMeters: Float = 200f,
    val workStartTime: String = "08:00",
    val workEndTime: String = "17:00",
    val overtimeMinMinutes: Int = 30,
    val overtimeRatePerHour: Long = 30000L,
    val adminPin: String = "1234",
    val allowSimulatedLocation: Boolean = true // Helpful for development/emulator testing
)
