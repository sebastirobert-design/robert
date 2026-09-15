package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private data class GuideTopic(
    val id: Int,
    val titleTa: String,
    val titleEn: String,
    val icon: ImageVector,
    val badgeColor: Color
)

private val guideTopics = listOf(
    GuideTopic(1, "1. ஆரம்ப அமைப்புகள்", "Initial Setup", Icons.Default.Settings, Navy800),
    GuideTopic(2, "2. தினசரி பயணப் பதிவு", "Daily Tour Entry", Icons.Default.DirectionsBus, Color(0xFF0284C7)),
    GuideTopic(3, "3. பயணமற்ற நாட்கள்", "Non-Travel Days", Icons.Default.CalendarToday, Color(0xFFD97706)),
    GuideTopic(4, "4. படிவம் 1 (Tour Diary)", "Form 1 Diary", Icons.Default.EditNote, EmeraldGreen),
    GuideTopic(5, "5. படிவம் 2 (TA Bill)", "Form 2 TA Bill", Icons.Default.ReceiptLong, Navy900),
    GuideTopic(6, "6. 119 பள்ளிகள் பட்டியல்", "119 Schools Directory", Icons.Default.School, Color(0xFF7C3AED)),
    GuideTopic(7, "7. PDF & Excel சேமிப்பு", "Print & Export", Icons.Default.Print, Color(0xFFE11D48)),
    GuideTopic(8, "8. Google Drive பேக்கப்", "Drive Backup", Icons.Default.CloudUpload, Color(0xFF059669))
)

