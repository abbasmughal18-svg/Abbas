package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Attendance
import com.example.data.FeePayment
import com.example.data.Student
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailsScreen(
    viewModel: AcademyViewModel,
    modifier: Modifier = Modifier
) {
    val students by viewModel.students.collectAsState()
    val allPayments by viewModel.allFeePayments.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var activeReportTab by remember { mutableStateOf(0) } // 0 = Student-wise, 1 = Datewise (All Students)

    val filteredStudents = remember(students, searchQuery) {
        students.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.fatherName.contains(searchQuery, ignoreCase = true) ||
                    it.grade.contains(searchQuery, ignoreCase = true)
        }
    }

    // Datewise attendance state
    var reportDate by remember { mutableStateOf(getTodayDateString()) }
    var showReportDatePicker by remember { mutableStateOf(false) }
    val dateAttendanceFlow = remember(reportDate) { viewModel.getAttendanceForDate(reportDate) }
    val dateAttendanceList by dateAttendanceFlow.collectAsState(initial = emptyList())

    val selectedStudentForProfile by viewModel.selectedStudent.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab header
        TabRow(
            selectedTabIndex = activeReportTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = activeReportTab == 0,
                onClick = { activeReportTab = 0 },
                text = { Text("Student Reports & Fees", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Person, contentDescription = null) }
            )
            Tab(
                selected = activeReportTab == 1,
                onClick = { activeReportTab = 1 },
                text = { Text("Date-wise Sheet", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.DateRange, contentDescription = null) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeReportTab == 0) {
            // Student-wise report view
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, father, or grade...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("report_search_input"),
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
                        text = if (students.isEmpty()) "No students enrolled yet." else "No matching students found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        val studentPayments = allPayments.filter { it.studentId == student.id }
                        val breakdown = viewModel.calculateFeeBreakdown(student, studentPayments)
                        val totalPending = breakdown.lastOrNull()?.remaining ?: 0.0

                        StudentReportCard(
                            student = student,
                            pendingFee = totalPending,
                            onClick = { viewModel.selectStudentForProfile(student) }
                        )
                    }
                }
            }
        } else {
            // Date-wise sheet view
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
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
                        text = "Selected Report Date",
                        style = MaterialTheme.typography.labelLarge,
                        color = Slate500
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showReportDatePicker = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Select Report Date",
                            tint = RoyalBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = reportDate,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val attendanceMap = remember(dateAttendanceList) { dateAttendanceList.associateBy { it.studentId } }

            if (students.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No students enrolled yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Slate500
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(students, key = { it.id }) { student ->
                        val att = attendanceMap[student.id]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate100),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = student.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Text(
                                        text = student.grade,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate500
                                    )
                                }

                                when (att?.status) {
                                    "PRESENT" -> BadgeRow(text = "PRESENT", color = PresentGreen, icon = Icons.Default.Check)
                                    "ABSENT" -> BadgeRow(text = "ABSENT", color = AbsentRed, icon = Icons.Default.Close)
                                    "LATE" -> BadgeRow(text = "LATE", color = LateAmber, icon = Icons.Default.Circle)
                                    else -> BadgeRow(text = "UNMARKED", color = Color.Gray, icon = Icons.Default.HelpOutline)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Manual date picker for reports
    if (showReportDatePicker) {
        ManualDatePickerDialog(
            currentDate = reportDate,
            onDismiss = { showReportDatePicker = false },
            onDateSelected = { date ->
                reportDate = date
                showReportDatePicker = false
            }
        )
    }

    // Student Profile Dialog (Full detailed report and fee system!)
    if (selectedStudentForProfile != null) {
        val student = selectedStudentForProfile!!
        val attendanceList by viewModel.selectedStudentAttendance.collectAsState()
        val paymentList by viewModel.selectedStudentPayments.collectAsState()

        StudentProfileDialog(
            student = student,
            attendance = attendanceList,
            payments = paymentList,
            viewModel = viewModel,
            onDismiss = { viewModel.selectStudentForProfile(null) }
        )
    }
}

@Composable
fun BadgeRow(text: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StudentReportCard(
    student: Student,
    pendingFee: Double,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("student_report_card_${student.id}"),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate100),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.5f)) {
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
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(student.grade, fontSize = 11.sp) },
                        modifier = Modifier.height(24.dp)
                    )
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Fee: Rs. ${student.fixedFee.toInt()}/mo", fontSize = 11.sp) },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Pending Fee",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (pendingFee <= 0) "Paid ✓" else "Rs. ${pendingFee.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (pendingFee <= 0) PresentGreen else AbsentRed
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "View Profile",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Student Profile & Detailed Report Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileDialog(
    student: Student,
    attendance: List<Attendance>,
    payments: List<FeePayment>,
    viewModel: AcademyViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var profileTabState by remember { mutableStateOf(0) } // 0 = Profile Info, 1 = Attendance History, 2 = Fees & Payments

    // Compute monthly fee breakdowns
    val monthlyFeeBreakdown = remember(student, payments) {
        viewModel.calculateFeeBreakdown(student, payments)
    }

    // Payment Form state
    var selectedPayMonth by remember { mutableStateOf("") }
    var payAmountStr by remember { mutableStateOf("") }
    var payRemarks by remember { mutableStateOf("") }
    var paySuccessMsg by remember { mutableStateOf("") }

    // Dropdown state for payment month selection
    var showMonthDropdown by remember { mutableStateOf(false) }
    val academicMonths = remember(student) { viewModel.getAcademicMonths(student.admissionDate) }

    // Pick first available month by default for payment
    LaunchedEffect(academicMonths) {
        if (academicMonths.isNotEmpty()) {
            selectedPayMonth = academicMonths.last()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Sir Abbas Academy Student Profile",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close Profile")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxSize()) {
                // Secondary Tab Row
                TabRow(
                    selectedTabIndex = profileTabState,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.Transparent
                ) {
                    Tab(
                        selected = profileTabState == 0,
                        onClick = { profileTabState = 0 },
                        text = { Text("Details", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = profileTabState == 1,
                        onClick = { profileTabState = 1 },
                        text = { Text("Attendance", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = profileTabState == 2,
                        onClick = { profileTabState = 2 },
                        text = { Text("Fees & Ledger", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.weight(1f)) {
                    when (profileTabState) {
                        0 -> {
                            // Details Tab
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                item {
                                    ProfileInfoRow(label = "Student Name", value = student.name)
                                    ProfileInfoRow(label = "Father's Name", value = student.fatherName)
                                    ProfileInfoRow(label = "Class / Grade", value = student.grade)
                                    ProfileInfoRow(label = "Date of Admission", value = student.admissionDate)
                                    ProfileInfoRow(label = "Parent Contact #1", value = student.parentContact1)
                                    if (student.parentContact2.isNotEmpty()) {
                                        ProfileInfoRow(label = "Parent Contact #2", value = student.parentContact2)
                                    }
                                    ProfileInfoRow(label = "Monthly Decided Fee", value = "Rs. ${student.fixedFee.toInt()}")
                                }

                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Share Report with Parents",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            shareStudentReport(context, student, attendance, monthlyFeeBreakdown)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Send Report via WhatsApp/SMS")
                                    }
                                }
                            }
                        }
                        1 -> {
                            // Attendance History Tab
                            val presentDays = attendance.count { it.status == "PRESENT" }
                            val absentDays = attendance.count { it.status == "ABSENT" }
                            val lateDays = attendance.count { it.status == "LATE" }
                            val totalDays = presentDays + absentDays + lateDays
                            val attendanceRate = if (totalDays > 0) (presentDays.toDouble() / totalDays * 100).toInt() else 0

                            Column(modifier = Modifier.fillMaxSize()) {
                                // Stats Card
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceAround,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$attendanceRate%",
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text("Attendance Rate", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$presentDays",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = PresentGreen
                                            )
                                            Text("Present", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$absentDays",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = AbsentRed
                                            )
                                            Text("Absent", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$lateDays",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = LateAmber
                                            )
                                            Text("Late", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Attendance Logs (All Time)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                if (attendance.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No attendance recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items(attendance.sortedByDescending { it.date }) { record ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = record.date,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    val statusLabel = when (record.status) {
                                                        "PRESENT" -> "PRESENT ✓"
                                                        "ABSENT" -> "ABSENT ✗"
                                                        "LATE" -> "LATE ○"
                                                        else -> record.status
                                                    }
                                                    val statusColor = when (record.status) {
                                                        "PRESENT" -> PresentGreen
                                                        "ABSENT" -> AbsentRed
                                                        "LATE" -> LateAmber
                                                        else -> Color.Gray
                                                    }
                                                    Text(
                                                        text = statusLabel,
                                                        fontWeight = FontWeight.Bold,
                                                        color = statusColor,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // Fees & Ledger Tab
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Ledger table
                                item {
                                    Text(
                                        text = "Monthly Fee Ledger (Carryover System)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    monthlyFeeBreakdown.forEach { row ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 8.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = row.monthYear,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    val finalPending = row.remaining
                                                    Text(
                                                        text = if (finalPending <= 0) "Paid ✓" else "Due: Rs. ${finalPending.toInt()}",
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (finalPending <= 0) PresentGreen else AbsentRed
                                                    )
                                                }

                                                Divider(modifier = Modifier.padding(vertical = 8.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column {
                                                        Text("Decided: Rs. ${row.fixedFee.toInt()}", style = MaterialTheme.typography.bodySmall)
                                                        Text("Carryover: Rs. ${row.carryoverFromPrevious.toInt()}", style = MaterialTheme.typography.bodySmall)
                                                    }
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text("Total Due: Rs. ${row.totalDue.toInt()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                        Text("Amount Paid: Rs. ${row.amountPaid.toInt()}", style = MaterialTheme.typography.bodySmall, color = PresentGreen, fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                if (row.paymentRecords.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text("Payments:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                    row.paymentRecords.forEach { p ->
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 2.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = "  • Rs. ${p.amountPaid.toInt()} on ${p.paymentDate} (${p.remarks})",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                            IconButton(
                                                                onClick = { viewModel.deleteFeePayment(p) },
                                                                modifier = Modifier.size(24.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Delete,
                                                                    contentDescription = "Delete Payment",
                                                                    tint = AbsentRed,
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Payment Form Section
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Record Fee Payment",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))

                                            // Month Selection Dropdown
                                            ExposedDropdownMenuBox(
                                                expanded = showMonthDropdown,
                                                onExpandedChange = { showMonthDropdown = !showMonthDropdown }
                                            ) {
                                                OutlinedTextField(
                                                    value = selectedPayMonth,
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text("Select Fee Month") },
                                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showMonthDropdown) },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .menuAnchor(),
                                                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                                                )
                                                ExposedDropdownMenu(
                                                    expanded = showMonthDropdown,
                                                    onDismissRequest = { showMonthDropdown = false }
                                                ) {
                                                    academicMonths.forEach { m ->
                                                        DropdownMenuItem(
                                                            text = { Text(m) },
                                                            onClick = {
                                                                selectedPayMonth = m
                                                                showMonthDropdown = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Amount paid
                                            OutlinedTextField(
                                                value = payAmountStr,
                                                onValueChange = { payAmountStr = it },
                                                label = { Text("Amount Paid (Rs.)") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Remarks
                                            OutlinedTextField(
                                                value = payRemarks,
                                                onValueChange = { payRemarks = it },
                                                label = { Text("Remarks (e.g. Cash, Receipt #)") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                                            )

                                            Spacer(modifier = Modifier.height(12.dp))

                                            if (paySuccessMsg.isNotEmpty()) {
                                                Text(
                                                    text = paySuccessMsg,
                                                    color = PresentGreen,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(bottom = 8.dp)
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    val amt = payAmountStr.toDoubleOrNull()
                                                    if (amt != null && selectedPayMonth.isNotEmpty()) {
                                                        val todayStr = getTodayDateString()
                                                        viewModel.addFeePayment(
                                                            studentId = student.id,
                                                            monthYear = selectedPayMonth,
                                                            amount = amt,
                                                            date = todayStr,
                                                            remarks = payRemarks.trim()
                                                        )
                                                        payAmountStr = ""
                                                        payRemarks = ""
                                                        paySuccessMsg = "Payment of Rs. ${amt.toInt()} recorded for $selectedPayMonth!"
                                                    } else {
                                                        paySuccessMsg = "Error: Please enter a valid payment amount."
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Submit Payment")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate100),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                color = Slate500,
                fontSize = 14.sp
            )
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                fontSize = 15.sp
            )
        }
    }
}

// Share reports to parents using SMS / WhatsApp Intent
fun shareStudentReport(
    context: Context,
    student: Student,
    attendanceList: List<Attendance>,
    breakdowns: List<MonthlyFeeStatus>
) {
    val presentDays = attendanceList.count { it.status == "PRESENT" }
    val absentDays = attendanceList.count { it.status == "ABSENT" }
    val lateDays = attendanceList.count { it.status == "LATE" }
    val totalDays = presentDays + absentDays + lateDays
    val attendanceRate = if (totalDays > 0) (presentDays.toDouble() / totalDays * 100).toInt() else 100

    val latestFee = breakdowns.lastOrNull()

    val reportText = StringBuilder()
    reportText.append("⭐ *SIR ABBAS ACADEMY REPORT* ⭐\n\n")
    reportText.append("*Student Name:* ${student.name}\n")
    reportText.append("*Father's Name:* ${student.fatherName}\n")
    reportText.append("*Grade:* ${student.grade}\n\n")

    reportText.append("📋 *Attendance Summary:*\n")
    reportText.append("• Rate: $attendanceRate%\n")
    reportText.append("• Present: $presentDays days\n")
    reportText.append("• Absent: $absentDays days\n")
    reportText.append("• Late: $lateDays days\n\n")

    reportText.append("💰 *Fee Summary:*\n")
    if (latestFee != null) {
        reportText.append("• Month: ${latestFee.monthYear}\n")
        reportText.append("• Decided Fee: Rs. ${latestFee.fixedFee.toInt()}\n")
        reportText.append("• Total Due: Rs. ${latestFee.totalDue.toInt()} (incl. pending)\n")
        reportText.append("• Amount Paid: Rs. ${latestFee.amountPaid.toInt()}\n")
        reportText.append("• Remaining Balance: Rs. ${latestFee.remaining.toInt()}\n\n")
        if (latestFee.remaining > 0) {
            reportText.append("⚠️ *Note:* Please clear the remaining balance of Rs. ${latestFee.remaining.toInt()} as soon as possible. Thank you!")
        } else {
            reportText.append("✓ *Note:* All fees are fully paid. Thank you!")
        }
    } else {
        reportText.append("• Monthly Decided Fee: Rs. ${student.fixedFee.toInt()}\n")
        reportText.append("• Status: No payments recorded yet.")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Sir Abbas Academy - Student Report")
        putExtra(Intent.EXTRA_TEXT, reportText.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Share Student Report"))
}

private fun getTodayDateString(): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return sdf.format(Date())
}
