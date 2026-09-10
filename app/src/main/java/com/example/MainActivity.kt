package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppHeader
import com.example.ui.components.PrintOptionsDialog
import com.example.ui.screens.Form1DiaryScreen
import com.example.ui.screens.Form2TaBillScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OfficerProfileScreen
import com.example.ui.screens.QuickNonTravelDialog
import com.example.ui.screens.QuickTourEntryDialog
import com.example.ui.screens.ReportsDashboardScreen
import com.example.ui.screens.SchoolDirectoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy900
import com.example.ui.viewmodel.TaBillViewModel
import com.example.util.DateUtils

class MainActivity : ComponentActivity() {

    private val viewModel: TaBillViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TaBillApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TaBillApp(viewModel: TaBillViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showPrintDialog by remember { mutableStateOf(false) }

    // Show feedback messages as snackbar
    LaunchedEffect(uiState.userFeedbackMessage) {
        uiState.userFeedbackMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearFeedback()
        }
    }

    val isTa = uiState.isTamil

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppHeader(
                uiState = uiState,
                onSelectMonth = { viewModel.setSelectedMonthYear(it) },
                onChangeMonth = { viewModel.changeMonth(it) },
                onResetToToday = { viewModel.resetToToday() },
                onToggleLanguage = { viewModel.toggleLanguage() },
                onPrint = {
                    showPrintDialog = true
                },
                onShare = { viewModel.shareOnWhatsApp(context) },
                onOfficerClick = {
                    viewModel.setSettingsSubTab(1)
                    viewModel.setActiveTab(3)
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Navy900,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                // Tab 0: Home (முகப்பு)
                NavigationBarItem(
                    selected = uiState.activeTab == 0,
                    onClick = { viewModel.setActiveTab(0) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text(if (isTa) "முகப்பு" else "Home", fontSize = 10.sp, fontWeight = if (uiState.activeTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Navy900,
                        selectedTextColor = GoldAccent,
                        indicatorColor = GoldAccent,
                        unselectedIconColor = Color(0xFF90A4AE),
                        unselectedTextColor = Color(0xFF90A4AE)
                    ),
                    modifier = Modifier.testTag("nav_item_home")
                )

                // Tab 1: Form 1 Diary (படிவம் 1)
                NavigationBarItem(
                    selected = uiState.activeTab == 1,
                    onClick = { viewModel.setActiveTab(1) },
                    icon = { Icon(Icons.Default.Article, contentDescription = "Diary") },
                    label = { Text(if (isTa) "படிவம் 1" else "Form 1", fontSize = 10.sp, fontWeight = if (uiState.activeTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Navy900,
                        selectedTextColor = GoldAccent,
                        indicatorColor = GoldAccent,
                        unselectedIconColor = Color(0xFF90A4AE),
                        unselectedTextColor = Color(0xFF90A4AE)
                    ),
                    modifier = Modifier.testTag("nav_item_form1")
                )

                // Tab 2: Form 2 TA Bill (படிவம் 2)
                NavigationBarItem(
                    selected = uiState.activeTab == 2,
                    onClick = { viewModel.setActiveTab(2) },
                    icon = { Icon(Icons.Default.Description, contentDescription = "TA Bill") },
                    label = { Text(if (isTa) "படிவம் 2" else "Form 2", fontSize = 10.sp, fontWeight = if (uiState.activeTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Navy900,
                        selectedTextColor = GoldAccent,
                        indicatorColor = GoldAccent,
                        unselectedIconColor = Color(0xFF90A4AE),
                        unselectedTextColor = Color(0xFF90A4AE)
                    ),
                    modifier = Modifier.testTag("nav_item_form2")
                )

                // Tab 3: Settings (அமைப்புகள் - பள்ளிகள், அலுவலர் & போக்குவரத்து படி விகிதம் உள்ளடக்கியது)
                NavigationBarItem(
                    selected = uiState.activeTab == 3,
                    onClick = { viewModel.setActiveTab(3) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text(if (isTa) "அமைப்புகள்" else "Settings", fontSize = 10.sp, fontWeight = if (uiState.activeTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Navy900,
                        selectedTextColor = GoldAccent,
                        indicatorColor = GoldAccent,
                        unselectedIconColor = Color(0xFF90A4AE),
                        unselectedTextColor = Color(0xFF90A4AE)
                    ),
                    modifier = Modifier.testTag("nav_item_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.activeTab) {
                0 -> HomeScreen(
                    uiState = uiState,
                    onOpenQuickTour = { viewModel.openQuickTourDialog() },
                    onOpenNonTravel = { viewModel.openQuickNonTravelDialog() },
                    onNavigateToTab = { viewModel.setActiveTab(it) },
                    onEditTour = { viewModel.openQuickTourDialog(it) },
                    onDeleteTour = { viewModel.deleteTourEntry(it) },
                    onPrintDiary = { viewModel.printForm1Diary(context) },
                    onPrintTaBill = { viewModel.printForm2TaBill(context) },
                    onShareSummary = { viewModel.shareOnWhatsApp(context) },
                    onExportToCsv = { viewModel.exportToExcelGoogleSheet(context) },
                    onChangeMonth = { viewModel.changeMonth(it) },
                    onResetToToday = { viewModel.resetToToday() }
                )
                1 -> Form1DiaryScreen(
                    uiState = uiState,
                    onOpenQuickTour = { viewModel.openQuickTourDialog() },
                    onPrintDiary = { viewModel.printForm1Diary(context) },
                    onShareDiary = { viewModel.shareOnWhatsApp(context) },
                    onExportToCsv = { viewModel.exportForm1Csv(context) }
                )
                2 -> Form2TaBillScreen(
                    uiState = uiState,
                    onOpenQuickTour = { viewModel.openQuickTourDialog() },
                    onPrintTaBill = { viewModel.printForm2TaBill(context) },
                    onShareTaBill = { viewModel.shareOnWhatsApp(context) },
                    onExportToCsv = { viewModel.exportForm2Csv(context) }
                )
                3 -> SettingsScreen(
                    uiState = uiState,
                    onUpdateSettings = { viewModel.updateSettings(it) },
                    onClearCurrentMonth = { viewModel.clearCurrentMonthEntries() },
                    onSwitchOfficer = { viewModel.switchOfficer(it) },
                    onUpdateOfficer = { viewModel.updateOfficerProfile(it) },
                    onSaveSchool = { viewModel.saveSchool(it) },
                    onDeleteSchool = { viewModel.deleteSchool(it) },
                    selectedSubTab = uiState.settingsSubTab,
                    onSelectSubTab = { viewModel.setSettingsSubTab(it) },
                    onPrintTaBill = { viewModel.printForm2TaBill(context) },
                    onExportToCsv = { viewModel.exportToExcelGoogleSheet(context) }
                )
                5 -> ReportsDashboardScreen(
                    uiState = uiState,
                    onExportToCsv = { viewModel.exportForm2Csv(context) },
                    onExportForm1Csv = { viewModel.exportForm1Csv(context) },
                    onExportForm2Csv = { viewModel.exportForm2Csv(context) },
                    onOpenQuickTour = { viewModel.openQuickTourDialog() },
                    onEditTour = { viewModel.openQuickTourDialog(it) },
                    onDeleteTour = { viewModel.deleteTourEntry(it) },
                    onSelectMonth = { viewModel.setSelectedMonthYear(it) },
                    onPrintTaBill = { viewModel.printForm2TaBill(context) },
                    onPrintForm1Pdf = { viewModel.printForm1Diary(context) },
                    onShareSummary = { viewModel.shareOnWhatsApp(context) }
                )
                else -> HomeScreen(
                    uiState = uiState,
                    onOpenQuickTour = { viewModel.openQuickTourDialog() },
                    onOpenNonTravel = { viewModel.openQuickNonTravelDialog() },
                    onNavigateToTab = { viewModel.setActiveTab(it) },
                    onEditTour = { viewModel.openQuickTourDialog(it) },
                    onDeleteTour = { viewModel.deleteTourEntry(it) },
                    onPrintDiary = { viewModel.printForm1Diary(context) },
                    onPrintTaBill = { viewModel.printForm2TaBill(context) },
                    onShareSummary = { viewModel.shareOnWhatsApp(context) },
                    onExportToCsv = { viewModel.exportToExcelGoogleSheet(context) },
                    onChangeMonth = { viewModel.changeMonth(it) },
                    onResetToToday = { viewModel.resetToToday() }
                )
            }
        }
    }

    // Modal Quick Tour Entry Dialog (LESS INPUT DATA)
    if (uiState.showQuickTourDialog) {
        QuickTourEntryDialog(
            allSchools = uiState.allSchools,
            monthYear = uiState.selectedMonthYear,
            editingEntry = uiState.editingTourEntry,
            existingEntries = uiState.tourEntries,
            activeOfficerName = uiState.activeOfficer?.name ?: "",
            onSaveTour = { dayOfMonth, dateFormatted, departureStation, departureHour, destinations, arrivalHourOutbound, returnDepartureHour, returnArrivalHour, purposeOfJourney, kindOfJourney, isRoundTrip, customDistanceKm, customBusFare, remarks, entryToReplace ->
                viewModel.saveQuickTour(
                    dayOfMonth = dayOfMonth,
                    dateFormatted = dateFormatted,
                    departureStation = departureStation,
                    departureHour = departureHour,
                    destinations = destinations,
                    arrivalHourOutbound = arrivalHourOutbound,
                    returnDepartureHour = returnDepartureHour,
                    returnArrivalHour = returnArrivalHour,
                    purposeOfJourney = purposeOfJourney,
                    kindOfJourney = kindOfJourney,
                    isRoundTrip = isRoundTrip,
                    customDistanceKm = customDistanceKm,
                    customBusFare = customBusFare,
                    remarks = remarks,
                    entryToReplace = entryToReplace
                )
            },
            onExportToCsv = { viewModel.exportToExcelGoogleSheet(context) },
            onDismiss = { viewModel.closeQuickTourDialog() },
            isTamil = isTa,
            initialDay = uiState.selectedDayOfMonth
        )
    }

    // Modal Quick Non-Travel Dialog (CL / Holiday / Office Duty)
    if (uiState.showQuickNonTravelDialog) {
        QuickNonTravelDialog(
            monthYear = uiState.selectedMonthYear,
            onAddNonTravel = { dayOfMonth, dateFormatted, type ->
                viewModel.addNonTravelDay(dayOfMonth, dateFormatted, type)
            },
            onDismiss = { viewModel.closeQuickNonTravelDialog() },
            isTamil = isTa,
            initialDay = uiState.selectedDayOfMonth
        )
    }

    // Modal Print & Export Reports Dialog (படிவம் 1 & படிவம் 2 தேர்வு)
    if (showPrintDialog) {
        val monthDisplay = DateUtils.getTamilMonthDisplay(uiState.selectedMonthYear)
        PrintOptionsDialog(
            uiState = uiState,
            monthDisplay = monthDisplay,
            onDismiss = { showPrintDialog = false },
            onViewForm1 = {
                showPrintDialog = false
                viewModel.setActiveTab(1)
            },
            onDirectPrintForm1 = {
                showPrintDialog = false
                viewModel.directPrintForm1(context)
            },
            onPdfExportForm1 = {
                showPrintDialog = false
                viewModel.printForm1Diary(context)
            },
            onViewForm2 = {
                showPrintDialog = false
                viewModel.setActiveTab(2)
            },
            onDirectPrintForm2 = {
                showPrintDialog = false
                viewModel.directPrintForm2(context)
            },
            onPdfExportForm2 = {
                showPrintDialog = false
                viewModel.printForm2TaBill(context)
            }
        )
    }
}
