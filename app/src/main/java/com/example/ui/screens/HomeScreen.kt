package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TourEntry
import com.example.ui.components.StatCard
import com.example.ui.theme.AmberDark
import com.example.ui.theme.BlueAccent
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
import com.example.util.Localization

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    uiState: TaBillUiState,
    onOpenQuickTour: () -> Unit,
    onOpenNonTravel: () -> Unit,
    onNavigateToTab: (Int) -> Unit, // 1 = Diary, 2 = TA Bill, 3 = Schools, 4 = Officer, 5 = Reports
    onEditTour: (TourEntry) -> Unit,
    onDeleteTour: (TourEntry) -> Unit,
    onPrintDiary: () -> Unit,
    onPrintTaBill: () -> Unit,
    onShareSummary: () -> Unit,
    onExportToCsv: () -> Unit = {},
    onChangeMonth: (Int) -> Unit = {},
    onResetToToday: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isTa = uiState.isTamil
    val monthDisplay = if (isTa) DateUtils.getTamilMonthDisplay(uiState.selectedMonthYear) else DateUtils.getEnglishMonthDisplay(uiState.selectedMonthYear)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F9))
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // 1. COMPACT MONTHLY SUMMARY STRIP (நேர்த்தியான சிறிய பயணப்படி சுருக்கம்)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("grand_total_hero_card"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Navy900),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    // Top Row: Claim Amount & Action Buttons (PDF & Prominent Green Excel Button)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Amount & Month Title (weight gives guaranteed room for buttons on right)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "₹${String.format("%.0f", uiState.grandTotal)}",
                                    color = GoldAccent,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = monthDisplay,
                                    color = Color(0xFFECEFF1),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = if (isTa) "பயணப்படி உரிமைக்கோரல்" else "Monthly TA Claim",
                                color = Color(0xFF90A4AE),
                                fontSize = 9.5.sp,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Right: PDF & Prominent Green Excel Button
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // PDF Button
                            Surface(
                                shape = RoundedCornerShape(7.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier
                                    .clickable { onPrintTaBill() }
                                    .testTag("home_pdf_btn")
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

                            // Green Excel Button (Prominent & Clear, inside the card)
                            Surface(
                                shape = RoundedCornerShape(7.dp),
                                color = Color(0xFF107C41), // Rich Microsoft Excel Green
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .clickable { onExportToCsv() }
                                    .testTag("home_excel_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TableChart,
                                        contentDescription = "Excel",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Excel",
                                        color = Color.White,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    // Compact Stats Strip: Slim pill at bottom of card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0F2744))
                            .padding(horizontal = 8.dp, vertical = 3.5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTa) "${uiState.totalToursCount} பயணங்கள்" else "${uiState.totalToursCount} Tours",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(text = "•", color = Color(0xFF455A64), fontSize = 9.sp)
                        Text(
                            text = "${uiState.totalKm} km",
                            color = Color(0xFFCFD8DC),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(text = "•", color = Color(0xFF455A64), fontSize = 9.sp)
                        Text(
                            text = if (isTa) "பஸ்: ₹${String.format("%.0f", uiState.totalBusFare)}" else "Bus: ₹${String.format("%.0f", uiState.totalBusFare)}",
                            color = Color(0xFFCFD8DC),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(text = "•", color = Color(0xFF455A64), fontSize = 9.sp)
                        Text(
                            text = "DA: ₹${String.format("%.0f", uiState.totalDaAmount)}",
                            color = GoldAccent,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. QUICK ACTION SHORTCUTS (The "LESS INPUT DATA" Shortcuts)
        item {
            Column {
                Text(
                    text = if (isTa) "விரைவு உள்ளீடு (Quick Actions)" else "Quick Entry Shortcuts",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Primary Quick Tour Button
                    Button(
                        onClick = onOpenQuickTour,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("quick_tour_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy700),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Tour", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTa) "+ புதிய பயணம்" else "+ New Tour",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Leave / Holiday / Duty Quick Button
                    OutlinedButton(
                        onClick = onOpenNonTravel,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("quick_holiday_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.EventBusy, contentDescription = "Leave", tint = CrimsonRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isTa) "+ விடுமுறை / CL" else "+ Leave / Holiday",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // 3. MONTHLY TOURS LIST
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTa) "$monthDisplay பயணப் பதிவுகள் (${uiState.tourEntries.size})" else "Tour Entries (${uiState.tourEntries.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
                Text(
                    text = if (isTa) "மொத்தம்: ${uiState.totalKm} km" else "Total: ${uiState.totalKm} km",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Navy700
                )
            }
        }

        if (uiState.tourEntries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBus,
                            contentDescription = "No Tours",
                            tint = Color.LightGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isTa) "இம்மாதத்திற்கு பயணப் பதிவுகள் இல்லை" else "No tours entered for this month yet",
                            fontSize = 13.5.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onOpenQuickTour,
                            colors = ButtonDefaults.buttonColors(containerColor = Navy700)
                        ) {
                            Text(if (isTa) "+ முதல் பயணத்தை சேர்க்க" else "+ Add First Tour")
                        }
                    }
                }
            }
        } else {
            items(uiState.tourEntries, key = { it.id }) { entry ->
                TourRowCard(
                    entry = entry,
                    isTamil = isTa,
                    onEdit = { onEditTour(entry) },
                    onDelete = { onDeleteTour(entry) }
                )
            }
        }
    }
}

@Composable
fun TourRowCard(
    entry: TourEntry,
    isTamil: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tour_card_${entry.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isNonTravel) {
                if (entry.nonTravelType.contains("விடுமுறை")) Color(0xFFFFF8E1)
                else if (entry.nonTravelType.contains("தற்செயல்")) Color(0xFFFFEBEE)
                else Color(0xFFE8EAF6)
            } else if (entry.isReturnLeg) {
                Color(0xFFF9FBE7)
            } else {
                Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Date badge + Leg type + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Navy900
                    ) {
                        Text(
                            text = entry.departureDate.take(5), // "02.07"
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    if (entry.isNonTravel) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (entry.nonTravelType.contains("விடுமுறை")) AmberDark
                            else if (entry.nonTravelType.contains("தற்செயல்")) CrimsonRed
                            else BlueAccent
                        ) {
                            Text(
                                text = entry.nonTravelType,
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    } else if (entry.isReturnLeg) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF558B2F)
                        ) {
                            Text(
                                text = if (isTamil) "மறுபயணம் (Return)" else "Return Leg",
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BlueAccent
                        ) {
                            Text(
                                text = if (isTamil) "புறப்பாடு (Outbound)" else "Outbound",
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Body: Departure -> Arrival
            if (entry.isNonTravel) {
                Text(
                    text = "${entry.departureStation} • ${entry.purposeOfJourney}",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entry.departureStation,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = entry.departureHour,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "to",
                        tint = Navy700,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(16.dp)
                    )

                    Column(modifier = Modifier.weight(1.3f)) {
                        Text(
                            text = entry.arrivalStation,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = entry.arrivalHour,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Breakdown footer chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "${entry.distanceKm} km",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "• ${entry.purposeOfJourney}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Text(
                        text = "₹${String.format("%.0f", entry.grandTotal)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = Navy900
                    )
                }
            }
        }
    }
}
