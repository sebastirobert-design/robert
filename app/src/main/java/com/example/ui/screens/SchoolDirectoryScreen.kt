package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ui.components.AppOutlinedTextField
import com.example.ui.components.SchoolAiAuditDialog
import com.example.ui.components.SchoolCsvImportDialog
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
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
    onImportCsv: () -> Unit = {},
    onDownloadCsvTemplate: () -> Unit = {},
    onExportCsv: () -> Unit = {},
    onResetToDefault: () -> Unit = {},
    onRunAiValidation: () -> Unit = {},
    onConfirmImportSchools: (replaceExisting: Boolean) -> Unit = {},
    onDismissSchoolCsvDialog: () -> Unit = {},
    onAutoFixSchoolIssues: () -> Unit = {},
    onDismissAiAuditDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isTa = uiState.isTamil
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var editingSchool by remember { mutableStateOf<School?>(null) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val totalCount = uiState.allSchools.size
    val beo1Count = uiState.allSchools.count { it.category == "BEO_I" }
    val beo2Count = uiState.allSchools.count { it.category == "BEO_II" }
    val beo3Count = uiState.allSchools.count { it.category == "BEO_III" }
    val otherCount = uiState.allSchools.count { it.category == "OTHER" }

    val categories = listOf(
        "ALL" to if (isTa) "அனைத்தும் ($totalCount)" else "All ($totalCount)",
        "BEO_I" to if (isTa) "BEO I ($beo1Count)" else "BEO I ($beo1Count)",
        "BEO_II" to if (isTa) "BEO II ($beo2Count)" else "BEO II ($beo2Count)",
        "BEO_III" to if (isTa) "BEO III ($beo3Count)" else "BEO III ($beo3Count)",
        "OTHER" to if (isTa) "அலுவலகம் ($otherCount)" else "Office ($otherCount)"
    )

    val filtered = uiState.allSchools.filter { school ->
        val matchesCategory = if (selectedCategory == "ALL") true else school.category == selectedCategory
        val q = searchQuery.trim().lowercase()
        val matchesQuery = if (q.isEmpty()) true else {
            school.nameEn.lowercase().contains(q) ||
            school.nameTa.contains(q) ||
            school.code.lowercase().contains(q) ||
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
            Spacer(modifier = Modifier.height(4.dp))

            // ==================== ULTRA-COMPACT CSV & AI TOOLBAR ====================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("school_csv_ai_card"),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Compact Title & School Count
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Navy900,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isTa) "CSV & AI" else "CSV & AI",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            color = Color(0xFFE0F2FE),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "$totalCount",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    // Right: Compact Action Buttons (AI Check, CSV Import, Template, Export, Reset)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. AI Check Button (Compact Gold Pill)
                        FilledTonalButton(
                            onClick = onRunAiValidation,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFFFEF3C7),
                                contentColor = Color(0xFF92400E)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 28.dp)
                                .testTag("ai_check_schools_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Check",
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isTa) "AI சரிபார்" else "AI Check",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 2. CSV Import Button (Compact Navy Pill)
                        Button(
                            onClick = onImportCsv,
                            colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 28.dp)
                                .testTag("import_schools_csv_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Import CSV",
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isTa) "CSV ஏற்று" else "Import",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 3. Sample CSV Template Button (Compact Outlined)
                        OutlinedButton(
                            onClick = onDownloadCsvTemplate,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 5.dp, vertical = 2.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 28.dp)
                                .testTag("download_csv_template_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Template",
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = if (isTa) "மாதிரி" else "Sample",
                                fontSize = 9.5.sp
                            )
                        }

                        // 4. Export CSV Button
                        IconButton(
                            onClick = onExportCsv,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("export_schools_csv_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Export CSV",
                                modifier = Modifier.size(14.dp),
                                tint = Navy700
                            )
                        }

                        // 5. Reset to Seed Schools
                        IconButton(
                            onClick = { showResetConfirmDialog = true },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("reset_schools_default_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset",
                                modifier = Modifier.size(14.dp),
                                tint = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Search Bar
            AppOutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("school_dir_search_input"),
                placeholder = {
                    Text(if (isTa) "$totalCount பள்ளிகளில் பெயர் அல்லது எண் தேடுக..." else "Search by school name or S.No...")
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
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Category Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(categories) { (key, label) ->
                    FilterChip(
                        selected = selectedCategory == key,
                        onClick = { selectedCategory = key },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = if (isTa) "பள்ளிகள் பட்டியல் (${filtered.size})" else "Schools (${filtered.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )

            Spacer(modifier = Modifier.height(3.dp))

            // List of Schools
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filtered, key = { it.id }) { school ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 68.dp, max = 90.dp)
                            .testTag("dir_school_${school.serialNo}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8EAF6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = school.serialNo.toString(),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Navy900
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    // Town / Village Name prominently displayed
                                    val townName = school.getStationOrVillageName(isTa)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = townName,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Navy900,
                                            maxLines = 1
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(3.dp),
                                            color = Color(0xFFE8F5E9)
                                        ) {
                                            Text(
                                                text = if (isTa) "ஊர்" else "Town",
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2E7D32),
                                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                                            )
                                        }
                                    }

                                    // Full School Name
                                    Text(
                                        text = if (isTa && school.nameTa.isNotEmpty()) school.nameTa else school.nameEn,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(top = 1.dp)
                                    ) {
                                        val (catLabel, catBg, catColor) = when (school.category) {
                                            "BEO_I" -> Triple("BEO I", Color(0xFFE8EAF6), Color(0xFF1A237E))
                                            "BEO_II" -> Triple("BEO II", Color(0xFFE0F2F1), Color(0xFF004D40))
                                            "BEO_III" -> Triple("BEO III", Color(0xFFEDE7F6), Color(0xFF4A148C))
                                            else -> Triple(if (isTa) "அலுவலகம்" else "Office", Color(0xFFFFF3E0), Color(0xFFE65100))
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(3.dp),
                                            color = catBg
                                        ) {
                                            Text(
                                                text = catLabel,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = catColor,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.5.dp)
                                            )
                                        }

                                        if (school.code.isNotEmpty()) {
                                            Text(
                                                text = "UDISE: ${school.code}",
                                                fontSize = 9.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFF8E1)
                                ) {
                                    Text(
                                        text = "${school.distanceFromHqKm} km • ₹${school.defaultBusFare}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE65100),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                FilledTonalButton(
                                    onClick = {
                                        editingSchool = school
                                        showAddEditDialog = true
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .defaultMinSize(minWidth = 40.dp, minHeight = 32.dp)
                                        .testTag("edit_school_btn_${school.serialNo}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = if (isTa) "திருத்து" else "Edit",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
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

    // ==================== CSV IMPORT DIALOG ====================
    if (uiState.showSchoolCsvImportDialog && uiState.pendingCsvSchools != null) {
        SchoolCsvImportDialog(
            parseResult = uiState.pendingCsvSchools,
            isTamil = isTa,
            onConfirm = onConfirmImportSchools,
            onDismiss = onDismissSchoolCsvDialog
        )
    }

    // ==================== AI AUDIT DIALOG ====================
    if (uiState.showAiAuditDialog) {
        SchoolAiAuditDialog(
            isLoading = uiState.isAiValidating,
            report = uiState.aiAuditReport,
            isTamil = isTa,
            onAutoFix = onAutoFixSchoolIssues,
            onDismiss = onDismissAiAuditDialog
        )
    }

    // ==================== RESET CONFIRMATION DIALOG ====================
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text(
                    text = if (isTa) "மாதிரி பள்ளிகளை மீட்டமைக்கவா?" else "Reset to Default Schools?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isTa) {
                        "தற்போதுள்ள பள்ளிகள் நீக்கப்பட்டு அசல் மாதிரி 119 பள்ளிகள் மீட்டமைக்கப்படும். தொடரலாமா?"
                    } else {
                        "Current schools will be replaced with original 119 seed schools. Continue?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetToDefault()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900)
                ) {
                    Text(if (isTa) "ஆம், மீட்டமை" else "Yes, Reset")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirmDialog = false }) {
                    Text(if (isTa) "ரத்து" else "Cancel")
                }
            }
        )
    }

    // ==================== ADD / EDIT SCHOOL DIALOG ====================
    if (showAddEditDialog) {
        var villageTa by remember {
            mutableStateOf(editingSchool?.villageTa?.ifEmpty { editingSchool?.getStationOrVillageName(true) } ?: "")
        }
        var villageEn by remember {
            mutableStateOf(editingSchool?.villageEn?.ifEmpty { editingSchool?.getStationOrVillageName(false) } ?: "")
        }
        var nameTa by remember { mutableStateOf(editingSchool?.nameTa ?: "") }
        var nameEn by remember { mutableStateOf(editingSchool?.nameEn ?: "") }
        var code by remember { mutableStateOf(editingSchool?.code ?: "") }
        var category by remember { mutableStateOf(editingSchool?.category ?: "BEO_I") }
        var distanceKm by remember { mutableStateOf(editingSchool?.distanceFromHqKm?.toString() ?: "15") }
        var busFare by remember { mutableStateOf(editingSchool?.defaultBusFare?.toString() ?: "15") }

        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            title = {
                Text(
                    text = if (editingSchool != null) "பள்ளி & ஊர் பெயர் திருத்துக (Edit School & Town)" else "புதிய பள்ளி சேர்க்க (Add School)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    // Town / Village Name
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ஊரின் பெயர் (Town Name - TA Bill & Diary)",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                            }
                            Text(
                                text = "பயண பதிவில் பள்ளியின் பெயர் வராமல் இந்த ஊரின் பெயர் மட்டுமே பதிவாகும்.",
                                fontSize = 11.sp,
                                color = Color(0xFF15803D)
                            )
                            AppOutlinedTextField(
                                value = villageTa,
                                onValueChange = { villageTa = it },
                                label = { Text("ஊரின் பெயர் தமிழில் (Town/Village)") },
                                placeholder = { Text("எ.கா. சேதுராணி, சாலையூர், சிவகங்கை") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_village_ta_field")
                            )
                        }
                    }

                    AppOutlinedTextField(
                        value = nameTa,
                        onValueChange = {
                            nameTa = it
                            if (villageTa.isBlank()) {
                                villageTa = School.extractVillageName(it)
                            }
                        },
                        label = { Text("பள்ளியின் முழு பெயர் தமிழில் (School Full Name)") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_school_name_ta_field")
                    )
                    AppOutlinedTextField(
                        value = nameEn,
                        onValueChange = {
                            nameEn = it
                            if (villageEn.isBlank()) {
                                villageEn = School.extractVillageName(it)
                            }
                        },
                        label = { Text("School Name (English)") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_school_name_en_field")
                    )
                    AppOutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("UDISE குறியீடு (UDISE Code)") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_school_udise_field")
                    )

                    Text("வட்டார கல்வி அலுவலர் பிரிவு (BEO Section):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Navy900)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        listOf(
                            "BEO_I" to "BEO I",
                            "BEO_II" to "BEO II",
                            "BEO_III" to "BEO III",
                            "OTHER" to (if (isTa) "அலுவலகம்" else "Office")
                        ).forEach { (catKey, catName) ->
                            FilterChip(
                                selected = category == catKey,
                                onClick = { category = catKey },
                                label = { Text(catName, fontSize = 11.sp) }
                            )
                        }
                    }

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

                    if (editingSchool != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = {
                                editingSchool?.let { onDeleteSchool(it) }
                                showAddEditDialog = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = CrimsonRed)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isTa) "இப்பள்ளியை நீக்குக (Delete School)" else "Delete School", color = CrimsonRed)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanVillageTa = villageTa.trim().ifEmpty { School.extractVillageName(nameTa) }
                        val cleanVillageEn = villageEn.trim().ifEmpty { School.extractVillageName(nameEn) }
                        val school = (editingSchool ?: School(
                            serialNo = (uiState.allSchools.maxOfOrNull { it.serialNo } ?: 0) + 1,
                            code = code.trim(),
                            nameEn = nameEn.ifEmpty { nameTa }.trim(),
                            nameTa = nameTa.trim(),
                            category = category,
                            villageTa = cleanVillageTa,
                            villageEn = cleanVillageEn,
                            distanceFromHqKm = distanceKm.toIntOrNull() ?: 15,
                            defaultBusFare = busFare.toIntOrNull() ?: 15
                        )).copy(
                            code = code.trim(),
                            nameTa = nameTa.trim(),
                            nameEn = nameEn.ifEmpty { nameTa }.trim(),
                            category = category,
                            villageTa = cleanVillageTa,
                            villageEn = cleanVillageEn,
                            distanceFromHqKm = distanceKm.toIntOrNull() ?: 15,
                            defaultBusFare = busFare.toIntOrNull() ?: 15
                        )
                        onSaveSchool(school)
                        showAddEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                    modifier = Modifier.testTag("save_school_confirm_btn")
                ) {
                    Text("சேமிக்க (Save)")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showAddEditDialog = false },
                    modifier = Modifier.testTag("cancel_school_edit_btn")
                ) {
                    Text("ரத்து (Cancel)")
                }
            }
        )
    }
}
