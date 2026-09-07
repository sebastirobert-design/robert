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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
fun Form1DiaryScreen(
    uiState: TaBillUiState,
    onOpenQuickTour: () -> Unit,
    onPrintDiary: () -> Unit,
    onShareDiary: () -> Unit,
    onExportToCsv: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isTa = uiState.isTamil
    val monthTa = DateUtils.getTamilMonthDisplay(uiState.selectedMonthYear)
    val entries = uiState.tourEntries
    val totalKm = entries.filter { !it.isNonTravel }.sumOf { it.distanceKm }

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
                        imageVector = Icons.Default.Article,
                        contentDescription = "Form 1",
                        tint = Color(0xFF00796B),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = if (isTa) "படிவம் 1: நாள்காட்டி (DIARY)" else "FORM 1: TOUR DIARY",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "9 Columns • $monthTa",
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
                            .clickable { onPrintDiary() }
                            .testTag("form1_print_btn")
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
                            .testTag("form1_export_csv_btn")
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

        // Official Form Container with Horizontal & Vertical Scrolling Table
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Document Header Representation
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
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "நாள் காட்டி $monthTa",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${uiState.activeOfficer.name} ${uiState.activeOfficer.designation} ${uiState.activeOfficer.headquarters} ${uiState.activeOfficer.district}",
                            fontSize = 12.sp,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Scrollable 9-Column Table
            item {
                val horizontalScrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .horizontalScroll(horizontalScrollState)
                ) {
                    Column(modifier = Modifier.width(780.dp)) {
                        // 1. Table Main Header (Row 1 & Row 2 & Col numbers)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TableHeaderBg)
                        ) {
                            // Row 1: Groups (DEPARTURE, ARRIVAL, Purpose, Kind, KM)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HeaderCell(text = "DEPARTURE", width = 230.dp, isGroup = true)
                                HeaderCell(text = "ARRIVAL", width = 250.dp, isGroup = true)
                                HeaderCell(text = "purpose of\njourney", width = 140.dp, isGroup = false)
                                HeaderCell(text = "kind of\njourney", width = 90.dp, isGroup = false)
                                HeaderCell(text = "No. of\nk.m.s", width = 70.dp, isGroup = false)
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.3f))

                            // Row 2: Sub-headers (station, date, hour ...)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(28.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SubHeaderCell(text = "station", width = 100.dp)
                                SubHeaderCell(text = "date", width = 75.dp)
                                SubHeaderCell(text = "hour", width = 55.dp)
                                SubHeaderCell(text = "station", width = 120.dp)
                                SubHeaderCell(text = "date", width = 75.dp)
                                SubHeaderCell(text = "hour", width = 55.dp)
                                SubHeaderCell(text = "", width = 140.dp)
                                SubHeaderCell(text = "", width = 90.dp)
                                SubHeaderCell(text = "", width = 70.dp)
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.3f))

                            // Row 3: Column Numbers (1 to 9)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(22.dp)
                                    .background(Color(0xFF263238)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NumberCell(text = "1", width = 100.dp)
                                NumberCell(text = "2", width = 75.dp)
                                NumberCell(text = "3", width = 55.dp)
                                NumberCell(text = "4", width = 120.dp)
                                NumberCell(text = "5", width = 75.dp)
                                NumberCell(text = "6", width = 55.dp)
                                NumberCell(text = "7", width = 140.dp)
                                NumberCell(text = "8", width = 90.dp)
                                NumberCell(text = "9", width = 70.dp)
                            }
                        }

                        // 2. Table Data Rows
                        if (entries.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isTa) "பயணப் பதிவுகள் இல்லை. '+ புதிய பயணம்' பொத்தானை அழுத்தவும்." else "No entries yet. Tap '+ New Tour' to add.",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            entries.forEachIndexed { index, entry ->
                                val isAlt = index % 2 == 1
                                val isHoliday = entry.isNonTravel
                                val rowBg = if (isHoliday) Color(0xFFFFFDE7) else if (isAlt) TableRowAlt else Color.White

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(rowBg)
                                        .border(0.5.dp, BorderColor),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    DataCell(text = entry.departureStation, width = 100.dp, alignLeft = true, isBold = isHoliday)
                                    DataCell(text = entry.departureDate, width = 75.dp)
                                    DataCell(text = entry.departureHour, width = 55.dp)
                                    DataCell(text = entry.arrivalStation, width = 120.dp, alignLeft = true, isBold = isHoliday)
                                    DataCell(text = entry.arrivalDate, width = 75.dp)
                                    DataCell(text = entry.arrivalHour, width = 55.dp)
                                    DataCell(text = entry.purposeOfJourney, width = 140.dp, alignLeft = true)
                                    DataCell(text = entry.kindOfJourney, width = 90.dp)
                                    DataCell(text = if (entry.distanceKm > 0) entry.distanceKm.toString() else "", width = 70.dp, isBold = true)
                                }
                            }
                        }

                        // 3. Totals Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFECEFF1))
                                .border(1.dp, Navy900),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(710.dp)
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = if (isTa) "மொத்த கி.மீ (TOTAL KM):" else "TOTAL KM:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Navy900
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(70.dp)
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$totalKm",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Navy900
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Signature Box
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp, topStart = 0.dp, topEnd = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "தேதி: _______________", fontSize = 11.5.sp, color = TextPrimary)
                            Text(text = "இடம்: இளையான்குடி", fontSize = 11.5.sp, color = TextPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = uiState.activeOfficer.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Navy900)
                            Text(text = uiState.activeOfficer.designation, fontSize = 11.sp, color = TextSecondary)
                            Text(text = "இளையான்குடி", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

// Header Cells
@Composable
fun HeaderCell(text: String, width: androidx.compose.ui.unit.Dp, isGroup: Boolean) {
    Box(
        modifier = Modifier
            .width(width)
            .border(0.5.dp, Color.White.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SubHeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .border(0.5.dp, Color.White.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFFCFD8DC),
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun NumberCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .border(0.5.dp, Color.White.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = GoldAccent,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DataCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    alignLeft: Boolean = false,
    isBold: Boolean = false
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .border(0.25.dp, BorderColor.copy(alpha = 0.5f)),
        contentAlignment = if (alignLeft) Alignment.CenterStart else Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 10.5.sp,
            color = TextPrimary,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            textAlign = if (alignLeft) TextAlign.Start else TextAlign.Center,
            maxLines = 2
        )
    }
}
