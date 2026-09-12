package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import java.util.Locale
import com.example.ui.components.AppOutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.School
import com.example.data.model.TourEntry
import com.example.ui.components.SchoolPickerSheet
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
import com.example.util.DateUtils
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickTourEntryDialog(
    allSchools: List<School>,
    monthYear: String,
    editingEntry: TourEntry?,
    existingEntries: List<TourEntry> = emptyList(),
    activeOfficerName: String = "",
    activeOfficerSlot: Int = 1,
    initialMode: String = "TOUR", // "TOUR" or "LEAVE"
    onSaveTour: (
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
        entryToReplace: TourEntry?,
        customArrivalStation: String?
    ) -> Unit,
    onAddNonTravel: ((dayOfMonth: Int, dateFormatted: String, type: String) -> Unit)? = null,
    onDeleteEntry: ((TourEntry) -> Unit)? = null,
    onUpdateSchool: ((School) -> Unit)? = null,
    onExportToCsv: () -> Unit = {},
    onDismiss: () -> Unit,
    isTamil: Boolean,
    initialDay: Int = DateUtils.getCurrentCalendar().get(java.util.Calendar.DAY_OF_MONTH),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val (year, month) = DateUtils.parseYearMonth(monthYear)
    val daysInMonth = remember(year, month) { DateUtils.getDaysInMonth(year, month) }
    val defaultDay = (editingEntry?.dayOfMonth ?: initialDay).coerceIn(1, 31)

    // Form states
    var currentEditingEntry by remember(editingEntry) { mutableStateOf(editingEntry) }
    var isEditingMode by remember(editingEntry) { mutableStateOf(editingEntry != null) }
    var showAlreadyExistsConfirmDialog by remember { mutableStateOf(false) }
    var showDiscardConfirmDialog by remember { mutableStateOf(false) }

    var entryMode by remember(editingEntry, initialMode) {
        mutableStateOf(
            if (editingEntry?.isNonTravel == true || initialMode == "LEAVE") "LEAVE" else "TOUR"
        )
    }
    var selectedNonTravelType by remember(editingEntry) {
        mutableStateOf(editingEntry?.nonTravelType?.ifEmpty { "தற்செயல்விடுப்பு" } ?: "தற்செயல்விடுப்பு")
    }
    var nonTravelReasonText by remember(editingEntry) {
        mutableStateOf(
            editingEntry?.purposeOfJourney ?: if (selectedNonTravelType == "தற்செயல்விடுப்பு") "தற்செயல்விடுப்பு (CL)" else "விடுமுறை"
        )
    }

    var dayOfMonth by remember { mutableIntStateOf(defaultDay) }
    var dayStr by remember { mutableStateOf(String.format(Locale.US, "%02d", defaultDay)) }
    var departureStation by remember { mutableStateOf(editingEntry?.departureStation ?: "தலைமையிடம்") }
    var departureHour by remember { mutableStateOf(DateUtils.formatStrictTime(editingEntry?.departureHour ?: "09:00 AM", "09:00 AM")) }
    var arrivalHourOutbound by remember { mutableStateOf(DateUtils.formatStrictTime(editingEntry?.arrivalHour ?: "09:30 AM", "09:30 AM")) }
    var returnDepartureHour by remember { mutableStateOf(DateUtils.formatStrictTime("04:10 PM")) }
    var returnArrivalHour by remember { mutableStateOf(DateUtils.formatStrictTime("05:45 PM")) }

    val standardPurposes = remember {
        listOf(
            "பள்ளிபார்வை" to "School Visit",
            "பள்ளி ஆய்வு" to "Inspection",
            "கலந்தாய்வு" to "Review / Counseling",
            "கூட்டம்" to "Meeting",
            "பயிற்சி" to "Training",
            "வீரசிங்கம் கேஸ்" to "Court Case 1",
            "சாத்தையா கேஸ்" to "Court Case 2",
            "வினாத்தாள் தயாரிப்பு" to "Question Paper Prep"
        )
    }
    val standardTaKeys = remember { standardPurposes.map { it.first }.toSet() }

    var purposeOfJourney by remember { mutableStateOf(editingEntry?.purposeOfJourney ?: "பள்ளிபார்வை") }
    var customPurposeText by remember(editingEntry) {
        val initialP = editingEntry?.purposeOfJourney ?: ""
        mutableStateOf(if (initialP.isNotBlank() && initialP !in standardTaKeys) initialP else "டி.இ.ஆர் மீட்டிங்")
    }

    var kindOfJourney by remember { mutableStateOf(editingEntry?.kindOfJourney ?: "பேருந்து") }
    var isRoundTrip by remember { mutableStateOf(true) }
    var customDistanceKmStr by remember { mutableStateOf(if (editingEntry != null && editingEntry.distanceKm > 0) editingEntry.distanceKm.toString() else "") }
    var customBusFareStr by remember { mutableStateOf(if (editingEntry != null && editingEntry.busFare > 0) String.format(Locale.US, "%.0f", editingEntry.busFare) else "") }
    var remarks by remember { mutableStateOf(editingEntry?.remarks ?: "") }

    val selectedDestinations = remember { mutableStateListOf<School>() }
    var showSchoolPicker by remember { mutableStateOf(false) }
    var editingDestinationSchool by remember { mutableStateOf<School?>(null) }

    // Town / Village Name state for arrival station (ஊரின் பெயர் மட்டும்)
    var arrivalStationTownText by remember(editingEntry) {
        mutableStateOf(
            editingEntry?.arrivalStation?.let { raw ->
                raw.split(".", ",").joinToString(".") { School.extractVillageName(it.trim()) }
            } ?: ""
        )
    }

    // Reactively update arrivalStationTownText when selectedDestinations change
    LaunchedEffect(selectedDestinations.toList()) {
        if (selectedDestinations.isNotEmpty()) {
            arrivalStationTownText = selectedDestinations.joinToString(".") { it.getStationOrVillageName(isTamil) }
        }
    }

    // Helper to find a matching school by village or name
    val findMatchingSchool: (String) -> School? = { query ->
        val q = query.trim()
        allSchools.find {
            it.villageTa.equals(q, ignoreCase = true) ||
            it.villageEn.equals(q, ignoreCase = true) ||
            it.getStationOrVillageName(true).equals(q, ignoreCase = true) ||
            it.getStationOrVillageName(false).equals(q, ignoreCase = true) ||
            it.nameTa.equals(q, ignoreCase = true) ||
            it.nameEn.equals(q, ignoreCase = true) ||
            (q.length >= 3 && (it.nameTa.contains(q) || it.nameEn.contains(q, ignoreCase = true) || it.villageTa.contains(q)))
        }
    }

    // Lambda to load an existing tour entry into the form for editing
    val loadExistingEntry: (TourEntry) -> Unit = { targetEntry ->
        currentEditingEntry = targetEntry
        isEditingMode = true
        dayOfMonth = targetEntry.dayOfMonth
        dayStr = String.format(Locale.US, "%02d", targetEntry.dayOfMonth)

        if (targetEntry.isNonTravel) {
            entryMode = "LEAVE"
            selectedNonTravelType = targetEntry.nonTravelType.ifEmpty { "தற்செயல்விடுப்பு" }
            nonTravelReasonText = targetEntry.purposeOfJourney
        } else {
            entryMode = "TOUR"
            departureStation = targetEntry.departureStation.ifEmpty { "தலைமையிடம்" }
        departureHour = DateUtils.formatStrictTime(targetEntry.departureHour.ifEmpty { "09:00 AM" }, "09:00 AM")
        arrivalHourOutbound = DateUtils.formatStrictTime(targetEntry.arrivalHour.ifEmpty { "09:30 AM" }, "09:30 AM")

        val matchingReturn = existingEntries.firstOrNull {
            (targetEntry.tripGroupId.isNotEmpty() && it.tripGroupId == targetEntry.tripGroupId && it.isReturnLeg) ||
            (it.dayOfMonth == targetEntry.dayOfMonth && it.isReturnLeg)
        }
        if (matchingReturn != null) {
            isRoundTrip = true
            returnDepartureHour = DateUtils.formatStrictTime(matchingReturn.departureHour.ifEmpty { "04:10 PM" })
            returnArrivalHour = DateUtils.formatStrictTime(matchingReturn.arrivalHour.ifEmpty { "05:45 PM" })
        } else {
            isRoundTrip = false
        }
        purposeOfJourney = targetEntry.purposeOfJourney.ifEmpty { "பள்ளிபார்வை" }
        if (purposeOfJourney !in standardTaKeys) {
            customPurposeText = purposeOfJourney
        }
        kindOfJourney = targetEntry.kindOfJourney.ifEmpty { "பேருந்து" }
        customDistanceKmStr = if (targetEntry.distanceKm > 0) targetEntry.distanceKm.toString() else ""
        customBusFareStr = if (targetEntry.busFare > 0) String.format(Locale.US, "%.0f", targetEntry.busFare) else ""
        remarks = targetEntry.remarks

        selectedDestinations.clear()
        val parts = targetEntry.arrivalStation.split(".", ",")
        for (p in parts) {
            val trimmed = p.trim()
            if (trimmed.isNotEmpty()) {
                val matchingSchool = findMatchingSchool(trimmed)
                if (matchingSchool != null) {
                    selectedDestinations.add(matchingSchool)
                } else {
                    val cleanVillage = School.extractVillageName(trimmed)
                    selectedDestinations.add(
                        School(
                            nameEn = cleanVillage,
                            nameTa = cleanVillage,
                            villageTa = cleanVillage,
                            villageEn = cleanVillage,
                            distanceFromHqKm = targetEntry.distanceKm,
                            defaultBusFare = targetEntry.busFare.toInt()
                        )
                    )
                }
            }
        }
        arrivalStationTownText = if (selectedDestinations.isNotEmpty()) {
            selectedDestinations.joinToString(".") { it.getStationOrVillageName(isTamil) }
        } else {
            parts.joinToString(".") { School.extractVillageName(it.trim()) }
        }
        }
    }

    // Pre-populate destination if editing initially
    remember(editingEntry) {
        if (editingEntry != null && selectedDestinations.isEmpty()) {
            val parts = editingEntry.arrivalStation.split(".", ",")
            for (p in parts) {
                val trimmed = p.trim()
                if (trimmed.isNotEmpty()) {
                    val matchingSchool = findMatchingSchool(trimmed)
                    if (matchingSchool != null) {
                        selectedDestinations.add(matchingSchool)
                    } else if (trimmed.isNotEmpty()) {
                        val cleanVillage = School.extractVillageName(trimmed)
                        selectedDestinations.add(
                            School(
                                nameEn = cleanVillage,
                                nameTa = cleanVillage,
                                villageTa = cleanVillage,
                                villageEn = cleanVillage,
                                distanceFromHqKm = editingEntry.distanceKm,
                                defaultBusFare = editingEntry.busFare.toInt()
                            )
                        )
                    }
                }
            }
            if (arrivalStationTownText.isBlank()) {
                arrivalStationTownText = if (selectedDestinations.isNotEmpty()) {
                    selectedDestinations.joinToString(".") { it.getStationOrVillageName(isTamil) }
                } else {
                    parts.joinToString(".") { School.extractVillageName(it.trim()) }
                }
            }
        }
    }

    // Auto-calculate Distance and Fare
    val computedDistance = if (customDistanceKmStr.isNotEmpty()) {
        customDistanceKmStr.toIntOrNull() ?: 15
    } else {
        selectedDestinations.sumOf { it.distanceFromHqKm }.let { if (it > 0) it else 15 }
    }

    val computedBusFare = if (customBusFareStr.isNotEmpty()) {
        customBusFareStr.toDoubleOrNull() ?: 15.0
    } else {
        selectedDestinations.sumOf { it.defaultBusFare.toDouble() }.let { if (it > 0) it else 15.0 }
    }

    // Daily Allowance (300 rate -> 210 amount for tour day)
    val daAmount = 210.0
    val terminalChargesPerLeg = 40.0 // 17a (20) + 17b (20) = 40
    val totalClaimEstimate = if (isRoundTrip) {
        (computedBusFare * 2) + daAmount + (terminalChargesPerLeg * 2)
    } else {
        computedBusFare + daAmount + terminalChargesPerLeg
    }

    val purposeOptions = listOf(
        "பள்ளிபார்வை" to "School Visit",
        "பள்ளி ஆய்வு" to "Inspection",
        "கலந்தாய்வு" to "Review / Counseling",
        "கூட்டம்" to "Meeting",
        "பயிற்சி" to "Training",
        "வீரசிங்கம் கேஸ்" to "Court Case 1",
        "சாத்தையா கேஸ்" to "Court Case 2",
        "டி.இ.ஆர் மீட்டிங்" to "DER Meeting",
        "வினாத்தாள் தயாரிப்பு" to "Question Paper Prep"
    )

    val modeOptions = listOf(
        "பேருந்து" to "Bus",
        "ரயில்" to "Train",
        "சொந்த வாகனம்" to "Own Vehicle"
    )

    val timePresetsDeparture = listOf("09:00 AM", "08:30 AM", "08:00 AM", "09:30 AM")
    val timePresetsArrival = listOf("09:30 AM", "10:00 AM", "09:00 AM", "10:30 AM")

    val existingEntriesForDay = remember(dayOfMonth, existingEntries, currentEditingEntry) {
        existingEntries.filter {
            it.dayOfMonth == dayOfMonth && (
                currentEditingEntry == null || (
                    (it.tripGroupId.isNotEmpty() && it.tripGroupId != currentEditingEntry?.tripGroupId) ||
                    (it.tripGroupId.isEmpty() && it.id != currentEditingEntry?.id)
                )
            )
        }
    }

    val performSave: () -> Unit = {
        val destinationsToSave = if (selectedDestinations.isNotEmpty()) {
            selectedDestinations.toList()
        } else {
            listOf(
                School(
                    nameEn = "PUPS MUNAIVENDRI",
                    nameTa = "முனைவென்றி",
                    distanceFromHqKm = 15,
                    defaultBusFare = 15
                )
            )
        }

        val dateFormatted = DateUtils.formatDate(year, month, dayOfMonth)
        val finalTownStation = if (arrivalStationTownText.isNotBlank()) {
            arrivalStationTownText.trim()
        } else {
            destinationsToSave.joinToString(".") { it.getStationOrVillageName(isTamil) }
        }

        onSaveTour(
            dayOfMonth,
            dateFormatted,
            departureStation,
            DateUtils.formatStrictTime(departureHour, "09:00 AM"),
            destinationsToSave,
            DateUtils.formatStrictTime(arrivalHourOutbound, "09:30 AM"),
            DateUtils.formatStrictTime(returnDepartureHour, "04:10 PM"),
            DateUtils.formatStrictTime(returnArrivalHour, "05:45 PM"),
            purposeOfJourney,
            kindOfJourney,
            isRoundTrip,
            customDistanceKmStr.toIntOrNull(),
            customBusFareStr.toDoubleOrNull(),
            remarks,
            currentEditingEntry,
            finalTownStation
        )
    }

    val performSaveLeave: (Boolean) -> Unit = { advanceToNext ->
        val dateFormatted = DateUtils.formatDate(year, month, dayOfMonth)
        val finalReason = nonTravelReasonText.ifBlank {
            when (selectedNonTravelType) {
                "தற்செயல்விடுப்பு" -> "தற்செயல்விடுப்பு (CL)"
                "விடுமுறை" -> "விடுமுறை (Holiday)"
                else -> "அலுவலகப்பணி"
            }
        }
        onAddNonTravel?.invoke(dayOfMonth, dateFormatted, finalReason)
        if (advanceToNext) {
            if (dayOfMonth < daysInMonth) {
                dayOfMonth += 1
                dayStr = String.format(Locale.US, "%02d", dayOfMonth)
                val nextDayEntry = existingEntries.firstOrNull { it.dayOfMonth == dayOfMonth }
                if (nextDayEntry != null) {
                    loadExistingEntry(nextDayEntry)
                } else {
                    currentEditingEntry = null
                    isEditingMode = false
                }
            } else {
                onDismiss()
            }
        } else {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = {
            // Intentionally empty: The window must NOT disappear until the user presses Save Tour or Cancel button.
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        // Prevent system back gesture or key from unexpectedly closing the dialog
        BackHandler(enabled = true) {
            // Stay open while editing: User must explicitly click "Save Tour" or "Cancel"
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .systemBarsPadding()
                .imePadding()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Absorb background clicks so the dialog never closes unexpectedly */ },
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.96f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Absorb clicks to prevent background events */ },
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    // Visual pull handle bar
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 4.dp, bottom = 6.dp)
                            .size(width = 38.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFD1D5DB))
                    )

                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Navy700),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBus,
                                    contentDescription = "Tour Entry",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isTamil) "புதிய பயணப் பதிவு (Less-Input Tour)" else "Quick Tour Entry",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900,
                                    maxLines = 1
                                )
                                Text(
                                    text = if (isTamil) "1 முறை உள்ளிட்டால் Form 1 & Form 2 தயாராகும்" else "Auto-generates Form 1 Diary & Form 2 TA Bill",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }

                        // Top Cancel / Close button
                        FilledTonalButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFFFEE2E2),
                                contentColor = CrimsonRed
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("header_cancel_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isTamil) "ரத்து" else "Cancel",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Editing status banner if an existing entry is being edited
                if (isEditingMode && currentEditingEntry != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                            border = BorderStroke(1.dp, Color(0xFF1976D2)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editing",
                                        tint = Color(0xFF1565C0),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isTamil) "இப்பதிவை திருத்துகிறீர்கள் (Editing Mode)" else "Editing Existing Tour",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0D47A1)
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        currentEditingEntry = null
                                        isEditingMode = false
                                    }
                                ) {
                                    Text(
                                        text = if (isTamil) "புதிய பதிவாக மாற்றுக" else "Cancel Edit",
                                        fontSize = 11.5.sp,
                                        color = CrimsonRed
                                    )
                                }
                            }
                        }
                    }
                }

                // 1. DATE & DAY SELECTION
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isTamil) "1. பயண தேதி (Travel Date)" else "1. Travel Date",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy800
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Previous Day Button
                                IconButton(
                                    onClick = {
                                        if (dayOfMonth > 1) {
                                            dayOfMonth -= 1
                                            dayStr = String.format(Locale.US, "%02d", dayOfMonth)
                                        }
                                    },
                                    enabled = dayOfMonth > 1,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronLeft,
                                        contentDescription = "Previous Day",
                                        tint = if (dayOfMonth > 1) Navy800 else Color.LightGray
                                    )
                                }

                                AppOutlinedTextField(
                                    value = dayStr,
                                    onValueChange = { input ->
                                        val filtered = input.filter { it.isDigit() }.take(2)
                                        dayStr = filtered
                                        val day = filtered.toIntOrNull()
                                        if (day != null && day in 1..31) {
                                            dayOfMonth = day
                                        }
                                    },
                                    label = { Text(if (isTamil) "தேதி" else "Day") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .width(85.dp)
                                        .testTag("tour_day_input")
                                        .onFocusChanged { focusState ->
                                            if (!focusState.isFocused) {
                                                val parsed = dayStr.toIntOrNull()
                                                if (dayStr.length == 1 && parsed != null && parsed in 1..9) {
                                                    dayStr = dayStr.padStart(2, '0')
                                                    dayOfMonth = parsed
                                                } else if (parsed != null && parsed in 1..31) {
                                                    dayOfMonth = parsed
                                                    dayStr = parsed.toString().padStart(2, '0')
                                                } else {
                                                    dayStr = dayOfMonth.toString().padStart(2, '0')
                                                }
                                            }
                                        },
                                    singleLine = true
                                )

                                // Next Day Button
                                IconButton(
                                    onClick = {
                                        if (dayOfMonth < 31) {
                                            dayOfMonth += 1
                                            dayStr = String.format(Locale.US, "%02d", dayOfMonth)
                                        }
                                    },
                                    enabled = dayOfMonth < 31,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Next Day",
                                        tint = if (dayOfMonth < 31) Navy800 else Color.LightGray
                                    )
                                }

                                val formattedDate = DateUtils.formatDate(year, month, dayOfMonth)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE8EAF6),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            val datePicker = android.app.DatePickerDialog(
                                                context,
                                                { _, _, _, d ->
                                                    dayOfMonth = d
                                                    dayStr = String.format(Locale.US, "%02d", d)
                                                },
                                                year,
                                                month - 1,
                                                dayOfMonth
                                            )
                                            datePicker.show()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = formattedDate,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Navy900
                                            )
                                            Text(
                                                text = DateUtils.getDayOfWeekTamil(formattedDate),
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = "Pick Date",
                                            tint = Navy700,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Horizontal Calendar Day Selector (1 to daysInMonth)
                            Text(
                                text = if (isTamil) "மாதத்தின் அனைத்து தேதிகள் (Click to Select):" else "All Days in Month:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                items(daysInMonth) { index ->
                                    val d = index + 1
                                    val isSelected = d == dayOfMonth
                                    val dayEntries = existingEntries.filter { it.dayOfMonth == d }
                                    val hasTour = dayEntries.any { !it.isNonTravel }
                                    val hasLeave = dayEntries.any { it.isNonTravel && it.nonTravelType == "தற்செயல்விடுப்பு" }
                                    val hasHoliday = dayEntries.any { it.isNonTravel && (it.nonTravelType == "விடுமுறை" || it.purposeOfJourney.contains("விடுமுறை")) }
                                    val hasDuty = dayEntries.any { it.isNonTravel && !hasLeave && !hasHoliday }
                                    val shortDayTa = DateUtils.getDayShortNameTamil(year, month, d)

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when {
                                            isSelected -> Navy800
                                            hasTour -> Color(0xFFF0FDF4)
                                            hasLeave -> Color(0xFFFEF2F2)
                                            hasHoliday -> Color(0xFFFFFBEB)
                                            hasDuty -> Color(0xFFEFF6FF)
                                            else -> Color(0xFFF8FAFC)
                                        },
                                        border = BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = when {
                                                isSelected -> GoldAccent
                                                hasTour -> EmeraldGreen
                                                hasLeave -> CrimsonRed
                                                hasHoliday -> AmberDark
                                                hasDuty -> BlueAccent
                                                else -> Color(0xFFCBD5E1)
                                            }
                                        ),
                                        modifier = Modifier
                                            .width(44.dp)
                                            .clickable {
                                                dayOfMonth = d
                                                dayStr = String.format(Locale.US, "%02d", d)
                                                val existing = dayEntries.firstOrNull()
                                                if (existing != null) {
                                                    loadExistingEntry(existing)
                                                } else {
                                                    currentEditingEntry = null
                                                    isEditingMode = false
                                                }
                                            }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "$d",
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                                color = if (isSelected) Color.White else Navy900
                                            )
                                            Text(
                                                text = shortDayTa,
                                                fontSize = 9.sp,
                                                color = if (isSelected) Color(0xFFCBD5E1) else TextSecondary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when {
                                                            isSelected -> GoldAccent
                                                            hasTour -> EmeraldGreen
                                                            hasLeave -> CrimsonRed
                                                            hasHoliday -> AmberDark
                                                            hasDuty -> BlueAccent
                                                            else -> Color.Transparent
                                                        }
                                                    )
                                            )
                                        }
                                    }
                                }
                            }

                            // ALREADY EXISTS WARNING CARD
                            if (existingEntriesForDay.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("already_exists_warning_card"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                                    border = BorderStroke(1.5.dp, Color(0xFFF57F17))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        // Header
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = "Already Exists",
                                                tint = Color(0xFFE65100),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isTamil) "already exists (இத்தேதியில் ஏற்கெனவே பதிவு உள்ளது!)" else "already exists (Entry already exists on this date!)",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB71C1C)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Officer and Date Info
                                        val displayOfficer = if (activeOfficerName.isNotBlank()) activeOfficerName else "B.E.O."
                                        val formattedDate = DateUtils.formatDate(year, month, dayOfMonth)
                                        Text(
                                            text = if (isTamil) "அலுவலர்: $displayOfficer • தேதி: $formattedDate" else "Officer: $displayOfficer • Date: $formattedDate",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Navy900
                                        )

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFFFD54F))

                                        // Recorded Details on that Date
                                        val mainEntry = existingEntriesForDay.firstOrNull { !it.isReturnLeg } ?: existingEntriesForDay.first()
                                        val returnEntry = existingEntriesForDay.firstOrNull { it.isReturnLeg }

                                        if (mainEntry.isNonTravel) {
                                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                                Text(text = if (isTamil) "வகை: " else "Type: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Navy900)
                                                Text(text = mainEntry.nonTravelType, fontSize = 12.sp, color = CrimsonRed, fontWeight = FontWeight.Bold)
                                            }
                                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                                Text(text = if (isTamil) "விவரம்: " else "Details: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Navy900)
                                                Text(text = mainEntry.purposeOfJourney, fontSize = 12.sp, color = TextPrimary)
                                            }
                                        } else {
                                            // Route
                                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                                Text(text = if (isTamil) "வழித்தடம்: " else "Route: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Navy900)
                                                Text(
                                                    text = "${mainEntry.departureStation} ➔ ${mainEntry.arrivalStation}",
                                                    fontSize = 12.sp,
                                                    color = Navy900,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            // Purpose & Mode
                                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                                Text(text = if (isTamil) "நோக்கம்: " else "Purpose: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Navy900)
                                                Text(
                                                    text = "${mainEntry.purposeOfJourney}  (${mainEntry.kindOfJourney})",
                                                    fontSize = 12.sp,
                                                    color = TextPrimary
                                                )
                                            }
                                            // Timings
                                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                                Text(text = if (isTamil) "நேரம்: " else "Timings: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Navy900)
                                                val timeStr = if (returnEntry != null) {
                                                    "${mainEntry.departureHour} - ${mainEntry.arrivalHour} (திரும்புதல்: ${returnEntry.departureHour} - ${returnEntry.arrivalHour})"
                                                } else {
                                                    "${mainEntry.departureHour} - ${mainEntry.arrivalHour}"
                                                }
                                                Text(text = timeStr, fontSize = 11.5.sp, color = TextPrimary)
                                            }
                                            // Distance, Fare & TA
                                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                                Text(text = if (isTamil) "கணக்கீடு: " else "Calculation: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Navy900)
                                                Text(
                                                    text = "${mainEntry.distanceKm} km • பஸ்: ₹${String.format(Locale.US, "%.0f", mainEntry.busFare)} • DA: ₹${String.format(Locale.US, "%.0f", mainEntry.daAmount)} • மொத்தம்: ₹${String.format(Locale.US, "%.0f", existingEntriesForDay.sumOf { it.grandTotal })}",
                                                    fontSize = 11.5.sp,
                                                    color = Color(0xFF1B5E20),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            if (mainEntry.remarks.isNotBlank()) {
                                                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                                    Text(text = if (isTamil) "குறிப்பு: " else "Remarks: ", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Navy900)
                                                    Text(text = mainEntry.remarks, fontSize = 11.5.sp, color = TextSecondary)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Action Buttons: Edit and Delete
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    loadExistingEntry(mainEntry)
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("edit_existing_entry_btn"),
                                                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (isTamil) "திருத்துக (Edit)" else "Edit",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            if (onDeleteEntry != null) {
                                                OutlinedButton(
                                                    onClick = {
                                                        onDeleteEntry(mainEntry)
                                                        currentEditingEntry = null
                                                        isEditingMode = false
                                                    },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .testTag("delete_existing_entry_btn"),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed),
                                                    border = BorderStroke(1.dp, CrimsonRed),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete",
                                                        modifier = Modifier.size(16.dp),
                                                        tint = CrimsonRed
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = if (isTamil) "நீக்குக (Delete)" else "Delete",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = CrimsonRed
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. ENTRY TYPE SELECTION (புதிய பயணம் vs விடுமுறை / பணி)
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isTamil) "2. பதிவு வகை (Select Entry Type)" else "2. Select Entry Type",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy800
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // 🚌 புதிய பயணம் (New Tour)
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { entryMode = "TOUR" }
                                        .testTag("mode_tour_tab"),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (entryMode == "TOUR") Navy800 else Color.Transparent,
                                    shadowElevation = if (entryMode == "TOUR") 2.dp else 0.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsBus,
                                            contentDescription = null,
                                            tint = if (entryMode == "TOUR") GoldAccent else Color(0xFF64748B),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isTamil) "புதிய பயணம்" else "New Tour",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (entryMode == "TOUR") Color.White else Color(0xFF334155)
                                        )
                                    }
                                }

                                // 🏖️ விடுமுறை / பணி (Leave / Holiday)
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { entryMode = "LEAVE" }
                                        .testTag("mode_leave_tab"),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (entryMode == "LEAVE") CrimsonRed else Color.Transparent,
                                    shadowElevation = if (entryMode == "LEAVE") 2.dp else 0.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.BeachAccess,
                                            contentDescription = null,
                                            tint = if (entryMode == "LEAVE") Color.White else Color(0xFF64748B),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isTamil) "விடுமுறை / பணி" else "Leave / Holiday",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (entryMode == "LEAVE") Color.White else Color(0xFF334155)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. LEAVE / HOLIDAY FORM (When "விடுமுறை / பணி" mode is selected)
                if (entryMode == "LEAVE") {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.EventBusy,
                                            contentDescription = null,
                                            tint = CrimsonRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isTamil) "விடுமுறை / பணி வகை தேர்வு" else "Select Leave / Duty Type",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Navy900
                                        )
                                    }

                                    val curDateStr = DateUtils.formatDate(year, month, dayOfMonth)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFF1F5F9)
                                    ) {
                                        Text(
                                            text = curDateStr,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Navy800,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // 3 Main Type Options as selectable Cards
                                val leaveTypes = listOf(
                                    Triple("தற்செயல்விடுப்பு", if (isTamil) "தற்செயல் விடுப்பு (CL)" else "Casual Leave (CL)", CrimsonRed),
                                    Triple("விடுமுறை", if (isTamil) "பொது விடுமுறை / ஞாயிறு" else "Holiday / Weekend", AmberDark),
                                    Triple("அலுவலகப்பணி", if (isTamil) "தலைமையிட அலுவலகப்பணி" else "HQ Office Duty", BlueAccent)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    leaveTypes.forEach { (typeKey, label, color) ->
                                        val isSelected = selectedNonTravelType == typeKey
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    selectedNonTravelType = typeKey
                                                    if (typeKey == "தற்செயல்விடுப்பு") nonTravelReasonText = "தற்செயல்விடுப்பு (CL)"
                                                    else if (typeKey == "விடுமுறை") nonTravelReasonText = "விடுமுறை (Holiday)"
                                                    else if (typeKey == "அலுவலகப்பணி") nonTravelReasonText = "அலுவலகப்பணி"
                                                },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) color.copy(alpha = 0.12f) else Color(0xFFF8FAFC),
                                            border = BorderStroke(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) color else Color(0xFFCBD5E1)
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) color else Color(0xFFE2E8F0)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = when (typeKey) {
                                                            "தற்செயல்விடுப்பு" -> Icons.Default.EventBusy
                                                            "விடுமுறை" -> Icons.Default.BeachAccess
                                                            else -> Icons.Default.Business
                                                        },
                                                        contentDescription = null,
                                                        tint = if (isSelected) Color.White else Color(0xFF64748B),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = label,
                                                    fontSize = 10.5.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) color else Color(0xFF334155),
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 2
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Fast Preset Reason Chips
                                Text(
                                    text = if (isTamil) "விரைவு காரணங்கள் (Quick Presets):" else "Quick Presets:",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val presets = listOf(
                                        "ஞாயிற்றுக்கிழமை" to "விடுமுறை",
                                        "2-வது சனிக்கிழமை" to "விடுமுறை",
                                        "அரசு விடுமுறை" to "விடுமுறை",
                                        "தற்செயல் விடுப்பு (CL)" to "தற்செயல்விடுப்பு",
                                        "BEO அலுவலக பணி" to "அலுவலகப்பணி",
                                        "வட்டார வள மையப் பயிற்சி" to "அலுவலகப்பணி",
                                        "நீதிமன்ற பணி (Court OD)" to "அலுவலகப்பணி"
                                    )
                                    presets.forEach { (presetLabel, matchingType) ->
                                        val isChipActive = nonTravelReasonText == presetLabel
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isChipActive) Navy700 else Color(0xFFF1F5F9),
                                            border = BorderStroke(1.dp, if (isChipActive) Navy700 else Color(0xFFE2E8F0)),
                                            modifier = Modifier.clickable {
                                                nonTravelReasonText = presetLabel
                                                selectedNonTravelType = matchingType
                                            }
                                        ) {
                                            Text(
                                                text = presetLabel,
                                                fontSize = 11.sp,
                                                color = if (isChipActive) Color.White else Color(0xFF334155),
                                                fontWeight = if (isChipActive) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                AppOutlinedTextField(
                                    value = nonTravelReasonText,
                                    onValueChange = { nonTravelReasonText = it },
                                    label = { Text(if (isTamil) "காரணம் / விவரிப்பு (Reason / Description)" else "Reason / Description") },
                                    placeholder = { Text(if (isTamil) "எ.கா: ஞாயிற்றுக்கிழமை / CL" else "e.g., Sunday / CL") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("non_travel_reason_input"),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Information Card
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF0FDF4),
                                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = EmeraldGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isTamil) {
                                                "படிவம் 1 பயணக் குறிப்பேட்டில் இப்பதிவு '$nonTravelReasonText' என்று பதிவாகும். TA பில்லில் கட்டணம் சேராது."
                                            } else {
                                                "Recorded as '$nonTravelReasonText' in Form 1 Diary. Excluded from TA travel claims."
                                            },
                                            fontSize = 11.sp,
                                            color = Color(0xFF166534),
                                            lineHeight = 15.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Action Buttons for Save Leave / Holiday
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { performSaveLeave(true) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .testTag("save_and_next_day_leave_btn"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                                    ) {
                                        Icon(imageVector = Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isTamil) "சேமித்து அடுத்த நாள் ➔" else "Save & Next ➔",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = { performSaveLeave(false) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .testTag("save_leave_btn"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = when (selectedNonTravelType) {
                                                "தற்செயல்விடுப்பு" -> CrimsonRed
                                                "விடுமுறை" -> AmberDark
                                                else -> BlueAccent
                                            }
                                        )
                                    ) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isTamil) "விடுமுறை சேமிக்க" else "Save Leave",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. TOUR FORM SECTIONS (When "புதிய பயணம்" mode is selected)
                if (entryMode == "TOUR") {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isTamil) "3. செல்லும் பள்ளி / இடம் (Destination)" else "3. Destination School / Place",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy800
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                AppOutlinedTextField(
                                    value = departureStation,
                                    onValueChange = { departureStation = it },
                                    label = { Text(if (isTamil) "புறப்படும் தலைமையிடம் (HQ)" else "Departure Station (HQ)") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("tour_departure_station_input"),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isTamil) "சென்றடையும் ஊர் தேர்வு:" else "Destination Town Selection:",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Navy800
                                    )
                                    Button(
                                        onClick = { showSchoolPicker = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Navy700),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("select_school_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = "Pick Town",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isTamil) "ஊர் தேர்வு (119)" else "Pick Town (119)", fontSize = 12.sp)
                                    }
                                }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Selected destinations chips list
                            if (selectedDestinations.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFF3E0),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showSchoolPicker = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = "Alert",
                                            tint = AmberDark
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isTamil) "இங்கு தொட்டு சென்றடையும் ஊரைத் தேர்ந்தெடுக்கவும் (119 ஊர்கள் உள்ளன)" else "Tap to choose destination town (119 available)",
                                            fontSize = 12.5.sp,
                                            color = Color(0xFFE65100),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    selectedDestinations.forEachIndexed { index, school ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFE8F5E9),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(
                                                        text = "${index + 1}.",
                                                        fontWeight = FontWeight.Bold,
                                                        color = EmeraldGreen
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Column {
                                                        val villageName = school.getStationOrVillageName(isTamil)
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Text(
                                                                text = villageName,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 14.sp,
                                                                color = Navy900
                                                            )
                                                            Surface(
                                                                shape = RoundedCornerShape(4.dp),
                                                                color = Color(0xFFC8E6C9)
                                                            ) {
                                                                Text(
                                                                    text = if (isTamil) "ஊர்" else "Town",
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color(0xFF1B5E20),
                                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                                )
                                                            }
                                                        }
                                                        Text(
                                                            text = "${school.distanceFromHqKm} km • ₹${school.defaultBusFare} bus fare",
                                                            fontSize = 11.sp,
                                                            color = TextSecondary
                                                        )
                                                    }
                                                }

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    IconButton(
                                                        onClick = { editingDestinationSchool = school },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = "Edit Town Name",
                                                            tint = BlueAccent,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = { selectedDestinations.removeAt(index) },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Remove",
                                                            tint = Color.Red,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Add next destination in sequence
                                    OutlinedButton(
                                        onClick = { showSchoolPicker = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add More")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isTamil) "+ அடுத்த ஊர் சேர்க்க (Add Destination Town)" else "+ Add Another Destination")
                                    }
                                }
                            }

                            // Prominent Destination Town / Village Box
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = null,
                                            tint = EmeraldGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isTamil) "சென்றடைந்த ஊர் (Destination Town / Station)" else "Destination Town / Station",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF166534)
                                        )
                                    }
                                    AppOutlinedTextField(
                                        value = arrivalStationTownText,
                                        onValueChange = { arrivalStationTownText = it },
                                        label = { Text(if (isTamil) "ஊரின் பெயர் மட்டும் (Town Name Only)" else "Town Name Only") },
                                        placeholder = { Text(if (isTamil) "எ.கா. சேதுராணி, சாலையூர்" else "e.g. Sethurani, Salaiyur") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("arrival_station_town_input"),
                                        supportingText = {
                                            Text(
                                                text = if (isTamil) "படிவம் 1 & 2-ல் பள்ளியின் பெயர் வராமல் இந்த ஊரின் பெயர் மட்டுமே பதிவாகும்." else "Only this town name will appear in Form 1 & 2 (not school name).",
                                                fontSize = 11.sp,
                                                color = Color(0xFF15803D)
                                            )
                                        },
                                        singleLine = true
                                    )
                                }
                            }

                            // Quick frequent chips
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isTamil) "அடிக்கடி செல்லும் ஊர்கள் / இடங்கள்:" else "Frequent Towns / Destinations:",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val frequent = allSchools.filter { it.isFrequent || it.serialNo in listOf(2, 15, 17, 59, 60, 79) }.take(6)
                                items(frequent) { school ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFFF1F5F9),
                                        modifier = Modifier.clickable {
                                            if (!selectedDestinations.any { it.id == school.id }) {
                                                selectedDestinations.add(school)
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = "+ ${school.getStationOrVillageName(isTamil)}",
                                            fontSize = 11.sp,
                                            color = Navy900,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. PURPOSE & MODE OF JOURNEY
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isTamil) "3. பயண நோக்கம் & வகை (Purpose & Mode)" else "3. Purpose & Travel Mode",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy800
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Purpose Chips (8 Standard + 1 Editable DER Meeting / Custom)
                            val isCustomPurposeActive = purposeOfJourney !in standardTaKeys

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // 8 Standard Purposes
                                standardPurposes.forEach { (ta, en) ->
                                    val label = if (isTamil) ta else en
                                    FilterChip(
                                        selected = purposeOfJourney == ta,
                                        onClick = { purposeOfJourney = ta },
                                        label = { Text(label, fontSize = 11.5.sp) }
                                    )
                                }

                                // 9th Purpose: "டி.இ.ஆர் மீட்டிங்" (Editable / Custom Purpose)
                                val derChipLabel = if (isCustomPurposeActive && customPurposeText.isNotBlank() && customPurposeText != "டி.இ.ஆர் மீட்டிங்") {
                                    "✏️ $customPurposeText"
                                } else {
                                    if (isTamil) "டி.இ.ஆர் மீட்டிங் (எடிட் ✏️)" else "DER Meeting (Edit ✏️)"
                                }

                                FilterChip(
                                    selected = isCustomPurposeActive,
                                    onClick = {
                                        purposeOfJourney = customPurposeText.ifBlank { "டி.இ.ஆர் மீட்டிங்" }
                                    },
                                    label = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = derChipLabel,
                                                fontSize = 11.5.sp,
                                                fontWeight = if (isCustomPurposeActive) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                )
                            }

                            // Editable text field when "டி.இ.ஆர் மீட்டிங்" / custom purpose is chosen
                            AnimatedVisibility(visible = isCustomPurposeActive) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    AppOutlinedTextField(
                                        value = customPurposeText,
                                        onValueChange = { input ->
                                            customPurposeText = input
                                            purposeOfJourney = input.ifBlank { "டி.இ.ஆர் மீட்டிங்" }
                                        },
                                        label = {
                                            Text(
                                                text = if (isTamil) "பயண நோக்கம் (எடிட் / விரும்பிய நோக்கம்)" else "Purpose of Journey (Edit / Custom)",
                                                fontSize = 12.sp
                                            )
                                        },
                                        placeholder = {
                                            Text(
                                                text = if (isTamil) "டி.இ.ஆர் மீட்டிங் அல்லது உங்கள் விருப்ப நோக்கம்..." else "DER Meeting or enter custom purpose...",
                                                fontSize = 11.5.sp,
                                                color = Color.Gray
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Purpose",
                                                tint = Navy700,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        trailingIcon = {
                                            if (customPurposeText.isNotBlank() && customPurposeText != "டி.இ.ஆர் மீட்டிங்") {
                                                TextButton(
                                                    onClick = {
                                                        customPurposeText = "டி.இ.ஆர் மீட்டிங்"
                                                        purposeOfJourney = "டி.இ.ஆர் மீட்டிங்"
                                                    }
                                                ) {
                                                    Text(
                                                        text = if (isTamil) "மீட்டமை" else "Reset",
                                                        fontSize = 11.sp,
                                                        color = CrimsonRed
                                                    )
                                                }
                                            }
                                        },
                                        supportingText = {
                                            Text(
                                                text = if (isTamil)
                                                    "💡 'டி.இ.ஆர் மீட்டிங்' என்ற நோக்கத்தை எடிட் செய்து நாம் விரும்பிய எந்த நோக்கத்தையும் இங்கே தட்டச்சு செய்யலாம்"
                                                else
                                                    "💡 Edit 'DER Meeting' or enter any custom purpose not in the 8 options",
                                                fontSize = 10.5.sp,
                                                color = Color(0xFF1E40AF)
                                            )
                                        },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("custom_purpose_input")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Travel Mode Chips
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                modeOptions.forEach { (ta, en) ->
                                    val label = if (isTamil) ta else en
                                    FilterChip(
                                        selected = kindOfJourney == ta,
                                        onClick = { kindOfJourney = ta },
                                        label = { Text(label, fontSize = 11.5.sp) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. TIMINGS & RETURN JOURNEY (Auto-Generated!)
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isTamil) "4. புறப்பாடு & திரும்பும் நேரம் (Timings & Return)" else "4. Timings & Auto-Return",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy800
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Outbound Departure & Arrival Timings
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AppTimeField(
                                    value = departureHour,
                                    onValueChange = { departureHour = it },
                                    label = if (isTamil) "புறப்படும் நேரம்" else "Departure Time",
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("tour_departure_hour_input"),
                                    isTamil = isTamil
                                )
                                AppTimeField(
                                    value = arrivalHourOutbound,
                                    onValueChange = { arrivalHourOutbound = it },
                                    label = if (isTamil) "சேரும் நேரம்" else "Arrival Time",
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("tour_arrival_hour_input"),
                                    isTamil = isTamil
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Auto-Return Switch
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isRoundTrip) Color(0xFFE3F2FD) else Color(0xFFF5F5F5),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isTamil) "மறுபயணம் (Return Journey) தானாக சேர்க்க" else "Auto-create Return Journey",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            color = if (isRoundTrip) Navy900 else TextSecondary
                                        )
                                        Text(
                                            text = if (isTamil) "பள்ளி ➔ தலைமையிடம் (4:10 PM - 5:45 PM)" else "School ➔ HQ (4:10 PM - 5:45 PM)",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Switch(
                                        checked = isRoundTrip,
                                        onCheckedChange = { isRoundTrip = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = BlueAccent),
                                        modifier = Modifier.testTag("return_journey_switch")
                                    )
                                }
                            }

                            if (isRoundTrip) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AppTimeField(
                                        value = returnDepartureHour,
                                        onValueChange = { returnDepartureHour = it },
                                        label = if (isTamil) "திரும்ப புறப்பாடு" else "Return Departure",
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("tour_return_dep_hour_input"),
                                        isTamil = isTamil
                                    )
                                    AppTimeField(
                                        value = returnArrivalHour,
                                        onValueChange = { returnArrivalHour = it },
                                        label = if (isTamil) "HQ சென்றடைந்த நேரம்" else "Return Arrival",
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("tour_return_arr_hour_input"),
                                        isTamil = isTamil
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. DISTANCE & FARE OVERRIDES (Optional)
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isTamil) "5. தொலைவு & கட்டணம் (Distance & Fare)" else "5. Distance & Fare Details",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy800
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AppOutlinedTextField(
                                    value = customDistanceKmStr,
                                    onValueChange = { customDistanceKmStr = it },
                                    label = { Text(if (isTamil) "கி.மீ (${computedDistance} km)" else "KM (${computedDistance} km)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("tour_distance_input"),
                                    singleLine = true
                                )
                                AppOutlinedTextField(
                                    value = customBusFareStr,
                                    onValueChange = { customBusFareStr = it },
                                    label = { Text(if (isTamil) "கட்டணம் ₹(₹${String.format("%.0f", computedBusFare)})" else "Fare (₹${String.format("%.0f", computedBusFare)})") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("tour_fare_input"),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                // 5B. REMARKS / TEXTAREA
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isTamil) "6. கூடுதல் குறிப்புகள் (Remarks / Notes)" else "6. Remarks / Purpose Notes",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy800
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            AppOutlinedTextField(
                                value = remarks,
                                onValueChange = { remarks = it },
                                label = { Text(if (isTamil) "குறிப்புகள் (கூடுதல் விவரங்கள்)..." else "Remarks or inspection notes...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("tour_remarks_textarea"),
                                singleLine = false,
                                minLines = 3,
                                maxLines = 5
                            )
                        }
                    }
                }

                // 6. LIVE TOTAL CLAIM BREAKDOWN CARD
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Navy900),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isTamil) "தானியங்கி பயணப்படி கணக்கீடு (Auto TA Calculation)" else "Automated TA Calculation",
                                color = GoldAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isRoundTrip) "பேருந்து கட்டணம் (2 வழிகள்):" else "பேருந்து கட்டணம்:",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "₹${String.format("%.0f", if (isRoundTrip) computedBusFare * 2 else computedBusFare)}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "தினப்படி DA (Rate 300 @ 70%):",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "₹210",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isRoundTrip) "முனையக் கட்டணம் 17a + 17b (2 வழிகள்):" else "முனையக் கட்டணம் 17a + 17b:",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "₹${String.format("%.0f", if (isRoundTrip) terminalChargesPerLeg * 2 else terminalChargesPerLeg)}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 6.dp),
                                color = Color(0xFF37474F)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isTamil) "மொத்த உரிமைக்கோரல் (Total Claim):" else "Estimated Grand Total:",
                                    color = GoldAccent,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "₹${String.format("%.0f", totalClaimEstimate)}",
                                    color = GoldAccent,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

            // Main Action Buttons Row: [ ரத்து செய் (Cancel) ]  [ பயணத்தை சேமிக்க (Save Tour) ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. ரத்து செய் (Cancel Button - Discards/Closes the dialog)
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(0.38f)
                        .height(52.dp)
                        .testTag("cancel_quick_tour_btn"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, CrimsonRed),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFFFF1F2),
                        contentColor = CrimsonRed
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTamil) "ரத்து செய்" else "Cancel",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                // 2. பயணத்தை சேமிக்க / விடுமுறை சேமிக்க (Save Button)
                Button(
                    onClick = {
                        if (entryMode == "LEAVE") {
                            performSaveLeave(false)
                        } else {
                            if (existingEntriesForDay.isNotEmpty() && currentEditingEntry == null) {
                                showAlreadyExistsConfirmDialog = true
                            } else {
                                performSave()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(0.62f)
                        .height(52.dp)
                        .testTag("save_quick_tour_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (entryMode == "LEAVE") {
                            when (selectedNonTravelType) {
                                "தற்செயல்விடுப்பு" -> CrimsonRed
                                "விடுமுறை" -> AmberDark
                                else -> BlueAccent
                            }
                        } else {
                            if (isEditingMode || currentEditingEntry != null) Navy800 else Navy700
                        }
                    )
                ) {
                    Icon(
                        imageVector = if (entryMode == "LEAVE") {
                            Icons.Default.Check
                        } else if (isEditingMode || currentEditingEntry != null) {
                            Icons.Default.Edit
                        } else {
                            Icons.Default.Check
                        },
                        contentDescription = "Save",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (entryMode == "LEAVE") {
                            if (isTamil) "விடுமுறை சேமிக்க" else "Save Leave"
                        } else if (isEditingMode || currentEditingEntry != null) {
                            if (isTamil) "மாற்றங்களைச் சேமிக்க" else "Update Tour"
                        } else {
                            if (isTamil) "பயணத்தை சேமிக்க" else "Save Tour"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Export to Excel / Google Sheet button (BOM UTF-8 CSV)
            OutlinedButton(
                onClick = onExportToCsv,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("export_to_excel_google_sheet_btn"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.2.dp, Color(0xFF16A34A)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFFF0FDF4),
                    contentColor = Color(0xFF15803D)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Export to Excel / Google Sheet",
                    tint = Color(0xFF15803D),
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isTamil) "Excel / Google Sheet-ல் ஏற்றுமதி (Export)" else "Export to Excel / Google Sheet",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF15803D)
                )
            }
        }
    }
}
}

    // Discard / Exit Confirmation Dialog when user clicks Close [X]
    if (showDiscardConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Confirm Close",
                    tint = CrimsonRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (isTamil) "பதிவை சேமிக்காமல் வெளியேறவா?" else "Exit Without Saving?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Navy900
                )
            },
            text = {
                Text(
                    text = if (isTamil)
                        "உள்ளிட்ட பயண விவரங்கள் இன்னும் சேமிக்கப்படவில்லை. 'பயணத்தை சேமிக்க' பட்டன் அழுத்திய பிறகே பதிவு சேமிக்கப்படும்.\n\nஇப்போது வெளியேறினால் உள்ளிட்டவை மறைந்துவிடும். வெளியேற விரும்புகிறீர்களா?"
                    else
                        "You have unsaved details in this tour form. Tour details are only saved when you tap 'Save Tour'.\n\nIf you exit now, entered details will be lost. Do you wish to exit?",
                    fontSize = 13.5.sp,
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDiscardConfirmDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isTamil) "வெளியேறு (Exit)" else "Exit Without Saving")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDiscardConfirmDialog = false }
                ) {
                    Text(
                        text = if (isTamil) "தொடர்ந்து உள்ளிடு (Keep Editing)" else "Keep Editing",
                        fontWeight = FontWeight.Bold,
                        color = Navy800
                    )
                }
            }
        )
    }

    // Modal School Picker Sheet
    if (showSchoolPicker) {
        val defaultCategory = when (activeOfficerSlot) {
            1 -> "BEO_I"
            2 -> "BEO_II"
            3 -> "BEO_III"
            else -> "ALL"
        }
        SchoolPickerSheet(
            allSchools = allSchools,
            selectedSchools = selectedDestinations,
            initialCategory = defaultCategory,
            onSelectSchool = { school ->
                if (!selectedDestinations.any { it.id == school.id }) {
                    selectedDestinations.add(school)
                }
                showSchoolPicker = false
            },
            onToggleSchool = { school ->
                if (selectedDestinations.any { it.id == school.id }) {
                    selectedDestinations.removeAll { it.id == school.id }
                } else {
                    selectedDestinations.add(school)
                }
            },
            onConfirmSelection = { showSchoolPicker = false },
            onDismiss = { showSchoolPicker = false },
            isTamil = isTamil,
            isMultiSelect = true,
            onUpdateSchool = { updatedSchool ->
                onUpdateSchool?.invoke(updatedSchool)
                val idx = selectedDestinations.indexOfFirst { it.id == updatedSchool.id }
                if (idx >= 0) {
                    selectedDestinations[idx] = updatedSchool
                }
                arrivalStationTownText = selectedDestinations.joinToString(".") { it.getStationOrVillageName(isTamil) }
            }
        )
    }

    // Dialog to edit destination school town name
    if (editingDestinationSchool != null) {
        val dest = editingDestinationSchool!!
        var editVillageTa by remember(dest) {
            mutableStateOf(dest.villageTa.ifEmpty { dest.getStationOrVillageName(true) })
        }
        var editVillageEn by remember(dest) {
            mutableStateOf(dest.villageEn.ifEmpty { dest.getStationOrVillageName(false) })
        }

        AlertDialog(
            onDismissRequest = { editingDestinationSchool = null },
            title = {
                Text(
                    text = if (isTamil) "ஊர் பெயர் திருத்துக" else "Edit Town Name",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Navy900
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (isTamil) "ஊரின் பெயர் தமிழில் (Town Name)" else "Town Name in Tamil",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                            Text(
                                text = if (isTamil) "பயண பதிவில் பள்ளியின் பெயர் வராமல் இந்த ஊரின் பெயர் மட்டுமே பதிவாகும்." else "Only this town name appears in tour entries (no school name).",
                                fontSize = 11.sp,
                                color = Color(0xFF15803D)
                            )
                            AppOutlinedTextField(
                                value = editVillageTa,
                                onValueChange = { editVillageTa = it },
                                label = { Text(if (isTamil) "ஊரின் பெயர் (எ.கா. சேதுராணி, சாலையூர்)" else "Town Name in Tamil") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_dest_village_ta_field")
                            )
                        }
                    }

                    AppOutlinedTextField(
                        value = editVillageEn,
                        onValueChange = { editVillageEn = it },
                        label = { Text(if (isTamil) "ஊரின் பெயர் ஆங்கிலத்தில்" else "Town Name in English") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_dest_village_en_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = dest.copy(
                            villageTa = editVillageTa.trim(),
                            villageEn = editVillageEn.trim()
                        )
                        onUpdateSchool?.invoke(updated)
                        val idx = selectedDestinations.indexOfFirst { it.id == updated.id }
                        if (idx >= 0) {
                            selectedDestinations[idx] = updated
                        }
                        arrivalStationTownText = selectedDestinations.joinToString(".") { it.getStationOrVillageName(isTamil) }
                        editingDestinationSchool = null
                    },
                    modifier = Modifier.testTag("save_dest_school_edit_btn")
                ) {
                    Text(if (isTamil) "சேமிக்க (Save)" else "Save")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { editingDestinationSchool = null },
                    modifier = Modifier.testTag("cancel_dest_school_edit_btn")
                ) {
                    Text(if (isTamil) "ரத்து செய்" else "Cancel")
                }
            }
        )
    }

    // Duplicate Date Already Exists Confirmation Dialog
    if (showAlreadyExistsConfirmDialog && existingEntriesForDay.isNotEmpty()) {
        val mainEntry = existingEntriesForDay.firstOrNull { !it.isReturnLeg } ?: existingEntriesForDay.first()
        val displayOfficer = if (activeOfficerName.isNotBlank()) activeOfficerName else "B.E.O."
        val formattedDate = DateUtils.formatDate(year, month, dayOfMonth)

        AlertDialog(
            onDismissRequest = { showAlreadyExistsConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "already exists",
                    fontWeight = FontWeight.Bold,
                    color = CrimsonRed,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isTamil)
                            "$formattedDate தேதியில் அலுவலர் $displayOfficer-க்கு ஏற்கெனவே ஒரு பதிவு உள்ளது!"
                        else
                            "An entry already exists for Officer $displayOfficer on $formattedDate!",
                        fontWeight = FontWeight.SemiBold,
                        color = Navy900,
                        fontSize = 13.5.sp
                    )

                    Surface(
                        color = Color(0xFFFFF8E1),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (isTamil) "ஏற்கெனவே உள்ள விவரம்:" else "Existing Details:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Navy900
                            )
                            if (mainEntry.isNonTravel) {
                                Text(text = "• வகை: ${mainEntry.nonTravelType}", fontSize = 11.5.sp)
                                Text(text = "• விவரம்: ${mainEntry.purposeOfJourney}", fontSize = 11.5.sp)
                            } else {
                                Text(text = "• தடம்: ${mainEntry.departureStation} ➔ ${mainEntry.arrivalStation}", fontSize = 11.5.sp)
                                Text(text = "• நோக்கம்: ${mainEntry.purposeOfJourney} (${mainEntry.kindOfJourney})", fontSize = 11.5.sp)
                                Text(text = "• நேரம்: ${mainEntry.departureHour} - ${mainEntry.arrivalHour}", fontSize = 11.5.sp)
                                Text(
                                    text = "• கட்டணம்: ₹${String.format(Locale.US, "%.0f", mainEntry.busFare)} • மொத்தம்: ₹${String.format(Locale.US, "%.0f", existingEntriesForDay.sumOf { it.grandTotal })}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isTamil)
                            "நீங்கள் பழைய பதிவை திருத்த விரும்புகிறீர்களா (Edit), அல்லது புதியதாக மாற்றியமைக்க விரும்புகிறீர்களா (Overwrite)?"
                        else
                            "Do you want to edit the existing entry or overwrite it with these new details?",
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAlreadyExistsConfirmDialog = false
                        loadExistingEntry(mainEntry)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isTamil) "பழையதை திருத்து (Edit)" else "Edit Existing")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(
                        onClick = { showAlreadyExistsConfirmDialog = false }
                    ) {
                        Text(if (isTamil) "ரத்து (Cancel)" else "Cancel", color = TextSecondary)
                    }
                    Button(
                        onClick = {
                            showAlreadyExistsConfirmDialog = false
                            currentEditingEntry = mainEntry
                            performSave()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                    ) {
                        Text(if (isTamil) "மாற்றியமை (Overwrite)" else "Overwrite")
                    }
                }
            }
        )
    }
}

