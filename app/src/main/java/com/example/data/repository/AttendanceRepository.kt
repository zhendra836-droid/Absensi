package com.example.data.repository

import com.example.data.dao.AttendanceDao
import com.example.data.dao.CompanySettingsDao
import com.example.data.dao.EmployeeDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.CompanySettings
import com.example.data.model.Employee
import kotlinx.coroutines.flow.Flow

class AttendanceRepository(
    private val employeeDao: EmployeeDao,
    private val attendanceDao: AttendanceDao,
    private val settingsDao: CompanySettingsDao
) {
    // Employees
    val allEmployees: Flow<List<Employee>> = employeeDao.getAllEmployees()
    suspend fun getEmployeeById(id: Long): Employee? = employeeDao.getEmployeeById(id)
    suspend fun getEmployeeByNip(nip: String): Employee? = employeeDao.getEmployeeByNip(nip)
    suspend fun insertEmployee(employee: Employee): Long = employeeDao.insertEmployee(employee)
    suspend fun updateEmployee(employee: Employee) = employeeDao.updateEmployee(employee)
    suspend fun deleteEmployee(employee: Employee) = employeeDao.deleteEmployee(employee)

    // Attendance
    val allRecords: Flow<List<AttendanceRecord>> = attendanceDao.getAllRecords()
    fun getRecordsByDate(date: String): Flow<List<AttendanceRecord>> = attendanceDao.getRecordsByDate(date)
    fun getRecordsByEmployee(employeeId: Long): Flow<List<AttendanceRecord>> = attendanceDao.getRecordsByEmployee(employeeId)
    fun getTodayRecordFlow(employeeId: Long, date: String): Flow<AttendanceRecord?> = attendanceDao.getTodayRecordFlow(employeeId, date)
    suspend fun getTodayRecord(employeeId: Long, date: String): AttendanceRecord? = attendanceDao.getTodayRecord(employeeId, date)
    fun getRecordsByMonth(monthPrefix: String): Flow<List<AttendanceRecord>> = attendanceDao.getRecordsByMonth(monthPrefix)
    suspend fun getRecordsByMonthSync(monthPrefix: String): List<AttendanceRecord> = attendanceDao.getRecordsByMonthSync(monthPrefix)
    suspend fun insertRecord(record: AttendanceRecord): Long = attendanceDao.insertRecord(record)
    suspend fun updateRecord(record: AttendanceRecord) = attendanceDao.updateRecord(record)
    suspend fun deleteRecord(record: AttendanceRecord) = attendanceDao.deleteRecord(record)

    // Company Settings
    val settingsFlow: Flow<CompanySettings?> = settingsDao.getSettings()
    suspend fun getSettingsSync(): CompanySettings? = settingsDao.getSettingsSync()
    suspend fun updateSettings(settings: CompanySettings) = settingsDao.insertOrUpdate(settings)
}
