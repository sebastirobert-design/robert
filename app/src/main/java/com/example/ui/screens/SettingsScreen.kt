package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.data.model.OfficerProfile
import com.example.data.model.School
import com.example.ui.components.AppOutlinedTextField
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy900
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TaBillUiState

/**
 * அமைப்புகள் திரை (Settings Screen)
 * பள்ளிகள், அலுவலர் விவரங்கள் மற்றும் போக்குவரத்து படி விகிதங்கள் அனைத்தையும்
 * ஒரே இடத்தில் ஒருங்கிணைத்து நிர்வகிக்கும் திரை.
 */
@Composable
fun SettingsScreen(
    uiState: TaBillUiState,
    onUpdateSettings: (AppSettings) -> Unit,
    onClearCurrentMonth: () -> Unit,
    onSwitchOfficer: (Long) -> Unit = {},
    onUpdateOfficer: (OfficerProfile) -> Unit = {},
    onSaveSchool: (School) -> Unit = {},
    onDeleteSchool: (School) -> Unit = {},
    selectedSubTab: Int = 0,
    onSelectSubTab: (Int) -> Unit = {},
    onPrintTaBill: () -> Unit = {},
    onExportToCsv: () -> Unit = {},
    onBackupToDrive: () -> Unit = {},
    onRestoreFromDrive: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isTa = uiState.isTamil

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F9))
    ) {
        // 3 Sub-Tabs inside Settings: படி விகிதம் (Rates), அலுவலர் (Officer), பள்ளிகள் (Schools)
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = Navy900,
            contentColor = Color.White,
            indicator = { tabPositions ->
                if (selectedSubTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                        color = GoldAccent
                    )
                }
            }
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { onSelectSubTab(0) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.CurrencyRupee,
                        contentDescription = "Rates",
                        modifier = Modifier.size(17.dp)
                    )
                },
                text = {
                    Text(
                        text = if (isTa) "படி விகிதம்" else "Rates & TA",
                        fontSize = 11.5.sp,
                        fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selectedContentColor = GoldAccent,
                unselectedContentColor = Color(0xFF90A4AE),
                modifier = Modifier.testTag("settings_subtab_rates")
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { onSelectSubTab(1) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Officer",
                        modifier = Modifier.size(17.dp)
                    )
                },
                text = {
                    Text(
                        text = if (isTa) "அலுவலர்" else "Officer",
                        fontSize = 11.5.sp,
                        fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selectedContentColor = GoldAccent,
                unselectedContentColor = Color(0xFF90A4AE),
                modifier = Modifier.testTag("settings_subtab_officer")
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { onSelectSubTab(2) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Schools",
                        modifier = Modifier.size(17.dp)
                    )
                },
                text = {
                    Text(
                        text = if (isTa) "பள்ளிகள்" else "Schools",
                        fontSize = 11.5.sp,
                        fontWeight = if (selectedSubTab == 2) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selectedContentColor = GoldAccent,
                unselectedContentColor = Color(0xFF90A4AE),
                modifier = Modifier.testTag("settings_subtab_schools")
            )
        }

        // SubTab Content
        when (selectedSubTab) {
            1 -> {
                OfficerProfileScreen(
                    uiState = uiState,
                    onSwitchOfficer = onSwitchOfficer,
                    onUpdateOfficer = onUpdateOfficer,
                    modifier = Modifier.fillMaxSize()
                )
            }
            2 -> {
                SchoolDirectoryScreen(
                    uiState = uiState,
                    onSaveSchool = onSaveSchool,
                    onDeleteSchool = onDeleteSchool,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                RatesAndSettingsView(
                    uiState = uiState,
                    onUpdateSettings = onUpdateSettings,
                    onClearCurrentMonth = onClearCurrentMonth,
                    onNavigateToOfficer = { onSelectSubTab(1) },
                    onNavigateToSchools = { onSelectSubTab(2) },
                    onPrintTaBill = onPrintTaBill,
                    onExportToCsv = onExportToCsv,
                    onBackupToDrive = onBackupToDrive,
                    onRestoreFromDrive = onRestoreFromDrive
                )
            }
        }
    }
}

/**
 * போக்குவரத்து படி விகிதம், மாதாந்திர சுருக்கம் & ஆஃப்லைன் தகவல்கள்
 */
@Composable
private fun RatesAndSettingsView(
    uiState: TaBillUiState,
    onUpdateSettings: (AppSettings) -> Unit,
    onClearCurrentMonth: () -> Unit,
    onNavigateToOfficer: () -> Unit,
    onNavigateToSchools: () -> Unit,
    onPrintTaBill: () -> Unit,
    onExportToCsv: () -> Unit,
    onBackupToDrive: () -> Unit = {},
    onRestoreFromDrive: () -> Unit = {}
) {
    val isTa = uiState.isTamil
    val settings = uiState.appSettings

    var daRateStr by remember(settings) { mutableStateOf(String.format("%.0f", settings.defaultDaRate)) }
    var daAmountStr by remember(settings) { mutableStateOf(String.format("%.0f", settings.defaultDaAmount)) }
    var t17aStr by remember(settings) { mutableStateOf(String.format("%.0f", settings.defaultTerminal17a)) }
    var t17bStr by remember(settings) { mutableStateOf(String.format("%.0f", settings.defaultTerminal17b)) }
    var baseHqTa by remember(settings) { mutableStateOf(settings.baseHeadquartersTa) }

    var showClearDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // 1. QUICK NAVIGATION: அலுவலர் & பள்ளிகள் இணைப்புகள் (Quick Links)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Officer Profile Quick Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToOfficer() }
                        .testTag("settings_goto_officer_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0E7FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Officer",
                                    tint = Color(0xFF4338CA),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isTa) "அலுவலர் விவரங்கள்" else "Officer Profile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = Navy900
                        )
                        Text(
                            text = uiState.activeOfficer.name,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                // Schools Directory Quick Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToSchools() }
                        .testTag("settings_goto_schools_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "Schools",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isTa) "பள்ளிகள் களஞ்சியம்" else "Schools Directory",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = Navy900
                        )
                        Text(
                            text = if (isTa) "${uiState.allSchools.size} பள்ளிகள் & பஸ் கட்டணம்" else "${uiState.allSchools.size} Schools & Fare",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 2. DETAILED MONTHLY TA BILL SUMMARY & EXPORT
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Navy900),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isTa) "${uiState.selectedMonthYear} பயணப்படி சுருக்கம்" else "${uiState.selectedMonthYear} TA Summary",
                                color = Color(0xFFB0BEC5),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${String.format("%.0f", uiState.grandTotal)}",
                                color = GoldAccent,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1E293B),
                                modifier = Modifier.clickable { onPrintTaBill() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Print,
                                        contentDescription = "Print PDF",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "PDF",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF107C41),
                                modifier = Modifier.clickable { onExportToCsv() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Excel",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Excel",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = Color(0xFF263238)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = if (isTa) "பயணங்கள்" else "Tours", color = Color(0xFF90A4AE), fontSize = 10.5.sp)
                            Text(text = "${uiState.totalToursCount}", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = if (isTa) "தொலைவு" else "Distance", color = Color(0xFF90A4AE), fontSize = 10.5.sp)
                            Text(text = "${uiState.totalKm} km", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = if (isTa) "பேருந்து கட்டணம்" else "Bus Fare", color = Color(0xFF90A4AE), fontSize = 10.5.sp)
                            Text(text = "₹${String.format("%.0f", uiState.totalBusFare)}", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = if (isTa) "தினப்படி (DA)" else "DA", color = Color(0xFF90A4AE), fontSize = 10.5.sp)
                            Text(text = "₹${String.format("%.0f", uiState.totalDaAmount)}", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. STANDARD RATES CONFIGURATION (பயணப்படி விகித அமைப்புகள்)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = Navy700)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTa) "பயணப்படி விகித அமைப்புகள் (Standard Rates)" else "TA Rates Configuration",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Navy900
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppOutlinedTextField(
                            value = daRateStr,
                            onValueChange = { daRateStr = it },
                            label = { Text("DA விகிதம் (Rate ₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_da_rate_input"),
                            singleLine = true
                        )
                        AppOutlinedTextField(
                            value = daAmountStr,
                            onValueChange = { daAmountStr = it },
                            label = { Text("DA தொகை (70% ₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_da_amount_input"),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppOutlinedTextField(
                            value = t17aStr,
                            onValueChange = { t17aStr = it },
                            label = { Text("முனையக் கட்டணம் 17a (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        AppOutlinedTextField(
                            value = t17bStr,
                            onValueChange = { t17bStr = it },
                            label = { Text("முனையக் கட்டணம் 17b (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    AppOutlinedTextField(
                        value = baseHqTa,
                        onValueChange = { baseHqTa = it },
                        label = { Text("தலைமையிடம் (Headquarters)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val updated = settings.copy(
                                defaultDaRate = daRateStr.toDoubleOrNull() ?: settings.defaultDaRate,
                                defaultDaAmount = daAmountStr.toDoubleOrNull() ?: settings.defaultDaAmount,
                                defaultTerminal17a = t17aStr.toDoubleOrNull() ?: settings.defaultTerminal17a,
                                defaultTerminal17b = t17bStr.toDoubleOrNull() ?: settings.defaultTerminal17b,
                                baseHeadquartersTa = baseHqTa
                            )
                            onUpdateSettings(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("settings_save_rates_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Save", tint = GoldAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isTa) "விகிதங்களைச் சேமி (Save Rates)" else "Save Rates", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. GOOGLE DRIVE BACKUP & RESTORE (கூகிள் டிரைவ் காப்புநகல் & மீட்டெடுப்பு)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Drive Backup",
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isTa) "கூகிள் டிரைவ் காப்புநகல் & மீட்டெடுப்பு" else "Google Drive Backup & Restore",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = Navy900
                            )
                            Text(
                                text = if (isTa) "டிரைவில் தரவுகளைப் பாதுகாப்பாகச் சேமிக்க" else "Sync & protect data on Google Drive",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isTa)
                            "செயலியின் அனைத்து மாதங்களின் பயணப் பதிவுகள், அலுவலர் சுயவிவரங்கள், 119 பள்ளிகள் மற்றும் படி விகிதங்களை ஒரே கிளிக்கில் கூகிள் டிரைவில் பாதுகாப்பாக காப்புநகல் எடுக்கலாம் அல்லது எப்போது வேண்டுமானாலும் மீட்டெடுக்கலாம்."
                        else
                            "Easily backup all tour diary entries, officer profiles, schools, and settings to Google Drive, or restore them seamlessly.",
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Backup Button
                        Button(
                            onClick = onBackupToDrive,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_google_drive_backup")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Backup",
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTa) "டிரைவில் பேக்கப்" else "Backup to Drive",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        // Restore Button
                        OutlinedButton(
                            onClick = onRestoreFromDrive,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy900),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_google_drive_restore")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Restore",
                                tint = Navy900,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTa) "மீட்டெடு (Restore)" else "Restore File",
                                color = Navy900,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // 5. 100% ஆஃப்லைன் பயன்பாடு & உள்ளூர் தரவுத்தளம் (Offline Architecture)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CloudDone, contentDescription = "Offline", tint = Color(0xFF16A34A))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTa) "100% ஆஃப்லைன் பயன்பாடு (Offline Architecture)" else "100% Offline Architecture",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF14532D)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isTa)
                            "• முழுமையான ஆஃப்லைன் இயக்கம்: இணைய இணைப்பு (Internet) இல்லாமலேயே உங்கள் கைபேசியில் முழுமையாக இயங்கும்.\n• உள்ளூர் தரவுத்தளம்: SQLite Room Database மூலம் உங்கள் சாதனத்தின் நினைவகத்திலேயே (Local Storage) பாதுகாப்பாக சேமிக்கப்படுகிறது.\n• 119 பள்ளிகள் & தொலைவுப்பட்டியல் முன்கூட்டியே ஏற்றப்பட்டுள்ளது.\n• நேரடி PDF & Excel ஆவணங்கள் சாதனம் மூலமாகவே உடனடியாக உருவாக்கப்படுகிறது."
                        else
                            "• 100% Standalone Offline: Operates seamlessly without an internet connection or cellular signal.\n• Local SQLite Room Database: All tour entries, 119 schools, officer profiles, and settings persist securely on this device.\n• Zero Cloud Dependency: No account creation or external server connection required.\n• Instant PDF & Excel export directly on-device.",
                        fontSize = 12.sp,
                        color = Color(0xFF166534),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 5. CLEAR CURRENT MONTH DATA
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear", tint = CrimsonRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTa) "மாதப் பதிவுகளை நீக்க (Clear Month Data)" else "Clear Current Month Data",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CrimsonRed
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isTa)
                            "${uiState.selectedMonthYear} மாதத்திற்கான அனைத்து பயணப் பதிவுகளையும் மொத்தமாக நீக்க இந்த பொத்தானைப் பயன்படுத்தலாம்."
                        else
                            "Delete all tour diary and TA bill entries for ${uiState.selectedMonthYear}.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showClearDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("settings_clear_month_btn")
                    ) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear", tint = CrimsonRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isTa) "${uiState.selectedMonthYear} பதிவுகளை நீக்கு" else "Clear ${uiState.selectedMonthYear} Entries")
                    }
                }
            }
        }

        // 6. APP INFORMATION
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = Navy700)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TA Bill & Diary App Information",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Navy900
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Form 1: Tour Diary (9 Columns: Departure, Arrival, Purpose, Mode, KM)\n• Form 2: TA Bill Preparation (All 20 Columns including DA, Bus Fare, Terminal Charges 17a/17b)\n• 119 Pre-loaded Schools for Ilayankudi Block, Sivagangai Dt.\n• PDF Printing & Excel / Google Sheet Export\n• Version 1.0.0 (Offline Standalone)",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(if (isTa) "உறுதிப்படுத்தவும் (Confirm Clear)" else "Confirm Clear") },
            text = { Text("${uiState.selectedMonthYear} ${if (isTa) "மாதத்தின் அனைத்து பயணப் பதிவுகளையும் நிச்சயமாக நீக்க விரும்புகிறீர்களா?" else "Do you really want to clear all tour entries for this month?"}") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearCurrentMonth()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isTa) "நீக்குக (Delete)" else "Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearDialog = false }) {
                    Text(if (isTa) "ரத்து (Cancel)" else "Cancel")
                }
            }
        )
    }
}
