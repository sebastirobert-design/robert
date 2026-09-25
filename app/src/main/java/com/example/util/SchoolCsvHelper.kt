package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.data.model.School
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

/**
 * பிற மாவட்ட / பிற ஒன்றிய வட்டாரக் கல்வி அலுவலர்கள் தங்கள் கட்டுப்பாட்டில் உள்ள
 * பள்ளிகளை தலைமையிட தூரத்தோடு பேருந்து கட்டணத்தோடு CSV முறையில்
 * ஏற்றுமதி மற்றும் பதிவேற்றம் செய்ய உதவும் துணைக்கருவி (School CSV Helper).
 */
object SchoolCsvHelper {

    data class ParseSchoolResult(
        val schools: List<School>,
        val warnings: List<String>,
        val totalRows: Int,
        val successCount: Int
    )

    /**
     * மாதிரி CSV கோப்பு வார்ப்புரு (Sample CSV Template).
     * BEO-க்கள் தங்களின் பள்ளிகள், ஊர், தலைமையிட தூரம், பேருந்து கட்டணம் ஆகியவற்றை
     * எளிதாக நிரப்பி பதிவேற்ற உதவும்.
     */
    fun generateSampleSchoolCsvTemplate(): String {
        val rows = listOf(
            "S.No,பள்ளி பெயர் (தமிழ்),School Name (English),ஊரின் பெயர் (தமிழ்),Town/Village (English),தலைமையிட தூரம் (KM),பேருந்து கட்டணம் (Rs),பிரிவு (BEO_I/BEO_II/BEO_III/OTHER),UDISE Code",
            "1,ஊராட்சி ஒன்றிய தொடக்கப் பள்ளி ஆண்டிபட்டி,PUPS ANDIPATTI,ஆண்டிபட்டி,ANDIPATTI,6,10,BEO_I,33240100101",
            "2,ஊராட்சி ஒன்றிய நடுநிலைப் பள்ளி மயிலாடும்பாறை,PUMS MAYILADUMPARAI,மயிலாடும்பாறை,MAYILADUMPARAI,14,15,BEO_I,33240100201",
            "3,அரசு உயர் தொடக்கப் பள்ளி கண்டமனூர்,GOVT PS KANDAMANUR,கண்டமனூர்,KANDAMANUR,18,15,BEO_II,33240100301",
            "4,புனித சூசையப்பர் தொடக்கப் பள்ளி வருசநாடு,ST JOSEPH PS VARUSANADU,வருசநாடு,VARUSANADU,24,20,BEO_II,33240100401",
            "5,ஊராட்சி ஒன்றிய தொடக்கப் பள்ளி கடமலைக்குண்டு,PUPS KADAMALAIKUNDU,கடமலைக்குண்டு,KADAMALAIKUNDU,12,15,BEO_III,33240100501",
            "6,வட்டாரக் கல்வி அலுவலகம்,BEO OFFICE HEADQUARTERS,தலைமையிடம்,HEADQUARTERS,0,0,OTHER,33240100000"
        )
        return "\uFEFF" + rows.joinToString("\r\n")
    }

    /**
     * தற்போது செயலியில் உள்ள பள்ளிகளை CSV கோப்பாக உருவாக்குதல்
     */
    fun exportSchoolsToCsv(schools: List<School>): String {
        val rows = mutableListOf<String>()
        // தலைப்பு வரிசை (Header Row)
        rows.add(
            listOf(
                "S.No",
                "School Name (Tamil)",
                "School Name (English)",
                "Town/Village (Tamil)",
                "Town/Village (English)",
                "Distance from HQ (km)",
                "Default Bus Fare (Rs)",
                "BEO Section",
                "UDISE Code"
            ).joinToString(",") { escapeCsv(it) }
        )

        schools.sortedBy { it.serialNo }.forEachIndexed { index, school ->
            val sNo = if (school.serialNo > 0) school.serialNo else (index + 1)
            val row = listOf(
                sNo.toString(),
                school.nameTa,
                school.nameEn,
                school.villageTa.ifEmpty { school.getStationOrVillageName(true) },
                school.villageEn.ifEmpty { school.getStationOrVillageName(false) },
                school.distanceFromHqKm.toString(),
                school.defaultBusFare.toString(),
                school.category,
                school.code
            )
            rows.add(row.joinToString(",") { escapeCsv(it) })
        }

        return "\uFEFF" + rows.joinToString("\r\n")
    }