@Composable
private fun AppTimeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    testTag: String = "",
    isTamil: Boolean = true
) {
    val context = LocalContext.current

    val openTimePicker = {
        val (h12, m, isPm) = DateUtils.parseTimeComponents(value)
        val hour24 = DateUtils.getHour24(h12, isPm)
        val dialog = android.app.TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val hour12 = if (hourOfDay == 0) 12 else if (hourOfDay > 12) hourOfDay - 12 else hourOfDay
                val amPm = if (hourOfDay < 12) "AM" else "PM"
                val formatted = String.format(Locale.US, "%02d:%02d %s", hour12, minute, amPm)
                onValueChange(formatted)
            },
            hour24,
            m,
            false // 12-hour AM/PM format
        )
        dialog.show()
    }

    AppOutlinedTextField(
        value = value,
        onValueChange = { input ->
            // Restrict input to digits, colon, space, and a, m, p (case-insensitive)
            val filtered = input.filter { it.isDigit() || it == ':' || it == ' ' || it in "aAmMpP" }.uppercase(Locale.US)
            if (filtered.length <= 8) {
                // If user entered 2 digits without colon, auto-insert ':'
                if (filtered.length == 2 && filtered.all { it.isDigit() } && !value.endsWith(":")) {
                    onValueChange("$filtered:")
                } else {
                    onValueChange(filtered)
                }
            }
        },
        label = { Text(label, maxLines = 1) },
        placeholder = { Text("09:00 AM", color = Color.Gray, fontSize = 11.sp) },
        trailingIcon = {
            IconButton(
                onClick = openTimePicker,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Pick Time",
                    tint = Navy700,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        supportingText = {
            Text(
                text = "hh:mm AM/PM",
                fontSize = 9.5.sp,
                color = TextSecondary
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Ascii
        ),
        singleLine = true,
        modifier = modifier
            .onFocusChanged { focusState ->
                if (!focusState.isFocused && value.isNotBlank()) {
                    onValueChange(DateUtils.formatStrictTime(value))
                }
            }
            .testTag(testTag)
    )
}
