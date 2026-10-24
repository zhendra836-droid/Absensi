package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.CompanySettings
import com.example.data.model.Employee
import com.example.data.repository.AttendanceRepository
import com.example.util.DateUtils
import com.example.util.ExcelExporter
import com.example.util.ImageUtils
import com.example.util.LocationHelper
import com.example.util.LocationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar

enum class AppRole {
    EMPLOYEE,
    ADMIN
}

enum class AdminTab {
    MONITORING,
    REKAP_EXCEL,
    KARYAWAN,
    PENGATURAN
}

data class UiNotification(
    val message: String,
    val isError: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AttendanceRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AttendanceRepository(
            database.employeeDao(),
            database.attendanceDao(),
            database.companySettingsDao()
        )
    }

    // Role state
    private val _currentRole = MutableStateFlow(AppRole.EMPLOYEE)
    val currentRole: StateFlow<AppRole> = _currentRole.asStateFlow()

    private val _adminAuthenticated = MutableStateFlow(false)
    val adminAuthenticated: StateFlow<Boolean> = _adminAuthenticated.asStateFlow()

    private val _adminTab = MutableStateFlow(AdminTab.MONITORING)
    val adminTab: StateFlow<AdminTab> = _adminTab.asStateFlow()

    // Company Settings
    val settings: StateFlow<CompanySettings> = repository.settingsFlow
        .combine(MutableStateFlow(Unit)) { s, _ ->
            s ?: CompanySettings()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CompanySettings()
        )

    // Employees list
    val employees: StateFlow<List<Employee>> = repository.allEmployees
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Active selected employee for attendance
    private val _selectedEmployee = MutableStateFlow<Employee?>(null)
    val selectedEmployee: StateFlow<Employee?> = _selectedEmployee.asStateFlow()

    // Today's attendance for the selected employee
    private val _todayAttendance = MutableStateFlow<AttendanceRecord?>(null)
    val todayAttendance: StateFlow<AttendanceRecord?> = _todayAttendance.asStateFlow()

    // History for selected employee
    private val _employeeHistory = MutableStateFlow<List<AttendanceRecord>>(emptyList())
    val employeeHistory: StateFlow<List<AttendanceRecord>> = _employeeHistory.asStateFlow()

    // All attendance records (for admin)
    val allRecords: StateFlow<List<AttendanceRecord>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Location state
    private val _currentLocation = MutableStateFlow<LocationResult?>(null)
    val currentLocation: StateFlow<LocationResult?> = _currentLocation.asStateFlow()

    private val _isRefreshingLocation = MutableStateFlow(false)
    val isRefreshingLocation: StateFlow<Boolean> = _isRefreshingLocation.asStateFlow()

    // Face Scan & Attendance Process state
    private val _showFaceScanner = MutableStateFlow(false)
    val showFaceScanner: StateFlow<Boolean> = _showFaceScanner.asStateFlow()

    private val _scanActionType = MutableStateFlow("MASUK") // "MASUK" or "PULANG"
    val scanActionType: StateFlow<String> = _scanActionType.asStateFlow()

    private val _isProcessingAttendance = MutableStateFlow(false)
    val isProcessingAttendance: StateFlow<Boolean> = _isProcessingAttendance.asStateFlow()

    // Notification / Toast
    private val _notification = MutableStateFlow<UiNotification?>(null)
    val notification: StateFlow<UiNotification?> = _notification.asStateFlow()

    // Selected record for photo & detail inspection dialog
    private val _detailRecord = MutableStateFlow<AttendanceRecord?>(null)
    val detailRecord: StateFlow<AttendanceRecord?> = _detailRecord.asStateFlow()

    // Month filter for Excel report
    private val _selectedMonth = MutableStateFlow(DateUtils.getCurrentMonthKey())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    init {
        // Auto select first employee when list loads
        viewModelScope.launch {
            employees.collect { list ->
                if (_selectedEmployee.value == null && list.isNotEmpty()) {
                    selectEmployee(list.first())
                }
            }
        }
        // Refresh initial location
        refreshLocation()
    }

    fun setRole(role: AppRole) {
        _currentRole.value = role
    }

    fun authenticateAdmin(pin: String): Boolean {
        val currentPin = settings.value.adminPin
        if (pin == currentPin) {
            _adminAuthenticated.value = true
            return true
        }
        _notification.value = UiNotification("PIN Admin salah! (Default: 1234)", isError = true)
        return false
    }

    fun logoutAdmin() {
        _adminAuthenticated.value = false
        _currentRole.value = AppRole.EMPLOYEE
    }

    fun setAdminTab(tab: AdminTab) {
        _adminTab.value = tab
    }

    fun selectEmployee(employee: Employee) {
        _selectedEmployee.value = employee
        loadEmployeeTodayRecord(employee.id)
        loadEmployeeHistory(employee.id)
    }

    private fun loadEmployeeTodayRecord(employeeId: Long) {
        viewModelScope.launch {
            val todayStr = DateUtils.getTodayDateString()
            val record = repository.getTodayRecord(employeeId, todayStr)
            _todayAttendance.value = record
        }
    }

    private fun loadEmployeeHistory(employeeId: Long) {
        viewModelScope.launch {
            repository.getRecordsByEmployee(employeeId).collect { list ->
                _employeeHistory.value = list
            }
        }
    }

    fun refreshLocation() {
        viewModelScope.launch {
            _isRefreshingLocation.value = true
            val currentSettings = settings.value
            val res = LocationHelper.getCurrentLocation(
                context = getApplication(),
                officeLat = currentSettings.officeLatitude,
                officeLng = currentSettings.officeLongitude,
                radiusTolerance = currentSettings.radiusToleranceMeters,
                simulateLocation = currentSettings.allowSimulatedLocation
            )
            _currentLocation.value = res
            _isRefreshingLocation.value = false
        }
    }

    fun openScanner(actionType: String) {
        val employee = _selectedEmployee.value
        if (employee == null) {
            _notification.value = UiNotification("Silakan pilih profil karyawan terlebih dahulu", isError = true)
            return
        }

        val today = _todayAttendance.value
        if (actionType == "MASUK" && today != null) {
            _notification.value = UiNotification("Anda sudah melakukan absen masuk hari ini!", isError = true)
            return
        }

        if (actionType == "PULANG") {
            if (today == null) {
                _notification.value = UiNotification("Anda belum melakukan absen masuk!", isError = true)
                return
            }
            if (today.checkOutTimestamp != null) {
                _notification.value = UiNotification("Anda sudah melakukan absen pulang hari ini!", isError = true)
                return
            }
        }

        _scanActionType.value = actionType
        _showFaceScanner.value = true
    }

    fun closeScanner() {
        _showFaceScanner.value = false
    }

    fun processAttendance(bitmap: Bitmap, notes: String = "") {
        val employee = _selectedEmployee.value ?: return
        val currentSettings = settings.value
        val loc = _currentLocation.value ?: run {
            _notification.value = UiNotification("Lokasi belum terdeteksi. Silakan coba lagi.", isError = true)
            return
        }

        _isProcessingAttendance.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()
                val todayStr = DateUtils.getTodayDateString()

                // Save watermarked photo
                val photoPath = ImageUtils.saveWatermarkedPhoto(
                    context = getApplication(),
                    sourceBitmap = bitmap,
                    employeeName = employee.name,
                    timestamp = now,
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    address = loc.address,
                    type = _scanActionType.value
                )

                if (_scanActionType.value == "MASUK") {
                    val lateMins = DateUtils.calculateLateMinutes(now, currentSettings.workStartTime)
                    val status = when {
                        !loc.isWithinRadius -> "LUAR_RADIUS"
                        lateMins > 0 -> "TERLAMBAT"
                        else -> "HADIR"
                    }

                    val record = AttendanceRecord(
                        employeeId = employee.id,
                        employeeNip = employee.nip,
                        employeeName = employee.name,
                        department = employee.department,
                        dateString = todayStr,
                        checkInTimestamp = now,
                        checkInPhotoPath = photoPath,
                        checkInLatitude = loc.latitude,
                        checkInLongitude = loc.longitude,
                        checkInAddress = loc.address,
                        checkInDistanceMeters = loc.distanceToOfficeMeters,
                        checkInStatus = status,
                        notes = notes
                    )

                    val newId = repository.insertRecord(record)
                    val inserted = record.copy(id = newId)
                    _todayAttendance.value = inserted

                    _notification.value = UiNotification(
                        if (status == "TERLAMBAT") "Absen Masuk Berhasil (Terlambat $lateMins Menit)"
                        else if (status == "LUAR_RADIUS") "Absen Masuk Tercatat (Di Luar Radius Kantor: ${loc.distanceToOfficeMeters.toInt()}m)"
                        else "Absen Masuk Berhasil Tepat Waktu!"
                    )
                } else {
                    // PULANG (Check Out)
                    val existing = _todayAttendance.value ?: repository.getTodayRecord(employee.id, todayStr)
                    if (existing != null) {
                        val durationMins = ((now - existing.checkInTimestamp) / (60 * 1000)).toInt()
                        val overtimeMins = DateUtils.calculateOvertimeMinutes(
                            checkOutTimestamp = now,
                            workEndTimeStr = currentSettings.workEndTime,
                            minOvertimeThresholdMinutes = currentSettings.overtimeMinMinutes
                        )

                        val updated = existing.copy(
                            checkOutTimestamp = now,
                            checkOutPhotoPath = photoPath,
                            checkOutLatitude = loc.latitude,
                            checkOutLongitude = loc.longitude,
                            checkOutAddress = loc.address,
                            checkOutDistanceMeters = loc.distanceToOfficeMeters,
                            workDurationMinutes = durationMins,
                            overtimeMinutes = overtimeMins,
                            notes = if (notes.isNotBlank()) "${existing.notes} | $notes".trim() else existing.notes
                        )

                        repository.updateRecord(updated)
                        _todayAttendance.value = updated

                        val overtimeMsg = if (overtimeMins > 0) " (Lembur: ${DateUtils.formatDuration(overtimeMins)})" else ""
                        _notification.value = UiNotification("Absen Pulang Berhasil! Durasi Kerja: ${DateUtils.formatDuration(durationMins)}$overtimeMsg")
                    }
                }

                _showFaceScanner.value = false
                loadEmployeeHistory(employee.id)
            } catch (e: Exception) {
                _notification.value = UiNotification("Gagal memproses absensi: ${e.message}", isError = true)
            } finally {
                _isProcessingAttendance.value = false
            }
        }
    }

    fun setSelectedMonth(monthKey: String) {
        _selectedMonth.value = monthKey
    }

    fun exportExcelReport(isCsv: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentSettings = settings.value
                val monthPrefix = _selectedMonth.value
                val records = repository.getRecordsByMonthSync(monthPrefix)

                if (records.isEmpty()) {
                    _notification.value = UiNotification("Tidak ada data kehadiran untuk bulan $monthPrefix", isError = true)
                    return@launch
                }

                val periodCal = Calendar.getInstance()
                val periodName = "Bulan $monthPrefix"

                val file: File = if (isCsv) {
                    ExcelExporter.exportToCsv(
                        context = getApplication(),
                        records = records,
                        settings = currentSettings,
                        periodName = periodName
                    )
                } else {
                    ExcelExporter.exportToExcel(
                        context = getApplication(),
                        records = records,
                        settings = currentSettings,
                        periodName = periodName
                    )
                }

                val mime = if (isCsv) "text/csv" else "application/vnd.ms-excel"
                val title = "Rekap Kehadiran $periodName"

                withContext(Dispatchers.Main) {
                    ExcelExporter.shareFile(getApplication(), file, mime, title)
                    _notification.value = UiNotification("Laporan Excel berhasil dibuat (${file.name})")
                }
            } catch (e: Exception) {
                _notification.value = UiNotification("Gagal ekspor Excel: ${e.message}", isError = true)
            }
        }
    }

    fun setOfficeToCurrentLocation() {
        val loc = _currentLocation.value ?: return
        viewModelScope.launch {
            val updated = settings.value.copy(
                officeLatitude = loc.latitude,
                officeLongitude = loc.longitude,
                officeName = "Lokasi Saat Ini (${loc.address.take(25)})"
            )
            repository.updateSettings(updated)
            refreshLocation()
            _notification.value = UiNotification("Lokasi kantor berhasil diperbarui ke posisi GPS saat ini!")
        }
    }

    fun updateCompanySettings(updated: CompanySettings) {
        viewModelScope.launch {
            repository.updateSettings(updated)
            refreshLocation()
            _notification.value = UiNotification("Pengaturan kantor berhasil disimpan!")
        }
    }

    fun addEmployee(nip: String, name: String, department: String, position: String, phone: String) {
        viewModelScope.launch {
            val colors = listOf(0xFF2563EB, 0xFF0D9488, 0xFF7C3AED, 0xFFD97706, 0xFFE11D48)
            val randomColor = colors.random()
            val emp = Employee(
                nip = nip.trim(),
                name = name.trim(),
                department = department.trim(),
                position = position.trim(),
                phone = phone.trim(),
                avatarColorHex = randomColor,
                faceRegistered = true
            )
            repository.insertEmployee(emp)
            _notification.value = UiNotification("Karyawan $name berhasil ditambahkan!")
        }
    }

    fun deleteEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.deleteEmployee(employee)
            if (_selectedEmployee.value?.id == employee.id) {
                _selectedEmployee.value = employees.value.firstOrNull { it.id != employee.id }
            }
            _notification.value = UiNotification("Karyawan ${employee.name} berhasil dihapus.")
        }
    }

    fun showRecordDetail(record: AttendanceRecord) {
        _detailRecord.value = record
    }

    fun clearRecordDetail() {
        _detailRecord.value = null
    }

    fun clearNotification() {
        _notification.value = null
    }
}
