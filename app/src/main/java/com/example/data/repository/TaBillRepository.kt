package com.example.data.repository

import com.example.data.local.TaBillDao
import com.example.data.model.AppSettings
import com.example.data.model.OfficerProfile
import com.example.data.model.School
import com.example.data.model.SchoolSeedData
import com.example.data.model.TourEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import com.example.util.DateUtils

class TaBillRepository(private val dao: TaBillDao) {

    val allSchools: Flow<List<School>> = dao.getAllSchools()
    val allOfficers: Flow<List<OfficerProfile>> = dao.getAllOfficers()
    val activeOfficer: Flow<OfficerProfile?> = dao.getActiveOfficer()
    val appSettings: Flow<AppSettings?> = dao.getSettings()

    fun getTourEntriesForMonth(officerId: Long, monthYear: String): Flow<List<TourEntry>> {
        return dao.getTourEntriesForMonth(officerId, monthYear)
    }

    fun getDistinctMonths(officerId: Long): Flow<List<String>> {
        return dao.getDistinctMonths(officerId)
    }

    suspend fun initializeDefaultDataIfNeeded() = withContext(Dispatchers.IO) {
        // Seed schools if empty or refresh if old categories or empty village names exist
        if (dao.getSchoolCount() == 0 || dao.getOldCategorySchoolCount() > 0 || dao.getSchoolsWithEmptyVillageCount() > 0) {
            dao.deleteAllSchools()
            dao.insertSchools(SchoolSeedData.initialSchools)
        }

        // Seed officers if empty
        if (dao.getOfficerCount() == 0) {
            dao.insertOfficers(SchoolSeedData.initialOfficers)
        }

        // Seed settings if empty
        val existingSettings = dao.getSettings()
        dao.insertOrUpdateSettings(
            AppSettings(
                id = 1,
                language = "ta",
                defaultDaRate = 300.0,
                defaultDaAmount = 210.0,
                defaultTerminal17a = 20.0,
                defaultTerminal17b = 20.0,
                defaultIncidental = 0.0,
                baseHeadquartersTa = "தலைமையிடம்",
                baseHeadquartersEn = "Headquarters",
                selectedOfficerId = 1L
            )
        )

        // Seed July 2026 sample entries if table is completely empty
        val allEntries = dao.getTourEntryById(1L)
        if (allEntries == null) {
            dao.insertTourEntries(SchoolSeedData.sampleMonthJuly2026Entries)

            // Also seed current month if different from 2026-07 so user has instant live data
            val currentMonth = com.example.util.DateUtils.getCurrentMonthYear()
            if (currentMonth != "2026-07") {
                val (curY, curM) = com.example.util.DateUtils.parseYearMonth(currentMonth)
                val currentMonthEntries = SchoolSeedData.sampleMonthJuly2026Entries.map { entry ->
                    val day = entry.dayOfMonth.coerceIn(1, 28)
                    val dateFormatted = com.example.util.DateUtils.formatDate(curY, curM, day)
                    entry.copy(
                        id = 0,
                        monthYear = currentMonth,
                        departureDate = if (entry.departureDate.isNotEmpty()) dateFormatted else "",
                        arrivalDate = if (entry.arrivalDate.isNotEmpty()) dateFormatted else ""
                    )
                }
                dao.insertTourEntries(currentMonthEntries)
            }
        }
    }

