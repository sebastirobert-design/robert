package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
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
        entryToReplace: TourEntry?
    ) -> Unit,
    onExportToCsv: () -> Unit = {},
    onDismiss: () -> Unit,
    isTamil: Boolean,
    initialDay: Int = DateUtils.getCurrentCalendar().get(java.util.Calendar.DAY_OF_MONTH),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val (year, month) = DateUtils.parseYearMonth(monthYear)
    val defaultDay = (editingEntry?.dayOfMonth ?: initialDay).coerceIn(1, 31)

    // Form states
    var currentEditingEntry by remember(editingEntry) { mutableStateOf(editingEntry) }
    var isEditingMode by remember(editingEntry) { mutableStateOf(editingEntry != null) }
    var showAlreadyExistsConfirmDialog by remember { mutableStateOf(false) }
    var showDiscardConfirmDialog by remember { mutableStateOf(false) }

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

    // Lambda to load an existing tour entry into the form for editing
    val loadExistingEntry: (TourEntry) -> Unit = { targetEntry ->
        currentEditingEntry = targetEntry
        isEditingMode = true
        dayOfMonth = targetEntry.dayOfMonth
        dayStr = String.format(Locale.US, "%02d", targetEntry.dayOfMonth)
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
                val matchingSchool = allSchools.find { it.nameTa == trimmed || it.nameEn.equals(trimmed, ignoreCase = true) }
                if (matchingSchool != null) {
                    selectedDestinations.add(matchingSchool)
                } else {
                    selectedDestinations.add(
                        School(
                            nameEn = trimmed,
                            nameTa = trimmed,
                            distanceFromHqKm = targetEntry.distanceKm,
                            defaultBusFare = targetEntry.busFare.toInt()
                        )
                    )
                }
            }
        }
    }

    // Pre-populate destination if editing initially
    remember(editingEntry) {
        if (editingEntry != null && selectedDestinations.isEmpty()) {
            val matchingSchool = allSchools.find {
                it.nameTa == editingEntry.arrivalStation || it.nameEn == editingEntry.arrivalStation
            }
            if (matchingSchool != null) {
                selectedDestinations.add(matchingSchool)
            } else if (editingEntry.arrivalStation.isNotEmpty()) {
                selectedDestinations.add(
                    School(
                        nameEn = editingEntry.arrivalStation,
                        nameTa = editingEntry.arrivalStation,
                        distanceFromHqKm = editingEntry.distanceKm,
                        defaultBusFare = editingEntry.busFare.toInt()
                    )
                )
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
            currentEditingEntry
        )
    }

    Dialog(
        onDismissRequest = {
            // Intentionally empty: The window must NOT disappear until the user presses Save Tour or confirms discard.
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .systemBarsPadding()
                .imePadding(),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                                Text(
                                    text = if (isTamil) "1 முறை உள்ளிட்டால் Form 1 & Form 2 தயாராகும்" else "Auto-generates Form 1 Diary & Form 2 TA Bill",
                                    fontSize = 11.5.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        IconButton(onClick = { showDiscardConfirmDialog = true }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
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

                                        // Action Button: "விரும்பினால் edit செய்யும் வசதி"
                                        Button(
                                            onClick = {
                                                loadExistingEntry(mainEntry)
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
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
                                                text = if (isTamil) "இப்பதிவை திருத்து (Edit Existing Details)" else "Edit Existing Details",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

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
                        }
                    }
                }

                // 2. DESTINATION SCHOOLS SELECTION (The Magic Fast-Input Component!)
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isTamil) "2. செல்லும் பள்ளி / இடம் (Destination)" else "2. Destination School / Place",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy800
                                )
                                Button(
                                    onClick = { showSchoolPicker = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Navy700),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("select_school_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = "Pick School",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isTamil) "பள்ளி தேர்வு (119)" else "Pick School", fontSize = 12.sp)
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
                                            text = if (isTamil) "இங்கு தொட்டு பள்ளியைத் தேர்ந்தெடுக்கவும் (119 பள்ளிகள் உள்ளன)" else "Tap to choose destination school (119 available)",
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
                                                        Text(
                                                            text = if (isTamil && school.nameTa.isNotEmpty()) school.nameTa else school.nameEn,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = Navy900
                                                        )
                                                        Text(
                                                            text = "${school.distanceFromHqKm} km • ₹${school.defaultBusFare} bus fare",
                                                            fontSize = 11.sp,
                                                            color = TextSecondary
                                                        )
                                                    }
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
                                        Text(if (isTamil) "+ அடுத்த பள்ளி சேர்க்க (Add Multi-School Tour)" else "+ Add Another Destination")
                                    }
                                }
                            }

                            // Quick frequent chips
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isTamil) "அடிக்கடி செல்லும் இடங்கள்:" else "Frequent Destinations:",
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
                                            text = "+ ${if (isTamil && school.nameTa.isNotEmpty()) school.nameTa else school.nameEn}",
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

            // Save Action Button
            Button(
                onClick = {
                    if (existingEntriesForDay.isNotEmpty() && currentEditingEntry == null) {
                        showAlreadyExistsConfirmDialog = true
                    } else {
                        performSave()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_quick_tour_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEditingMode || currentEditingEntry != null) Navy800 else Navy700
                )
            ) {
                Icon(
                    imageVector = if (isEditingMode || currentEditingEntry != null) Icons.Default.Edit else Icons.Default.Check,
                    contentDescription = "Save"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditingMode || currentEditingEntry != null) {
                        if (isTamil) "மாற்றங்களைச் சேமிக்க (Update Tour)" else "Update Tour & Refresh Bills"
                    } else {
                        if (isTamil) "பயணத்தை சேமிக்க (Save Tour)" else "Save Tour & Generate Bills"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Export to Excel / Google Sheet button (BOM UTF-8 CSV)
            OutlinedButton(
                onClick = onExportToCsv,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("export_to_excel_google_sheet_btn"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.5.dp, Color(0xFF16A34A)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFFF0FDF4),
                    contentColor = Color(0xFF15803D)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Export to Excel / Google Sheet",
                    tint = Color(0xFF15803D)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Export to Excel / Google Sheet",
                    fontSize = 15.sp,
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
        SchoolPickerSheet(
            allSchools = allSchools,
            selectedSchools = selectedDestinations,
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
            isMultiSelect = true
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
