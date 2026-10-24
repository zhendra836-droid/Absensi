package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.AttendanceRecord
import com.example.data.model.CompanySettings
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExporter {

    /**
     * Generates an Excel-compatible (.xls) Spreadsheet report with styling,
     * header company info, table borders, status tags, and total overtime calculations.
     */
    fun exportToExcel(
        context: Context,
        records: List<AttendanceRecord>,
        settings: CompanySettings,
        periodName: String
    ): File {
        val fileName = "Laporan_Presensi_${periodName.replace(" ", "_")}_${System.currentTimeMillis()}.xls"
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, fileName)

        var totalHadir = 0
        var totalTerlambat = 0
        var totalLuarRadius = 0
        var totalMenitKerja = 0
        var totalMenitLembur = 0

        records.forEach { r ->
            when (r.checkInStatus) {
                "HADIR" -> totalHadir++
                "TERLAMBAT" -> totalTerlambat++
                "LUAR_RADIUS" -> totalLuarRadius++
            }
            totalMenitKerja += r.workDurationMinutes
            totalMenitLembur += r.overtimeMinutes
        }

        val totalUangLembur = (totalMenitLembur / 60.0 * settings.overtimeRatePerHour).toLong()

        val sb = StringBuilder()
        sb.append("<html xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\" xmlns=\"http://www.w3.org/TR/REC-html40\">")
        sb.append("<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">")
        sb.append("<style>")
        sb.append("body { font-family: 'Calibri', 'Segoe UI', Arial, sans-serif; }")
        sb.append(".title { font-size: 18pt; font-weight: bold; color: #1E3A8A; }")
        sb.append(".subtitle { font-size: 12pt; color: #475569; }")
        sb.append(".summary-box { background-color: #F1F5F9; border: 1px solid #CBD5E1; padding: 10px; margin-bottom: 15px; }")
        sb.append("table { border-collapse: collapse; width: 100%; font-size: 11pt; }")
        sb.append("th { background-color: #1E40AF; color: #FFFFFF; border: 1px solid #1E3A8A; padding: 8px 10px; text-align: center; }")
        sb.append("td { border: 1px solid #CBD5E1; padding: 6px 10px; text-align: left; }")
        sb.append(".even { background-color: #F8FAFC; }")
        sb.append(".center { text-align: center; }")
        sb.append(".right { text-align: right; }")
        sb.append(".badge-hadir { color: #047857; font-weight: bold; }")
        sb.append(".badge-terlambat { color: #B45309; font-weight: bold; }")
        sb.append(".badge-luar { color: #B91C1C; font-weight: bold; }")
        sb.append(".total-row { background-color: #DBEAFE; font-weight: bold; border-top: 2px solid #1E40AF; }")
        sb.append("</style></head><body>")

        // Title and header
        sb.append("<div class=\"title\">${settings.companyName.uppercase()}</div>")
        sb.append("<div class=\"subtitle\">REKAPITULASI KEHADIRAN & PERHITUNGAN LEMBUR KARYAWAN</div>")
        sb.append("<p><strong>Periode:</strong> $periodName &nbsp;|&nbsp; <strong>Lokasi:</strong> ${settings.officeName} &nbsp;|&nbsp; <strong>Dicetak:</strong> ${SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.forLanguageTag("id-ID")).format(Date())} WIB</p>")

        // KPI Box
        sb.append("<table class=\"summary-box\" style=\"width:auto; margin-bottom: 20px;\">")
        sb.append("<tr>")
        sb.append("<td><strong>Total Data:</strong> ${records.size} Presensi</td>")
        sb.append("<td><strong>Tepat Waktu:</strong> $totalHadir</td>")
        sb.append("<td><strong>Terlambat:</strong> $totalTerlambat</td>")
        sb.append("<td><strong>Luar Radius:</strong> $totalLuarRadius</td>")
        sb.append("<td><strong>Total Jam Lembur:</strong> ${DateUtils.formatDuration(totalMenitLembur)}</td>")
        sb.append("<td><strong>Total Upah Lembur:</strong> ${DateUtils.formatCurrencyIdr(totalUangLembur)}</td>")
        sb.append("</tr>")
        sb.append("</table>")

        // Main Data Table
        sb.append("<table>")
        sb.append("<thead>")
        sb.append("<tr>")
        sb.append("<th>No</th>")
        sb.append("<th>Tanggal</th>")
        sb.append("<th>NIP</th>")
        sb.append("<th>Nama Karyawan</th>")
        sb.append("<th>Divisi</th>")
        sb.append("<th>Jam Masuk</th>")
        sb.append("<th>Jam Pulang</th>")
        sb.append("<th>Durasi Kerja</th>")
        sb.append("<th>Status</th>")
        sb.append("<th>Jam Lembur</th>")
        sb.append("<th>Est. Uang Lembur</th>")
        sb.append("<th>Jarak GPS</th>")
        sb.append("<th>Koordinat Check-In</th>")
        sb.append("<th>Alamat Lokasi</th>")
        sb.append("<th>Catatan</th>")
        sb.append("</tr>")
        sb.append("</thead>")
        sb.append("<tbody>")

        records.forEachIndexed { index, r ->
            val rowClass = if (index % 2 == 0) "even" else "odd"
            val statusClass = when (r.checkInStatus) {
                "HADIR" -> "badge-hadir"
                "TERLAMBAT" -> "badge-terlambat"
                else -> "badge-luar"
            }
            val uangLemburItem = (r.overtimeMinutes / 60.0 * settings.overtimeRatePerHour).toLong()

            sb.append("<tr class=\"$rowClass\">")
            sb.append("<td class=\"center\">${index + 1}</td>")
            sb.append("<td class=\"center\">${r.dateString}</td>")
            sb.append("<td class=\"center\">${r.employeeNip}</td>")
            sb.append("<td><strong>${r.employeeName}</strong></td>")
            sb.append("<td>${r.department}</td>")
            sb.append("<td class=\"center\">${DateUtils.formatTime(r.checkInTimestamp)}</td>")
            sb.append("<td class=\"center\">${DateUtils.formatTime(r.checkOutTimestamp)}</td>")
            sb.append("<td class=\"center\">${DateUtils.formatDuration(r.workDurationMinutes)}</td>")
            sb.append("<td class=\"center $statusClass\">${r.checkInStatus}</td>")
            sb.append("<td class=\"center\">${DateUtils.formatDuration(r.overtimeMinutes)}</td>")
            sb.append("<td class=\"right\">${DateUtils.formatCurrencyIdr(uangLemburItem)}</td>")
            sb.append("<td class=\"center\">${String.format(Locale.US, "%.0f m", r.checkInDistanceMeters)}</td>")
            sb.append("<td class=\"center\">${String.format(Locale.US, "%.5f, %.5f", r.checkInLatitude, r.checkInLongitude)}</td>")
            sb.append("<td>${r.checkInAddress}</td>")
            sb.append("<td>${r.notes.ifEmpty { "-" }}</td>")
            sb.append("</tr>")
        }

        // Total Summary Row
        sb.append("<tr class=\"total-row\">")
        sb.append("<td colspan=\"7\" class=\"right\">TOTAL KESELURUHAN:</td>")
        sb.append("<td class=\"center\">${DateUtils.formatDuration(totalMenitKerja)}</td>")
        sb.append("<td></td>")
        sb.append("<td class=\"center\">${DateUtils.formatDuration(totalMenitLembur)}</td>")
        sb.append("<td class=\"right\">${DateUtils.formatCurrencyIdr(totalUangLembur)}</td>")
        sb.append("<td colspan=\"4\"></td>")
        sb.append("</tr>")

        sb.append("</tbody>")
        sb.append("</table>")

        // Signatures footer
        sb.append("<br><br>")
        sb.append("<table style=\"width:100%; border:none;\">")
        sb.append("<tr>")
        sb.append("<td style=\"width:50%; border:none; text-align:center;\">Dibuat oleh,<br><br><br><br><strong>( Human Resources )</strong></td>")
        sb.append("<td style=\"width:50%; border:none; text-align:center;\">Mengetahui,<br><br><br><br><strong>( Pimpinan Perusahaan )</strong></td>")
        sb.append("</tr>")
        sb.append("</table>")

        sb.append("</body></html>")

        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                writer.write(sb.toString())
            }
        }

        return file
    }

    /**
     * Generates a CSV file with UTF-8 BOM so Excel opens it cleanly without character corruption.
     */
    fun exportToCsv(
        context: Context,
        records: List<AttendanceRecord>,
        settings: CompanySettings,
        periodName: String
    ): File {
        val fileName = "Laporan_Presensi_${periodName.replace(" ", "_")}_${System.currentTimeMillis()}.csv"
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, fileName)

        FileOutputStream(file).use { fos ->
            // Write UTF-8 BOM
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                // Header info
                writer.write("Laporan Kehadiran Karyawan - ${settings.companyName}\n")
                writer.write("Periode: $periodName\n")
                writer.write("Lokasi Kantor: ${settings.officeName}\n\n")

                // Column headers
                writer.write("No;Tanggal;NIP;Nama Karyawan;Divisi;Jam Masuk;Jam Pulang;Durasi Kerja;Status;Jam Lembur;Upah Lembur;Jarak GPS;Latitude;Longitude;Alamat;Catatan\n")

                records.forEachIndexed { index, r ->
                    val uangLembur = (r.overtimeMinutes / 60.0 * settings.overtimeRatePerHour).toLong()
                    val line = listOf(
                        "${index + 1}",
                        r.dateString,
                        r.employeeNip,
                        "\"${r.employeeName}\"",
                        "\"${r.department}\"",
                        DateUtils.formatTime(r.checkInTimestamp),
                        DateUtils.formatTime(r.checkOutTimestamp),
                        "\"${DateUtils.formatDuration(r.workDurationMinutes)}\"",
                        r.checkInStatus,
                        "\"${DateUtils.formatDuration(r.overtimeMinutes)}\"",
                        uangLembur.toString(),
                        String.format(Locale.US, "%.1fm", r.checkInDistanceMeters),
                        String.format(Locale.US, "%.6f", r.checkInLatitude),
                        String.format(Locale.US, "%.6f", r.checkInLongitude),
                        "\"${r.checkInAddress.replace("\"", "'")}\"",
                        "\"${r.notes.replace("\"", "'")}\""
                    ).joinToString(";")
                    writer.write(line + "\n")
                }
            }
        }

        return file
    }

    /**
     * Share file intent via Android Chooser
     */
    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "Berikut terlampir rekapan data kehadiran karyawan ($title).")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Buka atau Bagikan Laporan Excel")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
