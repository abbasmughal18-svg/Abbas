package com.example.data

import kotlinx.coroutines.flow.Flow

class AcademyRepository(private val academyDao: AcademyDao) {

    val allStudents: Flow<List<Student>> = academyDao.getAllStudents()
    val allFeePayments: Flow<List<FeePayment>> = academyDao.getAllFeePayments()

    fun getStudentById(id: Int): Flow<Student?> = academyDao.getStudentById(id)

    suspend fun insertStudent(student: Student): Long {
        return academyDao.insertStudent(student)
    }

    suspend fun updateStudent(student: Student) {
        academyDao.updateStudent(student)
    }

    suspend fun deleteStudent(student: Student) {
        academyDao.deleteStudent(student)
    }

    fun getAttendanceForDate(date: String): Flow<List<Attendance>> {
        return academyDao.getAttendanceForDate(date)
    }

    fun getAttendanceForStudent(studentId: Int): Flow<List<Attendance>> {
        return academyDao.getAttendanceForStudent(studentId)
    }

    suspend fun saveAttendance(attendance: Attendance) {
        academyDao.insertAttendance(attendance)
    }

    suspend fun saveAttendances(attendances: List<Attendance>) {
        academyDao.insertAttendances(attendances)
    }

    suspend fun deleteAttendanceForDateAndStudent(date: String, studentId: Int) {
        academyDao.deleteAttendanceForDateAndStudent(date, studentId)
    }

    fun getFeePaymentsForStudent(studentId: Int): Flow<List<FeePayment>> {
        return academyDao.getFeePaymentsForStudent(studentId)
    }

    fun getFeePaymentsForMonth(monthYear: String): Flow<List<FeePayment>> {
        return academyDao.getFeePaymentsForMonth(monthYear)
    }

    suspend fun insertFeePayment(feePayment: FeePayment) {
        academyDao.insertFeePayment(feePayment)
    }

    suspend fun deleteFeePayment(feePayment: FeePayment) {
        academyDao.deleteFeePayment(feePayment)
    }
}
