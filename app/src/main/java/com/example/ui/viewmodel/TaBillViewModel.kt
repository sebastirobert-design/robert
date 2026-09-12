package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AppSettings
import com.example.data.model.OfficerProfile
import com.example.data.model.School
import com.example.data.model.TourEntry
import com.example.data.repository.TaBillRepository
import com.example.util.DateUtils
import com.example.util.PrintExportHelper
import java.util.Calendar
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
            _quickTourInitialMode
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
                quickTourInitialMode = args[10] as String
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
                userFeedbackMessage = ctrl.feedback
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

    fun addNonTravelDayFromDialog(dayOfMonth: Int, dateFormatted: String, type: String, closeDialog: Boolean = false) {
        viewModelScope.launch {
            val state = uiState.value
            repository.addNonTravelDay(
                officerId = state.activeOfficer.id,
                monthYear = state.selectedMonthYear,
                dayOfMonth = dayOfMonth,
                dateFormatted = dateFormatted,
                type = type
            )
            if (closeDialog) {
                closeQuickTourDialog()
            }
            _feedbackMessage.value = "$type ($dateFormatted) சேர்க்கப்பட்டது! (Added)"
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

    fun addNonTravelDay(dayOfMonth: Int, dateFormatted: String, type: String) {
        viewModelScope.launch {
            val state = uiState.value
            repository.addNonTravelDay(
                officerId = state.activeOfficer.id,
                monthYear = state.selectedMonthYear,
                dayOfMonth = dayOfMonth,
                dateFormatted = dateFormatted,
                type = type
            )
            closeQuickNonTravelDialog()
            _feedbackMessage.value = "$type சேர்க்கப்பட்டது! (Added)"
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
    val quickTourInitialMode: String = "TOUR"
)

data class AppFlowData(
    val activeOfficer: OfficerProfile,
    val allOfficers: List<OfficerProfile>,
    val allSchools: List<School>,
    val appSettings: AppSettings,
    val tourEntries: List<TourEntry>
)
