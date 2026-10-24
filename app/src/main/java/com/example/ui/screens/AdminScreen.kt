package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.CompanySettings
import com.example.data.model.Employee
import com.example.ui.AdminTab
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.RoseError
import com.example.util.DateUtils
import java.util.Locale

@Composable
fun AdminScreen(
    isAuthenticated: Boolean,
    activeTab: AdminTab,
    settings: CompanySettings,
    employees: List<Employee>,
    allRecords: List<AttendanceRecord>,
    selectedMonth: String,
    onAuthenticate: (String) -> Boolean,
    onTabSelected: (AdminTab) -> Unit,
    onMonthSelected: (String) -> Unit,
    onExportExcel: (isCsv: Boolean) -> Unit,
    onSetOfficeToCurrentLocation: () -> Unit,
    onSaveSettings: (CompanySettings) -> Unit,
    onAddEmployee: (nip: String, name: String, dept: String, pos: String, phone: String) -> Unit,
    onDeleteEmployee: (Employee) -> Unit,
    onRecordClick: (AttendanceRecord) -> Unit,
    onExitAdmin: () -> Unit
) {
    if (!isAuthenticated) {
        AdminLoginDialog(
            onAuthenticate = onAuthenticate,
            onCancel = onExitAdmin
        )
        return
    }

    var showAddEmployeeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(color = NavyDark, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onExitAdmin) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali ke Karyawan",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = "Panel Manajemen Admin",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = settings.companyName,
                                    color = CyanAccent,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        IconButton(
                            onClick = onExitAdmin,
                            modifier = Modifier.testTag("exit_admin_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Keluar Admin",
                                tint = Color(0xFFF87171)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tab Navigation
                    ScrollableTabRow(
                        selectedTabIndex = activeTab.ordinal,
                        containerColor = NavyDark,
                        contentColor = Color.White,
                        edgePadding = 16.dp
                    ) {
                        Tab(
                            selected = activeTab == AdminTab.MONITORING,
                            onClick = { onTabSelected(AdminTab.MONITORING) },
                            text = { Text("Monitoring") },
                            icon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                            modifier = Modifier.testTag("tab_monitoring")
                        )
                        Tab(
                            selected = activeTab == AdminTab.REKAP_EXCEL,
                            onClick = { onTabSelected(AdminTab.REKAP_EXCEL) },
                            text = { Text("Rekap & Excel") },
                            icon = { Icon(Icons.Default.TableChart, contentDescription = null) },
                            modifier = Modifier.testTag("tab_rekap_excel")
                        )
                        Tab(
                            selected = activeTab == AdminTab.KARYAWAN,
                            onClick = { onTabSelected(AdminTab.KARYAWAN) },
                            text = { Text("Karyawan (${employees.size})") },
                            icon = { Icon(Icons.Default.Group, contentDescription = null) },
                            modifier = Modifier.testTag("tab_karyawan")
                        )
                        Tab(
                            selected = activeTab == AdminTab.PENGATURAN,
                            onClick = { onTabSelected(AdminTab.PENGATURAN) },
                            text = { Text("Aturan & Lokasi") },
                            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            modifier = Modifier.testTag("tab_pengaturan")
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (activeTab == AdminTab.KARYAWAN) {
                FloatingActionButton(
                    onClick = { showAddEmployeeDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_employee_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Karyawan")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (activeTab) {
                AdminTab.MONITORING -> {
                    AdminMonitoringContent(
                        employees = employees,
                        records = allRecords,
                        onRecordClick = onRecordClick
                    )
                }
                AdminTab.REKAP_EXCEL -> {
                    AdminRekapExcelContent(
                        settings = settings,
                        records = allRecords,
                        selectedMonth = selectedMonth,
                        onMonthSelected = onMonthSelected,
                        onExportExcel = onExportExcel,
                        onRecordClick = onRecordClick
                    )
                }
                AdminTab.KARYAWAN -> {
                    AdminEmployeeListContent(
                        employees = employees,
                        onDelete = onDeleteEmployee
                    )
                }
                AdminTab.PENGATURAN -> {
                    AdminSettingsContent(
                        settings = settings,
                        onSetOfficeToCurrentLocation = onSetOfficeToCurrentLocation,
                        onSaveSettings = onSaveSettings
                    )
                }
            }
        }

        if (showAddEmployeeDialog) {
            AddEmployeeDialog(
                onDismiss = { showAddEmployeeDialog = false },
                onConfirm = { nip, name, dept, pos, phone ->
                    onAddEmployee(nip, name, dept, pos, phone)
                    showAddEmployeeDialog = false
                }
            )
        }
    }
}

@Composable
private fun AdminMonitoringContent(
    employees: List<Employee>,
    records: List<AttendanceRecord>,
    onRecordClick: (AttendanceRecord) -> Unit
) {
    val todayStr = DateUtils.getTodayDateString()
    val todayRecords = records.filter { it.dateString == todayStr }

    val hadirCount = todayRecords.count { it.checkInStatus == "HADIR" }
    val lateCount = todayRecords.count { it.checkInStatus == "TERLAMBAT" }
    val outRadiusCount = todayRecords.count { it.checkInStatus == "LUAR_RADIUS" }
    val lemburCount = todayRecords.count { it.overtimeMinutes > 0 }
    val belumAbsenCount = (employees.size - todayRecords.size).coerceAtLeast(0)

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // KPI Grid
        item {
            Text(
                text = "Ringkasan Kehadiran Hari Ini ($todayStr)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiCard(
                    title = "Total Staf",
                    value = "${employees.size}",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Tepat Waktu",
                    value = "$hadirCount",
                    color = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Terlambat",
                    value = "$lateCount",
                    color = AmberWarning,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiCard(
                    title = "Luar Radius",
                    value = "$outRadiusCount",
                    color = RoseError,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Lembur",
                    value = "$lemburCount",
                    color = CyanAccent,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Belum Absen",
                    value = "$belumAbsenCount",
                    color = Color(0xFF64748B),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Log Presensi Hari Ini (${todayRecords.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (todayRecords.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Belum ada karyawan yang melakukan presensi hari ini.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(todayRecords) { r ->
                AttendanceItemRow(record = r, onClick = { onRecordClick(r) })
            }
        }
    }
}

@Composable
private fun AdminRekapExcelContent(
    settings: CompanySettings,
    records: List<AttendanceRecord>,
    selectedMonth: String,
    onMonthSelected: (String) -> Unit,
    onExportExcel: (isCsv: Boolean) -> Unit,
    onRecordClick: (AttendanceRecord) -> Unit
) {
    val monthRecords = records.filter { it.dateString.startsWith(selectedMonth) }

    val totalJamKerjaMins = monthRecords.sumOf { it.workDurationMinutes }
    val totalLemburMins = monthRecords.sumOf { it.overtimeMinutes }
    val totalUangLembur = (totalLemburMins / 60.0 * settings.overtimeRatePerHour).toLong()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Month Selector Chips
        item {
            Text(
                text = "Pilih Periode Laporan Bulanan",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("2026-10", "2026-09", "2026-08", "2026-07").forEach { m ->
                    val isSel = m == selectedMonth
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { onMonthSelected(m) }
                    ) {
                        Text(
                            text = m,
                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Monthly Total Overtime & Work Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Akumulasi Otomatis Akhir Bulan ($selectedMonth)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Jam Kerja", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(DateUtils.formatDuration(totalJamKerjaMins), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Total Jam Lembur", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(DateUtils.formatDuration(totalLemburMins), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AmberWarning)
                        }
                        Column {
                            Text("Est. Biaya Lembur", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(DateUtils.formatCurrencyIdr(totalUangLembur), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Excel Export Action Buttons
            Text(
                text = "Unduh & Ekspor Laporan Otomatis",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onExportExcel(false) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("download_excel_button")
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unduh Excel (.xls)", fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = { onExportExcel(true) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("download_csv_button")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bagikan CSV", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Tabel Lembar Kerja Presensi ($selectedMonth: ${monthRecords.size} baris)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Interactive In-App Excel Sheet Preview Table
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    // Excel Sheet Header Row
                    Row(
                        modifier = Modifier
                            .background(NavyDark)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        TableCell("No", width = 36.dp, isHeader = true)
                        TableCell("Tgl", width = 85.dp, isHeader = true)
                        TableCell("NIP", width = 80.dp, isHeader = true)
                        TableCell("Nama", width = 140.dp, isHeader = true)
                        TableCell("Masuk", width = 65.dp, isHeader = true)
                        TableCell("Pulang", width = 65.dp, isHeader = true)
                        TableCell("Durasi", width = 90.dp, isHeader = true)
                        TableCell("Lembur", width = 80.dp, isHeader = true)
                        TableCell("Status", width = 85.dp, isHeader = true)
                    }

                    monthRecords.forEachIndexed { idx, r ->
                        val bg = if (idx % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        Row(
                            modifier = Modifier
                                .background(bg)
                                .clickable { onRecordClick(r) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            TableCell("${idx + 1}", width = 36.dp)
                            TableCell(r.dateString, width = 85.dp)
                            TableCell(r.employeeNip, width = 80.dp)
                            TableCell(r.employeeName, width = 140.dp, isBold = true)
                            TableCell(DateUtils.formatTime(r.checkInTimestamp), width = 65.dp)
                            TableCell(DateUtils.formatTime(r.checkOutTimestamp), width = 65.dp)
                            TableCell(DateUtils.formatDuration(r.workDurationMinutes), width = 90.dp)
                            TableCell(DateUtils.formatDuration(r.overtimeMinutes), width = 80.dp, color = if (r.overtimeMinutes > 0) AmberWarning else Color.Unspecified)
                            TableCell(r.checkInStatus, width = 85.dp, color = when(r.checkInStatus) {
                                "HADIR" -> EmeraldSuccess
                                "TERLAMBAT" -> AmberWarning
                                else -> RoseError
                            })
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isHeader: Boolean = false,
    isBold: Boolean = false,
    color: Color = Color.Unspecified
) {
    Text(
        text = text,
        modifier = Modifier.width(width),
        style = if (isHeader) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall,
        fontWeight = if (isHeader || isBold) FontWeight.Bold else FontWeight.Normal,
        color = if (isHeader) Color.White else color,
        maxLines = 1
    )
}

@Composable
private fun AdminEmployeeListContent(
    employees: List<Employee>,
    onDelete: (Employee) -> Unit
) {
    var employeeToDelete by remember { mutableStateOf<Employee?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text(
                text = "Daftar Karyawan Terdaftar (${employees.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Kelola data pegawai, NIP, dan hak akses presensi",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(employees) { emp ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(emp.avatarColorHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emp.name.take(1),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(emp.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text("${emp.nip} • ${emp.position}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(emp.department, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }

                    IconButton(onClick = { employeeToDelete = emp }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFEF4444))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    if (employeeToDelete != null) {
        AlertDialog(
            onDismissRequest = { employeeToDelete = null },
            title = { Text("Hapus Karyawan") },
            text = { Text("Yakin ingin menghapus ${employeeToDelete?.name} (${employeeToDelete?.nip})?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        employeeToDelete?.let { onDelete(it) }
                        employeeToDelete = null
                    }
                ) {
                    Text("Hapus", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { employeeToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun AdminSettingsContent(
    settings: CompanySettings,
    onSetOfficeToCurrentLocation: () -> Unit,
    onSaveSettings: (CompanySettings) -> Unit
) {
    var companyName by remember(settings) { mutableStateOf(settings.companyName) }
    var officeName by remember(settings) { mutableStateOf(settings.officeName) }
    var officeLat by remember(settings) { mutableStateOf(settings.officeLatitude.toString()) }
    var officeLng by remember(settings) { mutableStateOf(settings.officeLongitude.toString()) }
    var radiusMeters by remember(settings) { mutableStateOf(settings.radiusToleranceMeters.toInt().toString()) }
    var workStart by remember(settings) { mutableStateOf(settings.workStartTime) }
    var workEnd by remember(settings) { mutableStateOf(settings.workEndTime) }
    var overtimeMin by remember(settings) { mutableStateOf(settings.overtimeMinMinutes.toString()) }
    var overtimeRate by remember(settings) { mutableStateOf(settings.overtimeRatePerHour.toString()) }
    var adminPin by remember(settings) { mutableStateOf(settings.adminPin) }
    var allowSimulated by remember(settings) { mutableStateOf(settings.allowSimulatedLocation) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Pengaturan Kantor & Radius Geofence",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = companyName,
            onValueChange = { companyName = it },
            label = { Text("Nama Perusahaan") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = officeName,
            onValueChange = { officeName = it },
            label = { Text("Nama Kantor / Gedung") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = officeLat,
                onValueChange = { officeLat = it },
                label = { Text("Latitude") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = officeLng,
                onValueChange = { officeLng = it },
                label = { Text("Longitude") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Button to use current GPS location
        Button(
            onClick = onSetOfficeToCurrentLocation,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
            modifier = Modifier.fillMaxWidth().testTag("use_current_gps_button")
        ) {
            Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gunakan Lokasi GPS Saat Ini Sebagai Kantor", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = radiusMeters,
            onValueChange = { radiusMeters = it },
            label = { Text("Radius Toleransi Geofence (Meter)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Jam Kerja & Aturan Lembur Otomatis",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = workStart,
                onValueChange = { workStart = it },
                label = { Text("Jam Masuk (HH:mm)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = workEnd,
                onValueChange = { workEnd = it },
                label = { Text("Jam Pulang (HH:mm)") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = overtimeMin,
                onValueChange = { overtimeMin = it },
                label = { Text("Min. Lembur (Menit)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = overtimeRate,
                onValueChange = { overtimeRate = it },
                label = { Text("Tarif Lembur/Jam (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = adminPin,
            onValueChange = { adminPin = it },
            label = { Text("PIN Keamanan Admin") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Simulation toggle for browser streaming emulator testing
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Mode Simulasi Lokasi", fontWeight = FontWeight.Bold)
                    Text("Memudahkan pengujian jarak & radius GPS di emulator browser.", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = allowSimulated, onCheckedChange = { allowSimulated = it })
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val updated = settings.copy(
                    companyName = companyName,
                    officeName = officeName,
                    officeLatitude = officeLat.toDoubleOrNull() ?: settings.officeLatitude,
                    officeLongitude = officeLng.toDoubleOrNull() ?: settings.officeLongitude,
                    radiusToleranceMeters = radiusMeters.toFloatOrNull() ?: settings.radiusToleranceMeters,
                    workStartTime = workStart,
                    workEndTime = workEnd,
                    overtimeMinMinutes = overtimeMin.toIntOrNull() ?: settings.overtimeMinMinutes,
                    overtimeRatePerHour = overtimeRate.toLongOrNull() ?: settings.overtimeRatePerHour,
                    adminPin = adminPin,
                    allowSimulatedLocation = allowSimulated
                )
                onSaveSettings(updated)
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("save_settings_button")
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Simpan Pengaturan", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
private fun AttendanceItemRow(
    record: AttendanceRecord,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(record.employeeName.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(record.employeeName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "${record.department} • In: ${DateUtils.formatTime(record.checkInTimestamp)} | Out: ${DateUtils.formatTime(record.checkOutTimestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (record.overtimeMinutes > 0) {
                    Text(
                        text = "Lembur: ${DateUtils.formatDuration(record.overtimeMinutes)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberWarning,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            val (sColor, sBg) = when (record.checkInStatus) {
                "HADIR" -> EmeraldSuccess to EmeraldSuccess.copy(alpha = 0.15f)
                "TERLAMBAT" -> AmberWarning to AmberWarning.copy(alpha = 0.15f)
                else -> RoseError to RoseError.copy(alpha = 0.15f)
            }

            Surface(shape = RoundedCornerShape(6.dp), color = sBg) {
                Text(
                    text = record.checkInStatus,
                    color = sColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun AdminLoginDialog(
    onAuthenticate: (String) -> Boolean,
    onCancel: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text("Masuk Mode Admin", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Masukkan PIN Admin untuk mengakses data rekapitulasi, export Excel, dan pengaturan kantor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 6) pin = it
                        errorMessage = ""
                    },
                    label = { Text("PIN Keamanan") },
                    placeholder = { Text("Default: 1234") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = errorMessage.isNotEmpty(),
                    supportingText = {
                        if (errorMessage.isNotEmpty()) {
                            Text(errorMessage, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("PIN Default: 1234", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("admin_pin_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val success = onAuthenticate(pin)
                    if (!success) {
                        errorMessage = "PIN salah. Silakan coba lagi."
                    }
                },
                modifier = Modifier.testTag("admin_login_submit_button")
            ) {
                Text("Masuk")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun AddEmployeeDialog(
    onDismiss: () -> Unit,
    onConfirm: (nip: String, name: String, dept: String, pos: String, phone: String) -> Unit
) {
    var nip by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var dept by remember { mutableStateOf("") }
    var pos by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Karyawan Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = nip,
                    onValueChange = { nip = it },
                    label = { Text("Nomor Induk Pegawai (NIP)") },
                    placeholder = { Text("Misal: EMP-005") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dept,
                    onValueChange = { dept = it },
                    label = { Text("Divisi / Departemen") },
                    placeholder = { Text("Misal: Operasional, IT, HR") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = pos,
                    onValueChange = { pos = it },
                    label = { Text("Jabatan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Nomor WhatsApp / Telp") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nip.isBlank() || name.isBlank()) {
                        errorMsg = "NIP dan Nama tidak boleh kosong"
                    } else {
                        onConfirm(nip, name, dept.ifEmpty { "Umum" }, pos.ifEmpty { "Staff" }, phone)
                    }
                }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
