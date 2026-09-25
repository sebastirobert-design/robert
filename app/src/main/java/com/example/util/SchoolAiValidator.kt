package com.example.util

import com.example.BuildConfig
import com.example.data.model.School
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * பள்ளிகள், தலைமையிட தூரம் மற்றும் பேருந்து கட்டணங்கள் சரியானதாக இருக்கிறதா
 * என்பதை ஆய்வு செய்யும் AI சரிபார்ப்பு கருவி (AI School & TA Verification Engine).
 * தமிழ்நாடு அரசு பயணப்படி விதிகளின்படி (TN TA Rules) பகுப்பாய்வு செய்து
 * அறிக்கை மற்றும் பரிந்துரைகளை வழங்கும்.
 */
object SchoolAiValidator {

    data class ValidationIssue(
        val schoolId: Long,
        val schoolName: String,
        val townName: String,
        val distanceKm: Int,
        val busFare: Int,
        val issueType: IssueType,
        val messageTa: String,
        val messageEn: String,
        val suggestion: String
    )

    enum class IssueType {
        FARE_ANOMALY,       // கட்டணம் மிக அதிகம் அல்லது மிக குறைவு
        ZERO_DISTANCE_FARE, // தூரம் 0 ஆனால் கட்டணம் உள்ளது
        HQ_LOCAL_RADIUS,    // 8 கி.மீக்குள் உள்ளூர் எல்லை
        MISSING_TOWN,       // ஊரின் பெயர் விடுபட்டுள்ளது
        MISSING_UDISE,      // UDISE குறியீடு இல்லை அல்லது தவறானது
        DUPLICATE_SCHOOL,   // ஒரே பெயர் மீண்டும் வந்துள்ளது
        LONG_DISTANCE       // அதிக தூரம் (> 30 km)
    }

