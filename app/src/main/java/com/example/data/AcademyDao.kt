package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademyDao {
    // Student Queries
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    fun getStudentById(id: Int): Flow<Student?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    // Attendance Queries
    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceForDate(date: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId")
    fun getAttendanceForStudent(studentId: Int): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendances(attendances: List<Attendance>)

    @Query("DELETE FROM attendance WHERE date = :date AND studentId = :studentId")
    suspend fun deleteAttendanceForDateAndStudent(date: String, studentId: Int)

    // Fee Queries
    @Query("SELECT * FROM fee_payments ORDER BY paymentDate DESC")
    fun getAllFeePayments(): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments WHERE studentId = :studentId ORDER BY paymentDate DESC")
    fun getFeePaymentsForStudent(studentId: Int): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments WHERE monthYear = :monthYear")
    fun getFeePaymentsForMonth(monthYear: String): Flow<List<FeePayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeePayment(feePayment: FeePayment)

    @Delete
    suspend fun deleteFeePayment(feePayment: FeePayment)
}
