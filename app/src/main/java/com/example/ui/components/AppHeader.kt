package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.viewmodel.TaBillUiState
import com.example.util.DateUtils
import com.example.util.Localization

@Composable
fun AppHeader(
    uiState: TaBillUiState,
    onSelectMonth: (String) -> Unit,
    onToggleLanguage: () -> Unit,
    onPrint: () -> Unit,
    onShare: () -> Unit,
    onOfficerClick: () -> Unit,
    onChangeMonth: (Int) -> Unit = {},
    onResetToToday: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isTa = uiState.isTamil
    var showMonthMenu by remember { mutableStateOf(false) }

    val availableMonths = (listOf(DateUtils.getCurrentMonthYear(), uiState.selectedMonthYear) + uiState.distinctMonths).distinct()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Navy900,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top row: App title & action icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GoldAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBus,
                            contentDescription = "TA Logo",
                            tint = Navy900,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isTa) "பயணப்படி & நாள்காட்டி" else "TA Bill & Tour Diary",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isTa) "இளையான்குடி ஒன்றியம் • சிவகங்கை" else "Ilayankudi Block • Sivagangai",
                            color = Color(0xFFB0BEC5),
                            fontSize = 12.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Language Switcher
                    IconButton(
                        onClick = onToggleLanguage,
                        modifier = Modifier.testTag("lang_toggle_btn")
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF263238)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Language",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isTa) "தமிழ்" else "EN",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Print / PDF Button
                    IconButton(
                        onClick = onPrint,
                        modifier = Modifier.testTag("header_print_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Print PDF",
                            tint = Color.White
                        )
                    }

                    // WhatsApp Share Button
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.testTag("header_share_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Second row: Active Officer Pill & Month Selector with < > navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Officer Chip
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    modifier = Modifier
                        .clickable { onOfficerClick() }
                        .testTag("officer_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Officer",
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${uiState.activeOfficer.shortDesignation}: ${uiState.activeOfficer.name.take(16)}",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Month Selector with Prev/Next controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Previous Month Button (<)
                    IconButton(
                        onClick = { onChangeMonth(-1) },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("prev_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Month Selector Chip with Dropdown
                    Box {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF004D40)),
                            modifier = Modifier
                                .clickable { showMonthMenu = true }
                                .testTag("month_selector_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Month",
                                    tint = Color(0xFF80CBC4),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isTa) DateUtils.getTamilMonthDisplay(uiState.selectedMonthYear) else DateUtils.getEnglishMonthDisplay(uiState.selectedMonthYear),
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showMonthMenu,
                            onDismissRequest = { showMonthMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (isTa) "இன்று (${DateUtils.formatDisplayDate(DateUtils.getCurrentCalendar())})" else "Today (${DateUtils.formatDisplayDate(DateUtils.getCurrentCalendar())})",
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen
                                    )
                                },
                                onClick = {
                                    onResetToToday()
                                    showMonthMenu = false
                                }
                            )
                            availableMonths.forEach { month ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (isTa) DateUtils.getTamilMonthDisplay(month) else DateUtils.getEnglishMonthDisplay(month),
                                            fontWeight = if (month == uiState.selectedMonthYear) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onSelectMonth(month)
                                        showMonthMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Next Month Button (>)
                    IconButton(
                        onClick = { onChangeMonth(1) },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("next_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Month",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Dynamic Live Date Display Row: displayDate (${day}.${month}.${year}) & displayDay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0D1B2A))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (uiState.displayDate.isNotEmpty()) uiState.displayDate else DateUtils.formatDisplayDate(DateUtils.getCurrentCalendar()),
                        color = GoldAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("display_date_text")
                    )
                    Text(
                        text = "•",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )
                    Text(
                        text = if (isTa) uiState.displayDay else uiState.displayDayEnglish,
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.testTag("display_day_text")
                    )
                }

                if (uiState.selectedMonthYear != DateUtils.getCurrentMonthYear()) {
                    Text(
                        text = if (isTa) "நடப்பு தேதிக்கு ↺" else "Today ↺",
                        color = Color(0xFF80CBC4),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onResetToToday() }
                            .testTag("reset_today_btn")
                    )
                } else {
                    Text(
                        text = if (isTa) "இயல்பான நடப்பு முறை" else "Live Date Mode",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