    /**
     * CSV கோப்பினை படித்து பள்ளிகள் பட்டியலாக மாற்றுதல் (Robust CSV Parser).
     * தமிழ் எழுத்துக்கள் மற்றும் பத்திகளை மிகத்துல்லியமாக பிரிக்கும்.
     */
    fun parseSchoolsFromCsv(csvText: String): ParseSchoolResult {
        val cleanText = csvText.removePrefix("\uFEFF").trim()
        if (cleanText.isBlank()) {
            return ParseSchoolResult(emptyList(), listOf("CSV கோப்பில் தரவு ஏதுமில்லை (Empty file)"), 0, 0)
        }

        val lines = splitCsvLines(cleanText)
        if (lines.isEmpty()) {
            return ParseSchoolResult(emptyList(), listOf("செல்லுபடியாகும் வரிகள் இல்லை"), 0, 0)
        }

        var headerIndex = -1
        var colMap = mutableMapOf<String, Int>()

        // தலைப்பு வரியை கண்டறிதல் (Detect Header Row)
        for (i in 0 until minOf(5, lines.size)) {
            val tokens = parseCsvLine(lines[i]).map { it.trim().lowercase() }
            val hasSchoolCol = tokens.any { it.contains("school") || it.contains("பள்ளி") || it.contains("name") || it.contains("பெயர்") }
            val hasDistCol = tokens.any { it.contains("dist") || it.contains("தூரம்") || it.contains("km") || it.contains("கி.மீ") }
            val hasFareCol = tokens.any { it.contains("fare") || it.contains("கட்டணம்") || it.contains("bus") || it.contains("பேருந்து") }

            if (hasSchoolCol || (hasDistCol && hasFareCol)) {
                headerIndex = i
                tokens.forEachIndexed { colIdx, token ->
                    when {
                        token.contains("s.no") || token.contains("வரிசை") || token == "no" || token == "id" -> colMap["sno"] = colIdx
                        token.contains("tamil") && (token.contains("name") || token.contains("பள்ளி")) -> colMap["name_ta"] = colIdx
                        token.contains("eng") && (token.contains("name") || token.contains("school")) -> colMap["name_en"] = colIdx
                        (token.contains("name") || token.contains("பள்ளி") || token.contains("பெயர்")) && !token.contains("ஊர்") && !token.contains("village") && !colMap.containsKey("name_ta") -> colMap["name_ta"] = colIdx
                        token.contains("tamil") && (token.contains("town") || token.contains("village") || token.contains("ஊர்")) -> colMap["village_ta"] = colIdx
                        token.contains("eng") && (token.contains("town") || token.contains("village")) -> colMap["village_en"] = colIdx
                        (token.contains("ஊர்") || token.contains("village") || token.contains("town") || token.contains("கிராமம்")) && !colMap.containsKey("village_ta") -> colMap["village_ta"] = colIdx
                        token.contains("dist") || token.contains("தூரம்") || token.contains("km") || token.contains("கி.மீ") -> colMap["dist"] = colIdx
                        token.contains("fare") || token.contains("கட்டணம்") || token.contains("ரூபாய்") || token.contains("bus") || token.contains("பேருந்து") -> colMap["fare"] = colIdx
                        token.contains("cat") || token.contains("பிரிவு") || token.contains("section") || token.contains("beo") -> colMap["cat"] = colIdx
                        token.contains("udise") || token.contains("குறியீடு") || token.contains("code") -> colMap["code"] = colIdx
                    }
                }
                break
            }
        }

        val startIndex = if (headerIndex >= 0) headerIndex + 1 else 0
        val schools = mutableListOf<School>()
        val warnings = mutableListOf<String>()
        var autoSno = 1

        for (lineIdx in startIndex until lines.size) {
            val rawLine = lines[lineIdx].trim()
            if (rawLine.isBlank()) continue

            val tokens = parseCsvLine(rawLine)
            if (tokens.isEmpty()) continue

            try {
                var sNo = autoSno
                var nameTa = ""
                var nameEn = ""
                var villageTa = ""
                var villageEn = ""
                var distKm = 15
                var busFare = 15
                var category = "BEO_I"
                var code = ""

                if (colMap.isNotEmpty()) {
                    // நெடுவரிசை வரைபடத்திலிருந்து எடுத்தல்
                    colMap["sno"]?.let { idx -> tokens.getOrNull(idx)?.toIntOrNull()?.let { sNo = it } }
                    colMap["name_ta"]?.let { idx -> nameTa = tokens.getOrNull(idx)?.trim() ?: "" }
                    colMap["name_en"]?.let { idx -> nameEn = tokens.getOrNull(idx)?.trim() ?: "" }
                    colMap["village_ta"]?.let { idx -> villageTa = tokens.getOrNull(idx)?.trim() ?: "" }
                    colMap["village_en"]?.let { idx -> villageEn = tokens.getOrNull(idx)?.trim() ?: "" }
                    colMap["dist"]?.let { idx ->
                        val rawDist = tokens.getOrNull(idx)?.filter { it.isDigit() || it == '.' } ?: ""
                        distKm = rawDist.toDoubleOrNull()?.toInt() ?: 15
                    }
                    colMap["fare"]?.let { idx ->
                        val rawFare = tokens.getOrNull(idx)?.filter { it.isDigit() || it == '.' } ?: ""
                        busFare = rawFare.toDoubleOrNull()?.toInt() ?: 15
                    }
                    colMap["cat"]?.let { idx ->
                        val rawCat = tokens.getOrNull(idx)?.trim()?.uppercase() ?: ""
                        category = when {
                            rawCat.contains("III") || rawCat.contains("3") -> "BEO_III"
                            rawCat.contains("II") || rawCat.contains("2") -> "BEO_II"
                            rawCat.contains("OFFICE") || rawCat.contains("அலுவலகம்") || rawCat.contains("OTHER") -> "OTHER"
                            else -> "BEO_I"
                        }
                    }
                    colMap["code"]?.let { idx -> code = tokens.getOrNull(idx)?.trim() ?: "" }
                } else {
                    // தலைப்பு இல்லாதபோது இடவரிசைப்படி பொருத்துதல் (Positional fallback)
                    // வழக்கமான வடிவம்: S.No, NameTa, NameEn, VillageTa, VillageEn, Dist, Fare, Category, UDISE
                    var curIdx = 0
                    if (tokens.getOrNull(0)?.toIntOrNull() != null && tokens.size > 3) {
                        sNo = tokens[0].toInt()
                        curIdx = 1
                    }
                    nameTa = tokens.getOrNull(curIdx)?.trim() ?: ""
                    nameEn = tokens.getOrNull(curIdx + 1)?.trim() ?: ""
                    villageTa = tokens.getOrNull(curIdx + 2)?.trim() ?: ""
                    villageEn = tokens.getOrNull(curIdx + 3)?.trim() ?: ""
                    val dStr = tokens.getOrNull(curIdx + 4)?.filter { it.isDigit() || it == '.' } ?: ""
                    distKm = dStr.toDoubleOrNull()?.toInt() ?: 15
                    val fStr = tokens.getOrNull(curIdx + 5)?.filter { it.isDigit() || it == '.' } ?: ""
                    busFare = fStr.toDoubleOrNull()?.toInt() ?: 15
                    category = tokens.getOrNull(curIdx + 6)?.trim() ?: "BEO_I"
                    code = tokens.getOrNull(curIdx + 7)?.trim() ?: ""
                }

                // பெயர்கள் சரிபார்த்தல் & சமன்செய்தல்
                if (nameTa.isBlank() && nameEn.isNotBlank()) nameTa = nameEn
                if (nameEn.isBlank() && nameTa.isNotBlank()) nameEn = nameTa

                if (nameTa.isBlank() && nameEn.isBlank()) {
                    warnings.add("வரிசை ${lineIdx + 1}: பள்ளியின் பெயர் இல்லை, தவிர்க்கப்பட்டது")
                    continue
                }

                // ஊர் பெயர் இல்லையெனில் பள்ளி பெயரிலிருந்து பிரித்தெடுத்தல்
                if (villageTa.isBlank()) {
                    villageTa = School.extractVillageName(nameTa)
                }
                if (villageEn.isBlank()) {
                    villageEn = School.extractVillageName(nameEn)
                }

                val school = School(
                    id = 0,
                    serialNo = sNo,
                    code = code,
                    nameEn = nameEn,
                    nameTa = nameTa,
                    category = category,
                    villageEn = villageEn,
                    villageTa = villageTa,
                    distanceFromHqKm = distKm.coerceAtLeast(0),
                    defaultBusFare = busFare.coerceAtLeast(0),
                    isFrequent = false
                )
                schools.add(school)
                autoSno++
            } catch (e: Exception) {
                warnings.add("வரிசை ${lineIdx + 1} செயலாக்குவதில் பிழை: ${e.message}")
            }
        }

        return ParseSchoolResult(
            schools = schools,
            warnings = warnings,
            totalRows = lines.size - (if (headerIndex >= 0) 1 else 0),
            successCount = schools.size
        )
    }

