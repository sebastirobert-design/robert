package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import com.example.ui.components.AppBottomNavBar
import com.example.ui.components.AppHeader
import com.example.ui.components.BackupChoiceDialog
import com.example.ui.components.PrintOptionsDialog
import com.example.ui.components.RestorePreviewDialog
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
import com.example.util.DriveBackupHelper

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
    val pendingRestoreData by viewModel.pendingRestoreData.collectAsState()
    val showBackupOptions by viewModel.showBackupOptionsDialog.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showPrintDialog by remember { mutableStateOf(false) }

    // SAF File Pickers for Backup & Restore
    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.saveBackupToUri(context, uri)
        }
    }

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.inspectBackupFromUri(context, uri)
        }
    }

    val importSchoolCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.inspectSchoolCsvFromUri(context, uri)
        }
    }

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
            AppBottomNavBar(
                activeTab = uiState.activeTab,
                isTamil = isTa,
                onTabSelected = { viewModel.setActiveTab(it) }
            )
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
                    onExportToCsv = { viewModel.exportToExcelGoogleSheet(context) },
                    onBackupToDrive = { viewModel.openBackupOptionsDialog() },
                    onRestoreFromDrive = {
                        restoreBackupLauncher.launch(arrayOf("application/json", "*/*"))
                    },
                    onImportSchoolCsv = {
                        importSchoolCsvLauncher.launch(arrayOf("text/*", "text/csv", "application/csv", "*/*"))
                    },
                    onDownloadSchoolCsvTemplate = { viewModel.downloadSchoolCsvTemplate(context) },
                    onExportSchoolsToCsv = { viewModel.exportCurrentSchoolsToCsv(context) },
                    onResetSchoolsToDefault = { viewModel.resetSchoolsToDefault() },
                    onRunAiSchoolValidation = { viewModel.runAiSchoolValidation() },
                    onConfirmImportSchools = { replaceExisting -> viewModel.confirmImportSchools(replaceExisting) },
                    onDismissSchoolCsvDialog = { viewModel.dismissSchoolCsvDialog() },
                    onAutoFixSchoolIssues = { viewModel.autoFixSchoolIssues() },
                    onDismissAiAuditDialog = { viewModel.dismissAiAuditDialog() }
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
            activeOfficerSlot = uiState.activeOfficer?.officerSlot ?: 1,
            initialMode = uiState.quickTourInitialMode,
            onSaveTour = { dayOfMonth, dateFormatted, departureStation, departureHour, destinations, arrivalHourOutbound, returnDepartureHour, returnArrivalHour, purposeOfJourney, kindOfJourney, isRoundTrip, customDistanceKm, customBusFare, remarks, entryToReplace, customArrivalStation ->
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
                    entryToReplace = entryToReplace,
                    customArrivalStation = customArrivalStation
                )
            },
            onAddNonTravel = { dayOfMonth, dateFormatted, type, customReason ->
                viewModel.addNonTravelDayFromDialog(dayOfMonth, dateFormatted, type, customReason)
            },
            onDeleteEntry = { entry ->
                viewModel.deleteTourEntry(entry)
            },
            onUpdateSchool = { school ->
                viewModel.saveSchool(school)
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
            onAddNonTravel = { dayOfMonth, dateFormatted, type, customReason ->
                viewModel.addNonTravelDay(dayOfMonth, dateFormatted, type, customReason)
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

    // Google Drive Backup Choice Dialog
    if (showBackupOptions) {
        BackupChoiceDialog(
            isTamil = isTa,
            onDirectDriveShare = {
                viewModel.backupDirectToGoogleDrive(context)
            },
            onSaveFileSaf = {
                val fileName = DriveBackupHelper.getSuggestedFileName()
                createBackupLauncher.launch(fileName)
            },
            onDismiss = {
                viewModel.closeBackupOptionsDialog()
            }
        )
    }

    // Google Drive Restore Preview & Confirmation Dialog
    pendingRestoreData?.let { backupData ->
        RestorePreviewDialog(
            backupData = backupData,
            isTamil = isTa,
            onConfirmRestore = { replaceAll ->
                viewModel.confirmRestore(replaceExistingTours = replaceAll)
            },
            onDismiss = {
                viewModel.dismissRestoreDialog()
            }
        )
    }
}
