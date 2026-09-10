package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TourEntry
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TaBillUiState
import com.example.util.DateUtils

/**
 * Reports Dashboard Screen
 * Allows users to:
 * 1. View all saved trips for the selected month with comprehensive journey details.
 * 2. Filter, search, and inspect trip records.
 * 3. Trigger the full 21-column CSV export for their Travelling Allowance (TA) Bill
 *    with UTF-8 BOM compatibility for Microsoft Excel and Google Sheets.
 * 4. Inspect raw 21-column CSV field mappings for any individual saved trip.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsDashboardScreen(
    uiState: TaBillUiState,
    onExportToCsv: () -> Unit,
    onOpenQuickTour: () -> Unit,
    onEditTour: (TourEntry) -> Unit,
    onDeleteTour: (TourEntry) -> Unit,
    onSelectMonth: (String) -> Unit,
    onPrintTaBill: () -> Unit,
    onShareSummary: () -> Unit,
    onExportForm1Csv: () -> Unit = onExportToCsv,
    onExportForm2Csv: () -> Unit = onExportToCsv,
    onPrintForm1Pdf: () -> Unit = onPrintTaBill,
    modifier: Modifier = Modifier
) {
    val isTa = uiState.isTamil
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableStateOf(0) } // 0: All, 1: Travel Only, 2: Non-Travel
    var showMonthDropdown by remember { mutableStateOf(false) }
    var inspectingEntry by remember { mutableStateOf<TourEntry?>(null) }
    var entryToDelete by remember { mutableStateOf<TourEntry?>(null) }

    // Month display string (e.g., July 2026 / ஜூலை 2026)
    val monthDisplay = if (isTa) DateUtils.getTamilMonthDisplay(uiState.selectedMonthYear) else DateUtils.getEnglishMonthDisplay(uiState.selectedMonthYear)

    // Compute expected CSV filename
    val monthParts = uiState.selectedMonthYear.split("-")
    val yearStr = monthParts.getOrNull(0) ?: "2026"
    val monthNum = monthParts.getOrNull(1)?.toIntOrNull() ?: 7
    val englishMonths = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val monthNameEn = if (monthNum in 1..12) englishMonths[monthNum - 1] else "Month"
    val expectedCsvFileName = "TA_Bill_${monthNameEn}_${yearStr}.csv"

    // Filtered entries
    val filteredEntries = uiState.tourEntries.filter { entry ->
        // Category filter
        val matchesCategory = when (selectedFilterIndex) {
            1 -> !entry.isNonTravel
            2 -> entry.isNonTravel
            else -> true
        }

        // Search query
        val matchesSearch = if (searchQuery.isBlank()) {
            true
        } else {
            val q = searchQuery.trim().lowercase()
            entry.arrivalStation.lowercase().contains(q) ||
                    entry.departureStation.lowercase().contains(q) ||
                    entry.purposeOfJourney.lowercase().contains(q) ||
                    entry.kindOfJourney.lowercase().contains(q) ||
                    entry.remarks.lowercase().contains(q) ||
                    entry.nonTravelType.lowercase().contains(q) ||
                    entry.departureDate.contains(q)
        }

        matchesCategory && matchesSearch
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. DASHBOARD BANNER & MONTH PICKER
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reports_banner_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Navy900),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(GoldAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TableChart,
                                    contentDescription = "Reports",
                                    tint = Navy900,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isTa) "அறிக்கைகள் மையம்" else "Reports Dashboard",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isTa) "பயணப் பதிவுகள் & TA பில் CSV ஏற்றுமதி" else "Saved Trips & TA Bill CSV Export",
                                    fontSize = 12.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }

                        // Month Switcher Chip
                        Box {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Navy800,
                                border = BorderStroke(1.dp, GoldAccent),
                                modifier = Modifier
                                    .clickable { showMonthDropdown = true }
                                    .testTag("reports_month_selector_chip")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Select Month",
                                        tint = GoldAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = monthDisplay,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showMonthDropdown,
                                onDismissRequest = { showMonthDropdown = false }
                            ) {
                                val availableMonths = listOf(
                                    "2026-07",
                                    "2026-08",
                                    "2026-09",
                                    "2026-10",
                                    "2026-11",
                                    "2026-12",
                                    "2022-11"
                                )
                                availableMonths.forEach { m ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = if (isTa) DateUtils.getTamilMonthDisplay(m) else DateUtils.getEnglishMonthDisplay(m),
                                                fontWeight = if (m == uiState.selectedMonthYear) FontWeight.Bold else FontWeight.Normal,
                                                color = if (m == uiState.selectedMonthYear) Navy700 else TextPrimary
                                            )
                                        },
                                        onClick = {
                                            onSelectMonth(m)
                                            showMonthDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = Color(0xFF263238)
                    )

                    // Quick Financial Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isTa) "மொத்தப் பயணங்கள்" else "Saved Trips",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${uiState.tourEntries.size} " + (if (isTa) "பதிவுகள்" else "entries"),
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isTa) "பயண தூரம்" else "Total Distance",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${uiState.totalKm} km",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isTa) "மொத்த TA கோரிக்கை" else "Grand Claim",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "₹${String.format("%.0f", uiState.grandTotal)}",
                                color = GoldAccent,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        // 2. REPORTS CONTAINER (படிவம் 1 & படிவம் 2 அட்டை)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // படிவம் 1 அட்டை
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reports_form1_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "படிவம் 1 : நாள்காட்டி (9 Columns)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 📄 படிவம் 1 PDF
                            OutlinedButton(
                                onClick = onPrintForm1Pdf,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("reports_form1_pdf_btn"),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF0F172A)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF0F172A)
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "📄 படிவம் 1 PDF",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1
                                )
                            }

                            // 📊 படிவம் 1 Sheet (CSV)
                            Button(
                                onClick = onExportForm1Csv,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("reports_form1_csv_btn"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF059669),
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "📊 படிவம் 1 Sheet (CSV)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // படிவம் 2 அட்டை
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reports_form2_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "படிவம் 2 : TA Bill (20 Columns)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 📄 படிவம் 2 PDF
                            OutlinedButton(
                                onClick = onPrintTaBill,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("reports_form2_pdf_btn"),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF0F172A)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF0F172A)
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "📄 படிவம் 2 PDF",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1
                                )
                            }

                            // 📊 படிவம் 2 Sheet (CSV)
                            Button(
                                onClick = onExportForm2Csv,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("reports_form2_csv_btn"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2563EB),
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "📊 படிவம் 2 Sheet (CSV)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // வாட்ஸ்அப் பகிர்வு (WhatsApp Summary)
                OutlinedButton(
                    onClick = onShareSummary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("reports_share_whatsapp_btn"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF25D366)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFF0FDF4),
                        contentColor = Color(0xFF16A34A)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTa) "வாட்ஸ்அப் பகிர்வு (WhatsApp Summary)" else "Share Summary via WhatsApp",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                }
            }
        }

        // 3. DETAILED METRICS GRID
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reports_metrics_grid_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isTa) "பயணக் கட்டண விவரங்கள் (Cost Breakdown)" else "Allowance Breakdown ($monthDisplay)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            label = if (isTa) "பேருந்து கட்டணம்" else "Bus Fare",
                            value = "₹${String.format("%.0f", uiState.totalBusFare)}",
                            color = Navy700,
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            label = if (isTa) "தினப்படி (DA)" else "DA Amount",
                            value = "₹${String.format("%.0f", uiState.totalDaAmount)}",
                            color = EmeraldGreen,
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            label = if (isTa) "முனையக் கட்டணம்" else "Terminal",
                            value = "₹${String.format("%.0f", uiState.totalTerminalCharges)}",
                            color = Color(0xFF0284C7),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = Color(0xFFF1F5F9)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTa) "மொத்த கோரப்படும் தொகை (Grand Total TA Claim)" else "Total TA Bill Claim Amount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "₹${String.format("%.0f", uiState.grandTotal)}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Navy900
                        )
                    }
                }
            }
        }

        // 4. SEARCH & FILTER CONTROLS
        item {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reports_search_field"),
                    placeholder = {
                        Text(
                            text = if (isTa) "பள்ளி பெயர், இடம், நோக்கம் தேடுக..." else "Search by school, place, purpose...",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Navy700,
                        unfocusedBorderColor = BorderColor
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilterIndex == 0,
                        onClick = { selectedFilterIndex = 0 },
                        label = {
                            Text(
                                text = if (isTa) "அனைத்துப் பதிவுகள் (${uiState.tourEntries.size})" else "All Trips (${uiState.tourEntries.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedFilterIndex == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Navy900,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("reports_filter_chip_all")
                    )

                    FilterChip(
                        selected = selectedFilterIndex == 1,
                        onClick = { selectedFilterIndex = 1 },
                        label = {
                            Text(
                                text = if (isTa) "பயணங்கள் (${uiState.travelEntries.size})" else "Travel (${uiState.travelEntries.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedFilterIndex == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF15803D),
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("reports_filter_chip_travel")
                    )

                    val nonTravelCount = uiState.tourEntries.count { it.isNonTravel }
                    FilterChip(
                        selected = selectedFilterIndex == 2,
                        onClick = { selectedFilterIndex = 2 },
                        label = {
                            Text(
                                text = if (isTa) "விடுமுறை / பணி ($nonTravelCount)" else "Duty/Leave ($nonTravelCount)",
                                fontSize = 12.sp,
                                fontWeight = if (selectedFilterIndex == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CrimsonRed,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("reports_filter_chip_nontravel")
                    )
                }
            }
        }

        // 5. SAVED TRIPS LIST HEADER
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTa) "சேமிக்கப்பட்ட பயணங்கள் (${filteredEntries.size})" else "Saved Trips (${filteredEntries.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )

                TextButton(
                    onClick = onOpenQuickTour,
                    modifier = Modifier.testTag("reports_add_trip_top_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Trip",
                        tint = Navy700,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isTa) "+ புதிய பயணம்" else "+ New Trip",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy700
                    )
                }
            }
        }

        // 6. SAVED TRIPS LIST ITEMS OR EMPTY STATE
        if (filteredEntries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reports_empty_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBus,
                                contentDescription = "No Trips",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) {
                                if (isTa) "\"$searchQuery\" பொருத்தமான பயணங்கள் எதுவும் இல்லை" else "No trips matching \"$searchQuery\""
                            } else {
                                if (isTa) "இம்மாதத்தில் சேமிக்கப்பட்ட பயணங்கள் எதுவும் இல்லை" else "No trips saved for $monthDisplay yet"
                            },
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onOpenQuickTour,
                            colors = ButtonDefaults.buttonColors(containerColor = Navy700),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (isTa) "+ புதிய பயணத்தை சேர்க்க" else "+ Add New Trip",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            itemsIndexed(filteredEntries, key = { _, item -> item.id }) { index, item ->
                SavedTripReportCard(
                    serialNo = index + 1,
                    entry = item,
                    isTamil = isTa,
                    monthYear = uiState.selectedMonthYear,
                    onInspect = { inspectingEntry = item },
                    onEdit = { onEditTour(item) },
                    onDelete = { entryToDelete = item }
                )
            }
        }
    }

    // 7. 21-COLUMN ROW INSPECTION PREVIEW DIALOG
    inspectingEntry?.let { entry ->
        Trip21ColumnPreviewDialog(
            entry = entry,
            monthYear = uiState.selectedMonthYear,
            isTamil = isTa,
            onDismiss = { inspectingEntry = null }
        )
    }

    // 8. DELETE CONFIRMATION DIALOG
    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = {
                Text(
                    text = if (isTa) "பயணத்தை நீக்கவா?" else "Delete Tour Entry?",
                    fontWeight = FontWeight.Bold,
                    color = CrimsonRed
                )
            },
            text = {
                val dest = if (entry.arrivalStation.isNotBlank()) entry.arrivalStation else entry.departureStation
                Text(
                    text = if (isTa) {
                        "${entry.departureDate} அன்று $dest மேற்கொண்ட பயணப் பதிவை நீக்க விரும்புகிறீர்களா?"
                    } else {
                        "Are you sure you want to delete the trip to $dest on ${entry.departureDate}?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTour(entry)
                        entryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isTa) "நீக்குக" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text(if (isTa) "ரத்து" else "Cancel")
                }
            }
        )
    }
}

/**
 * Metric column item for quick overview
 */