@Composable
fun UserGuideDialog(
    isTamil: Boolean,
    onDismiss: () -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var selectedTopicId by remember { mutableIntStateOf(1) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = Color(0xFFF8FAFC),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("user_guide_dialog")
            ) {
                // Header Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Navy900)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(GoldAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = "Manual",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isTamil) "பயனர் கையேடு (User Manual)" else "BEO TA Bill - User Manual",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isTamil) "இளையான்குடி ஒன்றியம் – முழுமையான வழிகாட்டி" else "Ilayankudi Block - Step-by-Step Guide",
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .testTag("close_user_guide_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Quick Navigation Chips
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(guideTopics) { topic ->
                            val isSelected = selectedTopicId == topic.id
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) topic.badgeColor else Color(0xFFF1F5F9),
                                modifier = Modifier.clickable {
                                    selectedTopicId = topic.id
                                    coroutineScope.launch {
                                        // Scroll to target item in list
                                        listState.animateScrollToItem(topic.id - 1)
                                    }
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = topic.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else topic.badgeColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isTamil) topic.titleTa else topic.titleEn,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Navy900
                                    )
                                }
                            }
                        }
                    }
                }

                // Content Scroll View
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // TOPIC 1: ஆரம்ப அமைப்புகள்
                    item {
                        GuideSectionCard(
                            stepNumber = "1",
                            title = if (isTamil) "ஆரம்ப அமைப்புகள் (Initial Setup)" else "Initial Setup",
                            icon = Icons.Default.Settings,
                            color = Navy800
                        ) {
                            Text(
                                text = if (isTamil)
                                    "செயலியைத் தொடங்கியதும் முதலில் செய்ய வேண்டிய அடிப்படை விவரங்கள்:"
                                else
                                    "Essential settings to configure upon first launch:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Navy900
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            GuideStepItem(
                                number = "A",
                                title = if (isTamil) "வட்டாரக் கல்வி அலுவலர் சுயவிவரம் (Profile)" else "Officer Profile",
                                desc = if (isTamil)
                                    "கீழேயுள்ள மெனுவில் 'அமைப்புகள்' (Settings) -> 'அலுவலர் சுயவிவரம்' பகுதிக்குச் செல்லவும். BEO 1, 2, அல்லது 3-ஐத் தேர்வுசெய்து பெயர், பதவி மற்றும் அடிப்படை ஊதியம் (Basic Pay) உள்ளிட்டு சேமிக்கவும்."
                                else
                                    "Navigate to Settings -> Officer Profile. Select Officer Slot (1, 2, or 3), enter Name, Designation, and Basic Pay, then tap Save."
                            )
                            GuideStepItem(
                                number = "B",
                                title = if (isTamil) "படி விகிதங்கள் (Rates & Allowances)" else "Standard Allowance Rates",
                                desc = if (isTamil)
                                    "தமிழ்நாடு அரசு கருவூல விதிகளின்படி தினசரிப் படி விகிதம் (DA Rate ₹300, DA Amount ₹210) மற்றும் முனையக் கட்டணம் (Terminal Charges 17(a) ₹20, 17(b) ₹20) முன்கூட்டியே அமைக்கப்பட்டுள்ளது. மாற்றம் தேவைப்பட்டால் மாற்றிக் கொள்ளலாம்."
                                else
                                    "Government rules are pre-configured: Daily Allowance (DA Rate ₹300, Claim ₹210) and Terminal Charges 17(a)/17(b) at ₹20 each."
                            )
                        }
                    }

                    // TOPIC 2: தினசரி பயணப் பதிவு சேர்த்தல்
                    item {
                        GuideSectionCard(
                            stepNumber = "2",
                            title = if (isTamil) "தினசரி பயணப் பதிவு சேர்த்தல் (Daily Tour Entry)" else "Daily Tour Entry",
                            icon = Icons.Default.DirectionsBus,
                            color = Color(0xFF0284C7)
                        ) {
                            Text(
                                text = if (isTamil)
                                    "பள்ளிப் பார்வை அல்லது ஆய்விற்குச் செல்லும்போது புதிய பயணத்தை எளிதாகப் பதிவு செய்யலாம்:"
                                else
                                    "Steps to enter school inspection or official tour travels:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Navy900
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            GuideStepItem(
                                number = "1",
                                title = if (isTamil) "புதிய பயணம் திறக்க (+)" else "Open New Tour (+)",
                                desc = if (isTamil)
                                    "முகப்புத் திரையில் வலதுபுறம் கீழே உள்ள நீல நிற (+) பட்டனை அழுத்தவும்."
                                else
                                    "Tap the blue floating (+) button at the bottom-right corner of the Home screen."
                            )
                            GuideStepItem(
                                number = "2",
                                title = if (isTamil) "பள்ளி தேர்வு (School Picker - 119 பள்ளிகள்)" else "School Selection",
                                desc = if (isTamil)
                                    "'பள்ளி தேர்வு' பட்டனை அழுத்தி பட்டியலிலிருந்து பள்ளியைத் தேர்வு செய்யவும் (எ.கா: சாலையூர் ஹமீதியா, சூராணம்). பள்ளிப் பெயரைத் தட்டச்சு செய்தும் தேடலாம். தேர்வு செய்தவுடன் தூரம் (Km) மற்றும் பேருந்து கட்டணம் தானாகவே பூர்த்தியாகும்!"
                                else
                                    "Tap 'School Picker' and choose your destination school. Distance (KM) and Bus Fare populate automatically from the built-in directory!"
                            )
                            GuideStepItem(
                                number = "3",
                                title = if (isTamil) "பயண நோக்கம் (Purpose of Tour)" else "Purpose of Tour",
                                desc = if (isTamil)
                                    "'பள்ளிபார்வை' (School Inspection), 'ஆய்வு', அல்லது 'அலுவல் பணி' என்பதைத் தேர்வு செய்யவும்."
                                else
                                    "Select Purpose: 'School Inspection' (பள்ளிபார்வை), 'Audit/Inspection', or 'Official Work'."
                            )
                            GuideStepItem(
                                number = "4",
                                title = if (isTamil) "சுற்றுப்பயணம் (Round Trip - To & Fro)" else "Round Trip Included",
                                desc = if (isTamil)
                                    "பள்ளிக்குச் சென்று தலைமையிடம் திரும்புவது (To & Fro) ஒரே பதிவாகவே முழுமையாகப் படிவம் 1 மற்றும் படிவம் 2-ல் தமிழ்நாடு அரசு விதிகளின்படி பதிவாகிவிடும்."
                                else
                                    "Departure from HQ and arrival back to HQ are recorded in a single entry as per TN Treasury norms."
                            )
                        }
                    }

                    // TOPIC 3: பயணமற்ற நாட்கள் பதிவு
                    item {
                        GuideSectionCard(
                            stepNumber = "3",
                            title = if (isTamil) "பயணமற்ற நாட்கள் பதிவு (Non-Travel Days)" else "Non-Travel Days",
                            icon = Icons.Default.CalendarToday,
                            color = Color(0xFFD97706)
                        ) {
                            Text(
                                text = if (isTamil)
                                    "பயணம் மேற்கொள்ளாத விடுப்பு மற்றும் அலுவலகப் பணி நாட்களைப் பதிவு செய்ய:"
                                else
                                    "Recording office duty or leaves with zero travel allowance:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Navy900
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            GuideStepItem(
                                number = "1",
                                title = if (isTamil) "பச்சை நிற நாள்காட்டி பொத்தான்" else "Green Calendar Quick Button",
                                desc = if (isTamil)
                                    "முகப்புத் திரையில் உள்ள பச்சை நிற நாள்காட்டி (+) பொத்தானை அழுத்தவும்."
                                else
                                    "Tap the green calendar (+) button on the Home screen to open quick non-travel recording."
                            )
                            GuideStepItem(
                                number = "2",
                                title = if (isTamil) "பணி / விடுப்பு நிலையைத் தேர்வு செய்ய" else "Select Duty or Leave Status",
                                desc = if (isTamil)
                                    "• தலைமையிடப் பணி (HQ Duty / Office)\n• தற்செயல் விடுப்பு (Casual Leave - CL)\n• மருத்துவ விடுப்பு (Medical Leave - ML)\n• அரசு விடுமுறை / ஞாயிறு (Holiday / Sunday)"
                                else
                                    "• HQ Duty / Office\n• Casual Leave (CL)\n• Medical Leave (ML)\n• Government Holiday / Sunday"
                            )
                            GuideStepItem(
                                number = "3",
                                title = if (isTamil) "கருவூலத் தணிக்கை விதி" else "Treasury Audit Compliance",
                                desc = if (isTamil)
                                    "இது படிவம் 1-ல் தெளிவாகக் காட்டப்படும்; படிவம் 2-ல் TA தொகை ₹0 எனக் கருவூல விதிகளின்படி மிகச் சரியாகப் பராமரிக்கப்படும்."
                                else
                                    "Appears in Form 1 Tour Diary for continuous day accountability, while claiming ₹0 in Form 2 TA Bill."
                            )
                        }
                    }

                    // TOPIC 4: படிவம் 1 – பயண நாள்குறிப்பு
                    item {
                        GuideSectionCard(
                            stepNumber = "4",
                            title = if (isTamil) "படிவம் 1 – பயண நாள்குறிப்பு (Form 1 Tour Diary)" else "Form 1 - Tour Diary",
                            icon = Icons.Default.EditNote,
                            color = EmeraldGreen
                        ) {
                            Text(
                                text = if (isTamil)
                                    "தமிழ்நாடு பள்ளிக் கல்வித் துறையின் அதிகாரப்பூர்வ 9 பத்திகள் (9 Columns) கொண்ட மாதாந்திர பயண நாள்குறிப்பு:"
                                else
                                    "Official 9-column monthly tour diary of the Tamil Nadu School Education Department:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Navy900
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isTamil)
                                    "1. தேதி (Date)\n2. புறப்பட்ட இடம் & நேரம் (Departure Station & Time)\n3. சேர்ந்த இடம் & நேரம் (Arrival Station & Time)\n4. பயண விதம் (Mode of Conveyance - பேருந்து/நடை/பைக்)\n5. தூரம் கி.மீ (Distance in KM)\n6. சென்ற நோக்கம் (Purpose of Journey)\n7. பள்ளி பார்வை விவரம் (Schools Inspected)\n8. குறிப்புகள் (Remarks)\n9. தலைமையிட திரும்பிய விவரம்"
                                else
                                    "1. Date\n2. Departure Station & Time\n3. Arrival Station & Time\n4. Conveyance Mode (Bus/Walk/Bike)\n5. Distance in KM\n6. Purpose of Journey\n7. Schools Inspected\n8. Remarks\n9. Return to HQ details",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 19.sp
                            )
                        }
                    }

                    // TOPIC 5: படிவம் 2 – TA Bill
                    item {
                        GuideSectionCard(
                            stepNumber = "5",
                            title = if (isTamil) "படிவம் 2 – TA Bill பயணப்படி பட்டியல்" else "Form 2 - TA Bill Preparation",
                            icon = Icons.Default.ReceiptLong,
                            color = Navy900
                        ) {
                            Text(
                                text = if (isTamil)
                                    "கருவூல தணிக்கைக்கான 20 பத்திகள் (20 Columns) கொண்ட முழுமையான TA Bill:"
                                else
                                    "Official 20-column bill preparation ready for treasury audit submission:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Navy900
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            GuideStepItem(
                                number = "★",
                                title = if (isTamil) "8 கி.மீ. விதி (Short Distance Rule)" else "8 KM Treasury Rule",
                                desc = if (isTamil)
                                    "8 கி.மீ.-க்கு குறைவான தூரப் பயணங்களுக்கு அரசு விதிகளின்படி DA கிடைக்காது. செயலி இதனைத் தானாகவே கண்டறிந்து ₹0 எனத் துல்லியமாக வைக்கிறது."
                                else
                                    "Journeys under 8 KM are automatically marked with ₹0 Daily Allowance (DA) as per government orders."
                            )
                            GuideStepItem(
                                number = "★",
                                title = if (isTamil) "பயணப்படி மற்றும் முனையக் கட்டணம்" else "DA & Terminal Charges",
                                desc = if (isTamil)
                                    "பேருந்து கட்டணம், தினசரிப் படி (DA ₹210), முனையக் கட்டணம் 17(a) & 17(b) அனைத்தும் வரிசை வாரியாகக் கூட்டப்பட்டு மாத மொத்தத் தொகை (Grand Total) கணக்கிடப்படுகிறது."
                                else
                                    "Bus fare, Daily Allowance, and Terminal charges 17(a)/17(b) are itemized and summed to the Grand Total."
                            )
                        }
                    }

                    // TOPIC 6: 119 பள்ளிகள் பட்டியல்
                    item {
                        GuideSectionCard(
                            stepNumber = "6",
                            title = if (isTamil) "119 பள்ளிகள் பட்டியல் (Schools Directory)" else "119 Schools Directory",
                            icon = Icons.Default.School,
                            color = Color(0xFF7C3AED)
                        ) {
                            Text(
                                text = if (isTamil)
                                    "இளையான்குடி ஒன்றியத்தின் அனைத்து 119 பள்ளிகளும் BEO வாரியாகப் பிரிக்கப்பட்டு ஏற்றப்பட்டுள்ளது:"
                                else
                                    "All 119 schools across Ilayankudi block categorized by BEO jurisdictions:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Navy900
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isTamil)
                                    "• BEO I வரம்பு: 35 தொடக்க / நடுநிலைப் பள்ளிகள்\n• BEO II வரம்பு: 47 தொடக்க / நடுநிலைப் பள்ளிகள்\n• BEO III வரம்பு: 37 தொடக்க / நடுநிலைப் பள்ளிகள்\n• சிறப்பு இடங்கள் (Frequent Stations): சிவகங்கை (DEO/CEO), மதுரை உயர்நீதிமன்றம் (வீரசிங்கம் கேஸ்), காளையார்கோவில் DIET, மதுரை பில்லர் ஹால்."
                                else
                                    "• BEO I Jurisdiction: 35 Schools\n• BEO II Jurisdiction: 47 Schools\n• BEO III Jurisdiction: 37 Schools\n• Frequent Stations: Sivagangai (DEO/CEO/Collectorate), Madurai High Court, Kalayarkovil DIET, Pillar Hall.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 19.sp
                            )
                        }
                    }

                    // TOPIC 7: அறிக்கைகள் அச்சிடுதல் & PDF/Excel
                    item {
                        GuideSectionCard(
                            stepNumber = "7",
                            title = if (isTamil) "அறிக்கைகள் அச்சிடு & PDF / Excel சேமிப்பு" else "Print Reports & Export PDF/Excel",
                            icon = Icons.Default.Print,
                            color = Color(0xFFE11D48)
                        ) {
                            Text(
                                text = if (isTamil)
                                    "தலைப்பில் உள்ள 'அறிக்கைகள் அச்சிடு & சேமி' (Print / Export) பொத்தான் மூலம் பெறக்கூடிய ஆவணங்கள்:"
                                else
                                    "Reports accessible via the header Print & Export dialog:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Navy900
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            GuideStepItem(
                                number = "PDF",
                                title = if (isTamil) "அதிகாரப்பூர்வ PDF ஆவணம்" else "Official PDF Document",
                                desc = if (isTamil)
                                    "படிவம் 1 (Tour Diary) மற்றும் படிவம் 2 (TA Bill) இரண்டையும் அரசு விதிகளின்படி A4 தாளில் பிரிண்ட் செய்யக்கூடிய அல்லது PDF ஆகப் பதிவிறக்கக்கூடிய வசதி."
                                else
                                    "High-quality print layout with official verification certificates and signature placeholders ready for A4 printing."
                            )
                            GuideStepItem(
                                number = "XLS",
                                title = if (isTamil) "Excel & Google Sheets (CSV)" else "Excel & Google Sheets (CSV)",
                                desc = if (isTamil)
                                    "தமிழ் எழுத்துருக்கள் உடையாமல் (UTF-8 Tamil) நேரடியாக Microsoft Excel அல்லது Google Sheets-ல் திறந்து திருத்தக்கூடிய கோப்பாகப் பதிவிறக்கலாம்."
                                else
                                    "Full UTF-8 BOM encoding ensures Tamil characters render perfectly in Microsoft Excel and Google Sheets."
                            )
                        }
                    }

                    // TOPIC 8: கூகிள் டிரைவ் பேக்கப் & மீட்டெடுப்பு
                    item {
                        GuideSectionCard(
                            stepNumber = "8",
                            title = if (isTamil) "கூகிள் டிரைவில் காப்புநகல் & மீட்டெடுப்பு" else "Google Drive Backup & Restore",
                            icon = Icons.Default.CloudUpload,
                            color = Color(0xFF059669)
                        ) {
                            Text(
                                text = if (isTamil)
                                    "உங்கள் தகவல்கள் ஒருபோதும் அழியாமல் பாதுகாக்க எளிய காப்புநகல் முறை:"
                                else
                                    "Safeguard your data across phone changes and upgrades:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Navy900
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            GuideStepItem(
                                number = "1",
                                title = if (isTamil) "டிரைவில் சேமிக்க (Backup to Drive)" else "Backup to Drive",
                                desc = if (isTamil)
                                    "'அமைப்புகள்' திரையில் உள்ள 'டிரைவில் பேக்கப்' பொத்தானை அழுத்தவும். உங்கள் போனின் Google Drive 'Save to Drive' அல்லது வாட்ஸ்அப் / மெயில் மூலம் உடனடியாகப் பதிவேற்றலாம்."
                                else
                                    "Tap 'Backup to Drive' in Settings to save your backup JSON file directly to Google Drive or share via email."
                            )
                            GuideStepItem(
                                number = "2",
                                title = if (isTamil) "மீட்டெடுக்க (Restore File)" else "Restore from File",
                                desc = if (isTamil)
                                    "புதிய போனில் செயலியை நிறுவும் போது, 'மீட்டெடு' பொத்தானை அழுத்தி கூகிள் டிரைவில் உள்ள பேக்கப் கோப்பைத் தேர்வு செய்தால் அனைத்துப் பதிவுகளும் ஒரு நொடியில் வந்துவிடும்!"
                                else
                                    "Tap 'Restore File', pick your backup from Google Drive, preview the contents, and restore with one tap."
                            )
                        }
                    }

                    // Bottom Tips Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "Tip",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isTamil) "முக்கியக் குறிப்பு (Quick Tip)" else "Important Note",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF92400E)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isTamil)
                                            "இச்செயலி 100% உங்கள் மொபைலிலேயே ஆஃப்லைனில் இயங்குகிறது. இணைய இணைப்பு தேவையில்லை. உங்கள் தகவல்கள் அனைத்தும் உங்கள் மொபைலில் மட்டுமே பாதுகாப்பாக இருக்கும்."
                                        else
                                            "This application runs 100% offline on your device. No internet required for daily operations. All records remain safe and private.",
                                        fontSize = 11.sp,
                                        color = Color(0xFFB45309),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                // Footer Close Button
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("user_guide_understand_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTamil) "புரிந்தது (Close Manual)" else "Understood (Close)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideSectionCard(
    stepNumber: String,
    title: String,
    icon: ImageVector,
    color: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Navy900,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun GuideStepItem(
    number: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Navy800
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )
        }
    }
}
