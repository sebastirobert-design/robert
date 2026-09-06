package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import com.example.ui.components.AppOutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.School
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy900
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TaBillUiState

@Composable
fun SchoolDirectoryScreen(
    uiState: TaBillUiState,
    onSaveSchool: (School) -> Unit,
    onDeleteSchool: (School) -> Unit,
    modifier: Modifier = Modifier
) {
    val isTa = uiState.isTamil
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var editingSchool by remember { mutableStateOf<School?>(null) }
    var showAddEditDialog by remember { mutableStateOf(false) }

    val categories = listOf(
        "ALL" to if (isTa) "அனைத்தும் (119)" else "All (119)",
        "PUPS" to if (isTa) "தொடக்கப்பள்ளி (PUPS)" else "Primary (PUPS)",
        "PUMS" to if (isTa) "நடுநிலைப்பள்ளி (PUMS)" else "Middle (PUMS)",
        "AIDED_PRIMARY" to if (isTa) "உதவி தொடக்கப்பள்ளி" else "Aided Primary",
        "AIDED_MIDDLE" to if (isTa) "உதவி நடுநிலைப்பள்ளி" else "Aided Middle",
        "OTHER" to if (isTa) "அலுவலகம் / நீதிமன்றம்" else "Offices / Court"
    )

    val filtered = uiState.allSchools.filter { school ->
        val matchesCategory = if (selectedCategory == "ALL") true else school.category == selectedCategory
        val q = searchQuery.trim().lowercase()
        val matchesQuery = if (q.isEmpty()) true else {
            school.nameEn.lowercase().contains(q) ||
            school.nameTa.contains(q) ||
            school.serialNo.toString() == q
        }
        matchesCategory && matchesQuery
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F6F9))
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            AppOutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("school_dir_search_input"),
                placeholder = {
                    Text(if (isTa) "119 பள்ளிகளில் பெயர் அல்லது எண் தேடுக..." else "Search by school name or S.No...")
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

            Spacer(modifier = Modifier.height(6.dp))

            // Category Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(categories) { (key, label) ->
                    FilterChip(
                        selected = selectedCategory == key,
                        onClick = { selectedCategory = key },
                        label = { Text(label, fontSize = 11.5.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isTa) "பள்ளிகள் பட்டியல் (${filtered.size})" else "Schools (${filtered.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )

            Spacer(modifier = Modifier.height(6.dp))

            // List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filtered, key = { it.id }) { school ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dir_school_${school.serialNo}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                        .background(Color(0xFFE8EAF6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = school.serialNo.toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Navy900
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = if (isTa && school.nameTa.isNotEmpty()) school.nameTa else school.nameEn,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    if (isTa && school.nameTa.isNotEmpty()) {
                                        Text(
                                            text = school.nameEn,
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFFF8E1)
                                ) {
                                    Text(
                                        text = "${school.distanceFromHqKm} km • ₹${school.defaultBusFare}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE65100),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        editingSchool = school
                                        showAddEditDialog = true
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to Add New School
        FloatingActionButton(
            onClick = {
                editingSchool = null
                showAddEditDialog = true
            },
            containerColor = Navy700,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_school_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add School")
        }
    }

    // Add / Edit School Dialog
    if (showAddEditDialog) {
        var nameTa by remember { mutableStateOf(editingSchool?.nameTa ?: "") }
        var nameEn by remember { mutableStateOf(editingSchool?.nameEn ?: "") }
        var distanceKm by remember { mutableStateOf(editingSchool?.distanceFromHqKm?.toString() ?: "15") }
        var busFare by remember { mutableStateOf(editingSchool?.defaultBusFare?.toString() ?: "15") }

        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            title = {
                Text(
                    text = if (editingSchool != null) "பள்ளி விவரம் திருத்துக" else "புதிய பள்ளி சேர்க்க",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppOutlinedTextField(
                        value = nameTa,
                        onValueChange = { nameTa = it },
                        label = { Text("பள்ளி பெயர் (தமிழ்)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    AppOutlinedTextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text("School Name (English)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppOutlinedTextField(
                            value = distanceKm,
                            onValueChange = { distanceKm = it },
                            label = { Text("Distance (KM)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        AppOutlinedTextField(
                            value = busFare,
                            onValueChange = { busFare = it },
                            label = { Text("Bus Fare (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val school = (editingSchool ?: School(
                            serialNo = (uiState.allSchools.maxOfOrNull { it.serialNo } ?: 119) + 1,
                            nameEn = nameEn.ifEmpty { nameTa },
                            nameTa = nameTa,
                            distanceFromHqKm = distanceKm.toIntOrNull() ?: 15,
                            defaultBusFare = busFare.toIntOrNull() ?: 15
                        )).copy(
                            nameTa = nameTa,
                            nameEn = nameEn.ifEmpty { nameTa },
                            distanceFromHqKm = distanceKm.toIntOrNull() ?: 15,
                            defaultBusFare = busFare.toIntOrNull() ?: 15
                        )
                        onSaveSchool(school)
                        showAddEditDialog = false
                    }
                ) {
                    Text("சேமிக்க (Save)")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddEditDialog = false }) {
                    Text("ரத்து (Cancel)")
                }
            }
        )
    }
}
