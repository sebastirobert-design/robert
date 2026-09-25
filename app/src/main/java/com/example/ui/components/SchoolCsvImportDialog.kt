package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.School
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy900
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.SchoolCsvHelper

/**
 * CSV கோப்பிலிருந்து பள்ளிகளை செயலியில் பதிவேற்றம் செய்யும் போது
 * உறுதிசெய்யும் மற்றும் முன்னோட்டம் காட்டும் உரையாடல் பெட்டி (CSV Import Dialog).
 */
@Composable
fun SchoolCsvImportDialog(
    parseResult: SchoolCsvHelper.ParseSchoolResult,
    isTamil: Boolean,
    onConfirm: (replaceExisting: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var replaceExisting by remember { mutableStateOf(true) }
    val schools = parseResult.schools

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0F2FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Navy900,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = if (isTamil) "பள்ளிகள் CSV பதிவேற்றம்" else "Import Schools CSV",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                    Text(
                        text = if (isTamil) "${parseResult.successCount} பள்ளிகள் கண்டறியப்பட்டன" else "${parseResult.successCount} schools found",
                        fontSize = 11.5.sp,
                        color = if (parseResult.successCount > 0) EmeraldGreen else Color(0xFFDC2626),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // பதிவேற்ற முறை தேர்வு (Import Mode Selection)
                Text(
                    text = if (isTamil) "பதிவேற்றும் முறை தேர்ந்தெடுக்கவும்:" else "Select Import Mode:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )

                // Option 1: Replace All Existing Schools (Recommended for new Block BEOs)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { replaceExisting = true }
                        .border(
                            width = if (replaceExisting) 1.5.dp else 1.dp,
                            color = if (replaceExisting) Navy900 else Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .testTag("csv_mode_replace_card"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (replaceExisting) Color(0xFFF0FDF4) else Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = replaceExisting,
                            onClick = { replaceExisting = true },
                            colors = RadioButtonDefaults.colors(selectedColor = Navy900)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isTamil) "முழுமையாக மாற்றுக (பரிந்துரை)" else "Replace All (Recommended)",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    color = EmeraldGreen,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isTamil) "பிற ஒன்றிய BEO" else "Other Block",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isTamil) "தற்போதைய பழைய பள்ளிகளை நீக்கிவிட்டு, இந்த புதிய பள்ளிகளை மட்டும் தலைமையிட தூரத்தோடு அமைக்கும்." else "Replaces existing schools entirely with this imported list.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                // Option 2: Append to existing
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { replaceExisting = false }
                        .border(
                            width = if (!replaceExisting) 1.5.dp else 1.dp,
                            color = if (!replaceExisting) Navy900 else Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .testTag("csv_mode_append_card"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (!replaceExisting) Color(0xFFEFF6FF) else Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !replaceExisting,
                            onClick = { replaceExisting = false },
                            colors = RadioButtonDefaults.colors(selectedColor = Navy900)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isTamil) "தற்போதைய பள்ளிகளுடன் சேர்க்க" else "Append / Merge with existing",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                            Text(
                                text = if (isTamil) "ஏற்கனவே உள்ள பள்ளிகளுடன் இந்த புதிய பள்ளிகளையும் சேர்க்கும்." else "Keeps current schools and adds these new schools.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                // மாதிரி முன்னோட்டம் (Preview of First 4 Schools)
                if (schools.isNotEmpty()) {
                    Text(
                        text = if (isTamil) "பள்ளிகள் மாதிரி முன்னோட்டம் (முதல் ${minOf(4, schools.size)}):" else "Preview (First ${minOf(4, schools.size)}):",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy900,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    schools.take(4).forEach { school ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${school.serialNo}. ${school.getStationOrVillageName(isTamil)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Navy900
                                    )
                                    Text(
                                        text = if (isTamil && school.nameTa.isNotBlank()) school.nameTa else school.nameEn,
                                        fontSize = 10.5.sp,
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                }
                                Surface(
                                    color = Color(0xFFFFF8E1),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${school.distanceFromHqKm} km • ₹${school.defaultBusFare}",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // எச்சரிக்கைகள் (Warnings)
                if (parseResult.warnings.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isTamil) "கவனிக்கத்தக்க குறிப்புகள்:" else "Parsing Notes:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            }
                            parseResult.warnings.take(3).forEach { warn ->
                                Text(
                                    text = "• $warn",
                                    fontSize = 10.sp,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(replaceExisting) },
                enabled = schools.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_csv_import_btn")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isTamil) "பள்ளிகளைப் பதிவேற்று (${schools.size})" else "Import (${schools.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("cancel_csv_import_btn")
            ) {
                Text(if (isTamil) "ரத்து" else "Cancel", fontSize = 12.sp)
            }
        }
    )
}
