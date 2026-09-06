package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import com.example.ui.components.AppOutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberDark
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy900
import com.example.ui.theme.TextSecondary
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickNonTravelDialog(
    monthYear: String,
    initialDay: Int = DateUtils.getCurrentCalendar().get(java.util.Calendar.DAY_OF_MONTH),
    onAddNonTravel: (dayOfMonth: Int, dateFormatted: String, type: String) -> Unit,
    onDismiss: () -> Unit,
    isTamil: Boolean,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()
    val (year, month) = DateUtils.parseYearMonth(monthYear)
    var dayOfMonth by remember { mutableIntStateOf(initialDay.coerceIn(1, 31)) }
    var dayStr by remember { mutableStateOf(String.format("%02d", initialDay.coerceIn(1, 31))) }
    var selectedType by remember { mutableStateOf("தற்செயல்விடுப்பு") }

    val types = listOf(
        Triple("தற்செயல்விடுப்பு", "Casual Leave (CL)", CrimsonRed),
        Triple("விடுமுறை", "Holiday / Weekend", AmberDark),
        Triple("அலுவலகப்பணி", "HQ Office Duty", BlueAccent)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTamil) "விடுமுறை / அலுவலகப் பணி பதிவு" else "Add Leave / Holiday / HQ Duty",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                    label = { Text(if (isTamil) "தேதி (Day)" else "Day (1-31)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .width(120.dp)
                        .testTag("non_travel_day_input")
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
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = formattedDate,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                        Text(
                            text = DateUtils.getDayOfWeekTamil(formattedDate),
                            fontSize = 11.5.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isTamil) "வகை தேர்வு செய்க:" else "Select Type:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Type selector cards
            types.forEach { (typeKey, typeLabel, color) ->
                val isSelected = selectedType == typeKey
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = { selectedType = typeKey },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFE8EAF6) else Color(0xFFF8F9FA)
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Navy700) else null
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when (typeKey) {
                            "தற்செயல்விடுப்பு" -> Icons.Default.EventBusy
                            "விடுமுறை" -> Icons.Default.BeachAccess
                            else -> Icons.Default.Business
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = typeKey,
                            tint = color
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = typeKey,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Navy900
                            )
                            Text(
                                text = typeLabel,
                                fontSize = 11.5.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val dateFormatted = DateUtils.formatDate(year, month, dayOfMonth)
                    onAddNonTravel(dayOfMonth, dateFormatted, selectedType)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_non_travel_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = Navy700),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (isTamil) "சேமிக்க (Save)" else "Save",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
