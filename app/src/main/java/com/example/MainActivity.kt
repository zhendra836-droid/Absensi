package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppRole
import com.example.ui.MainViewModel
import com.example.ui.components.FaceScannerSheet
import com.example.ui.components.RecordDetailDialog
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.EmployeeScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAttendanceApp()
            }
        }
    }
}

@Composable
fun MainAttendanceApp(viewModel: MainViewModel = viewModel()) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val adminAuthenticated by viewModel.adminAuthenticated.collectAsStateWithLifecycle()
    val adminTab by viewModel.adminTab.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val selectedEmployee by viewModel.selectedEmployee.collectAsStateWithLifecycle()
    val todayAttendance by viewModel.todayAttendance.collectAsStateWithLifecycle()
    val employeeHistory by viewModel.employeeHistory.collectAsStateWithLifecycle()
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val isRefreshingLocation by viewModel.isRefreshingLocation.collectAsStateWithLifecycle()
    val showFaceScanner by viewModel.showFaceScanner.collectAsStateWithLifecycle()
    val scanActionType by viewModel.scanActionType.collectAsStateWithLifecycle()
    val isProcessingAttendance by viewModel.isProcessingAttendance.collectAsStateWithLifecycle()
    val notification by viewModel.notification.collectAsStateWithLifecycle()
    val detailRecord by viewModel.detailRecord.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Request Location Permissions
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshLocation()
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Show notifications via Snackbar
    LaunchedEffect(notification) {
        notification?.let {
            snackbarHostState.showSnackbar(it.message)
            viewModel.clearNotification()
        }
    }

    // Back handling for Admin mode
    BackHandler(enabled = currentRole == AppRole.ADMIN) {
        viewModel.setRole(AppRole.EMPLOYEE)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRole) {
                AppRole.EMPLOYEE -> {
                    EmployeeScreen(
                        settings = settings,
                        employees = employees,
                        selectedEmployee = selectedEmployee,
                        todayRecord = todayAttendance,
                        history = employeeHistory,
                        locationResult = currentLocation,
                        isRefreshingLocation = isRefreshingLocation,
                        onSelectEmployee = { viewModel.selectEmployee(it) },
                        onRefreshLocation = { viewModel.refreshLocation() },
                        onClockInClick = { viewModel.openScanner("MASUK") },
                        onClockOutClick = { viewModel.openScanner("PULANG") },
                        onRecordClick = { viewModel.showRecordDetail(it) },
                        onSwitchToAdmin = { viewModel.setRole(AppRole.ADMIN) }
                    )
                }
                AppRole.ADMIN -> {
                    AdminScreen(
                        isAuthenticated = adminAuthenticated,
                        activeTab = adminTab,
                        settings = settings,
                        employees = employees,
                        allRecords = allRecords,
                        selectedMonth = selectedMonth,
                        onAuthenticate = { viewModel.authenticateAdmin(it) },
                        onTabSelected = { viewModel.setAdminTab(it) },
                        onMonthSelected = { viewModel.setSelectedMonth(it) },
                        onExportExcel = { viewModel.exportExcelReport(it) },
                        onSetOfficeToCurrentLocation = { viewModel.setOfficeToCurrentLocation() },
                        onSaveSettings = { viewModel.updateCompanySettings(it) },
                        onAddEmployee = { nip, name, dept, pos, phone ->
                            viewModel.addEmployee(nip, name, dept, pos, phone)
                        },
                        onDeleteEmployee = { viewModel.deleteEmployee(it) },
                        onRecordClick = { viewModel.showRecordDetail(it) },
                        onExitAdmin = { viewModel.logoutAdmin() }
                    )
                }
            }

            // Face Biometric Scanner Dialog
            if (showFaceScanner && selectedEmployee != null) {
                FaceScannerSheet(
                    employee = selectedEmployee!!,
                    actionType = scanActionType,
                    locationResult = currentLocation,
                    isProcessing = isProcessingAttendance,
                    onDismiss = { viewModel.closeScanner() },
                    onCaptureComplete = { bitmap, notes ->
                        viewModel.processAttendance(bitmap, notes)
                    }
                )
            }

            // Attendance Record Detail Dialog (Photo & Geotag Proof)
            if (detailRecord != null) {
                RecordDetailDialog(
                    record = detailRecord!!,
                    onDismiss = { viewModel.clearRecordDetail() }
                )
            }
        }
    }
}