@Composable
private fun MetricItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Saved Trip Card on Reports Screen with comprehensive route and financial breakdown
 */
@Composable
fun SavedTripReportCard(
    serialNo: Int,
    entry: TourEntry,
    isTamil: Boolean,
    monthYear: String,
    onInspect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNonTravel = entry.isNonTravel
    val travelDate = if (entry.departureDate.isNotBlank()) entry.departureDate else "${String.format("%02d", entry.dayOfMonth)}.$monthYear"
    val depStation = if (entry.departureStation.isNotBlank()) entry.departureStation else "தலைமையிடம்"
    val arrStation = if (entry.arrivalStation.isNotBlank()) entry.arrivalStation else depStation
    val depHour = if (entry.departureHour.isNotBlank()) entry.departureHour else "08:00 AM"
    val arrHour = if (entry.arrivalHour.isNotBlank()) entry.arrivalHour else "09:00 AM"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("saved_trip_card_$serialNo"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNonTravel) Color(0xFFFFF7ED) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(
            1.dp,
            if (isNonTravel) Color(0xFFFED7AA) else Color(0xFFE2E8F0)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Serial Badge, Date, Category Badge, Action Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Serial pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Navy900
                    ) {
                        Text(
                            text = "#$serialNo",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    // Date
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(0.5.dp, BorderColor)
                    ) {
                        Text(
                            text = travelDate,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    // Non-travel or Travel tag
                    if (isNonTravel) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFEE2E2)
                        ) {
                            Text(
                                text = entry.nonTravelType.ifBlank { if (isTamil) "விடுமுறை" else "Leave" },
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonRed,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = if (entry.isReturnLeg) (if (isTamil) "திரும்பும் பயணம்" else "Return Leg") else (if (isTamil) "பயணம்" else "Tour"),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Quick Action buttons: Edit, Inspect, Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onInspect,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Inspect 21 Columns",
                            tint = Navy700,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Trip",
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Trip",
                            tint = CrimsonRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (!isNonTravel) {
                // Route Visualizer (Departure -> Destination)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dep point
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = depStation,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = depHour,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(start = 14.dp)
                        )
                    }

                    // Arrow Icon
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "to",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(16.dp)
                    )

                    // Arr point
                    Column(modifier = Modifier.weight(1.2f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Navy700)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = arrStation,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = arrHour,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(start = 14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Purpose and Mode Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val purposeText = entry.purposeOfJourney.ifBlank { if (isTamil) "பள்ளி ஆய்வு" else "School Visit" }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFEFF6FF)
                    ) {
                        Text(
                            text = "🎯 $purposeText",
                            fontSize = 11.sp,
                            color = Color(0xFF1D4ED8),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    val modeText = entry.kindOfJourney.ifBlank { "பேருந்து" }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = "🚌 $modeText",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (entry.distanceKm > 0) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = "${entry.distanceKm} km",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = Color(0xFFF1F5F9)
                )

                // Cost Breakdown Row for this Trip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (entry.busFare > 0) {
                            Column {
                                Text(text = if (isTamil) "கட்டணம்" else "Fare", fontSize = 10.sp, color = TextSecondary)
                                Text(text = "₹${String.format("%.0f", entry.busFare)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                        if (entry.daAmount > 0) {
                            Column {
                                Text(text = if (isTamil) "தினப்படி" else "DA", fontSize = 10.sp, color = TextSecondary)
                                Text(text = "₹${String.format("%.0f", entry.daAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                            }
                        }
                        val terminal = entry.terminalCharge17a + entry.terminalCharge17b
                        if (terminal > 0) {
                            Column {
                                Text(text = if (isTamil) "முனையம்" else "Terminal", fontSize = 10.sp, color = TextSecondary)
                                Text(text = "₹${String.format("%.0f", terminal)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                            }
                        }
                    }

                    // Total for this trip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isTamil) "மொத்தம்: " else "Total: ",
                                fontSize = 11.sp,
                                color = Color(0xFF166534)
                            )
                            Text(
                                text = "₹${String.format("%.0f", entry.grandTotal)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }
            } else {
                // Non-Travel details (Duty at headquarters, CL, or Public Holiday)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EventBusy,
                        contentDescription = "Duty / Leave",
                        tint = CrimsonRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = entry.nonTravelType.ifBlank { entry.purposeOfJourney },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                        Text(
                            text = if (isTamil) "தலைமையகப் பணி / பயணமில்லா நாள்" else "Headquarters Duty / Non-Travel Day",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Remarks note if any
            if (entry.remarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFFFBEB),
                    border = BorderStroke(0.5.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📝 ${entry.remarks}",
                        fontSize = 11.sp,
                        color = Color(0xFF92400E),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Inspection dialog displaying all 21 CSV columns mapped for a specific saved trip.
 */
@Composable
fun Trip21ColumnPreviewDialog(
    entry: TourEntry,
    monthYear: String,
    isTamil: Boolean,
    onDismiss: () -> Unit
) {
    val travelDate = if (entry.departureDate.isNotBlank()) entry.departureDate else "${String.format("%02d", entry.dayOfMonth)}.$monthYear"
    val depStation = if (entry.departureStation.isNotBlank()) entry.departureStation else "தலைமையிடம்"
    val depHour = entry.departureHour
    val arrStation = if (entry.arrivalStation.isNotBlank()) entry.arrivalStation else depStation
    val arrDate = if (entry.arrivalDate.isNotBlank()) entry.arrivalDate else travelDate
    val arrHour = entry.arrivalHour
    val purpose = if (entry.isNonTravel) {
        if (entry.purposeOfJourney.isNotBlank()) entry.purposeOfJourney else entry.nonTravelType
    } else {
        entry.purposeOfJourney
    }
    val mode = if (entry.isNonTravel) "" else if (entry.kindOfJourney.isNotBlank()) entry.kindOfJourney else "பேருந்து"
    val km = if (entry.isNonTravel || entry.distanceKm <= 0) "" else "${entry.distanceKm}"
    val busFare = if (entry.isNonTravel || entry.busFare <= 0.0) "" else String.format("%.0f", entry.busFare)
    val daRate = if (entry.isNonTravel || entry.daRate <= 0.0) "" else String.format("%.0f", entry.daRate)
    val daAmount = if (entry.isNonTravel || entry.daAmount <= 0.0) "" else String.format("%.0f", entry.daAmount)
    val terminalA = if (entry.isNonTravel || entry.terminalCharge17a <= 0.0) "" else String.format("%.0f", entry.terminalCharge17a)
    val terminalB = if (entry.isNonTravel || entry.terminalCharge17b <= 0.0) "" else String.format("%.0f", entry.terminalCharge17b)
    val incid = if (entry.isNonTravel || entry.incidentalCharges <= 0.0) "" else String.format("%.0f", entry.incidentalCharges)
    val total = if (entry.isNonTravel || entry.grandTotal <= 0.0) "" else String.format("%.0f", entry.grandTotal)
    val remarks = entry.remarks

    val csvFields = listOf(
        "1. புறப்பட்ட இடம் (Dep Station)" to depStation,
        "2. புறப்பட்ட தேதி (Dep Date)" to travelDate,
        "3. புறப்பட்ட நேரம் (Dep Hour)" to depHour,
        "4. சென்றடைந்த இடம் (Arr Station)" to arrStation,
        "5. சென்றடைந்த தேதி (Arr Date)" to arrDate,
        "6. சென்றடைந்த நேரம் (Arr Hour)" to arrHour,
        "7. பயண வகை (Kind of Journey)" to mode,
        "8. பயண நோக்கம் (Purpose of Journey)" to purpose,
        "9. தூரம் (கி.மீ) (No. of km)" to km,
        "10. இரயில் வகுப்பு (Rail Class)" to entry.railClass,
        "11. பயணச் சீட்டுகள் (No of Fares)" to entry.railNoOfFares,
        "12. இரயில் கட்டணம் (Rail Amount)" to if (entry.railAmount > 0) String.format("%.0f", entry.railAmount) else "",
        "13. பேருந்து கட்டணம் (Bus Fare)" to busFare,
        "14. சாலையில் சென்ற தூரம் (Road Dist)" to "",
        "15. தினப்படி வீதம் (DA Rate)" to daRate,
        "16. தினப்படி தொகை (DA Amount)" to daAmount,
        "17(a). முனையக் கட்டணம் 17(a)" to terminalA,
        "17(b). முனையக் கட்டணம் 17(b)" to terminalB,
        "18. இதர கட்டணம் (Incid)" to incid,
        "19. மொத்தத் தொகை (TOTAL)" to total,
        "20. குறிப்புகள் (Remarks)" to remarks
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isTamil) "21-நெடுவரிசை CSV தரவு முன்னோட்டம்" else "21-Column CSV Row Data",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(csvFields) { _, (colName, colVal) ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(0.5.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = colName,
                                fontSize = 11.5.sp,
                                color = TextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = colVal.ifBlank { "—" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (colVal.isNotBlank()) Navy900 else Color(0xFF94A3B8),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Navy700)
            ) {
                Text(if (isTamil) "சரி" else "Close")
            }
        }
    )
}
