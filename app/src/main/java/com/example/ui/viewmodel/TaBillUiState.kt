package com.example.ui.viewmodel

import com.example.data.model.AppSettings
import com.example.data.model.OfficerProfile
import com.example.data.model.School
import com.example.data.model.TourEntry

data class TaBillUiState(
    val selectedMonthYear: String = "2026-07",
    val displayDate: String = "",
    val displayDay: String = "",
    val displayDayEnglish: String = "",
    val selectedDayOfMonth: Int = 1,
    val activeOfficer: OfficerProfile = OfficerProfile(),
    val allOfficers: List<OfficerProfile> = emptyList(),
    val tourEntries: List<TourEntry> = emptyList(),
    val allSchools: List<School> = emptyList(),
    val appSettings: AppSettings = AppSettings(),
    val distinctMonths: List<String> = listOf("2026-07", "2026-08", "2022-11"),
    val activeTab: Int = 0, // 0: Home, 1: Form 1 Diary, 2: Form 2 TA Bill, 3: Settings
    val settingsSubTab: Int = 0, // 0: Transport & Rates, 1: Officer, 2: Schools
    val isLoading: Boolean = false,
    val showQuickTourDialog: Boolean = false,
    val showQuickNonTravelDialog: Boolean = false,
    val quickTourInitialMode: String = "TOUR", // "TOUR" or "LEAVE"
    val showSchoolPickerSheet: Boolean = false,
    val editingTourEntry: TourEntry? = null,
    val schoolSearchQuery: String = "",
    val userFeedbackMessage: String? = null
) {
    val isTamil: Boolean get() = appSettings.language == "ta"

    val displayMonthTamil: String
        get() = com.example.util.DateUtils.getTamilMonthDisplay(selectedMonthYear)

    val displayMonthEnglish: String
        get() = com.example.util.DateUtils.getEnglishMonthDisplay(selectedMonthYear)

    val travelEntries: List<TourEntry>
        get() = tourEntries.filter { it.isTaEligible }

    val totalKm: Int
        get() = travelEntries.sumOf { it.distanceKm }

    val totalBusFare: Double
        get() = travelEntries.sumOf { it.busFare }

    val totalDaAmount: Double
        get() = travelEntries.sumOf { it.daAmount }

    val totalTerminalCharges: Double
        get() = travelEntries.sumOf { it.terminalCharge17a + it.terminalCharge17b }

    val totalIncidental: Double
        get() = travelEntries.sumOf { it.incidentalCharges }

    val grandTotal: Double
        get() = travelEntries.sumOf { it.grandTotal }

    val totalToursCount: Int
        get() = travelEntries.size
}
