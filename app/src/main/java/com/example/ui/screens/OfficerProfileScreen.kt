package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.example.data.model.OfficerProfile
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy900
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TaBillUiState

@Composable
fun OfficerProfileScreen(
    uiState: TaBillUiState,
    onSwitchOfficer: (Long) -> Unit,
    onUpdateOfficer: (OfficerProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val isTa = uiState.isTamil
    val currentOfficer = uiState.activeOfficer

    var name by remember(currentOfficer) { mutableStateOf(currentOfficer.name) }
    var designation by remember(currentOfficer) { mutableStateOf(currentOfficer.designation) }
    var shortDesignation by remember(currentOfficer) { mutableStateOf(currentOfficer.shortDesignation) }
    var headquarters by remember(currentOfficer) { mutableStateOf(currentOfficer.headquarters) }
    var district by remember(currentOfficer) { mutableStateOf(currentOfficer.district) }
    var basicPayStr by remember(currentOfficer) { mutableStateOf(String.format("%.0f", currentOfficer.basicPay)) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F9))
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Switch Officer Profile Card
        item {
            Text(
                text = if (isTa) "அலுவலர் தேர்வு (Select Officer)" else "Select Officer Profile",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                uiState.allOfficers.forEach { off ->
                    val isSelected = off.id == currentOfficer.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSwitchOfficer(off.id) }
                            .testTag("officer_card_${off.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFE8EAF6) else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Navy700 else Color(0xFFCFD8DC)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Officer",
                                        tint = if (isSelected) GoldAccent else Color.DarkGray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${off.shortDesignation}: ${off.name}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Basic Pay: ₹${String.format("%.0f", off.basicPay)} • ${off.headquarters}",
                                        fontSize = 11.5.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            if (isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF2E7D32)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = "Active", tint = Color.White, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(text = "செயலில்", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Edit Officer Form
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isTa) "அலுவலர் விவரங்கள் திருத்துக" else "Edit Officer Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Navy900
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AppOutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("அலுவலர் பெயர் (Officer Name)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("officer_name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    AppOutlinedTextField(
                        value = designation,
                        onValueChange = { designation = it },
                        label = { Text("பதவி (Designation)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppOutlinedTextField(
                            value = shortDesignation,
                            onValueChange = { shortDesignation = it },
                            label = { Text("சுருக்கப் பெயர் (e.g. BEO 1)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        AppOutlinedTextField(
                            value = basicPayStr,
                            onValueChange = { basicPayStr = it },
                            label = { Text("அடிப்படை ஊதியம் (Basic Pay ₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("officer_basic_pay_input"),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppOutlinedTextField(
                            value = headquarters,
                            onValueChange = { headquarters = it },
                            label = { Text("தலைமையிடம் (Headquarters)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        AppOutlinedTextField(
                            value = district,
                            onValueChange = { district = it },
                            label = { Text("மாவட்டம் (District)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val updated = currentOfficer.copy(
                                name = name,
                                designation = designation,
                                shortDesignation = shortDesignation,
                                basicPay = basicPayStr.toDoubleOrNull() ?: currentOfficer.basicPay,
                                headquarters = headquarters,
                                district = district
                            )
                            onUpdateOfficer(updated)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_officer_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy700),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Save")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("விவரங்களை சேமிக்க (Save Profile)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
