package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.FileProvider
import com.example.data.model.OfficerProfile
import com.example.data.model.TourEntry
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object PrintExportHelper {

    fun generateForm1DiaryHtml(
        officer: OfficerProfile,
        monthYear: String,
        entries: List<TourEntry>
    ): String {
        val monthTa = DateUtils.getTamilMonthDisplay(monthYear)
        val sb = StringBuilder()

        sb.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Tour Diary - $monthTa</title>
                <style>
                    @page { size: A4 portrait; margin: 8mm; }
                    body { font-family: 'Noto Sans Tamil', 'Segoe UI', Arial, sans-serif; font-size: 11px; width: 100%; margin: 0; padding: 12px; background-color: #f8fafc; color: #000; box-sizing: border-box; }
                    .header-title { text-align: center; font-size: 14px; font-weight: bold; margin-bottom: 4px; }
                    .header-sub { text-align: center; font-size: 13px; font-weight: bold; margin-bottom: 12px; }
                    table { width: 100%; border-collapse: collapse; margin-top: 5px; }
                    th, td { border: 1px solid #000; padding: 4px 5px; text-align: center; font-size: 10.5px; }
                    th { background-color: #f2f2f2; font-weight: bold; }
                    .col-left { text-align: left; }
                    .col-center { text-align: center; }
                    .col-right { text-align: right; }
                    .num-header { font-size: 9px; font-weight: normal; }
                    .holiday-row { background-color: #fafafa; font-style: italic; }
                    .footer-sig { margin-top: 30px; width: 100%; display: flex; justify-content: space-between; }
                    @media print {
                        @page { size: A4 portrait; margin: 8mm; }
                        body { width: 100%; margin: 0; padding: 0; background-color: #fff; }
                        .no-print { display: none !important; }
                    }
                </style>
                <script src="https://cdnjs.cloudflare.com/ajax/libs/html2pdf.js/0.10.1/html2pdf.bundle.min.js"></script>
                <script>
                    function safeExportPdf() {
                        try {
                            var el = document.getElementById('report-content') || document.body;
                            var opt = {
                                margin: [8, 8, 8, 8],
                                filename: 'Tour_Diary_${monthYear.replace("-", "_")}.pdf',
                                image: { type: 'jpeg', quality: 0.98 },
                                html2canvas: { scale: 2, useCORS: true },
                                jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
                            };
                            if (window.html2pdf) {
                                html2pdf().set(opt).from(el).save();
                            } else if (typeof window.print === 'function') {
                                window.print();
                            }
                        } catch (e) {
                            console.error("PDF export error: ", e);
                        }
                    }
                </script>
            </head>
            <body>
                <div class="no-print" style="margin-bottom: 12px; padding: 8px 12px; background: #F0FDF4; border: 1.5px solid #16A34A; border-radius: 8px; display: flex; justify-content: space-between; align-items: center;">
                    <span style="font-weight: bold; color: #15803D; font-size: 12.5px;">📄 Form 1 Tour Diary - PDF Export</span>
                    <button onclick="safeExportPdf()" style="background: #15803D; color: #FFF; border: none; padding: 6px 14px; border-radius: 6px; font-weight: bold; cursor: pointer; font-size: 12px;">📥 Download PDF</button>
                </div>
                <div id="report-content">
                <div class="header-title">நாள் காட்டி $monthTa</div>
                <div class="header-sub">${officer.name} ${officer.designation} ${officer.headquarters} ${officer.district}</div>
                
                <table>
                    <thead>
                        <tr>
                            <th colspan="3">DEPARTURE</th>
                            <th colspan="3">ARRIVAL</th>
                            <th rowspan="2" style="width:16%;">purpose of<br>journey</th>
                            <th rowspan="2" style="width:10%;">kind of<br>journey</th>
                            <th rowspan="2" style="width:7%;">No. of<br>k.m.s</th>
                        </tr>
                        <tr>
                            <th style="width:15%;">station</th>
                            <th style="width:10%;">date</th>
                            <th style="width:10%;">hour</th>
                            <th style="width:18%;">station</th>
                            <th style="width:10%;">date</th>
                            <th style="width:10%;">hour</th>
                        </tr>
                        <tr class="num-header">
                            <th>1</th><th>2</th><th>3</th><th>4</th><th>5</th><th>6</th><th>7</th><th>8</th><th>9</th>
                        </tr>
                    </thead>
                    <tbody>
        """.trimIndent())

        for (entry in entries) {
            if (entry.isNonTravel) {
                sb.append("""
                    <tr class="holiday-row">
                        <td class="col-left">${entry.departureStation}</td>
                        <td>${entry.departureDate}</td>
                        <td>-</td>
                        <td class="col-left">${entry.arrivalStation}</td>
                        <td>${entry.arrivalDate}</td>
                        <td>-</td>
                        <td>${entry.purposeOfJourney}</td>
                        <td>-</td>
                        <td>-</td>
                    </tr>
                """.trimIndent())
            } else {
                sb.append("""
                    <tr>
                        <td class="col-left">${entry.departureStation}</td>
                        <td>${entry.departureDate}</td>
                        <td>${entry.departureHour}</td>
                        <td class="col-left">${entry.arrivalStation}</td>
                        <td>${entry.arrivalDate}</td>
                        <td>${entry.arrivalHour}</td>
                        <td>${entry.purposeOfJourney}</td>
                        <td>${entry.kindOfJourney}</td>
                        <td>${if (entry.distanceKm > 0) entry.distanceKm else ""}</td>
                    </tr>
                """.trimIndent())
            }
        }

        val totalKms = entries.filter { !it.isNonTravel }.sumOf { it.distanceKm }
        sb.append("""
                    <tr style="font-weight: bold; background: #eee;">
                        <td colspan="8" style="text-align: right; padding-right: 15px;">மொத்த கி.மீ (TOTAL KM)</td>
                        <td>$totalKms</td>
                    </tr>
                </tbody>
            </table>
            
            <table style="border: none; margin-top: 40px; width: 100%;">
                <tr style="border: none;">
                    <td style="border: none; text-align: left; width: 50%;">
                        தேதி: ________________<br>
                        இடம்: இளையான்குடி
                    </td>
                    <td style="border: none; text-align: right; width: 50%;">
                        <b>${officer.name}</b><br>
                        ${officer.designation}<br>
                        இளையான்குடி
                    </td>
                </tr>
            </table>
            </div>
            </body>
            </html>
        """.trimIndent())

        return sb.toString()
    }

    fun generateForm2TaBillHtml(
        officer: OfficerProfile,
        monthYear: String,
        entries: List<TourEntry>
    ): String {
        val monthBillHeader = DateUtils.getBillMonthHeader(monthYear)
        val englishMonth = DateUtils.getEnglishMonthDisplay(monthYear)
        val basicPayFormatted = String.format("%.0f", officer.basicPay)
        val travelEntries = entries.filter { !it.isNonTravel }

        val totalKm = travelEntries.sumOf { it.distanceKm }
        val totalBusFare = travelEntries.sumOf { it.busFare }
        val totalDaAmount = travelEntries.sumOf { it.daAmount }
        val totalTerminal17a = travelEntries.sumOf { it.terminalCharge17a }
        val totalTerminal17b = travelEntries.sumOf { it.terminalCharge17b }
        val totalIncidental = travelEntries.sumOf { it.incidentalCharges }
        val grandTotal = travelEntries.sumOf { it.grandTotal }

        val sb = StringBuilder()

        sb.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>TA Bill - $monthBillHeader</title>
                <style>
                    @page { size: A4 landscape; margin: 6mm; }
                    body { font-family: 'Noto Sans Tamil', 'Segoe UI', Arial, sans-serif; font-size: 9.5px; width: 100%; margin: 0; padding: 10px; background-color: #f8fafc; color: #000; box-sizing: border-box; }
                    .bill-header-box { border: 1.5px solid #000; padding: 6px 10px; margin-bottom: 6px; }
                    .top-table { width: 100%; border: none; font-size: 10px; font-weight: bold; }
                    .top-table td { border: none; padding: 2px 0; }
                    table.ta-table { width: 100%; border-collapse: collapse; margin-top: 4px; }
                    table.ta-table th, table.ta-table td { border: 1px solid #000; padding: 3px 2px; text-align: center; font-size: 9px; }
                    table.ta-table th { background-color: #f0f0f0; font-weight: bold; font-size: 8.5px; }
                    .col-left { text-align: left; padding-left: 3px; }
                    .col-right { text-align: right; padding-right: 3px; }
                    .num-row th { font-size: 8px; font-weight: normal; background-color: #fafafa; }
                    .total-row { font-weight: bold; background-color: #e8e8e8; }
                    .cert-box { margin-top: 15px; font-size: 9px; line-height: 1.4; border: 1px solid #999; padding: 8px; }
                    @media print {
                        @page { size: A4 landscape; margin: 6mm; }
                        body { width: 100%; margin: 0; padding: 0; background-color: #fff; }
                        .no-print { display: none !important; }
                    }
                </style>
                <script src="https://cdnjs.cloudflare.com/ajax/libs/html2pdf.js/0.10.1/html2pdf.bundle.min.js"></script>
                <script>
                    function safeExportPdf() {
                        try {
                            var el = document.getElementById('report-content-ta') || document.body;
                            var opt = {
                                margin: [6, 6, 6, 6],
                                filename: 'TA_Bill_${monthYear.replace("-", "_")}.pdf',
                                image: { type: 'jpeg', quality: 0.98 },
                                html2canvas: { scale: 2, useCORS: true },
                                jsPDF: { unit: 'mm', format: 'a4', orientation: 'landscape' }
                            };
                            if (window.html2pdf) {
                                html2pdf().set(opt).from(el).save();
                            } else if (typeof window.print === 'function') {
                                window.print();
                            }
                        } catch (e) {
                            console.error("PDF export error: ", e);
                        }
                    }
                </script>
            </head>
            <body>
                <div class="no-print" style="margin-bottom: 10px; padding: 8px 12px; background: #F0FDF4; border: 1.5px solid #16A34A; border-radius: 8px; display: flex; justify-content: space-between; align-items: center;">
                    <span style="font-weight: bold; color: #15803D; font-size: 12px;">📄 Form 2 Travelling Allowance (TA) Bill - PDF Export</span>
                    <button onclick="safeExportPdf()" style="background: #15803D; color: #FFF; border: none; padding: 6px 14px; border-radius: 6px; font-weight: bold; cursor: pointer; font-size: 12px;">📥 Download PDF</button>
                </div>
                <div id="report-content-ta">
                <div class="bill-header-box">
                    <table class="top-table">
                        <tr>
                            <td style="width: 75%;">Travelling Allowance Bill of the <b><u>$englishMonth ($monthBillHeader)</u></b> Establishment of ${officer.designation} Ilayankudi,Sivagangai Dt</td>
                            <td style="width: 25%; text-align: right;">BASICPAY-Rs: <b>$basicPayFormatted</b></td>
                        </tr>
                        <tr>
                            <td colspan="2" style="font-size: 10.5px; padding-top: 2px;">Travelling allowance Bill of <b>${officer.name}, ${officer.shortDesignation}, ILAYANKUDI, SIVAGANGAI DT.</b></td>
                        </tr>
                    </table>
                </div>
                
                <table class="ta-table">
                    <thead>
                        <tr>
                            <th colspan="9" style="background:#ddd;">PARTICULARS OF JOURNEY AND HALTS</th>
                            <th colspan="3">RAILWAY / STEAMER</th>
                            <th colspan="2">ROAD & BUS</th>
                            <th colspan="2">DAILY ALLOWANCE</th>
                            <th colspan="2">TERMINAL CHARGES</th>
                            <th rowspan="2">Inci-<br>dental</th>
                            <th rowspan="2" style="background:#ffe082;">Grand<br>Total</th>
                            <th rowspan="2">Remarks</th>
                        </tr>
                        <tr>
                            <!-- 1 to 3 Departure -->
                            <th>1. Dep. Station</th><th>2. Dep. Date</th><th>3. Dep. Hour</th>
                            <!-- 4 to 6 Arrival -->
                            <th>4. Arr. Station</th><th>5. Arr. Date</th><th>6. Arr. Hour</th>
                            <!-- 7, 8 -->
                            <th>7. Purpose of Journey</th>
                            <th>8. Kind of Journey</th>
                            <!-- 9, 10, 11 Rail -->
                            <th>9. Class</th><th>10. No. of fares</th><th>11. Amount</th>
                            <!-- 12, 13, 14 Road & Bus -->
                            <th>12. Road Dist</th><th>13. Rate</th><th>14. Bus Fare</th>
                            <!-- 15, 16 DA -->
                            <th>15. DA Rate</th><th>16. DA Amount</th>
                            <!-- 17a, 17b -->
                            <th>17(a)</th><th>17(b)</th>
                        </tr>
                        <tr class="num-row">
                            <th>1</th><th>2</th><th>3</th><th>4</th><th>5</th><th>6</th><th>7</th><th>8</th><th>9</th>
                            <th>10</th><th>11</th><th>12</th><th>13</th><th>14</th><th>15</th><th>16</th><th>17(a)</th><th>17(b)</th><th>18</th><th>19</th><th>20</th>
                        </tr>
                    </thead>
                    <tbody>
        """.trimIndent())

        val mainTours = if (entries.any { it.isReturnLeg }) {
            entries.filter { !it.isReturnLeg }
        } else {
            entries
        }

        var htmlTotalKm = 0
        var htmlTotalBusFare = 0.0
        var htmlTotalDaAmount = 0.0
        var htmlTotalTerminal17a = 0.0
        var htmlTotalTerminal17b = 0.0
        var htmlTotalIncidental = 0.0
        var htmlGrandTotal = 0.0

        for (tour in mainTours) {
            val depStation = tour.departureStation.ifEmpty { "தலைமையிடம்" }
            val depDate = tour.departureDate
            val depHour = if (tour.isNonTravel) "" else tour.departureHour.ifEmpty { "08:00 AM" }
            val arrStation = tour.arrivalStation.ifEmpty { "தலைமையிடம்" }
            val arrDate = tour.arrivalDate.ifEmpty { tour.departureDate }
            val arrHour = if (tour.isNonTravel) "" else tour.arrivalHour.ifEmpty { "09:00 AM" }
            val purpose = if (tour.isNonTravel) {
                tour.purposeOfJourney.ifEmpty { tour.nonTravelType }
            } else {
                tour.purposeOfJourney
            }
            val mode = if (tour.isNonTravel) "" else tour.kindOfJourney.ifEmpty { "பேருந்து" }
            val railClass = tour.railClass
            val noOfFares = tour.railNoOfFares
            val railAmount = if (tour.railAmount > 0) String.format(Locale.US, "%.0f", tour.railAmount) else ""
            val distance = if (tour.isNonTravel || tour.distanceKm <= 0) "" else "${tour.distanceKm}"
            val rate = ""
            val fare = if (tour.isNonTravel || tour.busFare <= 0.0) "" else String.format(Locale.US, "%.0f", tour.busFare)
            val daRate = if (tour.isNonTravel || tour.daRate <= 0.0) "" else String.format(Locale.US, "%.0f", tour.daRate)
            val daAmount = if (tour.isNonTravel || tour.daAmount <= 0.0) "" else String.format(Locale.US, "%.0f", tour.daAmount)
            val terminalA = if (tour.isNonTravel || tour.terminalCharge17a <= 0.0) "" else String.format(Locale.US, "%.0f", tour.terminalCharge17a)
            val terminalB = if (tour.isNonTravel || tour.terminalCharge17b <= 0.0) "" else String.format(Locale.US, "%.0f", tour.terminalCharge17b)
            val incid = if (tour.isNonTravel || tour.incidentalCharges <= 0.0) "0" else String.format(Locale.US, "%.0f", tour.incidentalCharges)
            val total = if (tour.isNonTravel || tour.grandTotal <= 0.0) "" else String.format(Locale.US, "%.0f", tour.grandTotal)
            val remarks = if (tour.remarks.isNotBlank()) tour.remarks else if (tour.isNonTravel) tour.nonTravelType else ""

            sb.append("""
                <tr>
                    <td class="col-left">$depStation</td>
                    <td>$depDate</td>
                    <td>$depHour</td>
                    <td class="col-left">$arrStation</td>
                    <td>$arrDate</td>
                    <td>$arrHour</td>
                    <td class="col-left">$purpose</td>
                    <td>$mode</td>
                    <td>$railClass</td>
                    <td>$noOfFares</td>
                    <td>$railAmount</td>
                    <td>$distance</td>
                    <td>$rate</td>
                    <td>$fare</td>
                    <td>$daRate</td>
                    <td>$daAmount</td>
                    <td>$terminalA</td>
                    <td>$terminalB</td>
                    <td>$incid</td>
                    <td style="font-weight:bold; background:#fff9c4;">$total</td>
                    <td>$remarks</td>
                </tr>
            """.trimIndent())

            if (!tour.isNonTravel) {
                htmlTotalKm += tour.distanceKm
                htmlTotalBusFare += tour.busFare
                htmlTotalDaAmount += tour.daAmount
                htmlTotalTerminal17a += tour.terminalCharge17a
                htmlTotalTerminal17b += tour.terminalCharge17b
                htmlTotalIncidental += tour.incidentalCharges
                htmlGrandTotal += tour.grandTotal

                // Return Trip
                val isHoliday = tour.nonTravelType.contains("விடுமுறை") || tour.nonTravelType.contains("Holiday") ||
                    tour.nonTravelType.contains("தற்செயல்") || tour.nonTravelType.contains("CL") ||
                    tour.purposeOfJourney.contains("CL") || tour.purposeOfJourney.contains("விடுமுறை")
                val isOfficeWork = tour.nonTravelType.contains("அலுவலக") || tour.nonTravelType.contains("Office") ||
                    tour.purposeOfJourney.contains("அலுவலக")

                if (!isHoliday && !isOfficeWork) {
                    val pairedReturn = entries.find {
                        it.isReturnLeg && ((it.tripGroupId.isNotBlank() && it.tripGroupId == tour.tripGroupId) || (it.dayOfMonth == tour.dayOfMonth))
                    }
                    val returnDepStation = pairedReturn?.departureStation?.ifEmpty { arrStation } ?: arrStation
                    val returnDepHour = pairedReturn?.departureHour?.ifEmpty { "04:10 PM" } ?: "04:10 PM"
                    val returnArrStation = pairedReturn?.arrivalStation?.ifEmpty { depStation } ?: depStation
                    val returnArrHour = pairedReturn?.arrivalHour?.ifEmpty { "05:00 PM" } ?: "05:00 PM"

                    val returnFareVal = if (pairedReturn != null && pairedReturn.busFare > 0) pairedReturn.busFare else tour.busFare
                    val returnFare = if (returnFareVal > 0.0) String.format(Locale.US, "%.0f", returnFareVal) else ""

                    val termAVal = if (pairedReturn != null && pairedReturn.terminalCharge17a > 0) pairedReturn.terminalCharge17a else (if (tour.terminalCharge17a > 0) tour.terminalCharge17a else 20.0)
                    val termA = String.format(Locale.US, "%.0f", termAVal)

                    val termBVal = if (pairedReturn != null && pairedReturn.terminalCharge17b > 0) pairedReturn.terminalCharge17b else (if (tour.terminalCharge17b > 0) tour.terminalCharge17b else 20.0)
                    val termB = String.format(Locale.US, "%.0f", termBVal)

                    val fareNum = returnFare.toDoubleOrNull() ?: 0.0
                    val termANum = termA.toDoubleOrNull() ?: 20.0
                    val termBNum = termB.toDoubleOrNull() ?: 20.0
                    val returnTotalVal = fareNum + termANum + termBNum
                    val returnTotal = if (returnTotalVal > 0.0) String.format(Locale.US, "%.0f", returnTotalVal) else ""

                    sb.append("""
                        <tr>
                            <td class="col-left">$returnDepStation</td>
                            <td>$depDate</td>
                            <td>$returnDepHour</td>
                            <td class="col-left">$returnArrStation</td>
                            <td>$depDate</td>
                            <td>$returnArrHour</td>
                            <td class="col-left"></td>
                            <td>$mode</td>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td>$distance</td>
                            <td></td>
                            <td>$returnFare</td>
                            <td></td>
                            <td>0</td>
                            <td>$termA</td>
                            <td>$termB</td>
                            <td>0</td>
                            <td style="font-weight:bold; background:#fff9c4;">$returnTotal</td>
                            <td></td>
                        </tr>
                    """.trimIndent())

                    htmlTotalKm += tour.distanceKm
                    htmlTotalBusFare += fareNum
                    htmlTotalTerminal17a += termANum
                    htmlTotalTerminal17b += termBNum
                    htmlGrandTotal += returnTotalVal
                }
            }
        }

        // Totals Row
        sb.append("""
                <tr class="total-row">
                    <td colspan="11" style="text-align: right; padding-right: 10px;"><b>TOTAL</b></td>
                    <td><b>$htmlTotalKm</b></td>
                    <td></td>
                    <td><b>${String.format(Locale.US, "%.0f", htmlTotalBusFare)}</b></td>
                    <td></td>
                    <td><b>${String.format(Locale.US, "%.0f", htmlTotalDaAmount)}</b></td>
                    <td><b>${String.format(Locale.US, "%.0f", htmlTotalTerminal17a)}</b></td>
                    <td><b>${String.format(Locale.US, "%.0f", htmlTotalTerminal17b)}</b></td>
                    <td><b>${String.format(Locale.US, "%.0f", htmlTotalIncidental)}</b></td>
                    <td style="background:#ffeb3b; font-size:10px;"><b>₹${String.format(Locale.US, "%.0f", htmlGrandTotal)}</b></td>
                    <td></td>
                </tr>
            </tbody>
            </table>
            
            <div class="cert-box">
                <b>CERTIFICATE:</b> Certified that the journeys were performed solely on official government duty for public service and inspection of schools/meetings, and the claims are strictly in accordance with Tamil Nadu Travelling Allowance Rules.
                <table style="width: 100%; border: none; margin-top: 25px;">
                    <tr style="border: none;">
                        <td style="border: none; text-align: left; width: 33%;">
                            Station: Ilayankudi<br>
                            Date: ____________
                        </td>
                        <td style="border: none; text-align: center; width: 34%;">
                            Passed for Payment: <b>Rs. ${String.format("%.0f", grandTotal)}/-</b><br>
                            (Rupees in words: ________________________)
                        </td>
                        <td style="border: none; text-align: right; width: 33%;">
                            <b>${officer.name}</b><br>
                            ${officer.designation}<br>
                            Ilayankudi
                        </td>
                    </tr>
                </table>
            </div>
            </div>
            </body>
            </html>
        """.trimIndent())

        return sb.toString()
    }

    fun printHtmlDocument(context: Context, htmlContent: String, jobName: String, isLandscape: Boolean = false) {
        try {
            val webView = WebView(context)
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    try {
                        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                        if (printManager != null) {
                            val printAdapter = webView.createPrintDocumentAdapter(jobName)
                            val mediaSize = if (isLandscape) {
                                PrintAttributes.MediaSize.ISO_A4.asLandscape()
                            } else {
                                PrintAttributes.MediaSize.ISO_A4.asPortrait()
                            }
                            val printAttributes = PrintAttributes.Builder()
                                .setMediaSize(mediaSize)
                                .setResolution(PrintAttributes.Resolution("id", "print", 300, 300))
                                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                                .build()
                            printManager.print(jobName, printAdapter, printAttributes)
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }

                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    // Safe error handling to avoid any webview crashes
                }
            }
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun openOrSharePdf(context: Context, pdfFile: File, title: String, isTamil: Boolean) {
        try {
            // Also copy to external Downloads directory so user can directly locate it
            try {
                val dlDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                if (dlDir != null) {
                    if (!dlDir.exists()) dlDir.mkdirs()
                    val dest = File(dlDir, pdfFile.name)
                    pdfFile.copyTo(dest, overwrite = true)
                }
            } catch (e: Throwable) {
                // Fallback to primary cache file
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "$title - PDF Document")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserTitle = if (isTamil) "PDF பதிவிறக்கம் / பகிர்வு (Download PDF)" else "Download / Open PDF"
            val chooser = Intent.createChooser(sendIntent, chooserTitle)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun generateForm1DiaryPdf(
        context: Context,
        officer: OfficerProfile,
        monthYear: String,
        entries: List<TourEntry>
    ): File {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()
        val safeMonth = monthYear.replace("-", "_")
        val pdfFile = File(exportDir, "Tour_Diary_${safeMonth}.pdf")

        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 Portrait width
        val pageHeight = 842 // A4 Portrait height
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 8f
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val subTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            strokeWidth = 0.6f
            style = Paint.Style.STROKE
        }
        val headerBgPaint = Paint().apply {
            color = Color.parseColor("#EAEAEA")
            style = Paint.Style.FILL
        }
        val holidayBgPaint = Paint().apply {
            color = Color.parseColor("#F9F9F9")
            style = Paint.Style.FILL
        }

        val leftMargin = 18f
        // 9 columns: Dep(Station 76, Date 48, Hour 44) + Arr(Station 76, Date 48, Hour 44) + Purpose 137 + Kind 54 + Km 32 = 559f
        val colWidths = floatArrayOf(76f, 48f, 44f, 76f, 48f, 44f, 137f, 54f, 32f)
        val totalTableWidth = colWidths.sum()

        fun drawHeader(canvas: Canvas, currentY: Float): Float {
            var y = currentY
            val monthTa = DateUtils.getTamilMonthDisplay(monthYear)
            canvas.drawText("நாள் காட்டி (TOUR DIARY) - $monthTa", pageWidth / 2f, y, titlePaint)
            y += 16f
            canvas.drawText("${officer.name}, ${officer.designation}, ${officer.headquarters}, ${officer.district}", pageWidth / 2f, y, subTitlePaint)
            y += 18f

            val hHeight = 44f
            canvas.drawRect(leftMargin, y, leftMargin + totalTableWidth, y + hHeight, headerBgPaint)
            canvas.drawRect(leftMargin, y, leftMargin + totalTableWidth, y + hHeight, linePaint)

            val depWidth = colWidths[0] + colWidths[1] + colWidths[2]
            val arrWidth = colWidths[3] + colWidths[4] + colWidths[5]
            canvas.drawText("DEPARTURE", leftMargin + depWidth / 2f - 26f, y + 12f, boldPaint)
            canvas.drawText("ARRIVAL", leftMargin + depWidth + arrWidth / 2f - 20f, y + 12f, boldPaint)
            canvas.drawText("Purpose of journey", leftMargin + depWidth + arrWidth + 6f, y + 18f, boldPaint)
            canvas.drawText("Kind", leftMargin + depWidth + arrWidth + colWidths[6] + 8f, y + 18f, boldPaint)
            canvas.drawText("No. km", leftMargin + depWidth + arrWidth + colWidths[6] + colWidths[7] + 2f, y + 18f, boldPaint)

            val subY = y + 18f
            canvas.drawLine(leftMargin, subY, leftMargin + depWidth + arrWidth, subY, linePaint)

            var curX = leftMargin
            val subLabels = arrayOf("station", "date", "hour", "station", "date", "hour")
            for (i in 0 until 6) {
                canvas.drawText(subLabels[i], curX + 3f, subY + 11f, textPaint)
                curX += colWidths[i]
            }

            val numY = y + 32f
            canvas.drawLine(leftMargin, numY, leftMargin + totalTableWidth, numY, linePaint)
            curX = leftMargin
            for (i in 1..9) {
                canvas.drawText("$i", curX + colWidths[i - 1] / 2f - 2.5f, numY + 9f, textPaint)
                curX += colWidths[i - 1]
            }

            curX = leftMargin
            for (w in colWidths) {
                canvas.drawLine(curX, y, curX, y + hHeight, linePaint)
                curX += w
            }
            canvas.drawLine(curX, y, curX, y + hHeight, linePaint)

            return y + hHeight
        }

        var y = drawHeader(canvas, 32f)

        val rowHeight = 16f
        for (entry in entries) {
            if (y + rowHeight > pageHeight - 65f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = drawHeader(canvas, 32f)
            }

            if (entry.isNonTravel) {
                canvas.drawRect(leftMargin, y, leftMargin + totalTableWidth, y + rowHeight, holidayBgPaint)
            }
            canvas.drawRect(leftMargin, y, leftMargin + totalTableWidth, y + rowHeight, linePaint)

            var curX = leftMargin
            val values = if (entry.isNonTravel) {
                arrayOf(
                    entry.departureStation,
                    entry.departureDate,
                    "-",
                    entry.arrivalStation,
                    entry.arrivalDate,
                    "-",
                    entry.purposeOfJourney,
                    "-",
                    "-"
                )
            } else {
                arrayOf(
                    entry.departureStation,
                    entry.departureDate,
                    entry.departureHour,
                    entry.arrivalStation,
                    entry.arrivalDate,
                    entry.arrivalHour,
                    entry.purposeOfJourney,
                    entry.kindOfJourney,
                    if (entry.distanceKm > 0) "${entry.distanceKm}" else ""
                )
            }

            for (i in values.indices) {
                canvas.drawLine(curX, y, curX, y + rowHeight, linePaint)
                val v = values[i]
                val maxLen = (colWidths[i] / 4.8f).toInt()
                val printV = if (v.length > maxLen) v.take(maxLen - 2) + ".." else v
                canvas.drawText(printV, curX + 2.5f, y + 11.5f, textPaint)
                curX += colWidths[i]
            }
            canvas.drawLine(curX, y, curX, y + rowHeight, linePaint)

            y += rowHeight
        }

        val totalKms = entries.filter { !it.isNonTravel }.sumOf { it.distanceKm }
        canvas.drawRect(leftMargin, y, leftMargin + totalTableWidth, y + rowHeight, headerBgPaint)
        canvas.drawRect(leftMargin, y, leftMargin + totalTableWidth, y + rowHeight, linePaint)
        canvas.drawText("TOTAL KMs (மொத்த கி.மீ):", leftMargin + totalTableWidth - 145f, y + 11.5f, boldPaint)
        canvas.drawText("$totalKms", leftMargin + totalTableWidth - 26f, y + 11.5f, boldPaint)
        y += rowHeight + 25f

        canvas.drawText("Date: ________________", leftMargin + 8f, y, textPaint)
        canvas.drawText("Station: ${officer.headquarters}", leftMargin + 8f, y + 14f, textPaint)

        canvas.drawText(officer.name, leftMargin + totalTableWidth - 150f, y, boldPaint)
        canvas.drawText(officer.designation, leftMargin + totalTableWidth - 150f, y + 14f, textPaint)
        canvas.drawText(officer.headquarters, leftMargin + totalTableWidth - 150f, y + 26f, textPaint)

        pdfDocument.finishPage(page)

        pdfFile.outputStream().use { fos ->
            pdfDocument.writeTo(fos)
        }
        pdfDocument.close()

        return pdfFile
    }

    fun generateForm2TaBillPdf(
        context: Context,
        officer: OfficerProfile,
        monthYear: String,
        entries: List<TourEntry>
    ): File {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()
        val safeMonth = monthYear.replace("-", "_")
        val pdfFile = File(exportDir, "TA_Bill_${safeMonth}.pdf")

        val pdfDocument = PdfDocument()
        val pageWidth = 842
        val pageHeight = 595
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 6.2f
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 6.2f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val headerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 5.6f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val headerNumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 5.8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            strokeWidth = 0.5f
            style = Paint.Style.STROKE
        }
        val headerBgPaint = Paint().apply {
            color = Color.parseColor("#ECEFF1")
            style = Paint.Style.FILL
        }
        val numBgPaint = Paint().apply {
            color = Color.parseColor("#CFD8DC")
            style = Paint.Style.FILL
        }
        val totalBgPaint = Paint().apply {
            color = Color.parseColor("#FFF9C4")
            style = Paint.Style.FILL
        }

        fun fitText(text: String, maxWidth: Float, paint: Paint): String {
            if (paint.measureText(text) <= maxWidth) return text
            var truncated = text
            while (truncated.isNotEmpty() && paint.measureText(truncated + "…") > maxWidth) {
                truncated = truncated.dropLast(1)
            }
            return if (truncated.isEmpty()) "" else truncated + "…"
        }

        val leftMargin = 19.5f
        val colWidths = floatArrayOf(
            58f, // 1. Dep. Station
            44f, // 2. Dep. Date
            38f, // 3. Dep. Hour
            58f, // 4. Arr. Station
            44f, // 5. Arr. Date
            38f, // 6. Arr. Hour
            66f, // 7. Purpose of Journey
            38f, // 8. Kind of Journey
            25f, // 9. Class
            25f, // 10. No. of Fares
            28f, // 11. Amount
            28f, // 12. Road Distance (km)
            24f, // 13. Rate
            33f, // 14. Bus Fare
            33f, // 15. DA Rate
            36f, // 16. DA Amount
            32f, // 17(a). Terminal Charges
            32f, // 17(b). Terminal Charges
            27f, // 18. Incidental Charges
            42f, // 19. TOTAL
            52f  // 20. Remarks
        )
        val totalTableWidth = colWidths.sum()

        val headerTitles = arrayOf(
            "Dep. Station", "Dep. Date", "Dep. Hour",
            "Arr. Station", "Arr. Date", "Arr. Hour",
            "Purpose of Journey", "Kind",
            "Class", "No. fares", "Amount",
            "Road Dist", "Rate", "Bus Fare",
            "DA Rate", "DA Amount",
            "17(a) Term", "17(b) Term", "Incid.",
            "TOTAL", "Remarks"
        )
        val headerNumbers = arrayOf(
            "1", "2", "3",
            "4", "5", "6",
            "7", "8",
            "9", "10", "11",
            "12", "13", "14",
            "15", "16",
            "17(a)", "17(b)", "18",
            "19", "20"
        )

        fun drawHeader(canvas: Canvas, currentY: Float): Float {
            var y = currentY
            val monthBillHeader = DateUtils.getBillMonthHeader(monthYear)
            val englishMonth = DateUtils.getEnglishMonthDisplay(monthYear)
            val basicPayFormatted = String.format(Locale.US, "%.0f", officer.basicPay)

            canvas.drawRect(leftMargin, y, leftMargin + totalTableWidth, y + 24f, linePaint)
            canvas.drawText(
                "Travelling Allowance Bill of the $englishMonth ($monthBillHeader) Establishment of ${officer.designation} ${officer.headquarters}",
                leftMargin + 6f,
                y + 10f,
                boldPaint
            )
            canvas.drawText("BASIC PAY - Rs: $basicPayFormatted", leftMargin + totalTableWidth - 140f, y + 10f, boldPaint)
            canvas.drawText("Travelling allowance Bill of ${officer.name}, ${officer.shortDesignation}, ${officer.headquarters} | Sivagangai Dt", leftMargin + 6f, y + 19f, textPaint)
            y += 28f

            val hHeight1 = 15f
            val hHeight2 = 12f
            val totalHHeight = hHeight1 + hHeight2

            canvas.drawRect(leftMargin, y, leftMargin + totalTableWidth, y + hHeight1, headerBgPaint)
            canvas.drawRect(leftMargin, y + hHeight1, leftMargin + totalTableWidth, y + totalHHeight, numBgPaint)
            canvas.drawRect(leftMargin, y, leftMargin + totalTableWidth, y + totalHHeight, linePaint)
            canvas.drawLine(leftMargin, y + hHeight1, leftMargin + totalTableWidth, y + hHeight1, linePaint)

            var curX = leftMargin
            for (i in headerTitles.indices) {
                canvas.drawLine(curX, y, curX, y + totalHHeight, linePaint)
                val title = fitText(headerTitles[i], colWidths[i] - 2f, headerTitlePaint)
                canvas.drawText(title, curX + 1.5f, y + 10.5f, headerTitlePaint)

                val numStr = headerNumbers[i]
                val numW = headerNumPaint.measureText(numStr)
                val numX = curX + (colWidths[i] - numW) / 2f
                canvas.drawText(numStr, numX, y + hHeight1 + 9f, headerNumPaint)

                curX += colWidths[i]
            }
            canvas.drawLine(curX, y, curX, y + totalHHeight, linePaint)

            return y + totalHHeight
        }

        val mainTours = if (entries.any { it.isReturnLeg }) {
            entries.filter { !it.isReturnLeg }
        } else {
            entries
        }

        class Form2PdfRow(
            val values: Array<String>
        )

        val rowsToDraw = mutableListOf<Form2PdfRow>()
        var pdfTotalKm = 0
        var pdfTotalBusFare = 0.0
        var pdfTotalDaAmount = 0.0
        var pdfTotalTerminal17a = 0.0
        var pdfTotalTerminal17b = 0.0
        var pdfTotalIncidental = 0.0
        var pdfGrandTotal = 0.0

        for (tour in mainTours) {
            val depStation = tour.departureStation.ifEmpty { "தலைமையிடம்" }
            val depDate = tour.departureDate
            val depHour = if (tour.isNonTravel) "" else tour.departureHour.ifEmpty { "08:00 AM" }
            val arrStation = tour.arrivalStation.ifEmpty { "தலைமையிடம்" }
            val arrDate = tour.arrivalDate.ifEmpty { tour.departureDate }
            val arrHour = if (tour.isNonTravel) "" else tour.arrivalHour.ifEmpty { "09:00 AM" }
            val purpose = if (tour.isNonTravel) {
                tour.purposeOfJourney.ifEmpty { tour.nonTravelType }
            } else {
                tour.purposeOfJourney
            }
            val mode = if (tour.isNonTravel) "" else tour.kindOfJourney.ifEmpty { "பேருந்து" }
            val railClass = tour.railClass
            val noOfFares = tour.railNoOfFares
            val railAmount = if (tour.railAmount > 0) String.format(Locale.US, "%.0f", tour.railAmount) else ""
            val distance = if (tour.isNonTravel || tour.distanceKm <= 0) "" else "${tour.distanceKm}"
            val rate = ""
            val fare = if (tour.isNonTravel || tour.busFare <= 0.0) "" else String.format(Locale.US, "%.0f", tour.busFare)
            val daRate = if (tour.isNonTravel || tour.daRate <= 0.0) "" else String.format(Locale.US, "%.0f", tour.daRate)
            val daAmount = if (tour.isNonTravel || tour.daAmount <= 0.0) "" else String.format(Locale.US, "%.0f", tour.daAmount)
            val terminalA = if (tour.isNonTravel || tour.terminalCharge17a <= 0.0) "" else String.format(Locale.US, "%.0f", tour.terminalCharge17a)
            val terminalB = if (tour.isNonTravel || tour.terminalCharge17b <= 0.0) "" else String.format(Locale.US, "%.0f", tour.terminalCharge17b)
            val incid = if (tour.isNonTravel || tour.incidentalCharges <= 0.0) "0" else String.format(Locale.US, "%.0f", tour.incidentalCharges)
            val total = if (tour.isNonTravel || tour.grandTotal <= 0.0) "" else String.format(Locale.US, "%.0f", tour.grandTotal)
            val remarks = if (tour.remarks.isNotBlank()) tour.remarks else if (tour.isNonTravel) tour.nonTravelType else ""

            // Outbound row
            rowsToDraw.add(
                Form2PdfRow(
                    arrayOf(
                        depStation, depDate, depHour,
                        arrStation, arrDate, arrHour,
                        purpose, mode,
                        railClass, noOfFares, railAmount,
                        distance, rate, fare,
                        daRate, daAmount,
                        terminalA, terminalB, incid,
                        total, remarks
                    )
                )
            )

            if (!tour.isNonTravel) {
                pdfTotalKm += tour.distanceKm
                pdfTotalBusFare += tour.busFare
                pdfTotalDaAmount += tour.daAmount
                pdfTotalTerminal17a += tour.terminalCharge17a
                pdfTotalTerminal17b += tour.terminalCharge17b
                pdfTotalIncidental += tour.incidentalCharges
                pdfGrandTotal += tour.grandTotal

                val isHoliday = tour.nonTravelType.contains("விடுமுறை") || tour.nonTravelType.contains("Holiday") ||
                    tour.nonTravelType.contains("தற்செயல்") || tour.nonTravelType.contains("CL") ||
                    tour.purposeOfJourney.contains("CL") || tour.purposeOfJourney.contains("விடுமுறை")
                val isOfficeWork = tour.nonTravelType.contains("அலுவலக") || tour.nonTravelType.contains("Office") ||
                    tour.purposeOfJourney.contains("அலுவலக")

                if (!isHoliday && !isOfficeWork) {
                    val pairedReturn = entries.find {
                        it.isReturnLeg && ((it.tripGroupId.isNotBlank() && it.tripGroupId == tour.tripGroupId) || (it.dayOfMonth == tour.dayOfMonth))
                    }
                    val returnDepStation = pairedReturn?.departureStation?.ifEmpty { arrStation } ?: arrStation
                    val returnDepHour = pairedReturn?.departureHour?.ifEmpty { "04:10 PM" } ?: "04:10 PM"
                    val returnArrStation = pairedReturn?.arrivalStation?.ifEmpty { depStation } ?: depStation
                    val returnArrHour = pairedReturn?.arrivalHour?.ifEmpty { "05:00 PM" } ?: "05:00 PM"

                    val returnFareVal = if (pairedReturn != null && pairedReturn.busFare > 0) pairedReturn.busFare else tour.busFare
                    val returnFare = if (returnFareVal > 0.0) String.format(Locale.US, "%.0f", returnFareVal) else ""

                    val termAVal = if (pairedReturn != null && pairedReturn.terminalCharge17a > 0) pairedReturn.terminalCharge17a else (if (tour.terminalCharge17a > 0) tour.terminalCharge17a else 20.0)
                    val termA = String.format(Locale.US, "%.0f", termAVal)

                    val termBVal = if (pairedReturn != null && pairedReturn.terminalCharge17b > 0) pairedReturn.terminalCharge17b else (if (tour.terminalCharge17b > 0) tour.terminalCharge17b else 20.0)
                    val termB = String.format(Locale.US, "%.0f", termBVal)

                    val fareNum = returnFare.toDoubleOrNull() ?: 0.0
                    val termANum = termA.toDoubleOrNull() ?: 20.0
                    val termBNum = termB.toDoubleOrNull() ?: 20.0
                    val returnTotalVal = fareNum + termANum + termBNum
                    val returnTotal = if (returnTotalVal > 0.0) String.format(Locale.US, "%.0f", returnTotalVal) else ""

                    // Return row
                    rowsToDraw.add(
                        Form2PdfRow(
                            arrayOf(
                                returnDepStation, depDate, returnDepHour,
                                returnArrStation, depDate, returnArrHour,
                                "", mode,
                                "", "", "",
                                distance, "", returnFare,
                                "", "0",
                                termA, termB, "0",
                                returnTotal, ""
                            )
                        )
                    )

                    pdfTotalKm += tour.distanceKm
                    pdfTotalBusFare += fareNum
                    pdfTotalTerminal17a += termANum
                    pdfTotalTerminal17b += termBNum
                    pdfGrandTotal += returnTotalVal
                }
            }
        }

        var y = drawHeader(canvas, 20f)
        val rowHeight = 14f

        for (row in rowsToDraw) {
            if (y + rowHeight > pageHeight - 75f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = drawHeader(canvas, 20f)
            }

            canvas.drawRect(leftMargin, y, leftMargin + totalTableWidth, y + rowHeight, linePaint)

            var curX = leftMargin
            for (i in row.values.indices) {
                canvas.drawLine(curX, y, curX, y + rowHeight, linePaint)
                val v = row.values[i]
                val isTotalCell = (i == 19)
                val printV = fitText(v, colWidths[i] - 2f, if (isTotalCell) boldPaint else textPaint)
                canvas.drawText(printV, curX + 1.5f, y + 10f, if (isTotalCell) boldPaint else textPaint)
                curX += colWidths[i]
            }
            canvas.drawLine(curX, y, curX, y + rowHeight, linePaint)

            y += rowHeight
        }

        if (y + rowHeight > pageHeight - 75f) {
            pdfDocument.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            y = drawHeader(canvas, 20f)
        }

        canvas.drawRect(leftMargin, y, leftMargin + totalTableWidth, y + rowHeight, totalBgPaint)
        canvas.drawRect(leftMargin, y, leftMargin + totalTableWidth, y + rowHeight, linePaint)

        val totVals = arrayOf(
            "TOTAL", "", "",
            "", "", "",
            "", "",
            "", "", "",
            "$pdfTotalKm",
            "",
            String.format(Locale.US, "%.0f", pdfTotalBusFare),
            "",
            String.format(Locale.US, "%.0f", pdfTotalDaAmount),
            String.format(Locale.US, "%.0f", pdfTotalTerminal17a),
            String.format(Locale.US, "%.0f", pdfTotalTerminal17b),
            String.format(Locale.US, "%.0f", pdfTotalIncidental),
            "₹" + String.format(Locale.US, "%.0f", pdfGrandTotal),
            ""
        )
        var curX = leftMargin
        for (i in totVals.indices) {
            canvas.drawLine(curX, y, curX, y + rowHeight, linePaint)
            val printV = fitText(totVals[i], colWidths[i] - 2f, boldPaint)
            canvas.drawText(printV, curX + 1.5f, y + 10f, boldPaint)
            curX += colWidths[i]
        }
        canvas.drawLine(curX, y, curX, y + rowHeight, linePaint)
        y += rowHeight + 12f

        canvas.drawRect(leftMargin, y, leftMargin + totalTableWidth, y + 40f, linePaint)
        canvas.drawText("CERTIFICATE: Certified that the journeys were performed solely on official government duty for public service.", leftMargin + 6f, y + 12f, textPaint)
        canvas.drawText("Passed for Payment: Rs. ${String.format(Locale.US, "%.0f", pdfGrandTotal)}/-", leftMargin + 6f, y + 25f, boldPaint)
        canvas.drawText("Station: ${officer.headquarters} | Date: _________", leftMargin + 6f, y + 35f, textPaint)
        canvas.drawText("${officer.name} (${officer.designation})", leftMargin + totalTableWidth - 190f, y + 35f, boldPaint)

        pdfDocument.finishPage(page)

        pdfFile.outputStream().use { fos ->
            pdfDocument.writeTo(fos)
        }
        pdfDocument.close()

        return pdfFile
    }

    fun exportOrPrintForm1(
        context: Context,
        officer: OfficerProfile,
        monthYear: String,
        entries: List<TourEntry>,
        isTamil: Boolean
    ): String {
        return try {
            // 1. Generate standalone HTML for browser/view fallback
            try {
                val html = generateForm1DiaryHtml(officer, monthYear, entries)
                val exportDir = File(context.cacheDir, "exports")
                if (!exportDir.exists()) exportDir.mkdirs()
                val htmlFile = File(exportDir, "Tour_Diary_${monthYear.replace("-", "_")}.html")
                htmlFile.writeText(html, Charsets.UTF_8)
            } catch (ignored: Throwable) {}

            // 2. Generate native PDF file directly on device
            val pdfFile = generateForm1DiaryPdf(context, officer, monthYear, entries)

            // 3. Trigger PDF file download / viewing / sharing intent
            openOrSharePdf(context, pdfFile, "Tour Diary $monthYear", isTamil)

            if (isTamil) {
                "PDF கோப்பு உருவாக்கப்பட்டு பதிவிறக்கப்பட்டது: ${pdfFile.name}"
            } else {
                "PDF generated and downloaded: ${pdfFile.name}"
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            if (isTamil) "PDF ஏற்றுமதி பிழை: ${e.localizedMessage}" else "PDF export error: ${e.localizedMessage}"
        }
    }

    fun exportOrPrintForm2(
        context: Context,
        officer: OfficerProfile,
        monthYear: String,
        entries: List<TourEntry>,
        isTamil: Boolean
    ): String {
        return try {
            // 1. Generate standalone HTML for browser/view fallback
            try {
                val html = generateForm2TaBillHtml(officer, monthYear, entries)
                val exportDir = File(context.cacheDir, "exports")
                if (!exportDir.exists()) exportDir.mkdirs()
                val htmlFile = File(exportDir, "TA_Bill_${monthYear.replace("-", "_")}.html")
                htmlFile.writeText(html, Charsets.UTF_8)
            } catch (ignored: Throwable) {}

            // 2. Generate native PDF file directly on device
            val pdfFile = generateForm2TaBillPdf(context, officer, monthYear, entries)

            // 3. Trigger PDF file download / viewing / sharing intent
            openOrSharePdf(context, pdfFile, "TA Bill $monthYear", isTamil)

            if (isTamil) {
                "TA Bill PDF கோப்பு உருவாக்கப்பட்டு பதிவிறக்கப்பட்டது: ${pdfFile.name}"
            } else {
                "TA Bill PDF generated and downloaded: ${pdfFile.name}"
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            if (isTamil) "PDF ஏற்றுமதி பிழை: ${e.localizedMessage}" else "PDF export error: ${e.localizedMessage}"
        }
    }

    fun shareWhatsAppSummary(
        context: Context,
        officer: OfficerProfile,
        monthYear: String,
        entries: List<TourEntry>
    ) {
        val monthTa = DateUtils.getTamilMonthDisplay(monthYear)
        val travelEntries = entries.filter { !it.isNonTravel }
        val totalKm = travelEntries.sumOf { it.distanceKm }
        val totalBusFare = travelEntries.sumOf { it.busFare }
        val totalDa = travelEntries.sumOf { it.daAmount }
        val totalTerminal = travelEntries.sumOf { it.terminalCharge17a + it.terminalCharge17b }
        val grandTotal = travelEntries.sumOf { it.grandTotal }

        val summaryText = """
📋 *பயணப்படி அறிக்கை (TA BILL SUMMARY)*
👤 *அலுவலர்:* ${officer.name}
💼 *பதவி:* ${officer.designation}
🏢 *தலைமையிடம்:* ${officer.headquarters}
📅 *மாதம்:* $monthTa
━━━━━━━━━━━━━━━━━
🚗 மொத்த பயணங்கள்: ${travelEntries.size}
📍 மொத்த தொலைவு: $totalKm கி.மீ
🚌 பேருந்து கட்டணம்: ₹${String.format("%.0f", totalBusFare)}
💰 தினப்படி (DA): ₹${String.format("%.0f", totalDa)}
🏢 முனையக் கட்டணம்: ₹${String.format("%.0f", totalTerminal)}
━━━━━━━━━━━━━━━━━
💵 *மொத்த பயணப்படி (Grand Total): ₹${String.format("%.0f", grandTotal)}*
━━━━━━━━━━━━━━━━━
_Generated via TA Bill & Tour Diary Mobile App_
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, summaryText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share TA Bill via WhatsApp / Email")
        context.startActivity(shareIntent)
    }

    /**
     * --- 1. பொதுவான CSV பதிவிறக்க செயல்பாடு (UTF-8 BOM உடன்) ---
     * தமிழ் எழுத்துக்கள் Google Sheets மற்றும் Microsoft Excel-ல் சரியாக தெரிய
     * UTF-8 BOM (\uFEFF) சேர்க்கப்பட்டு கோப்பாக சேமிக்கப்பட்டு பகிரப்படுகிறது.
     */
    fun downloadCSV(context: Context, csvContent: String, fileName: String) {
        try {
            // தமிழ் எழுத்துக்கள் Google Sheets-ல் சரியாக தெரிய UTF-8 BOM (\uFEFF) சேர்க்கப்படுகிறது
            val contentWithBom = if (csvContent.startsWith('\uFEFF')) csvContent else "\uFEFF$csvContent"
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            val file = File(exportDir, fileName)
            file.writeText(contentWithBom, Charsets.UTF_8)

            // Downloads அடைவிற்கும் நகலெடுக்கப்பட்டு உள்ளூர் சாதன சேமிப்பகம் உறுதிசெய்யப்படுகிறது
            try {
                val dlDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                if (dlDir != null) {
                    if (!dlDir.exists()) dlDir.mkdirs()
                    val dest = File(dlDir, fileName)
                    file.copyTo(dest, overwrite = true)
                }
            } catch (e: Throwable) {
                // Ignore external storage error and continue with FileProvider
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, fileName)
                putExtra(Intent.EXTRA_TEXT, "Exported $fileName in CSV format (Tamil UTF-8 BOM).")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(sendIntent, "Open / Download ($fileName)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback plain text share
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, csvContent)
                putExtra(Intent.EXTRA_SUBJECT, fileName)
            }
            val chooser = Intent.createChooser(fallbackIntent, "Export $fileName")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }

    /**
     * --- 2. படிவம் 1: நாள்காட்டி (Form 1 - 9 Columns Export) ---
     * படிவம் 1-ல் உள்ள அதே 9 நெடுவரிசைகள்:
     * 1. Departure Station
     * 2. Departure Date
     * 3. Departure Hour
     * 4. Arrival Station
     * 5. Arrival Date
     * 6. Arrival Hour
     * 7. Purpose of Journey
     * 8. Mode of Journey
     * 9. Distance (km)
     */
    fun generateForm1Csv(tourRecords: List<TourEntry>): String {
        val headers = arrayOf(
            "1. Departure Station",
            "2. Departure Date",
            "3. Departure Hour",
            "4. Arrival Station",
            "5. Arrival Date",
            "6. Arrival Hour",
            "7. Purpose of Journey",
            "8. Mode of Journey",
            "9. Distance (km)"
        )

        val rows = mutableListOf<String>()
        rows.add(headers.joinToString(",") { "\"$it\"" })

        val mainTours = if (tourRecords.any { it.isReturnLeg }) {
            tourRecords.filter { !it.isReturnLeg }
        } else {
            tourRecords
        }

        for (tour in mainTours) {
            val depStation = tour.departureStation.ifEmpty { "தலைமையிடம்" }
            val depDate = tour.departureDate
            val depHour = if (tour.isNonTravel) "" else tour.departureHour.ifEmpty { "08:00 AM" }
            val arrStation = tour.arrivalStation.ifEmpty { "தலைமையிடம்" }
            val arrDate = tour.arrivalDate.ifEmpty { tour.departureDate }
            val arrHour = if (tour.isNonTravel) "" else tour.arrivalHour.ifEmpty { "09:00 AM" }
            val purpose = if (tour.isNonTravel) {
                tour.purposeOfJourney.ifEmpty { tour.nonTravelType }
            } else {
                tour.purposeOfJourney
            }
            val mode = if (tour.isNonTravel) "" else tour.kindOfJourney.ifEmpty { "பேருந்து" }
            val distance = if (tour.isNonTravel || tour.distanceKm <= 0) "" else "${tour.distanceKm}"

            // புறப்பாடு பயணம் (Onward Journey)
            val onwardRow = arrayOf(
                depStation,
                depDate,
                depHour,
                arrStation,
                arrDate,
                arrHour,
                purpose,
                mode,
                distance
            )
            rows.add(onwardRow.joinToString(",") { escapeCsv(it) })

            // திரும்பும் பயணம் (Return Journey)
            val isHoliday = tour.isNonTravel && (
                tour.nonTravelType.contains("விடுமுறை") || tour.nonTravelType.contains("Holiday") ||
                tour.nonTravelType.contains("தற்செயல்") || tour.nonTravelType.contains("CL") ||
                tour.purposeOfJourney.contains("CL") || tour.purposeOfJourney.contains("விடுமுறை")
            )
            val isOfficeWork = tour.isNonTravel && (
                tour.nonTravelType.contains("அலுவலக") || tour.nonTravelType.contains("Office") ||
                tour.purposeOfJourney.contains("அலுவலக")
            )

            if (!tour.isNonTravel && !isHoliday && !isOfficeWork) {
                val pairedReturn = tourRecords.find {
                    it.isReturnLeg && ((it.tripGroupId.isNotBlank() && it.tripGroupId == tour.tripGroupId) || (it.dayOfMonth == tour.dayOfMonth))
                }
                val returnDepHour = pairedReturn?.departureHour?.ifEmpty { "04:10 PM" } ?: "04:10 PM"
                val returnArrHour = pairedReturn?.arrivalHour?.ifEmpty { "05:45 PM" } ?: "05:45 PM"
                val returnArrStation = pairedReturn?.arrivalStation?.ifEmpty { depStation } ?: depStation
                val returnDepStation = pairedReturn?.departureStation?.ifEmpty { arrStation } ?: arrStation

                val returnRow = arrayOf(
                    returnDepStation,
                    depDate,
                    returnDepHour,
                    returnArrStation,
                    depDate,
                    returnArrHour,
                    "", // நோக்கம் காலியாக விடப்படும்
                    mode,
                    distance
                )
                rows.add(returnRow.joinToString(",") { escapeCsv(it) })
            }
        }

        return "\uFEFF" + rows.joinToString("\r\n")
    }

    fun exportForm1ToCSV(
        context: Context,
        tourRecords: List<TourEntry>,
        fileName: String = "Form_1_Calendar.csv"
    ) {
        val csv = generateForm1Csv(tourRecords)
        downloadCSV(context, csv, fileName)
    }

    /**
     * --- 3. படிவம் 2: TA Bill (Form 2 - 20 Columns Export) ---
     * அரசு படிவம் 2-ன் நிலையான 20 நெடுவரிசைகள்:
     * 1. Dep. Station
     * 2. Dep. Date
     * 3. Dep. Hour
     * 4. Arr. Station
     * 5. Arr. Date
     * 6. Arr. Hour
     * 7. Purpose of Journey
     * 8. Kind of Journey
     * 9. Class
     * 10. No. of Fares
     * 11. Amount
     * 12. Road Distance (km)
     * 13. Rate
     * 14. Bus Fare Amount
     * 15. DA Rate
     * 16. DA Amount
     * 17(a). Terminal Charges
     * 17(b). Terminal Charges
     * 18. Incidental
     * 19. TOTAL
     * 20. Remarks
     */
    fun generateForm2Csv(
        tourRecords: List<TourEntry>,
        currentMonth: String = "",
        officer: OfficerProfile? = null
    ): String {
        val rows = mutableListOf<String>()

        // 1. அலுவலக மேல் விவரங்கள் (Top Details)
        val monthDisplay = if (currentMonth.isNotBlank()) {
            if (currentMonth.contains("-")) DateUtils.getEnglishMonthDisplay(currentMonth) else currentMonth
        } else {
            DateUtils.getEnglishMonthDisplay(DateUtils.getCurrentMonthYear())
        }

        val officerName = officer?.name?.ifBlank { "S. PAUL DAVID ROSARIO" } ?: "S. PAUL DAVID ROSARIO"
        val desig = officer?.shortDesignation?.ifBlank { "B.E.O." } ?: "B.E.O."
        val hq = "ILAYANKUDI, SIVAGANGAI DT."
        val basicPay = if (officer != null && officer.basicPay > 0) {
            String.format(Locale.US, "%.0f", officer.basicPay)
        } else {
            "100600"
        }

        // Row 1: Travelling Allowance Bill of the Establishment...
        rows.add(escapeCsv("Travelling Allowance Bill of the Establishment of வட்டார கல்வி அலுவலர் (Block Educational Officer) Ilayankudi, Sivagangai Dt for the month of $monthDisplay"))

        // Row 2: Travelling allowance Bill of S. PAUL DAVID ROSARIO, B.E.O., ILAYANKUDI, SIVAGANGAI DT.,...,BASICPAY-Rs: 100600
        val line2 = listOf(
            "Travelling allowance Bill of $officerName, $desig, $hq",
            "", "", "", "", "", "", "", "", "",
            "BASICPAY-Rs: $basicPay"
        )
        rows.add(line2.joinToString(",") { escapeCsv(it) })

        // Row 3: ஒரு வரி இடைவெளி (Empty line)
        rows.add("")

        // 2. அட்டவணைத் தலைப்புகள் (20 Columns)
        val headers = arrayOf(
            "1. Dep. Station",
            "2. Dep. Date",
            "3. Dep. Hour",
            "4. Arr. Station",
            "5. Arr. Date",
            "6. Arr. Hour",
            "7. Purpose of Journey",
            "8. Kind of Journey",
            "9. Class",
            "10. No. of Fares",
            "11. Amount",
            "12. Road Distance (km)",
            "13. Rate",
            "14. Bus Fare Amount",
            "15. DA Rate",
            "16. DA Amount",
            "17(a). Terminal Charges",
            "17(b). Terminal Charges",
            "18. Incidental",
            "19. TOTAL",
            "20. Remarks"
        )
        rows.add(headers.joinToString(",") { escapeCsv(it) })

        val mainTours = if (tourRecords.any { it.isReturnLeg }) {
            tourRecords.filter { !it.isReturnLeg }
        } else {
            tourRecords
        }

        for (tour in mainTours) {
            val depStation = tour.departureStation.ifEmpty { "தலைமையிடம்" }
            val depDate = tour.departureDate
            val depHour = if (tour.isNonTravel) "" else tour.departureHour.ifEmpty { "08:00 AM" }
            val arrStation = tour.arrivalStation.ifEmpty { "தலைமையிடம்" }
            val arrDate = tour.arrivalDate.ifEmpty { tour.departureDate }
            val arrHour = if (tour.isNonTravel) "" else tour.arrivalHour.ifEmpty { "09:00 AM" }
            val purpose = if (tour.isNonTravel) {
                tour.purposeOfJourney.ifEmpty { tour.nonTravelType }
            } else {
                tour.purposeOfJourney
            }
            val mode = if (tour.isNonTravel) "" else tour.kindOfJourney.ifEmpty { "பேருந்து" }
            val railClass = ""
            val noOfFares = ""
            val railAmount = ""
            val distance = if (tour.isNonTravel || tour.distanceKm <= 0) "" else "${tour.distanceKm}"
            val rate = ""
            val fare = if (tour.isNonTravel || tour.busFare <= 0.0) "" else String.format(Locale.US, "%.0f", tour.busFare)
            val daRate = if (tour.isNonTravel || tour.daRate <= 0.0) "" else String.format(Locale.US, "%.0f", tour.daRate)
            val daAmount = if (tour.isNonTravel || tour.daAmount <= 0.0) "" else String.format(Locale.US, "%.0f", tour.daAmount)
            val terminalA = if (tour.isNonTravel || tour.terminalCharge17a <= 0.0) "" else String.format(Locale.US, "%.0f", tour.terminalCharge17a)
            val terminalB = if (tour.isNonTravel || tour.terminalCharge17b <= 0.0) "" else String.format(Locale.US, "%.0f", tour.terminalCharge17b)
            val incid = if (tour.isNonTravel || tour.incidentalCharges <= 0.0) "0" else String.format(Locale.US, "%.0f", tour.incidentalCharges)
            val total = if (tour.isNonTravel || tour.grandTotal <= 0.0) "" else String.format(Locale.US, "%.0f", tour.grandTotal)
            val remarks = if (tour.remarks.isNotBlank()) tour.remarks else if (tour.isNonTravel) tour.nonTravelType else ""

            // புறப்பாடு வரிசை (Onward Record)
            val onwardRow = arrayOf(
                depStation,
                depDate,
                depHour,
                arrStation,
                arrDate,
                arrHour,
                purpose,
                mode,
                railClass,
                noOfFares,
                railAmount,
                distance,
                rate,
                fare,
                daRate,
                daAmount,
                terminalA,
                terminalB,
                incid,
                total,
                remarks
            )
            rows.add(onwardRow.joinToString(",") { escapeCsv(it) })

            // திரும்பும் வரிசை (Return Record)
            val isHoliday = tour.isNonTravel && (
                tour.nonTravelType.contains("விடுமுறை") || tour.nonTravelType.contains("Holiday") ||
                tour.nonTravelType.contains("தற்செயல்") || tour.nonTravelType.contains("CL") ||
                tour.purposeOfJourney.contains("CL") || tour.purposeOfJourney.contains("விடுமுறை")
            )
            val isOfficeWork = tour.isNonTravel && (
                tour.nonTravelType.contains("அலுவலக") || tour.nonTravelType.contains("Office") ||
                tour.purposeOfJourney.contains("அலுவலக")
            )

            if (!tour.isNonTravel && !isHoliday && !isOfficeWork) {
                val pairedReturn = tourRecords.find {
                    it.isReturnLeg && ((it.tripGroupId.isNotBlank() && it.tripGroupId == tour.tripGroupId) || (it.dayOfMonth == tour.dayOfMonth))
                }
                val returnDepHour = pairedReturn?.departureHour?.ifEmpty { "04:10 PM" } ?: "04:10 PM"
                val returnArrHour = pairedReturn?.arrivalHour?.ifEmpty { "05:45 PM" } ?: "05:45 PM"
                val returnArrStation = pairedReturn?.arrivalStation?.ifEmpty { depStation } ?: depStation
                val returnDepStation = pairedReturn?.departureStation?.ifEmpty { arrStation } ?: arrStation

                val returnFareVal = if (pairedReturn != null && pairedReturn.busFare > 0) pairedReturn.busFare else tour.busFare
                val returnFare = if (returnFareVal > 0.0) String.format(Locale.US, "%.0f", returnFareVal) else ""

                val termAVal = if (pairedReturn != null && pairedReturn.terminalCharge17a > 0) pairedReturn.terminalCharge17a else (if (tour.terminalCharge17a > 0) tour.terminalCharge17a else 20.0)
                val termA = String.format(Locale.US, "%.0f", termAVal)

                val termBVal = if (pairedReturn != null && pairedReturn.terminalCharge17b > 0) pairedReturn.terminalCharge17b else (if (tour.terminalCharge17b > 0) tour.terminalCharge17b else 20.0)
                val termB = String.format(Locale.US, "%.0f", termBVal)

                val fareNum = returnFare.toDoubleOrNull() ?: 0.0
                val termANum = termA.toDoubleOrNull() ?: 20.0
                val termBNum = termB.toDoubleOrNull() ?: 20.0
                val returnTotalVal = fareNum + termANum + termBNum
                val returnTotal = if (returnTotalVal > 0.0) String.format(Locale.US, "%.0f", returnTotalVal) else ""

                val returnRow = arrayOf(
                    returnDepStation,
                    depDate,
                    returnDepHour,
                    returnArrStation,
                    depDate,
                    returnArrHour,
                    "", // நோக்கம் காலியாக விடப்படும்
                    mode,
                    "", // 9. Class (காலி)
                    "", // 10. No of fares (காலி)
                    "", // 11. Amount (காலி)
                    distance,
                    "", // 13. Rate (காலி)
                    returnFare,
                    "", // திரும்புதலுக்கு DA Rate இல்லை
                    "0", // DA Amount 0
                    termA,
                    termB,
                    "0", // Incidental 0
                    returnTotal,
                    "" // Remarks (காலி)
                )
                rows.add(returnRow.joinToString(",") { escapeCsv(it) })
            }
        }

        // CSV TOTAL Row
        val totalKm = tourRecords.filter { !it.isNonTravel }.sumOf { it.distanceKm }
        val totalBusFare = tourRecords.filter { !it.isNonTravel }.sumOf { it.busFare }
        val totalDaAmount = tourRecords.filter { !it.isNonTravel }.sumOf { it.daAmount }
        val totalTerminalA = tourRecords.filter { !it.isNonTravel }.sumOf { it.terminalCharge17a }
        val totalTerminalB = tourRecords.filter { !it.isNonTravel }.sumOf { it.terminalCharge17b }
        val totalIncidental = tourRecords.filter { !it.isNonTravel }.sumOf { it.incidentalCharges }
        val grandTotal = tourRecords.filter { !it.isNonTravel }.sumOf { it.grandTotal }

        val totalRow = arrayOf(
            "TOTAL",
            "", "", "", "", "", "", "",
            "", // 9. Class
            "", // 10. Fares
            "", // 11. Rail Amt
            if (totalKm > 0) "$totalKm" else "",
            "", // 13. Rate
            if (totalBusFare > 0.0) String.format(Locale.US, "%.0f", totalBusFare) else "0",
            "", // 15. DA Rate
            if (totalDaAmount > 0.0) String.format(Locale.US, "%.0f", totalDaAmount) else "0",
            if (totalTerminalA > 0.0) String.format(Locale.US, "%.0f", totalTerminalA) else "0",
            if (totalTerminalB > 0.0) String.format(Locale.US, "%.0f", totalTerminalB) else "0",
            if (totalIncidental > 0.0) String.format(Locale.US, "%.0f", totalIncidental) else "0",
            String.format(Locale.US, "%.0f", grandTotal), // 19. TOTAL
            "" // 20. Remarks (Empty under Remarks)
        )
        rows.add(totalRow.joinToString(",") { escapeCsv(it) })

        return "\uFEFF" + rows.joinToString("\r\n")
    }

    fun exportForm2ToCSV(
        context: Context,
        tourRecords: List<TourEntry>,
        currentMonth: String = "",
        officer: OfficerProfile? = null,
        fileName: String = "Form_2_TA_Bill.csv"
    ) {
        val csv = generateForm2Csv(tourRecords, currentMonth, officer)
        downloadCSV(context, csv, fileName)
    }

    /**
     * exportTABillToCSV - matches user specifications
     */
    fun exportTABillToCSV(
        tourRecords: List<TourEntry>,
        currentMonth: String = "",
        officer: OfficerProfile? = null
    ): String {
        return generateForm2Csv(tourRecords, currentMonth, officer)
    }

    /**
     * Backward-compatible delegation to Form 2 CSV
     */
    fun exportToTAFormatCSV(
        monthYear: String,
        entries: List<TourEntry>
    ): String {
        return generateForm2Csv(entries, monthYear)
    }

    fun generateTourRecordsCsv(
        monthYear: String,
        entries: List<TourEntry>
    ): String {
        return generateForm2Csv(entries, monthYear)
    }

    private fun escapeCsv(value: String): String {
        val trimmed = value.trim()
        val escaped = trimmed.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    /**
     * Exports Form 2 TA Bill as a .csv file and opens the Android share chooser
     * for Google Sheets, Excel, or Files.
     */
    fun exportToExcelGoogleSheet(
        context: Context,
        monthYear: String,
        entries: List<TourEntry>,
        officer: OfficerProfile? = null
    ) {
        val parts = monthYear.split("-")
        val yearStr = parts.getOrNull(0) ?: "2026"
        val monthNum = parts.getOrNull(1)?.toIntOrNull() ?: 7
        val monthNames = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        val monthNameEn = if (monthNum in 1..12) monthNames[monthNum - 1] else "Month"
        val fileName = "Form_2_TA_Bill_${monthNameEn}_${yearStr}.csv"
        
        exportForm2ToCSV(context, entries, monthYear, officer, fileName)
    }
}