    data class AiAuditReport(
        val totalSchools: Int,
        val qualityScore: Int, // 0 - 100
        val isOverallValid: Boolean,
        val localRadiusCount: Int, // <= 8 km
        val outstationCount: Int,  // > 8 km
        val averageDistanceKm: Double,
        val averageBusFare: Double,
        val issues: List<ValidationIssue>,
        val aiNarrativeSummaryTa: String,
        val aiRecommendationsTa: List<String>,
        val isGeminiPowered: Boolean
    )

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * பள்ளிகள் பட்டியல் முழுவதையும் விரிவாக ஆய்வு செய்து AI தணிக்கை அறிக்கை உருவாக்குதல்
     */
    suspend fun auditSchools(
        schools: List<School>,
        apiKey: String = BuildConfig.GEMINI_API_KEY
    ): AiAuditReport = withContext(Dispatchers.IO) {
        if (schools.isEmpty()) {
            return@withContext AiAuditReport(
                totalSchools = 0,
                qualityScore = 0,
                isOverallValid = false,
                localRadiusCount = 0,
                outstationCount = 0,
                averageDistanceKm = 0.0,
                averageBusFare = 0.0,
                issues = emptyList(),
                aiNarrativeSummaryTa = "பள்ளிகள் பட்டியல் காலியாக உள்ளது. தயவுசெய்து CSV மூலம் பதிவேற்றவும் அல்லது சேர்க்கவும்.",
                aiRecommendationsTa = listOf("மாதிரி CSV கோப்பினை பதிவிறக்கி உங்கள் ஒன்றிய பள்ளிகளை பதிவேற்றவும்."),
                isGeminiPowered = false
            )
        }

        // 1. உள்ளூர் விதிகளின்படி துல்லியமான பகுப்பாய்வு (Rule-based Audit)
        val issues = mutableListOf<ValidationIssue>()
        var localCount = 0
        var outstationCount = 0
        var totalDist = 0
        var totalFare = 0

        val seenNames = mutableSetOf<String>()
        val seenUdise = mutableSetOf<String>()

        schools.forEach { school ->
            val town = school.getStationOrVillageName(true)
            val dist = school.distanceKm()
            val fare = school.defaultBusFare
            totalDist += dist
            totalFare += fare

            // 8 கி.மீ எல்லை விதி சரிபார்ப்பு (TN TA Rules HQ boundary)
            if (dist <= 8) {
                localCount++
                if (dist == 0 && fare > 0) {
                    issues.add(
                        ValidationIssue(
                            schoolId = school.id,
                            schoolName = school.nameTa.ifEmpty { school.nameEn },
                            townName = town,
                            distanceKm = dist,
                            busFare = fare,
                            issueType = IssueType.ZERO_DISTANCE_FARE,
                            messageTa = "தலைமையிட தூரம் 0 கி.மீ ஆனால் பேருந்து கட்டணம் ₹$fare என குறிப்பிடப்பட்டுள்ளது.",
                            messageEn = "Distance is 0 km but bus fare is ₹$fare.",
                            suggestion = "தலைமையிடத்தில் உள்ள அலுவலகத்திற்கு கட்டணம் ₹0 என மாற்றவும்."
                        )
                    )
                } else if (dist in 1..8) {
                    // தகவல் குறிப்பு (Informational: within 8 km HQ radius)
                    // In TN Govt TA rules, journeys <= 8 km from HQ are within local radius
                }
            } else {
                outstationCount++
            }

            // பேருந்து கட்டண பொருத்தம் சரிபார்ப்பு (Fare vs Distance consistency)
            val expectedFareRange = getExpectedFareRange(dist)
            if (fare == 0 && dist > 5) {
                issues.add(
                    ValidationIssue(
                        schoolId = school.id,
                        schoolName = school.nameTa.ifEmpty { school.nameEn },
                        townName = town,
                        distanceKm = dist,
                        busFare = fare,
                        issueType = IssueType.FARE_ANOMALY,
                        messageTa = "தூரம் $dist கி.மீ உள்ள பள்ளிக்கு பேருந்து கட்டணம் ₹0 என உள்ளது.",
                        messageEn = "Bus fare is ₹0 for $dist km.",
                        suggestion = "அரசு பேருந்து கட்டணப்படி குறைந்தபட்சம் ₹${expectedFareRange.first} என பதிவு செய்யவும்."
                    )
                )
            } else if (fare < expectedFareRange.first && dist >= 10) {
                issues.add(
                    ValidationIssue(
                        schoolId = school.id,
                        schoolName = school.nameTa.ifEmpty { school.nameEn },
                        townName = town,
                        distanceKm = dist,
                        busFare = fare,
                        issueType = IssueType.FARE_ANOMALY,
                        messageTa = "தூரம் $dist கி.மீ-க்கு கட்டணம் ₹$fare என்பது வழக்கமான கட்டணத்தை (₹${expectedFareRange.first}) விட குறைவாக உள்ளது.",
                        messageEn = "Fare ₹$fare is low for distance $dist km (Expected ~₹${expectedFareRange.first}-₹${expectedFareRange.second}).",
                        suggestion = "பேருந்து கட்டணத்தை ₹${expectedFareRange.first} அல்லது ₹${expectedFareRange.second} என மாற்றலாம்."
                    )
                )
            } else if (fare > expectedFareRange.second + 15) {
                issues.add(
                    ValidationIssue(
                        schoolId = school.id,
                        schoolName = school.nameTa.ifEmpty { school.nameEn },
                        townName = town,
                        distanceKm = dist,
                        busFare = fare,
                        issueType = IssueType.FARE_ANOMALY,
                        messageTa = "தூரம் $dist கி.மீ-க்கு கட்டணம் ₹$fare என்பது வழக்கமான அரசு கட்டணத்தை விட மிக அதிகமாக உள்ளது.",
                        messageEn = "Fare ₹$fare seems unusually high for $dist km.",
                        suggestion = "அரசு சாதாரண/விரைவு பேருந்து கட்டண விகிதத்தை சரிபார்க்கவும்."
                    )
                )
            }

            // ஊரின் பெயர் விடுபட்டுள்ளதா?
            if (school.villageTa.isBlank() && school.villageEn.isBlank()) {
                issues.add(
                    ValidationIssue(
                        schoolId = school.id,
                        schoolName = school.nameTa.ifEmpty { school.nameEn },
                        townName = "-",
                        distanceKm = dist,
                        busFare = fare,
                        issueType = IssueType.MISSING_TOWN,
                        messageTa = "ஊரின் பெயர் விடுபட்டுள்ளது (Town name missing). பயண பதிவில் ஊரின் பெயர் மட்டுமே வரும்.",
                        messageEn = "Town/Village name is missing.",
                        suggestion = "ஊரின் பெயரை குறிப்பிடவும் (எ.கா: ${School.extractVillageName(school.nameTa)})."
                    )
                )
            }

            // UDISE எண் சரிபார்ப்பு
            val code = school.code.trim()
            if (code.isNotEmpty() && (code.length != 11 || !code.all { it.isDigit() })) {
                issues.add(
                    ValidationIssue(
                        schoolId = school.id,
                        schoolName = school.nameTa.ifEmpty { school.nameEn },
                        townName = town,
                        distanceKm = dist,
                        busFare = fare,
                        issueType = IssueType.MISSING_UDISE,
                        messageTa = "UDISE குறியீடு '$code' வழக்கமான 11 இலக்க வடிவில் இல்லை.",
                        messageEn = "UDISE code should be 11 digits.",
                        suggestion = "சரியான 11 இலக்க UDISE எண்ணை பதிவிடவும்."
                    )
                )
            }

            // நகல் பள்ளிகள் சரிபார்ப்பு (Duplicate Detection)
            val normName = (school.nameTa.ifEmpty { school.nameEn }).trim().lowercase()
            if (normName.isNotEmpty()) {
                if (seenNames.contains(normName)) {
                    issues.add(
                        ValidationIssue(
                            schoolId = school.id,
                            schoolName = school.nameTa,
                            townName = town,
                            distanceKm = dist,
                            busFare = fare,
                            issueType = IssueType.DUPLICATE_SCHOOL,
                            messageTa = "இதே பள்ளி பெயர் மீண்டும் பதிவாகியுள்ளது (சாத்தியமான நகல் பதிவு).",
                            messageEn = "Possible duplicate school entry.",
                            suggestion = "பள்ளி பட்டியல் மற்றும் வரிசை எண்ணை சரிபார்க்கவும்."
                        )
                    )
                } else {
                    seenNames.add(normName)
                }
            }

            if (code.isNotEmpty()) {
                if (seenUdise.contains(code)) {
                    issues.add(
                        ValidationIssue(
                            schoolId = school.id,
                            schoolName = school.nameTa,
                            townName = town,
                            distanceKm = dist,
                            busFare = fare,
                            issueType = IssueType.DUPLICATE_SCHOOL,
                            messageTa = "UDISE எண் '$code' மற்றொரு பள்ளிக்கும் கொடுக்கப்பட்டுள்ளது.",
                            messageEn = "Duplicate UDISE code found.",
                            suggestion = "UDISE எண்ணை சரிபார்க்கவும்."
                        )
                    )
                } else {
                    seenUdise.add(code)
                }
            }
        }

        // தரம் மற்றும் மதிப்பீடு கணக்கீடு (Quality Score)
        val errorDeduction = issues.count { it.issueType != IssueType.HQ_LOCAL_RADIUS } * 3
        val qualityScore = (100 - errorDeduction).coerceIn(40, 100)
        val isOverallValid = qualityScore >= 80

        val avgDist = totalDist.toDouble() / schools.size
        val avgFare = totalFare.toDouble() / schools.size

        // 2. Gemini AI பகுப்பாய்வு (Direct REST API via gemini-3.5-flash)
        var geminiSummary: String? = null
        val recommendations = mutableListOf<String>()

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                geminiSummary = queryGeminiForAudit(schools, issues, localCount, outstationCount, apiKey)
            } catch (e: Exception) {
                // If Gemini network call fails, gracefully fallback to local AI report
                geminiSummary = null
            }
        }

        val finalSummary = geminiSummary ?: generateLocalAuditSummary(
            total = schools.size,
            score = qualityScore,
            localCount = localCount,
            outCount = outstationCount,
            avgDist = avgDist,
            avgFare = avgFare,
            issuesCount = issues.size
        )

        // இயல்புநிலை பரிந்துரைகள்
        if (localCount > 0) {
            recommendations.add("8 கி.மீக்குள் உள்ள $localCount பள்ளிகள் தலைமையிட உள்ளூர் எல்லையில் வருகின்றன. இப்பயணங்களுக்கு பேருந்து கட்டணம் கோரலாம்; ஆனால் தினசரி படி (DA) தணிக்கை விதிகளுக்கு உட்பட்டது.")
        }
        if (issues.any { it.issueType == IssueType.FARE_ANOMALY }) {
            recommendations.add("தூரம் மற்றும் பேருந்து கட்டணத்தில் கண்டறியப்பட்ட முரண்பாடுகளை அரசு கட்டண அட்டவணைக்கு ஏற்ப சமன்செய்யவும்.")
        }
        if (issues.any { it.issueType == IssueType.MISSING_TOWN }) {
            recommendations.add("பயணப் படிவங்களில் (Form 1 & Form 2) பள்ளியின் முழுப் பெயருக்குப் பதில் ஊரின் பெயர் மட்டுமே அச்சிடப்படும் என்பதால், விடுபட்ட ஊர் பெயர்களை உடனே பூர்த்தி செய்யவும்.")
        }
        recommendations.add("பிற மாவட்ட/ஒன்றிய BEO-க்கள் பள்ளி பட்டியலை CSV-ல் திருத்தி எளிதாக ஒரே கிளிக்கில் மீண்டும் பதிவேற்றலாம்.")

        AiAuditReport(
            totalSchools = schools.size,
            qualityScore = qualityScore,
            isOverallValid = isOverallValid,
            localRadiusCount = localCount,
            outstationCount = outstationCount,
            averageDistanceKm = avgDist,
            averageBusFare = avgFare,
            issues = issues,
            aiNarrativeSummaryTa = finalSummary,
            aiRecommendationsTa = recommendations,
            isGeminiPowered = (geminiSummary != null)
        )
    }

    private fun School.distanceKm(): Int = distanceFromHqKm

    /**
     * தமிழ்நாட்டில் அரசு சாதாரண/விரைவு பேருந்துகளின் வழக்கமான தூர-கட்டண வரம்பு
     */
    private fun getExpectedFareRange(distanceKm: Int): Pair<Int, Int> {
        return when {
            distanceKm <= 0 -> Pair(0, 0)
            distanceKm <= 5 -> Pair(5, 10)
            distanceKm <= 10 -> Pair(7, 12)
            distanceKm <= 15 -> Pair(10, 18)
            distanceKm <= 20 -> Pair(15, 22)
            distanceKm <= 25 -> Pair(18, 25)
            distanceKm <= 30 -> Pair(20, 30)
            else -> Pair((distanceKm * 0.8).toInt().coerceAtLeast(25), (distanceKm * 1.4).toInt())
        }
    }

    /**
     * Gemini 3.5 Flash REST API அழைப்பு
     */
    private fun queryGeminiForAudit(
        schools: List<School>,
        issues: List<ValidationIssue>,
        localCount: Int,
        outstationCount: Int,
        apiKey: String
    ): String {
        val sampleSchools = schools.take(20).joinToString("\n") { s ->
            "- ${s.nameTa} (${s.getStationOrVillageName(true)}): ${s.distanceFromHqKm} km, ₹${s.defaultBusFare}, ${s.category}"
        }

        val prompt = """
            நீங்கள் தமிழ்நாடு பள்ளி கல்வித்துறை வட்டாரக் கல்வி அலுவலர் (Block Educational Officer - BEO) பயணப்படி (Travelling Allowance) தணிக்கை நிபுணர் (Audit Specialist).
            கீழே உள்ள பள்ளி பட்டியல், தலைமையிட தூரம் மற்றும் பேருந்து கட்டணங்களை தமிழ்நாடு அரசு பயணப்படி விதிகளின்படி (TN TA Rules) ஆய்வு செய்து சுருக்கமான, தெளிவான தணிக்கை அறிக்கையை தமிழில் தருக:
            
            மொத்த பள்ளிகள்: ${schools.size}
            8 கி.மீ தலைமையிட எல்லைக்குள் உள்ள பள்ளிகள்: $localCount
            8 கி.மீக்கு அப்பால் உள்ள பள்ளிகள்: $outstationCount
            கண்டறியப்பட்ட முரண்பாடுகள்: ${issues.size}
            
            மாதிரி பள்ளிகள் விபரம்:
            $sampleSchools
            
            தயவுசெய்து பின்வரும் தலைப்புகளில் 4-5 வரிகளில் தெளிவான பதிலை தமிழில் தருக:
            1. ஒட்டுமொத்த மதிப்பீடு (Overall Quality)
            2. தலைமையிட தூரம் மற்றும் பேருந்து கட்டண பொருத்தம்
            3. 8 கி.மீ உள்ளூர் எல்லை மற்றும் பயணப்படி தகுதி
            4. BEO-க்களுக்கான வழிகாட்டுதல்
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            }
            put("contents", contents)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.4)
                put("topP", 0.9)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Gemini API Error: ${response.code} ${response.message}")
        }

        val responseString = response.body?.string() ?: ""
        val responseJson = JSONObject(responseString)
        val candidates = responseJson.optJSONArray("candidates")
        val candidate = candidates?.optJSONObject(0)
        val content = candidate?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val text = parts?.optJSONObject(0)?.optString("text")

        return if (!text.isNullOrBlank()) text.trim() else throw Exception("Empty Gemini Response")
    }

    /**
     * உள்ளூர் ஆஃப்லைன் AI சுருக்க அறிக்கை (Offline Local AI Summary)
     */
    private fun generateLocalAuditSummary(
        total: Int,
        score: Int,
        localCount: Int,
        outCount: Int,
        avgDist: Double,
        avgFare: Double,
        issuesCount: Int
    ): String {
        val statusText = when {
            score >= 90 -> "பள்ளிகள் பட்டியல் மிகச் சிறப்பாகவும், தூரம் மற்றும் பேருந்து கட்டணங்கள் தமிழ்நாடு அரசு பயணப்படி விதிகளுக்கு ஒத்ததாகவும் உள்ளது."
            score >= 75 -> "பள்ளிகள் பட்டியல் பெரும்பாலும் சரியாக உள்ளது. சில கட்டண மற்றும் ஊர் பெயர் முரண்பாடுகளை சரிசெய்வது தணிக்கை மறுப்புகளைத் தவிர்க்க உதவும்."
            else -> "பள்ளிகள் தரவில் சில கவனிக்கத்தக்க முரண்பாடுகள் உள்ளன. பயணப்படி கோருவதற்கு முன் தூரம் மற்றும் கட்டணங்களை சரிபார்க்கவும்."
        }

        return """
            📊 AI தணிக்கை பகுப்பாய்வு முடிவு:
            $statusText
            
            • மொத்த பள்ளிகள்: $total (சராசரி தூரம்: %.1f கி.மீ | சராசரி கட்டணம்: ₹%.1f)
            • 8 கி.மீக்கு அப்பால் (முழு பயணப்படிக்கு தகுதியானவை): $outCount பள்ளிகள்
            • 8 கி.மீ தலைமையிட எல்லைக்குள் (உள்ளூர் ஆய்வு): $localCount பள்ளிகள்
            • கவனிக்க வேண்டிய அம்சங்கள்: $issuesCount
            
            குறிப்பு: BEO பயணப்படி கணக்கீட்டில் 8 கி.மீக்கு அப்பால் உள்ள பள்ளிகளுக்கு முழு தினசரி படி (DA) மற்றும் பேருந்து கட்டணம் முறையாக அனுமதிக்கப்படும்.
        """.trimIndent().format(avgDist, avgFare)
    }
}
