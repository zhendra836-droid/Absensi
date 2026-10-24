package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AttendanceDao
import com.example.data.dao.CompanySettingsDao
import com.example.data.dao.EmployeeDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.CompanySettings
import com.example.data.model.Employee
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Database(
    entities = [Employee::class, AttendanceRecord::class, CompanySettings::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun companySettingsDao(): CompanySettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "presensiku_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val settingsDao = database.companySettingsDao()
                        val current = settingsDao.getSettingsSync()
                        if (current != null && current.companyName == "PT Sinergi Digital Mandiri") {
                            settingsDao.update(current.copy(companyName = "PT. RAZMI MINORI SEJAHTERA"))
                        }
                    }
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
                val settingsDao = database.companySettingsDao()
                val employeeDao = database.employeeDao()
                val attendanceDao = database.attendanceDao()

                // Default settings: Jakarta Pusat office
                val defaultSettings = CompanySettings(
                    id = 1,
                    companyName = "PT. RAZMI MINORI SEJAHTERA",
                    officeName = "Kantor Pusat Jakarta",
                    officeLatitude = -6.175392,
                    officeLongitude = 106.827153,
                    radiusToleranceMeters = 250f,
                    workStartTime = "08:00",
                    workEndTime = "17:00",
                    overtimeMinMinutes = 30,
                    overtimeRatePerHour = 35000L,
                    adminPin = "1234",
                    allowSimulatedLocation = true
                )
                settingsDao.insertOrUpdate(defaultSettings)

                // Initial sample employees
                val emp1 = Employee(
                    id = 1,
                    nip = "EMP-001",
                    name = "Ahmad Rizky Pratama",
                    department = "Teknologi Informasi",
                    position = "Lead Mobile Engineer",
                    pin = "1234",
                    phone = "0812-3456-7890",
                    avatarColorHex = 0xFF2563EB,
                    faceRegistered = true
                )
                val emp2 = Employee(
                    id = 2,
                    nip = "EMP-002",
                    name = "Siti Nurhaliza",
                    department = "Keuangan & Akuntansi",
                    position = "Finance Specialist",
                    pin = "1234",
                    phone = "0813-8877-6655",
                    avatarColorHex = 0xFF0D9488,
                    faceRegistered = true
                )
                val emp3 = Employee(
                    id = 3,
                    nip = "EMP-003",
                    name = "Bambang Wijaya",
                    department = "Operasional & Lapangan",
                    position = "Operations Supervisor",
                    pin = "1234",
                    phone = "0819-2233-4455",
                    avatarColorHex = 0xFFD97706,
                    faceRegistered = true
                )
                val emp4 = Employee(
                    id = 4,
                    nip = "EMP-004",
                    name = "Dewi Lestari",
                    department = "Human Resources",
                    position = "HR Manager",
                    pin = "1234",
                    phone = "0857-1122-3344",
                    avatarColorHex = 0xFF7C3AED,
                    faceRegistered = true
                )
                employeeDao.insertAll(listOf(emp1, emp2, emp3, emp4))

                // Populate realistic past attendance records for current month so Excel report is rich & demonstrable immediately!
                val cal = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val records = mutableListOf<AttendanceRecord>()

                // Populate past 7 working days
                for (i in 1..7) {
                    val pastCal = Calendar.getInstance()
                    pastCal.add(Calendar.DAY_OF_YEAR, -i)
                    val dayOfWeek = pastCal.get(Calendar.DAY_OF_WEEK)
                    if (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY) continue

                    val dateStr = dateFormat.format(pastCal.time)

                    // Employee 1 (Rizky): Hadir, overtime 90 mins
                    pastCal.set(Calendar.HOUR_OF_DAY, 7)
                    pastCal.set(Calendar.MINUTE, 52)
                    val inTime1 = pastCal.timeInMillis
                    pastCal.set(Calendar.HOUR_OF_DAY, 18)
                    pastCal.set(Calendar.MINUTE, 30)
                    val outTime1 = pastCal.timeInMillis
                    records.add(
                        AttendanceRecord(
                            employeeId = 1,
                            employeeNip = emp1.nip,
                            employeeName = emp1.name,
                            department = emp1.department,
                            dateString = dateStr,
                            checkInTimestamp = inTime1,
                            checkInPhotoPath = "",
                            checkInLatitude = -6.1754,
                            checkInLongitude = 106.8272,
                            checkInAddress = "Jl. Merdeka Barat No. 12, Jakarta Pusat",
                            checkInDistanceMeters = 35f,
                            checkInStatus = "HADIR",
                            checkOutTimestamp = outTime1,
                            checkOutPhotoPath = "",
                            checkOutLatitude = -6.1754,
                            checkOutLongitude = 106.8272,
                            checkOutAddress = "Jl. Merdeka Barat No. 12, Jakarta Pusat",
                            checkOutDistanceMeters = 35f,
                            workDurationMinutes = 638,
                            overtimeMinutes = 90,
                            notes = "Penyelesaian deployment sistem v2"
                        )
                    )

                    // Employee 2 (Siti): Hadir tepat waktu, pulang 17:15 (no lembur)
                    pastCal.set(Calendar.HOUR_OF_DAY, 8)
                    pastCal.set(Calendar.MINUTE, 2)
                    val inTime2 = pastCal.timeInMillis
                    pastCal.set(Calendar.HOUR_OF_DAY, 17)
                    pastCal.set(Calendar.MINUTE, 15)
                    val outTime2 = pastCal.timeInMillis
                    records.add(
                        AttendanceRecord(
                            employeeId = 2,
                            employeeNip = emp2.nip,
                            employeeName = emp2.name,
                            department = emp2.department,
                            dateString = dateStr,
                            checkInTimestamp = inTime2,
                            checkInPhotoPath = "",
                            checkInLatitude = -6.1752,
                            checkInLongitude = 106.8270,
                            checkInAddress = "Jl. Merdeka Barat No. 12, Jakarta Pusat",
                            checkInDistanceMeters = 40f,
                            checkInStatus = "HADIR",
                            checkOutTimestamp = outTime2,
                            checkOutPhotoPath = "",
                            checkOutLatitude = -6.1752,
                            checkOutLongitude = 106.8270,
                            checkOutAddress = "Jl. Merdeka Barat No. 12, Jakarta Pusat",
                            checkOutDistanceMeters = 40f,
                            workDurationMinutes = 553,
                            overtimeMinutes = 0,
                            notes = "Tutup buku harian operasional"
                        )
                    )

                    // Employee 3 (Bambang): 1 day Terlambat, lembur 120 mins
                    val isLate = (i % 3 == 0)
                    pastCal.set(Calendar.HOUR_OF_DAY, if (isLate) 8 else 7)
                    pastCal.set(Calendar.MINUTE, if (isLate) 24 else 55)
                    val inTime3 = pastCal.timeInMillis
                    pastCal.set(Calendar.HOUR_OF_DAY, 19)
                    pastCal.set(Calendar.MINUTE, 0)
                    val outTime3 = pastCal.timeInMillis
                    records.add(
                        AttendanceRecord(
                            employeeId = 3,
                            employeeNip = emp3.nip,
                            employeeName = emp3.name,
                            department = emp3.department,
                            dateString = dateStr,
                            checkInTimestamp = inTime3,
                            checkInPhotoPath = "",
                            checkInLatitude = -6.1755,
                            checkInLongitude = 106.8271,
                            checkInAddress = "Jl. Merdeka Barat No. 12, Jakarta Pusat",
                            checkInDistanceMeters = 60f,
                            checkInStatus = if (isLate) "TERLAMBAT" else "HADIR",
                            checkOutTimestamp = outTime3,
                            checkOutPhotoPath = "",
                            checkOutLatitude = -6.1755,
                            checkOutLongitude = 106.8271,
                            checkOutAddress = "Jl. Merdeka Barat No. 12, Jakarta Pusat",
                            checkOutDistanceMeters = 60f,
                            workDurationMinutes = if (isLate) 636 else 665,
                            overtimeMinutes = 120,
                            notes = "Stock opname gudang material"
                        )
                    )
                }

                attendanceDao.insertAll(records)
            }
        }
    }
}
