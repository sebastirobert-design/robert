package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.rememberModalBottomSheetState
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
        remarks: String
    ) -> Unit,
    onExportToCsv: () -> Unit = {},
    onDismiss: () -> Unit,
    isTamil: Boolean,
    initialDay: Int = DateUtils.getCurrentCalendar().get(java.util.Calendar.DAY_OF_MONTH),
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val (year, month) = DateUtils.parseYearMonth(monthYear)
    val defaultDay = (editingEntry?.dayOfMonth ?: initialDay).coerceIn(1, 31)

    // Form states
    var dayOfMonth by remember { mutableIntStateOf(defaultDay) }
    var dayStr by remember { mutableStateOf(String.format("%02d", defaultDay)) }
    var departureStation by remember { mutableStateOf(editingEntry?.departureStation ?: "தலைமையிடம்") }
    var departureHour by remember { mutableStateOf(DateUtils.formatStrictTime(editingEntry?.departureHour ?: "08:00 AM")) }
    var arrivalHourOutbound by remember { mutableStateOf(DateUtils.formatStrictTime(editingEntry?.arrivalHour ?: "09:00 AM")) }
    var returnDepartureHour by remember { mutableStateOf(DateUtils.formatStrictTime("04:10 PM")) }
    var returnArrivalHour by remember { mutableStateOf(DateUtils.formatStrictTime("05:45 PM")) }
    var purposeOfJourney by remember { mutableStateOf(editingEntry?.purposeOfJourney ?: "பள்ளிபார்வை") }
    var kindOfJourney by remember { mutableStateOf(editingEntry?.kindOfJourney ?: "பேருந்து") }
    var isRoundTrip by remember { mutableStateOf(true) }
    var customDistanceKmStr by remember { mutableStateOf(if (editingEntry != null && editingEntry.distanceKm > 0) editingEntry.distanceKm.toString() else "") }
    var customBusFareStr by remember { mutableStateOf(if (editingEntry != null && editingEntry.busFare > 0) String.format("%.0f", editingEntry.busFare) else "") }
    var remarks by remember { mutableStateOf(editingEntry?.remarks ?: "") }

    val selectedDestinations = remember { mutableStateListOf<School>() }
    var showSchoolPicker by remember { mutableStateOf(false) }

    // Pre-populate destination if editing
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

    val timePresetsDeparture = listOf("08:00 AM", "08:30 AM", "09:00 AM", "07:30 AM")
    val timePresetsArrival = listOf("09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF8F9FA),
        modifier = modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
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

                IconButton(onClick = onDismiss) {
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
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
                                    label = { Text(if (isTamil) "தேதி (Day 1-31)" else "Day (1-31)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .width(130.dp)
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

                                val formattedDate = DateUtils.formatDate(year, month, dayOfMonth)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE8EAF6),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = formattedDate,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Navy900
                                        )
                                        Text(
                                            text = DateUtils.getDayOfWeekTamil(formattedDate),
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
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

                            // Purpose Chips
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                purposeOptions.forEach { (ta, en) ->
                                    val label = if (isTamil) ta else en
                                    FilterChip(
                                        selected = purposeOfJourney == ta,
                                        onClick = { purposeOfJourney = ta },
                                        label = { Text(label, fontSize = 11.5.sp) }
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
                        DateUtils.formatStrictTime(departureHour, "08:00 AM"),
                        destinationsToSave,
                        DateUtils.formatStrictTime(arrivalHourOutbound, "09:00 AM"),
                        DateUtils.formatStrictTime(returnDepartureHour, "04:10 PM"),
                        DateUtils.formatStrictTime(returnArrivalHour, "05:45 PM"),
                        purposeOfJourney,
                        kindOfJourney,
                        isRoundTrip,
                        customDistanceKmStr.toIntOrNull(),
                        customBusFareStr.toDoubleOrNull(),
                        remarks
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_quick_tour_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy700)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Save")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isTamil) "பயணத்தை சேமிக்க (Save Tour)" else "Save Tour & Generate Bills",
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
        placeholder = { Text("08:00 AM", color = Color.Gray, fontSize = 11.sp) },
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
