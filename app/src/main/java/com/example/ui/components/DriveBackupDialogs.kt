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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToDrive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
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
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.DriveBackupHelper

/**
 * கூகிள் டிரைவ் பேக்கப் முறை தேர்வு உரையாடல் (Backup Mode Selection Dialog)
 */
@Composable
fun BackupChoiceDialog(
    isTamil: Boolean,
    onDirectDriveShare: () -> Unit,
    onSaveFileSaf: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Drive Backup",
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isTamil) "கூகிள் டிரைவில் காப்புநகல்" else "Google Drive Backup",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                    Text(
                        text = if (isTamil) "அனைத்து தரவுகளையும் சேமிக்க" else "Save all app data",
                        fontSize = 11.5.sp,
                        color = TextSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isTamil)
                        "உங்கள் சாதனத்திலுள்ள அனைத்து மாதங்களின் பயணப் பதிவுகள், அலுவலர் விவரங்கள், 119 பள்ளிகள் மற்றும் அமைப்புகள் ஒரு முழுமையான கோப்பாகச் சேமிக்கப்படும்."
                    else
                        "All monthly tour entries, officer profiles, schools, and rate settings will be packaged into a backup file.",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Option 1: Direct Google Drive Upload / Share Sheet
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            onDismiss()
                            onDirectDriveShare()
                        }
                        .testTag("backup_option_direct_drive"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF16A34A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddToDrive,
                                contentDescription = "Drive",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isTamil) "Google Drive-ல் பதிவேற்ற / பகிர" else "Upload / Share to Google Drive",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF14532D)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isTamil) "டிரைவ் பயன்பாட்டின் 'Save to Drive' அல்லது வாட்ஸ்அப் / மின்னஞ்சல்" else "Use Drive app's 'Save to Drive', WhatsApp, or email",
                                fontSize = 10.5.sp,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }

                // Option 2: Choose folder using SAF Document Picker
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            onDismiss()
                            onSaveFileSaf()
                        }
                        .testTag("backup_option_save_file"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Navy700),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Save file",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isTamil) "கோப்புத் தேர்வி மூலம் சேமிக்க (Folder Picker)" else "Save to Folder / Google Drive directly",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Navy900
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isTamil) "Google Drive அல்லது சாதனத்தின் பதிவிறக்கங்கள் கோப்புறையில் சேமிக்கலாம்" else "Pick Google Drive folder or Downloads in document picker",
                                fontSize = 10.5.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = if (isTamil) "ரத்து (Cancel)" else "Cancel")
            }
        }
    )
}

/**
 * காப்புநகல் மீட்டெடுப்பு முன்னோட்டம் மற்றும் உறுதிப்படுத்தல் (Restore Confirmation Dialog)
 */
@Composable
fun RestorePreviewDialog(
    backupData: DriveBackupHelper.BackupData,
    isTamil: Boolean,
    onConfirmRestore: (replaceExisting: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var replaceAll by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEFF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Restore",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isTamil) "காப்புநகல் மீட்டெடுப்பு" else "Restore from Backup",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                    Text(
                        text = if (isTamil) "தரவு சரிபார்க்கப்பட்டது" else "Backup verified",
                        fontSize = 11.5.sp,
                        color = Color(0xFF16A34A)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Summary Card of Backup Content
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Valid",
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTamil) "கோப்பு தேதி: ${backupData.exportedAt}" else "Backup Date: ${backupData.exportedAt}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Navy900
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color(0xFFCBD5E1)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (isTamil) "பயணப் பதிவுகள்" else "Tour Entries",
                                    fontSize = 10.5.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${backupData.tourCount}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                            }
                            Column {
                                Text(
                                    text = if (isTamil) "அலுவலர்கள்" else "Officers",
                                    fontSize = 10.5.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${backupData.officerCount}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                            }
                            Column {
                                Text(
                                    text = if (isTamil) "பள்ளிகள்" else "Schools",
                                    fontSize = 10.5.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${backupData.schoolCount}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                            }
                        }
                    }
                }

                Text(
                    text = if (isTamil) "மீட்டெடுக்கும் முறை (Restore Mode):" else "Select Restore Mode:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )

                // Option 1: Clean Replace (Recommended)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { replaceAll = true }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = replaceAll,
                        onClick = { replaceAll = true },
                        colors = RadioButtonDefaults.colors(selectedColor = Navy900)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = if (isTamil) "முழுமையாகப் புதுப்பி (Clean Restore - சிறந்தது)" else "Clean Replace (Recommended)",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Navy900
                        )
                        Text(
                            text = if (isTamil) "காப்புநகல் கோப்பில் உள்ளபடி துல்லியமாக அமைக்கும்" else "Replaces database entries to match the backup exactly",
                            fontSize = 10.5.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Option 2: Merge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { replaceAll = false }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = !replaceAll,
                        onClick = { replaceAll = false },
                        colors = RadioButtonDefaults.colors(selectedColor = Navy900)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = if (isTamil) "தற்போதுள்ள பதிவுகளுடன் இணை (Merge)" else "Merge with Existing Entries",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Navy900
                        )
                        Text(
                            text = if (isTamil) "தற்போதைய பதிவுகளை நீக்காமல் புதியவற்றை இணைக்கும்" else "Keep current records and add new ones",
                            fontSize = 10.5.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmRestore(replaceAll) },
                colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_restore_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isTamil) "இப்போது மீட்டெடு" else "Restore Now",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = if (isTamil) "ரத்து" else "Cancel")
            }
        }
    )
}