    suspend fun addQuickTourTrip(
        officerId: Long,
        monthYear: String,
        dayOfMonth: Int,
        dateFormatted: String, // e.g. "02.07.2026"
        departureStation: String, // default "தலைமையிடம்"
        departureHour: String, // "09:00 AM"
        destinations: List<School>, // 1 or more schools
        arrivalHourOutbound: String, // "09:30 AM"
        returnDepartureHour: String, // "04:10 PM"
        returnArrivalHour: String, // "05:45 PM"
        purposeOfJourney: String, // "பள்ளிபார்வை"
        kindOfJourney: String, // "பேருந்து"
        isRoundTrip: Boolean,
        daRate: Double,
        daAmount: Double,
        terminal17a: Double,
        terminal17b: Double,
        customDistanceKm: Int? = null,
        customBusFare: Double? = null,
        remarks: String = "",
        customArrivalStation: String? = null
    ) = withContext(Dispatchers.IO) {
        val tripGroupId = UUID.randomUUID().toString()
        val entriesToInsert = mutableListOf<TourEntry>()

        if (destinations.isEmpty()) return@withContext

        // Single or chained destinations - ONLY the town/village name (ஊரின் பெயர் மட்டும்)
        val destinationVillageName = if (!customArrivalStation.isNullOrBlank()) {
            customArrivalStation.trim()
        } else {
            destinations.joinToString(".") { it.getStationOrVillageName(isTamil = true) }
        }
        val totalDistanceKm = customDistanceKm ?: destinations.sumOf { it.distanceKm() }
        val farePerLeg = customBusFare ?: destinations.sumOf { it.defaultBusFare.toDouble() }

        val validDepHour = DateUtils.formatStrictTime(departureHour, "09:00 AM")
        val validArrHour = DateUtils.formatStrictTime(arrivalHourOutbound, "09:30 AM")
        val validRetDepHour = DateUtils.formatStrictTime(returnDepartureHour, "04:10 PM")
        val validRetArrHour = DateUtils.formatStrictTime(returnArrivalHour, "05:45 PM")

        // 1. OUTBOUND ENTRY
        val outboundGrandTotal = farePerLeg + daAmount + terminal17a + terminal17b
        val outboundEntry = TourEntry(
            officerId = officerId,
            monthYear = monthYear,
            orderIndex = dayOfMonth * 10 + 1,
            dayOfMonth = dayOfMonth,
            departureStation = departureStation,
            departureDate = dateFormatted,
            departureHour = validDepHour,
            arrivalStation = destinationVillageName,
            arrivalDate = dateFormatted,
            arrivalHour = validArrHour,
            purposeOfJourney = purposeOfJourney,
            kindOfJourney = kindOfJourney,
            distanceKm = totalDistanceKm,
            busFare = farePerLeg,
            daDays = "1",
            daRate = daRate,
            daAmount = daAmount,
            terminalCharge17a = terminal17a,
            terminalCharge17b = terminal17b,
            grandTotal = outboundGrandTotal,
            remarks = remarks,
            tripGroupId = tripGroupId,
            isReturnLeg = false
        )
        entriesToInsert.add(outboundEntry)

        // 2. RETURN ENTRY (if round trip selected)
        if (isRoundTrip) {
            val returnGrandTotal = farePerLeg + terminal17a + terminal17b // Return leg does not duplicate daily DA
            val returnDeparture = if (!customArrivalStation.isNullOrBlank()) {
                customArrivalStation.trim()
            } else {
                destinations.last().getStationOrVillageName(isTamil = true)
            }
            val returnEntry = TourEntry(
                officerId = officerId,
                monthYear = monthYear,
                orderIndex = dayOfMonth * 10 + 2,
                dayOfMonth = dayOfMonth,
                departureStation = returnDeparture,
                departureDate = dateFormatted,
                departureHour = validRetDepHour,
                arrivalStation = departureStation,
                arrivalDate = dateFormatted,
                arrivalHour = validRetArrHour,
                purposeOfJourney = purposeOfJourney,
                kindOfJourney = kindOfJourney,
                distanceKm = totalDistanceKm,
                busFare = farePerLeg,
                daDays = "",
                daRate = 0.0,
                daAmount = 0.0,
                terminalCharge17a = terminal17a,
                terminalCharge17b = terminal17b,
                grandTotal = returnGrandTotal,
                remarks = remarks,
                tripGroupId = tripGroupId,
                isReturnLeg = true
            )
            entriesToInsert.add(returnEntry)
        }

        dao.insertTourEntries(entriesToInsert)
    }

