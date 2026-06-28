package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AcademyDatabase
import com.example.data.AcademyRepository
import com.example.data.Attendance
import com.example.data.FeePayment
import com.example.data.Student
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AcademyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AcademyRepository

    init {
        val database = AcademyDatabase.getDatabase(application)
        repository = AcademyRepository(database.academyDao())
    }

    // List of all students
    val students: StateFlow<List<Student>> = repository.allStudents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // List of all fee payments
    val allFeePayments: StateFlow<List<FeePayment>> = repository.allFeePayments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getAttendanceForDate(date: String): Flow<List<Attendance>> {
        return repository.getAttendanceForDate(date)
    }

    // Current selected date for attendance (dd.mm.yyyy)
    private val _selectedDate = MutableStateFlow(getCurrentDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Attendance records for the selected date
    val attendanceForSelectedDate: StateFlow<Map<Int, Attendance>> = _selectedDate
        .flatMapLatest { date ->
            repository.getAttendanceForDate(date)
        }
        .map { list ->
            list.associateBy { it.studentId }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // Active student selected for Profile details
    private val _selectedStudent = MutableStateFlow<Student?>(null)
    val selectedStudent: StateFlow<Student?> = _selectedStudent.asStateFlow()

    // Selected student's attendance history
    val selectedStudentAttendance: StateFlow<List<Attendance>> = _selectedStudent
        .flatMapLatest { student ->
            if (student == null) flowOf(emptyList())
            else repository.getAttendanceForStudent(student.id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Selected student's fee payments
    val selectedStudentPayments: StateFlow<List<FeePayment>> = _selectedStudent
        .flatMapLatest { student ->
            if (student == null) flowOf(emptyList())
            else repository.getFeePaymentsForStudent(student.id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Student to be edited in the form (null means "Add" mode)
    private val _editingStudent = MutableStateFlow<Student?>(null)
    val editingStudent: StateFlow<Student?> = _editingStudent.asStateFlow()

    // Helpers
    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun selectStudentForProfile(student: Student?) {
        _selectedStudent.value = student
    }

    fun setEditingStudent(student: Student?) {
        _editingStudent.value = student
    }

    // Database Actions - Students
    fun saveStudent(
        id: Int = 0,
        name: String,
        fatherName: String,
        grade: String,
        admissionDate: String,
        parentContact1: String,
        parentContact2: String,
        fixedFee: Double
    ) {
        viewModelScope.launch {
            val student = Student(
                id = id,
                name = name.trim(),
                fatherName = fatherName.trim(),
                grade = grade,
                admissionDate = admissionDate.trim(),
                parentContact1 = parentContact1.trim(),
                parentContact2 = parentContact2.trim(),
                fixedFee = fixedFee
            )
            if (id == 0) {
                repository.insertStudent(student)
            } else {
                repository.updateStudent(student)
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            if (_selectedStudent.value?.id == student.id) {
                _selectedStudent.value = null
            }
            if (_editingStudent.value?.id == student.id) {
                _editingStudent.value = null
            }
        }
    }

    // Database Actions - Attendance
    fun setAttendanceStatus(studentId: Int, status: String) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val currentMap = attendanceForSelectedDate.value
            val existing = currentMap[studentId]

            if (existing != null) {
                if (existing.status == status) {
                    // Tapping same status removes/unmarks it
                    repository.deleteAttendanceForDateAndStudent(date, studentId)
                } else {
                    // Update status
                    repository.saveAttendance(existing.copy(status = status))
                }
            } else {
                // Insert new record
                repository.saveAttendance(
                    Attendance(
                        studentId = studentId,
                        date = date,
                        status = status
                    )
                )
            }
        }
    }

    // Database Actions - Fee Payment
    fun addFeePayment(studentId: Int, monthYear: String, amount: Double, date: String, remarks: String) {
        viewModelScope.launch {
            val payment = FeePayment(
                studentId = studentId,
                monthYear = monthYear,
                amountPaid = amount,
                paymentDate = date,
                remarks = remarks
            )
            repository.insertFeePayment(payment)
        }
    }

    fun deleteFeePayment(payment: FeePayment) {
        viewModelScope.launch {
            repository.deleteFeePayment(payment)
        }
    }

    // Generate monthly list starting from June 2026 to current system month or custom range
    fun getAcademicMonths(admissionDateStr: String): List<String> {
        val months = mutableListOf<String>()
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val admissionCal = Calendar.getInstance().apply {
            time = try {
                sdf.parse(admissionDateStr) ?: Date()
            } catch (e: Exception) {
                Date()
            }
        }

        // Start Calendar: June 1, 2026
        val startCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.JUNE)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        // Whichever is later: June 2026 or Admission Date
        val compareCal = if (admissionCal.after(startCal)) admissionCal else startCal

        // End Calendar: current date
        val endCal = Calendar.getInstance()
        // If today is before June 2026 in testing, let's still show at least June 2026
        if (endCal.before(startCal)) {
            endCal.time = startCal.time
        }

        val loopCal = Calendar.getInstance().apply {
            time = compareCal.time
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

        while (!loopCal.after(endCal)) {
            months.add(monthFormat.format(loopCal.time))
            loopCal.add(Calendar.MONTH, 1)
        }

        // If list is empty (e.g. edge cases), guarantee June 2026
        if (months.isEmpty()) {
            months.add("June 2026")
        }

        return months
    }

    // Logic to calculate monthly fee breakdown with carryover for a student
    fun calculateFeeBreakdown(student: Student, payments: List<FeePayment>): List<MonthlyFeeStatus> {
        val academicMonths = getAcademicMonths(student.admissionDate)
        val breakdown = mutableListOf<MonthlyFeeStatus>()
        var carryover = 0.0

        for (month in academicMonths) {
            val monthlyPayments = payments.filter { it.monthYear.equals(month, ignoreCase = true) }
            val paidForThisMonth = monthlyPayments.sumOf { it.amountPaid }
            val fixed = student.fixedFee
            val totalDue = fixed + carryover
            val remaining = totalDue - paidForThisMonth

            breakdown.add(
                MonthlyFeeStatus(
                    monthYear = month,
                    fixedFee = fixed,
                    carryoverFromPrevious = carryover,
                    totalDue = totalDue,
                    amountPaid = paidForThisMonth,
                    remaining = remaining,
                    paymentRecords = monthlyPayments
                )
            )

            // The remaining amount carries over to the next month
            carryover = remaining
        }

        return breakdown
    }
}

// Data class representing calculated fee status for a month
data class MonthlyFeeStatus(
    val monthYear: String,
    val fixedFee: Double,
    val carryoverFromPrevious: Double,
    val totalDue: Double,
    val amountPaid: Double,
    val remaining: Double,
    val paymentRecords: List<FeePayment>
)
