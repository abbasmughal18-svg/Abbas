package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Attendance
import com.example.data.Student
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayAttendanceScreen(
    viewModel: AcademyViewModel,
    modifier: Modifier = Modifier,
    onStudentClick: (Student) -> Unit
) {
    val students by viewModel.students.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val attendanceMap by viewModel.attendanceForSelectedDate.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // Filter students
    val filteredStudents = remember(students, searchQuery) {
        students.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.fatherName.contains(searchQuery, ignoreCase = true)
        }
    }

    // Attendance counts
    val presentCount = remember(filteredStudents, attendanceMap) {
        filteredStudents.count { attendanceMap[it.id]?.status == "PRESENT" }
    }
    val absentCount = remember(filteredStudents, attendanceMap) {
        filteredStudents.count { attendanceMap[it.id]?.status == "ABSENT" }
    }
    val lateCount = remember(filteredStudents, attendanceMap) {
        filteredStudents.count { attendanceMap[it.id]?.status == "LATE" }
    }
    val unmarkedCount = remember(filteredStudents, attendanceMap) {
        filteredStudents.size - presentCount - absentCount - lateCount
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Date selector and Calendar button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate100),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Attendance Date",
                    style = MaterialTheme.typography.labelLarge,
                    color = Slate500
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.clickable { showDatePickerDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Date",
                        tint = RoyalBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedDate,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick shift buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { shiftDate(viewModel, selectedDate, -1) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate100, contentColor = Slate800)
                    ) {
                        Text("Prev Day")
                    }
                    Button(
                        onClick = { viewModel.setSelectedDate(getTodayDateString()) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue, contentColor = Color.White)
                    ) {
                        Text("Today")
                    }
                    Button(
                        onClick = { shiftDate(viewModel, selectedDate, 1) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate100, contentColor = Slate800)
                    ) {
                        Text("Next Day")
                    }
                }
            }
        }

        // Stats Summary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatItem(
                label = "Present",
                count = presentCount,
                color = PresentGreen,
                containerColor = PresentGreenContainer,
                modifier = Modifier.weight(1f)
            )
            StatItem(
                label = "Absent",
                count = absentCount,
                color = AbsentRed,
                containerColor = AbsentRedContainer,
                modifier = Modifier.weight(1f)
            )
            StatItem(
                label = "Late",
                count = lateCount,
                color = LateAmber,
                containerColor = LateAmberContainer,
                modifier = Modifier.weight(1f)
            )
            StatItem(
                label = "Pending",
                count = unmarkedCount,
                color = Color.Gray,
                containerColor = Color(0xFFF0F0F0),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Student
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search student or father name...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("attendance_search_input"),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredStudents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (students.isEmpty()) "No students added yet.\nGo to Add / Edit tab to start!" else "No students match your search.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredStudents, key = { it.id }) { student ->
                    val attendance = attendanceMap[student.id]
                    AttendanceStudentCard(
                        student = student,
                        attendance = attendance,
                        onStatusChange = { status ->
                            viewModel.setAttendanceStatus(student.id, status)
                        },
                        onNameClick = { onStudentClick(student) }
                    )
                }
            }
        }
    }

    // Manual date dialog
    if (showDatePickerDialog) {
        ManualDatePickerDialog(
            currentDate = selectedDate,
            onDismiss = { showDatePickerDialog = false },
            onDateSelected = { date ->
                viewModel.setSelectedDate(date)
                showDatePickerDialog = false
            }
        )
    }
}

@Composable
fun AttendanceStudentCard(
    student: Student,
    attendance: Attendance?,
    onStatusChange: (String) -> Unit,
    onNameClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_attendance_card_${student.id}"),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate100),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Student Info
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .clickable { onNameClick() }
            ) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RoyalBlue
                )
                Text(
                    text = "S/O: ${student.fatherName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate500
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(student.grade, fontSize = 11.sp) },
                        modifier = Modifier.height(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fee: Rs. ${student.fixedFee.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate500
                    )
                }
            }

            // Status Buttons Row
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Present (Tick)
                val isPresent = attendance?.status == "PRESENT"
                IconButton(
                    onClick = { onStatusChange("PRESENT") },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isPresent) PresentGreen else Slate100)
                        .testTag("btn_present_${student.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Present",
                        tint = if (isPresent) Color.White else Slate500,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Absent (Cross)
                val isAbsent = attendance?.status == "ABSENT"
                IconButton(
                    onClick = { onStatusChange("ABSENT") },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isAbsent) AbsentRed else Slate100)
                        .testTag("btn_absent_${student.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Absent",
                        tint = if (isAbsent) Color.White else Slate500,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Late (Circle)
                val isLate = attendance?.status == "LATE"
                IconButton(
                    onClick = { onStatusChange("LATE") },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isLate) LateAmber else Slate100)
                        .testTag("btn_late_${student.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Circle,
                        contentDescription = "Late",
                        tint = if (isLate) Color.White else Slate500,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    count: Int,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualDatePickerDialog(
    currentDate: String,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    var dateInput by remember { mutableStateOf(currentDate) }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Date") },
        text = {
            Column {
                Text(
                    text = "Enter date in format DD.MM.YYYY (e.g. 28.06.2026):",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = dateInput,
                    onValueChange = {
                        dateInput = it
                        hasError = false
                    },
                    isError = hasError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Date (dd.mm.yyyy)") },
                    supportingText = {
                        if (hasError) {
                            Text("Please enter a valid date format (dd.mm.yyyy)", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isValidDateFormat(dateInput)) {
                        onDateSelected(dateInput)
                    } else {
                        hasError = true
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Helpers
private fun getTodayDateString(): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return sdf.format(Date())
}

private fun shiftDate(viewModel: AcademyViewModel, currentDateStr: String, daysToShift: Int) {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    try {
        val date = sdf.parse(currentDateStr) ?: Date()
        val cal = Calendar.getInstance().apply {
            time = date
            add(Calendar.DAY_OF_YEAR, daysToShift)
        }
        viewModel.setSelectedDate(sdf.format(cal.time))
    } catch (e: Exception) {
        // Fallback to today
        viewModel.setSelectedDate(getTodayDateString())
    }
}

fun isValidDateFormat(dateStr: String): Boolean {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).apply {
        isLenient = false
    }
    return try {
        sdf.parse(dateStr)
        dateStr.split(".").size == 3
    } catch (e: Exception) {
        false
    }
}