    suspend fun addNonTravelDay(
        officerId: Long,
        monthYear: String,
        dayOfMonth: Int,
        dateFormatted: String,
        type: String // "தற்செயல்விடுப்பு" (CL), "விடுமுறை" (Holiday), "அலுவலகப்பணி" (HQ Duty)
    ) = withContext(Dispatchers.IO) {
        val entry = when (type) {
            "தற்செயல்விடுப்பு" -> TourEntry(
                officerId = officerId,
                monthYear = monthYear,
                orderIndex = dayOfMonth * 10,
                dayOfMonth = dayOfMonth,
                isNonTravel = true,
                nonTravelType = "தற்செயல்விடுப்பு",
                departureStation = "தற்செயல்விடுப்பு",
                departureDate = dateFormatted,
                departureHour = "",
                arrivalStation = "தற்செயல்விடுப்பு",
                arrivalDate = dateFormatted,
                arrivalHour = "",
                purposeOfJourney = "தற்செயல்விடுப்பு (CL)",
                kindOfJourney = "",
                distanceKm = 0
            )
            "விடுமுறை" -> TourEntry(
                officerId = officerId,
                monthYear = monthYear,
                orderIndex = dayOfMonth * 10,
                dayOfMonth = dayOfMonth,
                isNonTravel = true,
                nonTravelType = "விடுமுறை",
                departureStation = "விடுமுறை",
                departureDate = dateFormatted,
                departureHour = "",
                arrivalStation = "விடுமுறை",
                arrivalDate = dateFormatted,
                arrivalHour = "",
                purposeOfJourney = "விடுமுறை (Holiday)",
                kindOfJourney = "",
                distanceKm = 0
            )
            else -> TourEntry(
                officerId = officerId,
                monthYear = monthYear,
                orderIndex = dayOfMonth * 10,
                dayOfMonth = dayOfMonth,
                isNonTravel = true,
                nonTravelType = "அலுவலகப்பணி",
                departureStation = "தலைமையிடம்",
                departureDate = dateFormatted,
                departureHour = "",
                arrivalStation = "அலுவலகப்பணி",
                arrivalDate = dateFormatted,
                arrivalHour = "",
                purposeOfJourney = "அலுவலகப்பணி (HQ Office Duty)",
                kindOfJourney = "",
                distanceKm = 0
            )
        }
        dao.insertTourEntry(entry)
    }

    suspend fun insertTourEntry(entry: TourEntry) = withContext(Dispatchers.IO) {
        dao.insertTourEntry(entry)
    }

    suspend fun updateTourEntry(entry: TourEntry) = withContext(Dispatchers.IO) {
        dao.updateTourEntry(entry)
    }

    suspend fun deleteTourEntry(entry: TourEntry) = withContext(Dispatchers.IO) {
        if (entry.tripGroupId.isNotEmpty()) {
            dao.deleteTourEntriesByTripGroup(entry.tripGroupId)
        } else {
            dao.deleteTourEntry(entry)
        }
    }

    suspend fun deleteTourEntryById(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteTourEntryById(id)
    }

    suspend fun clearMonthEntries(officerId: Long, monthYear: String) = withContext(Dispatchers.IO) {
        dao.deleteTourEntriesForMonth(officerId, monthYear)
    }

    suspend fun searchSchools(query: String): Flow<List<School>> {
        return dao.searchSchools(query)
    }

    suspend fun insertSchool(school: School) = withContext(Dispatchers.IO) {
        dao.insertSchool(school)
    }

    suspend fun updateSchool(school: School) = withContext(Dispatchers.IO) {
        dao.updateSchool(school)
    }

    suspend fun deleteSchool(school: School) = withContext(Dispatchers.IO) {
        dao.deleteSchool(school)
    }

    suspend fun updateOfficer(officer: OfficerProfile) = withContext(Dispatchers.IO) {
        dao.updateOfficer(officer)
    }

    suspend fun setActiveOfficer(id: Long) = withContext(Dispatchers.IO) {
        dao.setActiveOfficer(id)
    }

    suspend fun updateSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateSettings(settings)
    }

    private fun School.distanceKm(): Int = if (distanceFromHqKm > 0) distanceFromHqKm else 15
}
