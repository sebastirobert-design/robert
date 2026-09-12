package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.text.input.KeyboardType
import com.example.data.model.School
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy900
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolPickerSheet(
    allSchools: List<School>,
    selectedSchools: List<School>,
    onSelectSchool: (School) -> Unit,
    onToggleSchool: (School) -> Unit,
    onConfirmSelection: () -> Unit,
    onDismiss: () -> Unit,
    isTamil: Boolean,
    isMultiSelect: Boolean = false,
    initialCategory: String = "ALL",
    onUpdateSchool: ((School) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var schoolToEdit by remember { mutableStateOf<School?>(null) }

    val categories = listOf(
        "ALL" to if (isTamil) "அனைத்தும் (119)" else "All (119)",
        "BEO_I" to if (isTamil) "BEO I (35 பள்ளிகள்)" else "BEO I (35 Schools)",
        "BEO_II" to if (isTamil) "BEO II (47 பள்ளிகள்)" else "BEO II (47 Schools)",
        "BEO_III" to if (isTamil) "BEO III (37 பள்ளிகள்)" else "BEO III (37 Schools)",
        "OTHER" to if (isTamil) "அலுவலகம் / நீதிமன்றம்" else "Offices / Court"
    )

    val filteredSchools = allSchools.filter { school ->
        val matchesCategory = if (selectedCategory == "ALL") true else school.category == selectedCategory
        val query = searchQuery.trim().lowercase()
        val matchesQuery = if (query.isEmpty()) true else {
            school.nameEn.lowercase().contains(query) ||
            school.nameTa.contains(query) ||
            school.serialNo.toString() == query ||
            school.code.lowercase().contains(query)
        }
        matchesCategory && matchesQuery
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Schools",
                        tint = Navy700,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTamil) "பள்ளிகள் பட்டியல் (119 Schools)" else "School Directory (119 Schools)",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Search Bar
            AppOutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("school_search_input"),
                placeholder = {
                    Text(if (isTamil) "பள்ளி பெயர் அல்லது ஊர் தேடுக..." else "Search school name or village...")
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Category Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(categories) { (key, label) ->
                    FilterChip(
                        selected = selectedCategory == key,
                        onClick = { selectedCategory = key },
                        label = { Text(label, fontSize = 11.5.sp) }
                    )
                }
            }

            // Selected count banner if multi-select
            if (isMultiSelect && selectedSchools.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isTamil) "${selectedSchools.size} பள்ளிகள் தேர்ந்தெடுக்கப்பட்டுள்ளன" else "${selectedSchools.size} schools selected",
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Button(
                            onClick = onConfirmSelection,
                            modifier = Modifier.testTag("confirm_school_selection_btn")
                        ) {
                            Text(if (isTamil) "சரி (Confirm)" else "Confirm")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Schools List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredSchools, key = { it.id }) { school ->
                    val isSelected = selectedSchools.any { it.id == school.id }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isMultiSelect) {
                                    onToggleSchool(school)
                                } else {
                                    onSelectSchool(school)
                                }
                            }
                            .testTag("school_item_${school.serialNo}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color(0xFFFAFAFA)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) BlueAccent else Color(0xFFECEFF1)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Text(
                                            text = school.serialNo.toString(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Navy900
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    val townName = school.getStationOrVillageName(isTamil)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = townName,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Navy900
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFE8F5E9)
                                        ) {
                                            Text(
                                                text = if (isTamil) "ஊர்" else "Town",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2E7D32),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = if (isTamil && school.nameTa.isNotEmpty()) school.nameTa else school.nameEn,
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(top = 1.dp)
                                    )
                                    if (isTamil && school.nameTa.isNotEmpty() && school.nameEn.isNotEmpty()) {
                                        Text(
                                            text = school.nameEn,
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(top = 3.dp)
                                    ) {
                                        val (catLabel, catBg, catColor) = when (school.category) {
                                            "BEO_I" -> Triple("BEO I", Color(0xFFE8EAF6), Color(0xFF1A237E))
                                            "BEO_II" -> Triple("BEO II", Color(0xFFE0F2F1), Color(0xFF004D40))
                                            "BEO_III" -> Triple("BEO III", Color(0xFFEDE7F6), Color(0xFF4A148C))
                                            else -> Triple(if (isTamil) "அலுவலகம்" else "Office/Court", Color(0xFFFFF3E0), Color(0xFFE65100))
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = catBg
                                        ) {
                                            Text(
                                                text = catLabel,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = catColor,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }

                                        if (school.code.isNotEmpty()) {
                                            Text(
                                                text = "UDISE: ${school.code}",
                                                fontSize = 10.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Distance & Fare Badge
                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFFFF8E1)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Place,
                                                contentDescription = "Distance",
                                                tint = Color(0xFFE65100),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "${school.distanceFromHqKm} km",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFE65100)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "₹${school.defaultBusFare} fare",
                                        fontSize = 10.5.sp,
                                        color = TextSecondary
                                    )
                                }

                                // Edit button for this school to correct Tamil town spelling errors
                                IconButton(
                                    onClick = { schoolToEdit = school },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFEFF6FF), CircleShape)
                                        .testTag("edit_school_picker_${school.serialNo}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = if (isTamil) "ஊர் பெயர் திருத்துக" else "Edit Town Name",
                                        tint = BlueAccent,
                                        modifier = Modifier.size(17.dp)
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

    // Dialog to edit school and correct Tamil town name spelling mistakes
    if (schoolToEdit != null) {
        val editing = schoolToEdit!!
        var editVillageTa by remember(editing) {
            mutableStateOf(editing.villageTa.ifEmpty { editing.getStationOrVillageName(true) })
        }
        var editVillageEn by remember(editing) {
            mutableStateOf(editing.villageEn.ifEmpty { editing.getStationOrVillageName(false) })
        }
        var editNameTa by remember(editing) { mutableStateOf(editing.nameTa) }
        var editNameEn by remember(editing) { mutableStateOf(editing.nameEn) }
        var editDistance by remember(editing) { mutableStateOf(editing.distanceFromHqKm.toString()) }
        var editFare by remember(editing) { mutableStateOf(editing.defaultBusFare.toString()) }

        AlertDialog(
            onDismissRequest = { schoolToEdit = null },
            title = {
                Text(
                    text = if (isTamil) "ஊர் & பள்ளி பெயர் திருத்துக" else "Edit Town & School Name",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Navy900
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (isTamil) "ஊரின் பெயர் தமிழில் (Town Name - TA Bill)" else "Town Name in Tamil",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                            Text(
                                text = if (isTamil) "தமிழில் எழுத்துப்பிழைகளை இங்கு திருத்திக் கொள்ளலாம். இப்பெயர் மட்டுமே பயணப் பதிவில் தோன்றும்." else "Correct Tamil spelling errors here. Only this town name appears in tour entries.",
                                fontSize = 11.sp,
                                color = Color(0xFF15803D)
                            )
                            AppOutlinedTextField(
                                value = editVillageTa,
                                onValueChange = { editVillageTa = it },
                                label = { Text(if (isTamil) "ஊரின் பெயர் (எ.கா. சேதுராணி, சாலையூர்)" else "Town Name in Tamil") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_picker_village_ta_field")
                            )
                        }
                    }

                    AppOutlinedTextField(
                        value = editVillageEn,
                        onValueChange = { editVillageEn = it },
                        label = { Text(if (isTamil) "ஊரின் பெயர் ஆங்கிலத்தில் (Town Name EN)" else "Town Name English") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_picker_village_en_field")
                    )

                    AppOutlinedTextField(
                        value = editNameTa,
                        onValueChange = { editNameTa = it },
                        label = { Text(if (isTamil) "பள்ளியின் பெயர் (School Name TA)" else "School Name Tamil") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_picker_name_ta_field")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppOutlinedTextField(
                            value = editDistance,
                            onValueChange = { editDistance = it },
                            label = { Text(if (isTamil) "தொலைவு கி.மீ" else "Distance KM") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("edit_picker_dist_field")
                        )
                        AppOutlinedTextField(
                            value = editFare,
                            onValueChange = { editFare = it },
                            label = { Text(if (isTamil) "கட்டணம் ₹" else "Fare ₹") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("edit_picker_fare_field")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = editing.copy(
                            villageTa = editVillageTa.trim(),
                            villageEn = editVillageEn.trim(),
                            nameTa = editNameTa.trim(),
                            distanceFromHqKm = editDistance.toIntOrNull() ?: editing.distanceFromHqKm,
                            defaultBusFare = editFare.toIntOrNull() ?: editing.defaultBusFare
                        )
                        onUpdateSchool?.invoke(updated)
                        schoolToEdit = null
                    },
                    modifier = Modifier.testTag("save_picker_school_edit_btn")
                ) {
                    Text(if (isTamil) "சேமிக்க (Save)" else "Save")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { schoolToEdit = null },
                    modifier = Modifier.testTag("cancel_picker_school_edit_btn")
                ) {
                    Text(if (isTamil) "ரத்து செய்" else "Cancel")
                }
            }
        )
    }
}
