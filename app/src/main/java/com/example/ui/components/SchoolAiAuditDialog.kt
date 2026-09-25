package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.theme.Navy900
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.SchoolAiValidator

/**
 * பள்ளிகள் பட்டியல், தலைமையிட தூரம் மற்றும் பேருந்து கட்டணங்கள்
 * தமிழ்நாடு அரசு பயணப்படி விதிகளின்படி சரியானதாக இருக்கிறதா என்பதை
 * செக் செய்யும் AI சரிபார்ப்பு உரையாடல் பெட்டி (AI Verification Dialog).
 */
@Composable
fun SchoolAiAuditDialog(
    isLoading: Boolean,
    report: SchoolAiValidator.AiAuditReport?,
    isTamil: Boolean,
    onAutoFix: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isTamil) "AI சரிபார்ப்பு & தணிக்கை" else "AI Verification & Quality Check",
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                        Text(
                            text = if (isTamil) "TN அரசு பயணப்படி (TA) விதிகள் பகுப்பாய்வு" else "TN TA Rules Compliance Analysis",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }
        },
        text = {
            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = Navy900,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isTamil) "AI மூலம் பள்ளிகள் மற்றும் கட்டணங்கள் ஆய்வு செய்யப்படுகிறது..." else "AI is analyzing distances, fares, & TA rules...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Navy900
                    )
                    Text(
                        text = if (isTamil) "தலைமையிட தூரம் • பேருந்து கட்டணம் • 8 கி.மீ எல்லை விதி" else "Headquarters Distance • Bus Fare • 8 KM Radius",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else if (report != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // தரம் & மதிப்பீடு அட்டை (Quality Score Hero Card)
                    val scoreColor = when {
                        report.qualityScore >= 90 -> EmeraldGreen
                        report.qualityScore >= 75 -> Color(0xFFD97706)
                        else -> Color(0xFFDC2626)
                    }
                    val scoreBg = when {
                        report.qualityScore >= 90 -> Color(0xFFF0FDF4)
                        report.qualityScore >= 75 -> Color(0xFFFFFBEB)
                        else -> Color(0xFFFEF2F2)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = scoreBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, scoreColor.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (report.qualityScore >= 80) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = scoreColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (report.qualityScore >= 90) {
                                            if (isTamil) "சரியானது • சிறந்த தரம்" else "Excellent • Valid"
                                        } else if (report.qualityScore >= 75) {
                                            if (isTamil) "நன்று • சில சரிசெய்தல்கள் தேவை" else "Good • Minor Fixes Needed"
                                        } else {
                                            if (isTamil) "கவனம் தேவை • முரண்பாடுகள் உள்ளன" else "Attention Needed • Discrepancies Found"
                                        },
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = scoreColor
                                    )
                                }
                                Text(
                                    text = if (isTamil) "மொத்தம் ${report.totalSchools} பள்ளிகள் ஆய்வு செய்யப்பட்டன" else "${report.totalSchools} schools evaluated",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Surface(
                                color = scoreColor,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${report.qualityScore}%",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // 4 முக்கிய அளவீடுகள் (Key Metrics Grid)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MetricBox(
                            title = if (isTamil) "8 கி.மீ அப்பால்" else "> 8 km",
                            value = "${report.outstationCount}",
                            subtitle = if (isTamil) "முழு DA தகுதி" else "Full TA Eligible",
                            color = EmeraldGreen,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            title = if (isTamil) "8 கி.மீக்குள்" else "<= 8 km",
                            value = "${report.localRadiusCount}",
                            subtitle = if (isTamil) "உள்ளூர் எல்லை" else "Local HQ Radius",
                            color = Color(0xFF0284C7),
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            title = if (isTamil) "சராசரி தூரம்" else "Avg Dist",
                            value = "%.1f km".format(report.averageDistanceKm),
                            subtitle = if (isTamil) "சராசரி ₹%.0f".format(report.averageBusFare) else "Avg ₹%.0f".format(report.averageBusFare),
                            color = Navy900,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // AI விரிவான தணிக்கை அறிக்கை (AI Narrative Report)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isTamil) "AI தணிக்கை அறிக்கை" else "AI Audit Narrative",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Navy900
                                    )
                                }
                                Surface(
                                    color = if (report.isGeminiPowered) Color(0xFFFEF3C7) else Color(0xFFE0E7FF),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (report.isGeminiPowered) "✨ Gemini 3.5 Flash" else "🛡️ Local AI Rules",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (report.isGeminiPowered) Color(0xFFB45309) else Color(0xFF3730A3),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = report.aiNarrativeSummaryTa,
                                fontSize = 11.5.sp,
                                lineHeight = 17.sp,
                                color = Navy900
                            )
                        }
                    }

                    // AI பரிந்துரைகள் (AI Recommendations)
                    if (report.aiRecommendationsTa.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (isTamil) "💡 AI வழிகாட்டுதல்கள் (BEO TA Claim):" else "💡 Recommendations:",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E40AF)
                                )
                                report.aiRecommendationsTa.forEach { rec ->
                                    Text(
                                        text = "• $rec",
                                        fontSize = 11.sp,
                                        color = Color(0xFF1E3A8A),
                                        modifier = Modifier.padding(top = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    // கண்டறியப்பட்ட முரண்பாடுகள் பட்டியல் (Issues / Discrepancies)
                    if (report.issues.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isTamil) "கவனிக்க வேண்டியவை (${report.issues.size}):" else "Issues Found (${report.issues.size}):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                        }

                        report.issues.forEach { issue ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFEE2E2)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                            ) {
                                Column(modifier = Modifier.padding(9.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = issue.schoolName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Navy900
                                        )
                                        Surface(
                                            color = Color(0xFFFEF2F2),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "${issue.distanceKm} km • ₹${issue.busFare}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFDC2626),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = issue.messageTa,
                                        fontSize = 10.5.sp,
                                        color = Color(0xFFB91C1C),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                    Text(
                                        text = "👉 பரிந்துரை: ${issue.suggestion}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF047857),
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (report != null && report.issues.isNotEmpty()) {
                Button(
                    onClick = onAutoFix,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("auto_fix_schools_btn")
                ) {
                    Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isTamil) "தானாக சீரமைக்க (Auto-Fix)" else "Auto-Fix Issues",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("close_ai_audit_btn")
                ) {
                    Text(if (isTamil) "சரி (Done)" else "Done", fontSize = 12.sp)
                }
            }
        },
        dismissButton = {
            if (report != null && report.issues.isNotEmpty()) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isTamil) "மூடுக" else "Close", fontSize = 12.sp)
                }
            }
        }
    )
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 9.5.sp, color = TextSecondary)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
            Text(subtitle, fontSize = 8.5.sp, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}
