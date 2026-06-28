package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AcademyViewModel
import com.example.ui.AddEditStudentScreen
import com.example.ui.StudentDetailsScreen
import com.example.ui.StudentProfileDialog
import com.example.ui.TodayAttendanceScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: AcademyViewModel = viewModel()
                var currentTab by remember { mutableStateOf(0) }
                val selectedStudentForProfile by viewModel.selectedStudent.collectAsState()
                val attendanceList by viewModel.selectedStudentAttendance.collectAsState()
                val paymentList by viewModel.selectedStudentPayments.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Sir Abbas Academy",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Attendance & Fee Ledger",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                label = { Text("Attendance", fontWeight = FontWeight.SemiBold) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Today's Attendance"
                                    )
                                },
                                modifier = Modifier.testTag("tab_attendance")
                            )
                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                label = { Text("Reports & Fees", fontWeight = FontWeight.SemiBold) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Assessment,
                                        contentDescription = "Reports and Fee Ledger"
                                    )
                                },
                                modifier = Modifier.testTag("tab_reports")
                            )
                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                label = { Text("Directory", fontWeight = FontWeight.SemiBold) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = "Directory & Enrollment"
                                    )
                                },
                                modifier = Modifier.testTag("tab_directory")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            0 -> TodayAttendanceScreen(
                                viewModel = viewModel,
                                onStudentClick = { student ->
                                    viewModel.selectStudentForProfile(student)
                                }
                            )
                            1 -> StudentDetailsScreen(
                                viewModel = viewModel
                            )
                            2 -> AddEditStudentScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }

                // Global Profile dialog (so it works from any screen)
                if (selectedStudentForProfile != null) {
                    val student = selectedStudentForProfile!!
                    StudentProfileDialog(
                        student = student,
                        attendance = attendanceList,
                        payments = paymentList,
                        viewModel = viewModel,
                        onDismiss = { viewModel.selectStudentForProfile(null) }
                    )
                }
            }
        }
    }
}
