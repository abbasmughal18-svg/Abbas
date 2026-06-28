package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Student
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditStudentScreen(
    viewModel: AcademyViewModel,
    modifier: Modifier = Modifier
) {
    val students by viewModel.students.collectAsState()
    val editingStudent by viewModel.editingStudent.collectAsState()

    // Form states
    var name by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("Grade 1") }
    var admissionDate by remember { mutableStateOf(getTodayDateString()) }
    var contact1 by remember { mutableStateOf("") }
    var contact2 by remember { mutableStateOf("") }
    var fixedFeeStr by remember { mutableStateOf("") }

    // Validation/Error states
    var nameError by remember { mutableStateOf(false) }
    var fatherNameError by remember { mutableStateOf(false) }
    var admissionDateError by remember { mutableStateOf(false) }
    var contact1Error by remember { mutableStateOf(false) }
    var fixedFeeError by remember { mutableStateOf(false) }

    var successMessage by remember { mutableStateOf("") }

    // Delete confirmation state
    var studentToDelete by remember { mutableStateOf<Student?>(null) }

    // Dropdown list for Grade selection
    val grades = remember { (1..12).map { "Grade $it" } }
    var gradeDropdownExpanded by remember { mutableStateOf(false) }

    // Sync form with editing student
    LaunchedEffect(editingStudent) {
        if (editingStudent != null) {
            val student = editingStudent!!
            name = student.name
            fatherName = student.fatherName
            grade = student.grade
            admissionDate = student.admissionDate
            contact1 = student.parentContact1
            contact2 = student.parentContact2
            fixedFeeStr = student.fixedFee.toInt().toString()
        } else {
            // Reset to defaults for addition
            name = ""
            fatherName = ""
            grade = "Grade 1"
            admissionDate = getTodayDateString()
            contact1 = ""
            contact2 = ""
            fixedFeeStr = ""
        }
        // Reset errors
        nameError = false
        fatherNameError = false
        admissionDateError = false
        contact1Error = false
        fixedFeeError = false
        successMessage = ""
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Form Title
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RoyalBlueContainer),
                colors = CardDefaults.cardColors(containerColor = RoyalBlueContainer.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = RoyalBlue,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (editingStudent != null) "Edit Student Profile" else "Enroll New Student",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue
                    )
                }
            }
        }

        // Student Basic Info
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Student Full Name *") },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )

                OutlinedTextField(
                    value = fatherName,
                    onValueChange = {
                        fatherName = it
                        fatherNameError = false
                    },
                    label = { Text("Father's Name *") },
                    isError = fatherNameError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )

                // Grade Select Dropdown
                ExposedDropdownMenuBox(
                    expanded = gradeDropdownExpanded,
                    onExpandedChange = { gradeDropdownExpanded = !gradeDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = grade,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Class/Grade") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                    )
                    ExposedDropdownMenu(
                        expanded = gradeDropdownExpanded,
                        onDismissRequest = { gradeDropdownExpanded = false }
                    ) {
                        grades.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g) },
                                onClick = {
                                    grade = g
                                    gradeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = admissionDate,
                    onValueChange = {
                        admissionDate = it
                        admissionDateError = false
                    },
                    label = { Text("Date of Admission * (dd.mm.yyyy)") },
                    isError = admissionDateError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
                    supportingText = {
                        if (admissionDateError) {
                            Text("Format must be DD.MM.YYYY", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("e.g., 28.06.2026", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )
            }
        }

        // Contact & Fee Info
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = contact1,
                    onValueChange = {
                        contact1 = it
                        contact1Error = false
                    },
                    label = { Text("Parent Contact Number 1 *") },
                    isError = contact1Error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )

                OutlinedTextField(
                    value = contact2,
                    onValueChange = { contact2 = it },
                    label = { Text("Parent Contact Number 2 (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )

                OutlinedTextField(
                    value = fixedFeeStr,
                    onValueChange = {
                        fixedFeeStr = it
                        fixedFeeError = false
                    },
                    label = { Text("Decided Fixed Monthly Fee (Rs.) *") },
                    isError = fixedFeeError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )
            }
        }

        // Success message and Buttons
        item {
            if (successMessage.isNotEmpty()) {
                Text(
                    text = successMessage,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (editingStudent != null) {
                    OutlinedButton(
                        onClick = { viewModel.setEditingStudent(null) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel Edit")
                    }
                }

                Button(
                    onClick = {
                        // Validate
                        if (name.isBlank()) nameError = true
                        if (fatherName.isBlank()) fatherNameError = true
                        if (!isValidDateFormat(admissionDate)) admissionDateError = true
                        if (contact1.isBlank()) contact1Error = true
                        val fee = fixedFeeStr.toDoubleOrNull()
                        if (fee == null || fee < 0) fixedFeeError = true

                        if (!nameError && !fatherNameError && !admissionDateError && !contact1Error && !fixedFeeError) {
                            viewModel.saveStudent(
                                id = editingStudent?.id ?: 0,
                                name = name,
                                fatherName = fatherName,
                                grade = grade,
                                admissionDate = admissionDate,
                                parentContact1 = contact1,
                                parentContact2 = contact2,
                                fixedFee = fee!!
                            )

                            if (editingStudent != null) {
                                successMessage = "Student profile updated successfully!"
                                viewModel.setEditingStudent(null)
                            } else {
                                successMessage = "Student '$name' enrolled successfully!"
                                // Reset
                                name = ""
                                fatherName = ""
                                grade = "Grade 1"
                                admissionDate = getTodayDateString()
                                contact1 = ""
                                contact2 = ""
                                fixedFeeStr = ""
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (editingStudent != null) "Update Details" else "Enroll Student")
                }
            }
        }

        // Academy Student Directory (Delete / Edit quick list)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Academy Directory (${students.size} Student${if (students.size == 1) "" else "s"})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (students.isEmpty()) {
            item {
                Text(
                    text = "No students registered yet in Sir Abbas Academy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate500
                )
            }
        } else {
            items(students, key = { it.id }) { student ->
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
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(
                                text = student.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = RoyalBlue
                            )
                            Text(
                                text = "Class: ${student.grade} | S/O: ${student.fatherName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.setEditingStudent(student) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Student",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { studentToDelete = student },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Student",
                                    tint = AbsentRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (studentToDelete != null) {
        val student = studentToDelete!!
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = { Text("Delete Student Confirmation") },
            text = {
                Text(
                    "Are you absolutely sure you want to delete student '${student.name}'? " +
                            "This will permanently delete all of their attendance history and fee payment logs. This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStudent(student)
                        studentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AbsentRed)
                ) {
                    Text("Delete Permanently", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun getTodayDateString(): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return sdf.format(Date())
}
