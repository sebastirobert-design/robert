package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TourEntry
import com.example.ui.theme.BorderColor
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.TableHeaderBg
import com.example.ui.theme.TableRowAlt
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TaBillUiState
import com.example.util.DateUtils

@Composable
fun Form2TaBillScreen(
    uiState: TaBillUiState,
    onOpenQuickTour: () -> Unit,
    onPrintTaBill: () -> Unit,
    onShareTaBill: () -> Unit,
    onExportToCsv: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isTa = uiState.isTamil
    val billMonth = DateUtils.getBillMonthHeader(uiState.selectedMonthYear)
    val englishMonth = DateUtils.getEnglishMonthDisplay(uiState.selectedMonthYear)
    val travelEntries = uiState.travelEntries
    val officer = uiState.activeOfficer

    val totalKm = travelEntries.sumOf { it.distanceKm }
    val totalBusFare = travelEntries.sumOf { it.busFare }
    val totalDaAmount = travelEntries.sumOf { it.daAmount }
    val totalT17a = travelEntries.sumOf { it.terminalCharge17a }
    val totalT17b = travelEntries.sumOf { it.terminalCharge17b }
    val totalIncidental = travelEntries.sumOf { it.incidentalCharges }
    val grandTotal = travelEntries.sumOf { it.grandTotal }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F9))
    ) {
        // Form Title & Action Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Form 2",
                        tint = Navy700,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = if (isTa) "படிவம் 2: பயணப்படி பட்டியல் (TA BILL)" else "FORM 2: TA BILL PREPARATION",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "20 Columns • $billMonth • ₹${String.format("%.0f", grandTotal)}",
                            fontSize = 10.5.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // PDF Button (Compact)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Navy700,
                        modifier = Modifier
                            .clickable { onPrintTaBill() }
                            .testTag("form2_print_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = "PDF",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "PDF",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Excel Button (Compact)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF107C41),
                        modifier = Modifier
                            .clickable { onExportToCsv() }
                            .testTag("form2_export_csv_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Excel",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Excel",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }

        // Scrollable Table Container
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Government Bill Header Box
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Travelling Allowance Bill of the $englishMonth ($billMonth) Establishment of ${officer.designation} Ilayankudi,Sivagangai Dt",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BASICPAY-Rs: ${String.format("%.0f", officer.basicPay)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Travelling allowance Bill of ${officer.name}, ${officer.shortDesignation}, ILAYANKUDI, SIVAGANGAI DT.",
                            fontSize = 11.5.sp,
                            color = TextPrimary
                        )
                    }
                }
            }

            // The 20-Column Table
            item {
                val horizontalScrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .horizontalScroll(horizontalScrollState)
                ) {
                    Column(modifier = Modifier.width(1545.dp)) {
                        // 1. TABLE MAIN HEADER (All 20 Columns Grouped)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TableHeaderBg)
                        ) {
                            // Row 1: Group Headers
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HeaderCell(text = "PARTICULARS OF JOURNEY AND HALTS", width = 705.dp, isGroup = true)
                                HeaderCell(text = "RAILWAY / STEAMER", width = 160.dp, isGroup = true)
                                HeaderCell(text = "Bus\nFare", width = 70.dp, isGroup = false)
                                HeaderCell(text = "Distance travelled by road", width = 140.dp, isGroup = true)
                                HeaderCell(text = "DAILY ALLOWANCE", width = 170.dp, isGroup = true)
                                HeaderCell(text = "Terminal charges", width = 110.dp, isGroup = true)
                                HeaderCell(text = "Inci-\ndental", width = 55.dp, isGroup = false)
                                HeaderCell(text = "Grand\nTotal", width = 75.dp, isGroup = false)
                                HeaderCell(text = "Remark", width = 60.dp, isGroup = false)
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.3f))

                            // Row 2: Sub Headers
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(30.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1-3 Departure
                                SubHeaderCell(text = "Station", width = 110.dp)
                                SubHeaderCell(text = "Date", width = 65.dp)
                                SubHeaderCell(text = "Hour", width = 55.dp)
                                // 4-6 Arrival
                                SubHeaderCell(text = "Station", width = 130.dp)
                                SubHeaderCell(text = "Date", width = 65.dp)
                                SubHeaderCell(text = "Hour", width = 55.dp)
                                // 7-9 Purpose, Mode, Km
                                SubHeaderCell(text = "Purpose of Journey", width = 120.dp)
                                SubHeaderCell(text = "Kind of\nJourney", width = 60.dp)
                                SubHeaderCell(text = "No. of\nkm", width = 45.dp)
                                // 10-12 Rail
                                SubHeaderCell(text = "Class", width = 45.dp)
                                SubHeaderCell(text = "No. of\nfares", width = 50.dp)
                                SubHeaderCell(text = "Amount", width = 65.dp)
                                // 13 Bus Fare
                                SubHeaderCell(text = "", width = 70.dp)
                                // 14-15 Road
                                SubHeaderCell(text = "At ord.\nrates", width = 70.dp)
                                SubHeaderCell(text = "At spec.\nrates", width = 70.dp)
                                // 16A, 16B, 16C DA
                                SubHeaderCell(text = "No. of\ndays", width = 50.dp)
                                SubHeaderCell(text = "Rate", width = 55.dp)
                                SubHeaderCell(text = "Amount", width = 65.dp)
                                // 17a, 17b Terminal
                                SubHeaderCell(text = "17a", width = 55.dp)
                                SubHeaderCell(text = "17b", width = 55.dp)
                                // 18, 19, 20
                                SubHeaderCell(text = "", width = 55.dp)
                                SubHeaderCell(text = "", width = 75.dp)
                                SubHeaderCell(text = "", width = 60.dp)
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.3f))

                            // Row 3: Standard Column Numbers 1 to 20
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(22.dp)
                                    .background(Color(0xFF263238)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NumberCell(text = "1", width = 110.dp)
                                NumberCell(text = "2", width = 65.dp)
                                NumberCell(text = "3", width = 55.dp)
                                NumberCell(text = "4", width = 130.dp)
                                NumberCell(text = "5", width = 65.dp)
                                NumberCell(text = "6", width = 55.dp)
                                NumberCell(text = "7", width = 120.dp)
                                NumberCell(text = "8", width = 60.dp)
                                NumberCell(text = "9", width = 45.dp)
                                NumberCell(text = "10", width = 45.dp)
                                NumberCell(text = "11", width = 50.dp)
                                NumberCell(text = "12", width = 65.dp)
                                NumberCell(text = "13", width = 70.dp)
                                NumberCell(text = "14", width = 70.dp)
                                NumberCell(text = "15", width = 70.dp)
                                NumberCell(text = "16A", width = 50.dp)
                                NumberCell(text = "16B", width = 55.dp)
                                NumberCell(text = "16C", width = 65.dp)
                                NumberCell(text = "17a", width = 55.dp)
                                NumberCell(text = "17b", width = 55.dp)
                                NumberCell(text = "18", width = 55.dp)
                                NumberCell(text = "19", width = 75.dp)
                                NumberCell(text = "20", width = 60.dp)
                            }
                        }

                        // 2. DATA ROWS
                        if (travelEntries.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isTa) "பயணப்படி பதிவுகள் இல்லை." else "No travel allowance entries found.",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            travelEntries.forEachIndexed { index, entry ->
                                val isAlt = index % 2 == 1
                                val rowBg = if (isAlt) TableRowAlt else Color.White

                                val dateShortDep = entry.departureDate.replace("2026", "26").replace("2022", "22")
                                val dateShortArr = entry.arrivalDate.replace("2026", "26").replace("2022", "22")
                                val busFareStr = if (entry.busFare > 0) String.format("%.0f", entry.busFare) else ""
                                val daRateStr = if (entry.daRate > 0) String.format("%.0f", entry.daRate) else ""
                                val daAmountStr = if (entry.daAmount > 0) String.format("%.0f", entry.daAmount) else ""
                                val t17aStr = if (entry.terminalCharge17a > 0) String.format("%.0f", entry.terminalCharge17a) else ""
                                val t17bStr = if (entry.terminalCharge17b > 0) String.format("%.0f", entry.terminalCharge17b) else ""
                                val grandTotalStr = if (entry.grandTotal > 0) String.format("%.0f", entry.grandTotal) else ""

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(rowBg)
                                        .border(0.5.dp, BorderColor),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    DataCell(text = entry.departureStation, width = 110.dp, alignLeft = true)
                                    DataCell(text = dateShortDep, width = 65.dp)
                                    DataCell(text = entry.departureHour, width = 55.dp)
                                    DataCell(text = entry.arrivalStation, width = 130.dp, alignLeft = true)
                                    DataCell(text = dateShortArr, width = 65.dp)
                                    DataCell(text = entry.arrivalHour, width = 55.dp)
                                    DataCell(text = entry.purposeOfJourney, width = 120.dp, alignLeft = true)
                                    DataCell(text = entry.kindOfJourney, width = 60.dp)
                                    DataCell(text = if (entry.distanceKm > 0) entry.distanceKm.toString() else "", width = 45.dp)
                                    DataCell(text = entry.railClass, width = 45.dp)
                                    DataCell(text = entry.railNoOfFares, width = 50.dp)
                                    DataCell(text = if (entry.railAmount > 0) String.format("%.0f", entry.railAmount) else "", width = 65.dp)
                                    DataCell(text = busFareStr, width = 70.dp, isBold = true)
                                    DataCell(text = entry.roadMileageOrdinary, width = 70.dp)
                                    DataCell(text = entry.roadMileageSpecial, width = 70.dp)
                                    DataCell(text = entry.daDays, width = 50.dp)
                                    DataCell(text = daRateStr, width = 55.dp)
                                    DataCell(text = daAmountStr, width = 65.dp, isBold = true)
                                    DataCell(text = t17aStr, width = 55.dp)
                                    DataCell(text = t17bStr, width = 55.dp)
                                    DataCell(text = if (entry.incidentalCharges > 0) String.format("%.0f", entry.incidentalCharges) else "", width = 55.dp)
                                    // Grand Total cell highlighted
                                    Box(
                                        modifier = Modifier
                                            .width(75.dp)
                                            .background(Color(0xFFFFF9C4))
                                            .padding(horizontal = 4.dp, vertical = 6.dp)
                                            .border(0.25.dp, BorderColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = grandTotalStr,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Navy900
                                        )
                                    }
                                    DataCell(text = entry.remarks, width = 60.dp)
                                }
                            }
                        }

                        // 3. OFFICIAL TOTALS ROW
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFECEFF1))
                                .border(1.dp, Navy900),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(660.dp)
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = if (isTa) "மொத்தம் (TOTAL):" else "TOTAL:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Navy900
                                )
                            }
                            DataCell(text = "$totalKm", width = 45.dp, isBold = true)
                            DataCell(text = "", width = 45.dp)
                            DataCell(text = "", width = 50.dp)
                            DataCell(text = "", width = 65.dp)
                            DataCell(text = String.format("%.0f", totalBusFare), width = 70.dp, isBold = true)
                            DataCell(text = "", width = 70.dp)
                            DataCell(text = "", width = 70.dp)
                            DataCell(text = "", width = 50.dp)
                            DataCell(text = "", width = 55.dp)
                            DataCell(text = String.format("%.0f", totalDaAmount), width = 65.dp, isBold = true)
                            DataCell(text = String.format("%.0f", totalT17a), width = 55.dp, isBold = true)
                            DataCell(text = String.format("%.0f", totalT17b), width = 55.dp, isBold = true)
                            DataCell(text = if (totalIncidental > 0) String.format("%.0f", totalIncidental) else "", width = 55.dp)
                            Box(
                                modifier = Modifier
                                    .width(75.dp)
                                    .background(GoldAccent)
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "₹${String.format("%.0f", grandTotal)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = Navy900
                                )
                            }
                            DataCell(text = "", width = 60.dp)
                        }
                    }
                }
            }

            // Bottom Certificate and Signature Box
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp, topStart = 0.dp, topEnd = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "CERTIFICATE: Certified that the journeys were performed solely on official government duty for public service and inspection of schools/meetings, and the claims are strictly in accordance with Tamil Nadu Travelling Allowance Rules.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Station: Ilayankudi", fontSize = 11.5.sp, color = TextPrimary)
                                Text(text = "Date: _______________", fontSize = 11.5.sp, color = TextPrimary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Passed for Payment:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Navy900)
                                Text(text = "Rs. ${String.format("%.0f", grandTotal)}/-", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = officer.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Navy900)
                                Text(text = officer.designation, fontSize = 11.sp, color = TextSecondary)
                                Text(text = "Ilayankudi", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
