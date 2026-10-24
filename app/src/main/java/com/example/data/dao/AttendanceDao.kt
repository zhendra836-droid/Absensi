package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records ORDER BY checkInTimestamp DESC")
    fun getAllRecords(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE dateString = :date ORDER BY checkInTimestamp DESC")
    fun getRecordsByDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId ORDER BY checkInTimestamp DESC")
    fun getRecordsByEmployee(employeeId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId AND dateString = :date LIMIT 1")
    fun getTodayRecordFlow(employeeId: Long, date: String): Flow<AttendanceRecord?>

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId AND dateString = :date LIMIT 1")
    suspend fun getTodayRecord(employeeId: Long, date: String): AttendanceRecord?

    @Query("SELECT * FROM attendance_records WHERE dateString LIKE :monthPrefix || '%' ORDER BY checkInTimestamp DESC")
    fun getRecordsByMonth(monthPrefix: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE dateString LIKE :monthPrefix || '%' ORDER BY checkInTimestamp ASC")
    suspend fun getRecordsByMonthSync(monthPrefix: String): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): AttendanceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<AttendanceRecord>)

    @Update
    suspend fun updateRecord(record: AttendanceRecord)

    @Delete
    suspend fun deleteRecord(record: AttendanceRecord)

    @Query("DELETE FROM attendance_records")
    suspend fun clearAll()
}