    /**
     * CSV கோப்பினை URI மூலம் படித்தல்
     */
    fun readCsvFromUri(context: Context, uri: Uri): String {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        return inputStream?.use { stream ->
            BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
                reader.readText()
            }
        } ?: ""
    }

    /**
     * CSV உள்ளடக்கத்தை கோப்பாக சேமித்து பகிர்தல்
     */
    fun shareCsvFile(context: Context, csvContent: String, fileName: String, title: String) {
        val contentWithBom = if (csvContent.startsWith('\uFEFF')) csvContent else "\uFEFF$csvContent"
        val exportDir = File(context.cacheDir, "schools_csv")
        if (!exportDir.exists()) exportDir.mkdirs()
        val file = File(exportDir, fileName)
        file.writeText(contentWithBom, Charsets.UTF_8)

        // Downloads அடைவிற்கும் நகலெடுக்க முயலுதல்
        try {
            val dlDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            if (dlDir != null) {
                if (!dlDir.exists()) dlDir.mkdirs()
                val dest = File(dlDir, fileName)
                file.copyTo(dest, overwrite = true)
            }
        } catch (_: Throwable) {}

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            putExtra(Intent.EXTRA_TEXT, "$title - $fileName")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(sendIntent, title)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    // Helper: Split text into CSV rows, taking into account multiline quotes
    private fun splitCsvLines(text: String): List<String> {
        val lines = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c == '\"') {
                inQuotes = !inQuotes
                sb.append(c)
            } else if ((c == '\n' || c == '\r') && !inQuotes) {
                if (c == '\r' && i + 1 < text.length && text[i + 1] == '\n') {
                    i++
                }
                if (sb.isNotBlank()) {
                    lines.add(sb.toString())
                }
                sb.clear()
            } else {
                sb.append(c)
            }
            i++
        }
        if (sb.isNotBlank()) {
            lines.add(sb.toString())
        }
        return lines
    }

    // Helper: Parse single CSV line into tokens
    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    sb.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim())
                sb.clear()
            } else {
                sb.append(c)
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    private fun escapeCsv(value: String): String {
        val trimmed = value.trim()
        val escaped = trimmed.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
