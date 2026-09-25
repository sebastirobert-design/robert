package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AppSettings
import com.example.data.model.OfficerProfile
import com.example.data.model.School
import com.example.data.model.TourEntry
import com.example.data.repository.TaBillRepository
import com.example.util.DateUtils
import com.example.util.DriveBackupHelper
import com.example.util.PrintExportHelper
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class TaBillViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaBillRepository

    // இயல்பான நடப்புத் தேதி முறை (let selectedDate = new Date(); // இன்றைய தேதி தானாக வரும்)
    private val _selectedCalendar = MutableStateFlow<Calendar>(DateUtils.getCurrentCalendar())
    private val _selectedMonthYear = MutableStateFlow<String>(DateUtils.getCurrentMonthYear())
    private val _activeTab = MutableStateFlow<Int>(0)
    private val _settingsSubTab = MutableStateFlow<Int>(0)
    val settingsSubTab: StateFlow<Int> = _settingsSubTab.asStateFlow()
    private val _showQuickTourDialog = MutableStateFlow<Boolean>(false)
    private val _showQuickNonTravelDialog = MutableStateFlow<Boolean>(false)
    private val _showSchoolPickerSheet = MutableStateFlow<Boolean>(false)
    private val _editingTourEntry = MutableStateFlow<TourEntry?>(null)
    private val _quickTourInitialMode = MutableStateFlow<String>("TOUR")
    private val _schoolSearchQuery = MutableStateFlow<String>("")
    private val _feedbackMessage = MutableStateFlow<String?>(null)

    // Google Drive Backup & Restore States
    private val _pendingRestoreData = MutableStateFlow<DriveBackupHelper.BackupData?>(null)
    val pendingRestoreData: StateFlow<DriveBackupHelper.BackupData?> = _pendingRestoreData.asStateFlow()

    private val _showBackupOptionsDialog = MutableStateFlow<Boolean>(false)
    val showBackupOptionsDialog: StateFlow<Boolean> = _showBackupOptionsDialog.asStateFlow()

    // Schools CSV & AI Verification States
    private val _pendingCsvSchools = MutableStateFlow<com.example.util.SchoolCsvHelper.ParseSchoolResult?>(null)
    private val _showSchoolCsvImportDialog = MutableStateFlow<Boolean>(false)
    private val _aiAuditReport = MutableStateFlow<com.example.util.SchoolAiValidator.AiAuditReport?>(null)
    private val _isAiValidating = MutableStateFlow<Boolean>(false)
    private val _showAiAuditDialog = MutableStateFlow<Boolean>(false)

    val uiState: StateFlow<TaBillUiState>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TaBillRepository(database.taBillDao())

        viewModelScope.launch {
            repository.initializeDefaultDataIfNeeded()
        }

        val activeOfficerFlow = repository.activeOfficer
        val allOfficersFlow = repository.allOfficers
        val allSchoolsFlow = repository.allSchools
        val appSettingsFlow = repository.appSettings

        val tourEntriesFlow = combine(activeOfficerFlow, _selectedMonthYear) { officer, month ->
            Pair(officer?.id ?: 1L, month)
        }.flatMapLatest { (officerId, month) ->
            repository.getTourEntriesForMonth(officerId, month)
        }

        val controlStateFlow: Flow<DialogControlState> = combine(
            _selectedCalendar,
            _selectedMonthYear,
            _activeTab,
            _settingsSubTab,
            _showQuickTourDialog,
            _showQuickNonTravelDialog,
            _showSchoolPickerSheet,
            _editingTourEntry,
            _schoolSearchQuery,
            _feedbackMessage,
            _quickTourInitialMode,
            _pendingCsvSchools,
            _showSchoolCsvImportDialog,
            _aiAuditReport,
            _isAiValidating,
            _showAiAuditDialog
        ) { args: Array<Any?> ->
            DialogControlState(
                selectedCalendar = args[0] as Calendar,
                selectedMonthYear = args[1] as String,
                activeTab = args[2] as Int,
                settingsSubTab = args[3] as Int,
                showQuickTour = args[4] as Boolean,
                showNonTravel = args[5] as Boolean,
                showSchoolPicker = args[6] as Boolean,
                editingEntry = args[7] as TourEntry?,
                searchQuery = args[8] as String,
                feedback = args[9] as String?,
                quickTourInitialMode = args[10] as String,
                pendingCsvSchools = args[11] as com.example.util.SchoolCsvHelper.ParseSchoolResult?,
                showSchoolCsvImportDialog = args[12] as Boolean,
                aiAuditReport = args[13] as com.example.util.SchoolAiValidator.AiAuditReport?,
                isAiValidating = args[14] as Boolean,
                showAiAuditDialog = args[15] as Boolean
            )
        }

        val appDataFlow: Flow<AppFlowData> = combine(
            activeOfficerFlow,
            allOfficersFlow,
            allSchoolsFlow,
            appSettingsFlow,
            tourEntriesFlow
        ) { activeOff, allOff, schools, settings, entries ->
            AppFlowData(
                activeOfficer = activeOff ?: OfficerProfile(),
                allOfficers = allOff,
                allSchools = schools,
                appSettings = settings ?: AppSettings(),
                tourEntries = entries
            )
        }

        uiState = combine(controlStateFlow, appDataFlow) { ctrl, data ->
            val cal = ctrl.selectedCalendar
            val displayDate = DateUtils.formatDisplayDate(cal)
            val displayDay = DateUtils.getDayNameTamil(cal)
            val displayDayEn = DateUtils.getDayNameEnglish(cal)
            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)

            val monthList = mutableListOf<String>()
            monthList.add(DateUtils.getCurrentMonthYear())
            monthList.add(ctrl.selectedMonthYear)
            monthList.addAll(listOf("2026-07", "2026-08", "2026-09", "2026-10", "2026-11", "2026-12", "2022-11"))

            TaBillUiState(
                selectedMonthYear = ctrl.selectedMonthYear,
                displayDate = displayDate,
                displayDay = displayDay,
                displayDayEnglish = displayDayEn,
                selectedDayOfMonth = dayOfMonth,
                distinctMonths = monthList.distinct(),
                activeOfficer = data.activeOfficer,
                allOfficers = data.allOfficers,
                tourEntries = data.tourEntries,
                allSchools = data.allSchools,
                appSettings = data.appSettings,
                activeTab = ctrl.activeTab,
                settingsSubTab = ctrl.settingsSubTab,
                showQuickTourDialog = ctrl.showQuickTour,
                showQuickNonTravelDialog = ctrl.showNonTravel,
                quickTourInitialMode = ctrl.quickTourInitialMode,
                showSchoolPickerSheet = ctrl.showSchoolPicker,
                editingTourEntry = ctrl.editingEntry,
                schoolSearchQuery = ctrl.searchQuery,
                userFeedbackMessage = ctrl.feedback,
                pendingCsvSchools = ctrl.pendingCsvSchools,
                showSchoolCsvImportDialog = ctrl.showSchoolCsvImportDialog,
                aiAuditReport = ctrl.aiAuditReport,
                isAiValidating = ctrl.isAiValidating,
                showAiAuditDialog = ctrl.showAiAuditDialog
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TaBillUiState()
        )
    }

    // முந்தைய / அடுத்த மாதம் மாற்றும் வசதி (changeMonth(direction))
    // loadMonthlyTours: _selectedMonthYear update triggers tourEntriesFlow automatically
    fun changeMonth(direction: Int) {
        val current = _selectedCalendar.value
        val newCal = Calendar.getInstance().apply {
            timeInMillis = current.timeInMillis
            add(Calendar.MONTH, direction)
        }
        _selectedCalendar.value = newCal
        _selectedMonthYear.value = DateUtils.getMonthYear(newCal)
    }

    fun setSelectedDate(calendar: Calendar) {
        _selectedCalendar.value = calendar
        _selectedMonthYear.value = DateUtils.getMonthYear(calendar)
    }

    fun setSelectedDate(year: Int, month: Int, day: Int) {
        val newCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
        }
        _selectedCalendar.value = newCal
        _selectedMonthYear.value = DateUtils.getMonthYear(newCal)
    }

    fun setSelectedDayOfMonth(day: Int) {
        val current = _selectedCalendar.value
        val newCal = Calendar.getInstance().apply {
            timeInMillis = current.timeInMillis
            val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, day.coerceIn(1, maxDay))
        }
        _selectedCalendar.value = newCal
        _selectedMonthYear.value = DateUtils.getMonthYear(newCal)
    }

    fun resetToToday() {
        val today = DateUtils.getCurrentCalendar()
        _selectedCalendar.value = today
        _selectedMonthYear.value = DateUtils.getMonthYear(today)
    }

    fun setSelectedMonthYear(monthYear: String) {
        _selectedMonthYear.value = monthYear
        val (y, m) = DateUtils.parseYearMonth(monthYear)
        val current = _selectedCalendar.value
        val newCal = Calendar.getInstance().apply {
            timeInMillis = current.timeInMillis
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m - 1)
            val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
            if (get(Calendar.DAY_OF_MONTH) > maxDay) {
                set(Calendar.DAY_OF_MONTH, maxDay)
            }
        }
        _selectedCalendar.value = newCal
    }

    fun setActiveTab(tabIndex: Int) {
        _activeTab.value = tabIndex
    }

    fun setSettingsSubTab(subTabIndex: Int) {
        _settingsSubTab.value = subTabIndex
    }

    fun openQuickTourDialog(entryToEdit: TourEntry? = null, initialMode: String = "TOUR") {
        _editingTourEntry.value = entryToEdit
        _quickTourInitialMode.value = if (entryToEdit?.isNonTravel == true) "LEAVE" else initialMode
        _showQuickTourDialog.value = true
    }

    fun closeQuickTourDialog() {
        _showQuickTourDialog.value = false
        _editingTourEntry.value = null
    }

    fun addNonTravelDayFromDialog(dayOfMonth: Int, dateFormatted: String, type: String, customReason: String? = null, closeDialog: Boolean = false) {
        viewModelScope.launch {
            val state = uiState.value
            repository.addNonTravelDay(
                officerId = state.activeOfficer.id,
                monthYear = state.selectedMonthYear,
                dayOfMonth = dayOfMonth,
                dateFormatted = dateFormatted,
                type = type,
                customReason = customReason
            )
            if (closeDialog) {
                closeQuickTourDialog()
            }
            val displayLabel = customReason?.trim()?.ifBlank { null } ?: type
            _feedbackMessage.value = "$displayLabel ($dateFormatted) சேர்க்கப்பட்டது! (Added)"
        }
    }

    fun openQuickNonTravelDialog() {
        _showQuickNonTravelDialog.value = true
    }

    fun closeQuickNonTravelDialog() {
        _showQuickNonTravelDialog.value = false
    }

    fun setSchoolPickerVisible(visible: Boolean) {
        _showSchoolPickerSheet.value = visible
    }

    fun setSchoolSearchQuery(query: String) {
        _schoolSearchQuery.value = query
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    fun saveQuickTour(
        dayOfMonth: Int,
        dateFormatted: String,
        departureStation: String,
        departureHour: String,
        destinations: List<School>,
        arrivalHourOutbound: String,
        returnDepartureHour: String,
        returnArrivalHour: String,
        purposeOfJourney: String,
        kindOfJourney: String,
        isRoundTrip: Boolean,
        customDistanceKm: Int?,
        customBusFare: Double?,
        remarks: String,
        entryToReplace: TourEntry? = null,
        customArrivalStation: String? = null
    ) {
        viewModelScope.launch {
            val state = uiState.value
            val officerId = state.activeOfficer.id
            val daRate = state.appSettings.defaultDaRate
            val daAmount = state.appSettings.defaultDaAmount
            val t17a = state.appSettings.defaultTerminal17a
            val t17b = state.appSettings.defaultTerminal17b

            val toReplace = entryToReplace ?: _editingTourEntry.value
            if (toReplace != null) {
                repository.deleteTourEntry(toReplace)
            }

            repository.addQuickTourTrip(
                officerId = officerId,
                monthYear = state.selectedMonthYear,
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
                daRate = daRate,
                daAmount = daAmount,
                terminal17a = t17a,
                terminal17b = t17b,
                customDistanceKm = customDistanceKm,
                customBusFare = customBusFare,
                remarks = remarks,
                customArrivalStation = customArrivalStation
            )
            closeQuickTourDialog()
            _feedbackMessage.value = if (toReplace != null) {
                "பயண விவரம் வெற்றிகரமாக மாற்றப்பட்டது! (Tour Updated)"
            } else {
                "பயணம் வெற்றிகரமாக சேர்க்கப்பட்டது! (Tour Added)"
            }
        }
    }

    fun addNonTravelDay(dayOfMonth: Int, dateFormatted: String, type: String, customReason: String? = null) {
        viewModelScope.launch {
            val state = uiState.value
            repository.addNonTravelDay(
                officerId = state.activeOfficer.id,
                monthYear = state.selectedMonthYear,
                dayOfMonth = dayOfMonth,
                dateFormatted = dateFormatted,
                type = type,
                customReason = customReason
            )
            closeQuickNonTravelDialog()
            val displayLabel = customReason?.trim()?.ifBlank { null } ?: type
            _feedbackMessage.value = "$displayLabel சேர்க்கப்பட்டது! (Added)"
        }
    }

    fun updateTourEntry(entry: TourEntry) {
        viewModelScope.launch {
            val updated = entry.copy(grandTotal = entry.calculatedGrandTotal())
            repository.updateTourEntry(updated)
            closeQuickTourDialog()
            _feedbackMessage.value = "பயண விவரம் புதுப்பிக்கப்பட்டது (Updated)"
        }
    }

    fun deleteTourEntry(entry: TourEntry) {
        viewModelScope.launch {
            repository.deleteTourEntry(entry)
            _feedbackMessage.value = "பயணம் நீக்கப்பட்டது (Deleted)"
        }
    }

    fun clearCurrentMonthEntries() {
        viewModelScope.launch {
            val state = uiState.value
            repository.clearMonthEntries(state.activeOfficer.id, state.selectedMonthYear)
            _feedbackMessage.value = "மாதப் பதிவுகள் அழிக்கப்பட்டன (Cleared)"
        }
    }

    fun switchOfficer(officerId: Long) {
        viewModelScope.launch {
            repository.setActiveOfficer(officerId)
            _feedbackMessage.value = "அலுவலர் மாற்றப்பட்டார் (Officer Switched)"
        }
    }

    fun updateOfficerProfile(officer: OfficerProfile) {
        viewModelScope.launch {
            repository.updateOfficer(officer)
            _feedbackMessage.value = "அலுவலர் விவரம் சேமிக்கப்பட்டது (Saved)"
        }
    }

    fun updateSettings(settings: AppSettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
            _feedbackMessage.value = "அமைப்புகள் சேமிக்கப்பட்டன (Settings Saved)"
        }
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            val current = uiState.value.appSettings
            val nextLang = if (current.language == "ta") "en" else "ta"
            repository.updateSettings(current.copy(language = nextLang))
        }
    }

    fun saveSchool(school: School) {
        viewModelScope.launch {
            if (school.id == 0L) {
                repository.insertSchool(school)
            } else {
                repository.updateSchool(school)
            }
            _feedbackMessage.value = "பள்ளி விவரம் சேமிக்கப்பட்டது (School Saved)"
        }
    }

    fun deleteSchool(school: School) {
        viewModelScope.launch {
            repository.deleteSchool(school)
            _feedbackMessage.value = "பள்ளி நீக்கப்பட்டது (School Deleted)"
        }
    }

    fun printForm1Diary(context: Context) {
        try {
            val state = uiState.value
            val msg = PrintExportHelper.exportOrPrintForm1(
                context = context,
                officer = state.activeOfficer,
                monthYear = state.selectedMonthYear,
                entries = state.tourEntries,
                isTamil = state.isTamil
            )
            _feedbackMessage.value = msg
        } catch (e: Throwable) {
            e.printStackTrace()
            _feedbackMessage.value = if (uiState.value.isTamil) "PDF ஏற்றுமதி பிழை: ${e.localizedMessage}" else "PDF export error: ${e.localizedMessage}"
        }
    }

    fun directPrintForm1(context: Context) {
        try {
            val state = uiState.value
            val html = PrintExportHelper.generateForm1DiaryHtml(
                officer = state.activeOfficer,
                monthYear = state.selectedMonthYear,
                entries = state.tourEntries
            )
            PrintExportHelper.printHtmlDocument(
                context = context,
                htmlContent = html,
                jobName = "Tour_Diary_${state.selectedMonthYear}"
            )
            _feedbackMessage.value = if (state.isTamil) "படிவம் 1 அச்சு அனுப்பப்பட்டது" else "Form 1 sent to printer"
        } catch (e: Throwable) {
            e.printStackTrace()
            _feedbackMessage.value = "Print error: ${e.localizedMessage}"
        }
    }

    fun printForm2TaBill(context: Context) {
        try {
            val state = uiState.value
            val msg = PrintExportHelper.exportOrPrintForm2(
                context = context,
                officer = state.activeOfficer,
                monthYear = state.selectedMonthYear,
                entries = state.tourEntries,
                isTamil = state.isTamil
            )
            _feedbackMessage.value = msg
        } catch (e: Throwable) {
            e.printStackTrace()
            _feedbackMessage.value = if (uiState.value.isTamil) "PDF ஏற்றுமதி பிழை: ${e.localizedMessage}" else "PDF export error: ${e.localizedMessage}"
        }
    }

    fun directPrintForm2(context: Context) {
        try {
            val state = uiState.value
            val html = PrintExportHelper.generateForm2TaBillHtml(
                officer = state.activeOfficer,
                monthYear = state.selectedMonthYear,
                entries = state.tourEntries
            )
            PrintExportHelper.printHtmlDocument(
                context = context,
                htmlContent = html,
                jobName = "TA_Bill_${state.selectedMonthYear}",
                isLandscape = true
            )
            _feedbackMessage.value = if (state.isTamil) "படிவம் 2 அச்சு அனுப்பப்பட்டது" else "Form 2 sent to printer"
        } catch (e: Throwable) {
            e.printStackTrace()
            _feedbackMessage.value = "Print error: ${e.localizedMessage}"
        }
    }

    fun shareOnWhatsApp(context: Context) {
        val state = uiState.value
        PrintExportHelper.shareWhatsAppSummary(
            context = context,
            officer = state.activeOfficer,
            monthYear = state.selectedMonthYear,
            entries = state.tourEntries
        )
    }

    fun exportForm1Csv(context: Context) {
        val state = uiState.value
        PrintExportHelper.exportForm1ToCSV(
            context = context,
            tourRecords = state.tourEntries,
            fileName = "Form_1_Calendar.csv"
        )
        _feedbackMessage.value = if (state.isTamil) {
            "படிவம் 1 (நாள்காட்டி - 9 நெடுவரிசைகள்) CSV பதிவிறக்கம் செய்யப்பட்டது"
        } else {
            "Form 1 (Calendar - 9 Columns) CSV downloaded"
        }
    }

    fun exportForm2Csv(context: Context) {
        val state = uiState.value
        PrintExportHelper.exportForm2ToCSV(
            context = context,
            tourRecords = state.tourEntries,
            currentMonth = state.selectedMonthYear,
            officer = state.activeOfficer,
            fileName = "Form_2_TA_Bill.csv"
        )
        _feedbackMessage.value = if (state.isTamil) {
            "படிவம் 2 (TA Bill - 20 நெடுவரிசைகள்) CSV பதிவிறக்கம் செய்யப்பட்டது"
        } else {
            "Form 2 (TA Bill - 20 Columns) CSV downloaded"
        }
    }

    fun exportToExcelGoogleSheet(context: Context) {
        val state = uiState.value
        PrintExportHelper.exportToExcelGoogleSheet(
            context = context,
            monthYear = state.selectedMonthYear,
            entries = state.tourEntries,
            officer = state.activeOfficer
        )
    }

    // ==================== GOOGLE DRIVE BACKUP & RESTORE ====================

    fun openBackupOptionsDialog() {
        _showBackupOptionsDialog.value = true
    }

    fun closeBackupOptionsDialog() {
        _showBackupOptionsDialog.value = false
    }

    /**
     * Creates a full JSON backup file and opens Android's native share sheet targeting Google Drive / Files / Messaging
     */
    fun backupDirectToGoogleDrive(context: Context) {
        viewModelScope.launch {
            try {
                val payload = repository.getAllDataForBackup()
                val json = DriveBackupHelper.createBackupJson(
                    officers = payload.officers,
                    settings = payload.settings,
                    schools = payload.schools,
                    tourEntries = payload.tours
                )
                val file = DriveBackupHelper.createBackupFile(context, json)
                val shareIntent = DriveBackupHelper.createShareToDriveIntent(context, file, uiState.value.isTamil)
                context.startActivity(shareIntent)
                _feedbackMessage.value = if (uiState.value.isTamil) {
                    "Google Drive-ல் சேமிக்க பகிர்தல் திறக்கப்பட்டது (${payload.tours.size} பயணங்கள், ${payload.officers.size} அலுவலர்கள்)"
                } else {
                    "Opened Google Drive save sheet (${payload.tours.size} tours, ${payload.officers.size} officers)"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _feedbackMessage.value = "Backup error: ${e.localizedMessage}"
            }
        }
    }

    /**
     * Saves full backup JSON to a SAF Document Uri (e.g. chosen Google Drive folder via document picker)
     */
    fun saveBackupToUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val payload = repository.getAllDataForBackup()
                val json = DriveBackupHelper.createBackupJson(
                    officers = payload.officers,
                    settings = payload.settings,
                    schools = payload.schools,
                    tourEntries = payload.tours
                )
                val success = DriveBackupHelper.writeJsonToUri(context, uri, json)
                if (success) {
                    _feedbackMessage.value = if (uiState.value.isTamil) {
                        "கூகிள் டிரைவில் / கோப்பில் காப்புநகல் வெற்றிகரமாகச் சேமிக்கப்பட்டது! (${payload.tours.size} பயணங்கள்)"
                    } else {
                        "Backup saved successfully to Google Drive / file! (${payload.tours.size} tours)"
                    }
                } else {
                    _feedbackMessage.value = if (uiState.value.isTamil) "கோப்பைச் சேமிக்க இயலவில்லை" else "Failed to save backup file"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _feedbackMessage.value = "Save error: ${e.localizedMessage}"
            }
        }
    }

    /**
     * Reads and parses a backup file picked from Google Drive / storage, showing a preview dialog
     */
    fun inspectBackupFromUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonString = DriveBackupHelper.readJsonFromUri(context, uri)
                if (jsonString.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        _feedbackMessage.value = if (uiState.value.isTamil) "காப்புநகல் கோப்பைப் படிக்க இயலவில்லை" else "Could not read backup file"
                    }
                    return@launch
                }

                val parseResult = DriveBackupHelper.parseBackupJson(jsonString)
                parseResult.onSuccess { data ->
                    withContext(Dispatchers.Main) {
                        _pendingRestoreData.value = data
                    }
                }.onFailure { err ->
                    withContext(Dispatchers.Main) {
                        _feedbackMessage.value = if (uiState.value.isTamil) {
                            "தவறான காப்புநகல் வடிவம்: ${err.localizedMessage}"
                        } else {
                            "Invalid backup format: ${err.localizedMessage}"
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _feedbackMessage.value = "Error reading backup: ${e.localizedMessage}"
                }
            }
        }
    }

    /**
     * Executes the restoration of verified backup data into the local Room database
     */
    fun confirmRestore(replaceExistingTours: Boolean = true) {
        val data = _pendingRestoreData.value ?: return
        viewModelScope.launch {
            try {
                repository.restoreBackupData(
                    tours = data.tourEntries,
                    schools = data.schools,
                    officers = data.officers,
                    settings = data.settings,
                    replaceExistingTours = replaceExistingTours
                )
                _pendingRestoreData.value = null
                _feedbackMessage.value = if (uiState.value.isTamil) {
                    "மீட்டெடுப்பு முடிந்தது! (${data.tourCount} பயணங்கள், ${data.officerCount} அலுவலர்கள், ${data.schoolCount} பள்ளிகள் புதுப்பிக்கப்பட்டன)"
                } else {
                    "Restore complete! (${data.tourCount} tours, ${data.officerCount} officers, ${data.schoolCount} schools restored)"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _feedbackMessage.value = "Restore error: ${e.localizedMessage}"
            }
        }
    }

    fun dismissRestoreDialog() {
        _pendingRestoreData.value = null
    }

    // ==================== SCHOOLS CSV MANAGEMENT ====================

    fun downloadSchoolCsvTemplate(context: Context) {
        val template = com.example.util.SchoolCsvHelper.generateSampleSchoolCsvTemplate()
        com.example.util.SchoolCsvHelper.shareCsvFile(
            context = context,
            csvContent = template,
            fileName = "Schools_Template_TN_BEO.csv",
            title = if (uiState.value.isTamil) "மாதிரி பள்ளிகள் CSV கோப்பு" else "Schools Sample CSV Template"
        )
        _feedbackMessage.value = if (uiState.value.isTamil) {
            "மாதிரி பள்ளிகள் CSV கோப்பு பகிரப்பட்டது (Google Sheets / Excel-ல் திறந்து உங்கள் ஒன்றிய பள்ளிகளை நிரப்பலாம்)"
        } else {
            "Sample schools CSV template shared"
        }
    }

    fun exportCurrentSchoolsToCsv(context: Context) {
        val schools = uiState.value.allSchools
        if (schools.isEmpty()) {
            _feedbackMessage.value = if (uiState.value.isTamil) "ஏற்றுமதி செய்ய பள்ளிகள் ஏதுமில்லை" else "No schools to export"
            return
        }
        val csv = com.example.util.SchoolCsvHelper.exportSchoolsToCsv(schools)
        val fileName = "BEO_Schools_${DateUtils.getCurrentMonthYear().replace("-", "_")}.csv"
        com.example.util.SchoolCsvHelper.shareCsvFile(
            context = context,
            csvContent = csv,
            fileName = fileName,
            title = if (uiState.value.isTamil) "பள்ளிகள் பட்டியல் CSV" else "Schools Directory CSV"
        )
        _feedbackMessage.value = if (uiState.value.isTamil) {
            "${schools.size} பள்ளிகள் CSV கோப்பாக ஏற்றுமதி செய்யப்பட்டது"
        } else {
            "${schools.size} schools exported as CSV"
        }
    }

    fun inspectSchoolCsvFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val csvContent = com.example.util.SchoolCsvHelper.readCsvFromUri(context, uri)
                val parseResult = com.example.util.SchoolCsvHelper.parseSchoolsFromCsv(csvContent)
                _pendingCsvSchools.value = parseResult
                _showSchoolCsvImportDialog.value = true
            } catch (e: Exception) {
                _feedbackMessage.value = "CSV படிப்பதில் பிழை: ${e.localizedMessage}"
            }
        }
    }

    fun confirmImportSchools(replaceExisting: Boolean) {
        val pending = _pendingCsvSchools.value ?: return
        val newSchools = pending.schools
        if (newSchools.isEmpty()) {
            _showSchoolCsvImportDialog.value = false
            _pendingCsvSchools.value = null
            return
        }

        viewModelScope.launch {
            try {
                if (replaceExisting) {
                    repository.replaceAllSchools(newSchools)
                } else {
                    repository.insertSchools(newSchools)
                }
                _showSchoolCsvImportDialog.value = false
                _pendingCsvSchools.value = null
                _feedbackMessage.value = if (uiState.value.isTamil) {
                    if (replaceExisting) {
                        "பழைய பள்ளிகள் நீக்கப்பட்டு புதிய ${newSchools.size} பள்ளிகள் தலைமையிட தூரத்தோடு வெற்றிகரமாக ஏற்றப்பட்டன!"
                    } else {
                        "புதிய ${newSchools.size} பள்ளிகள் சேர்க்கப்பட்டன!"
                    }
                } else {
                    "${newSchools.size} schools imported successfully!"
                }
            } catch (e: Exception) {
                _feedbackMessage.value = "பள்ளிகள் பதிவேற்றத்தில் பிழை: ${e.localizedMessage}"
            }
        }
    }

    fun dismissSchoolCsvDialog() {
        _showSchoolCsvImportDialog.value = false
        _pendingCsvSchools.value = null
    }

    fun resetSchoolsToDefault() {
        viewModelScope.launch {
            try {
                repository.resetSchoolsToDefault()
                _feedbackMessage.value = if (uiState.value.isTamil) {
                    "மாதிரி பள்ளிகள் (119 பள்ளிகள்) மீட்டமைக்கப்பட்டது"
                } else {
                    "Default schools restored"
                }
            } catch (e: Exception) {
                _feedbackMessage.value = "Error: ${e.localizedMessage}"
            }
        }
    }

    // ==================== AI SCHOOL VALIDATION ====================

    fun runAiSchoolValidation() {
        viewModelScope.launch {
            _isAiValidating.value = true
            _showAiAuditDialog.value = true
            try {
                val report = com.example.util.SchoolAiValidator.auditSchools(uiState.value.allSchools)
                _aiAuditReport.value = report
            } catch (e: Exception) {
                _feedbackMessage.value = "AI சரிபார்ப்பில் பிழை: ${e.localizedMessage}"
            } finally {
                _isAiValidating.value = false
            }
        }
    }

    fun dismissAiAuditDialog() {
        _showAiAuditDialog.value = false
    }

    fun autoFixSchoolIssues() {
        viewModelScope.launch {
            try {
                val currentSchools = uiState.value.allSchools
                var fixedCount = 0
                val updatedSchools = currentSchools.map { school ->
                    var changed = false
                    var fixedTown = school.villageTa
                    var fixedFare = school.defaultBusFare

                    if (fixedTown.isBlank()) {
                        fixedTown = School.extractVillageName(school.nameTa)
                        changed = true
                    }
                    if (school.distanceFromHqKm == 0 && fixedFare > 0) {
                        fixedFare = 0
                        changed = true
                    } else if (school.distanceFromHqKm > 5 && fixedFare == 0) {
                        fixedFare = when {
                            school.distanceFromHqKm <= 10 -> 10
                            school.distanceFromHqKm <= 18 -> 15
                            school.distanceFromHqKm <= 25 -> 20
                            else -> 25
                        }
                        changed = true
                    }

                    if (changed) {
                        fixedCount++
                        school.copy(
                            villageTa = fixedTown,
                            villageEn = if (school.villageEn.isBlank()) School.extractVillageName(school.nameEn) else school.villageEn,
                            defaultBusFare = fixedFare
                        )
                    } else {
                        school
                    }
                }

                if (fixedCount > 0) {
                    repository.replaceAllSchools(updatedSchools)
                    val report = com.example.util.SchoolAiValidator.auditSchools(updatedSchools)
                    _aiAuditReport.value = report
                    _feedbackMessage.value = if (uiState.value.isTamil) {
                        "$fixedCount பள்ளிகளின் முரண்பாடுகள் தானாக சரிசெய்யப்பட்டன!"
                    } else {
                        "Auto-fixed $fixedCount school issues!"
                    }
                } else {
                    _feedbackMessage.value = if (uiState.value.isTamil) "சரிசெய்ய வேண்டிய முரண்பாடுகள் ஏதுமில்லை" else "No issues to fix"
                }
            } catch (e: Exception) {
                _feedbackMessage.value = "Auto-fix error: ${e.localizedMessage}"
            }
        }
    }
}

// Helper data classes for state combination
data class DialogControlState(
    val selectedCalendar: Calendar,
    val selectedMonthYear: String,
    val activeTab: Int,
    val settingsSubTab: Int,
    val showQuickTour: Boolean,
    val showNonTravel: Boolean,
    val showSchoolPicker: Boolean,
    val editingEntry: TourEntry?,
    val searchQuery: String,
    val feedback: String?,
    val quickTourInitialMode: String = "TOUR",
    val pendingCsvSchools: com.example.util.SchoolCsvHelper.ParseSchoolResult? = null,
    val showSchoolCsvImportDialog: Boolean = false,
    val aiAuditReport: com.example.util.SchoolAiValidator.AiAuditReport? = null,
    val isAiValidating: Boolean = false,
    val showAiAuditDialog: Boolean = false
)

data class AppFlowData(
    val activeOfficer: OfficerProfile,
    val allOfficers: List<OfficerProfile>,
    val allSchools: List<School>,
    val appSettings: AppSettings,
    val tourEntries: List<TourEntry>
)
