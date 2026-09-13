package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.AppSettings
import com.example.data.model.OfficerProfile
import com.example.data.model.School
import com.example.data.model.TourEntry
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Google Drive Backup & Restore Helper
 * Converts entire Room Database (Tour Entries, Officers, Settings, Schools)
 * to a standardized JSON backup file and supports direct Google Drive save/restore via SAF and Share Sheet.
 */
object DriveBackupHelper {

    private const val BACKUP_VERSION = 1
    private const val APP_IDENTIFIER = "ILAYANKUDI_BEO_TA_BILL_APP"

    data class BackupData(
        val version: Int,
        val exportedAt: String,
        val backupNote: String,
        val tourCount: Int,
        val officerCount: Int,
        val schoolCount: Int,
        val officers: List<OfficerProfile>,
        val settings: AppSettings?,
        val schools: List<School>,
        val tourEntries: List<TourEntry>
    )

    /**
     * Serializes all app data to a clean JSON string
     */
    fun createBackupJson(
        officers: List<OfficerProfile>,
        settings: AppSettings?,
        schools: List<School>,
        tourEntries: List<TourEntry>
    ): String {
        val root = JSONObject()
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        root.put("app", APP_IDENTIFIER)
        root.put("version", BACKUP_VERSION)
        root.put("exportedAt", timestamp)
        root.put("description", "Ilayankudi BEO Tour Diary & TA Bill Database Full Backup")

        // Officers
        val officersArray = JSONArray()
        for (o in officers) {
            val obj = JSONObject()
            obj.put("id", o.id)
            obj.put("officerSlot", o.officerSlot)
            obj.put("name", o.name)
            obj.put("designation", o.designation)
            obj.put("shortDesignation", o.shortDesignation)
            obj.put("headquarters", o.headquarters)
            obj.put("district", o.district)
            obj.put("basicPay", o.basicPay)
            obj.put("isActive", o.isActive)
            officersArray.put(obj)
        }
        root.put("officers", officersArray)

        // Settings
        if (settings != null) {
            val setObj = JSONObject()
            setObj.put("id", settings.id)
            setObj.put("language", settings.language)
            setObj.put("defaultDaRate", settings.defaultDaRate)
            setObj.put("defaultDaAmount", settings.defaultDaAmount)
            setObj.put("defaultTerminal17a", settings.defaultTerminal17a)
            setObj.put("defaultTerminal17b", settings.defaultTerminal17b)
            setObj.put("defaultIncidental", settings.defaultIncidental)
            setObj.put("baseHeadquartersEn", settings.baseHeadquartersEn)
            setObj.put("baseHeadquartersTa", settings.baseHeadquartersTa)
            setObj.put("selectedOfficerId", settings.selectedOfficerId)
            root.put("settings", setObj)
        }

        // Schools (Custom/Updated directory)
        val schoolsArray = JSONArray()
        for (s in schools) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("serialNo", s.serialNo)
            obj.put("code", s.code)
            obj.put("nameEn", s.nameEn)
            obj.put("nameTa", s.nameTa)
            obj.put("category", s.category)
            obj.put("villageEn", s.villageEn)
            obj.put("villageTa", s.villageTa)
            obj.put("distanceFromHqKm", s.distanceFromHqKm)
            obj.put("defaultBusFare", s.defaultBusFare)
            obj.put("isFrequent", s.isFrequent)
            schoolsArray.put(obj)
        }
        root.put("schools", schoolsArray)

        // Tour Entries
        val toursArray = JSONArray()
        for (t in tourEntries) {
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("officerId", t.officerId)
            obj.put("monthYear", t.monthYear)
            obj.put("orderIndex", t.orderIndex)
            obj.put("dayOfMonth", t.dayOfMonth)
            obj.put("isNonTravel", t.isNonTravel)
            obj.put("nonTravelType", t.nonTravelType)
            obj.put("departureStation", t.departureStation)
            obj.put("departureDate", t.departureDate)
            obj.put("departureHour", t.departureHour)
            obj.put("arrivalStation", t.arrivalStation)
            obj.put("arrivalDate", t.arrivalDate)
            obj.put("arrivalHour", t.arrivalHour)
            obj.put("purposeOfJourney", t.purposeOfJourney)
            obj.put("kindOfJourney", t.kindOfJourney)
            obj.put("distanceKm", t.distanceKm)
            obj.put("railClass", t.railClass)
            obj.put("railNoOfFares", t.railNoOfFares)
            obj.put("railAmount", t.railAmount)
            obj.put("busFare", t.busFare)
            obj.put("roadMileageOrdinary", t.roadMileageOrdinary)
            obj.put("roadMileageSpecial", t.roadMileageSpecial)
            obj.put("daDays", t.daDays)
            obj.put("daRate", t.daRate)
            obj.put("daAmount", t.daAmount)
            obj.put("terminalCharge17a", t.terminalCharge17a)
            obj.put("terminalCharge17b", t.terminalCharge17b)
            obj.put("incidentalCharges", t.incidentalCharges)
            obj.put("grandTotal", t.grandTotal)
            obj.put("remarks", t.remarks)
            obj.put("tripGroupId", t.tripGroupId)
            obj.put("isReturnLeg", t.isReturnLeg)
            toursArray.put(obj)
        }
        root.put("tourEntries", toursArray)

        return root.toString(2)
    }

    /**
     * Parses and validates a JSON backup string
     */
    fun parseBackupJson(jsonString: String): Result<BackupData> {
        return runCatching {
            val root = JSONObject(jsonString)
            val app = root.optString("app", "")
            val version = root.optInt("version", 1)
            val exportedAt = root.optString("exportedAt", "Unknown Date")
            val note = root.optString("description", "")

            // Parse Officers
            val officers = mutableListOf<OfficerProfile>()
            val officersArray = root.optJSONArray("officers")
            if (officersArray != null) {
                for (i in 0 until officersArray.length()) {
                    val obj = officersArray.getJSONObject(i)
                    officers.add(
                        OfficerProfile(
                            id = obj.optLong("id", 0L),
                            officerSlot = obj.optInt("officerSlot", i + 1),
                            name = obj.optString("name", "Officer"),
                            designation = obj.optString("designation", "Block Educational Officer"),
                            shortDesignation = obj.optString("shortDesignation", "B.E.O."),
                            headquarters = obj.optString("headquarters", "இளையான்குடி (Ilayankudi)"),
                            district = obj.optString("district", "சிவகங்கை மாவட்டம்"),
                            basicPay = obj.optDouble("basicPay", 100600.0),
                            isActive = obj.optBoolean("isActive", i == 0)
                        )
                    )
                }
            }

            // Parse Settings
            var settings: AppSettings? = null
            val setObj = root.optJSONObject("settings")
            if (setObj != null) {
                settings = AppSettings(
                    id = setObj.optInt("id", 1),
                    language = setObj.optString("language", "ta"),
                    defaultDaRate = setObj.optDouble("defaultDaRate", 300.0),
                    defaultDaAmount = setObj.optDouble("defaultDaAmount", 210.0),
                    defaultTerminal17a = setObj.optDouble("defaultTerminal17a", 20.0),
                    defaultTerminal17b = setObj.optDouble("defaultTerminal17b", 20.0),
                    defaultIncidental = setObj.optDouble("defaultIncidental", 0.0),
                    baseHeadquartersEn = setObj.optString("baseHeadquartersEn", "Headquarters (Ilayankudi)"),
                    baseHeadquartersTa = setObj.optString("baseHeadquartersTa", "தலைமையிடம் (இளையான்குடி)"),
                    selectedOfficerId = setObj.optLong("selectedOfficerId", 1L)
                )
            }

            // Parse Schools
            val schools = mutableListOf<School>()
            val schoolsArray = root.optJSONArray("schools")
            if (schoolsArray != null) {
                for (i in 0 until schoolsArray.length()) {
                    val obj = schoolsArray.getJSONObject(i)
                    schools.add(
                        School(
                            id = obj.optLong("id", 0L),
                            serialNo = obj.optInt("serialNo", i + 1),
                            code = obj.optString("code", ""),
                            nameEn = obj.optString("nameEn", ""),
                            nameTa = obj.optString("nameTa", ""),
                            category = obj.optString("category", "BEO_I"),
                            villageEn = obj.optString("villageEn", ""),
                            villageTa = obj.optString("villageTa", ""),
                            distanceFromHqKm = obj.optInt("distanceFromHqKm", 15),
                            defaultBusFare = obj.optInt("defaultBusFare", 15),
                            isFrequent = obj.optBoolean("isFrequent", false)
                        )
                    )
                }
            }

            // Parse Tour Entries
            val tourEntries = mutableListOf<TourEntry>()
            val toursArray = root.optJSONArray("tourEntries")
            if (toursArray != null) {
                for (i in 0 until toursArray.length()) {
                    val obj = toursArray.getJSONObject(i)
                    tourEntries.add(
                        TourEntry(
                            id = obj.optLong("id", 0L),
                            officerId = obj.optLong("officerId", 1L),
                            monthYear = obj.optString("monthYear", "2026-07"),
                            orderIndex = obj.optInt("orderIndex", 0),
                            dayOfMonth = obj.optInt("dayOfMonth", 1),
                            isNonTravel = obj.optBoolean("isNonTravel", false),
                            nonTravelType = obj.optString("nonTravelType", ""),
                            departureStation = obj.optString("departureStation", "தலைமையிடம்"),
                            departureDate = obj.optString("departureDate", ""),
                            departureHour = obj.optString("departureHour", "09:00 AM"),
                            arrivalStation = obj.optString("arrivalStation", ""),
                            arrivalDate = obj.optString("arrivalDate", ""),
                            arrivalHour = obj.optString("arrivalHour", "09:30 AM"),
                            purposeOfJourney = obj.optString("purposeOfJourney", "பள்ளிபார்வை"),
                            kindOfJourney = obj.optString("kindOfJourney", "பேருந்து"),
                            distanceKm = obj.optInt("distanceKm", 0),
                            railClass = obj.optString("railClass", ""),
                            railNoOfFares = obj.optString("railNoOfFares", ""),
                            railAmount = obj.optDouble("railAmount", 0.0),
                            busFare = obj.optDouble("busFare", 0.0),
                            roadMileageOrdinary = obj.optString("roadMileageOrdinary", ""),
                            roadMileageSpecial = obj.optString("roadMileageSpecial", ""),
                            daDays = obj.optString("daDays", ""),
                            daRate = obj.optDouble("daRate", 300.0),
                            daAmount = obj.optDouble("daAmount", 0.0),
                            terminalCharge17a = obj.optDouble("terminalCharge17a", 0.0),
                            terminalCharge17b = obj.optDouble("terminalCharge17b", 0.0),
                            incidentalCharges = obj.optDouble("incidentalCharges", 0.0),
                            grandTotal = obj.optDouble("grandTotal", 0.0),
                            remarks = obj.optString("remarks", ""),
                            tripGroupId = obj.optString("tripGroupId", ""),
                            isReturnLeg = obj.optBoolean("isReturnLeg", false)
                        )
                    )
                }
            }

            BackupData(
                version = version,
                exportedAt = exportedAt,
                backupNote = note,
                tourCount = tourEntries.size,
                officerCount = officers.size,
                schoolCount = schools.size,
                officers = officers,
                settings = settings,
                schools = schools,
                tourEntries = tourEntries
            )
        }
    }

    /**
     * Writes backup JSON to a cache file ready for sharing to Google Drive
     */
    fun createBackupFile(context: Context, jsonString: String): File {
        val backupDir = File(context.cacheDir, "backups")
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        val fileDateFormat = SimpleDateFormat("yyyy_MM_dd_HHmm", Locale.getDefault()).format(Date())
        val fileName = "Ilayankudi_TABill_Backup_$fileDateFormat.json"
        val file = File(backupDir, fileName)
        file.writeText(jsonString, Charsets.UTF_8)
        return file
    }

    /**
     * Launches Android Share chooser targeting Google Drive / Files / Messaging
     */
    fun createShareToDriveIntent(context: Context, backupFile: File, isTamil: Boolean): Intent {
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, backupFile)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_SUBJECT,
                if (isTamil) "இளையான்குடி BEO பயணப்படி & நாள்காட்டி காப்புநகல் (Google Drive Backup)"
                else "Ilayankudi BEO TA Bill & Diary Backup (Google Drive)"
            )
            putExtra(
                Intent.EXTRA_TEXT,
                if (isTamil) "இளையான்குடி BEO செயலியின் அனைத்துப் பயணப் பதிவுகள் மற்றும் தரவுகளின் கூகிள் டிரைவ் காப்புநகல் கோப்பு."
                else "Google Drive Backup file containing all Tour Diary & TA Bill entries from Ilayankudi BEO App."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserTitle = if (isTamil) "கூகிள் டிரைவில் சேமி / காப்புநகல் பகிர்" else "Save to Google Drive / Share Backup"
        return Intent.createChooser(intent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Writes JSON directly to an SAF Document Uri (e.g. user selected Google Drive via document picker)
     */
    fun writeJsonToUri(context: Context, uri: Uri, jsonString: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Reads JSON content directly from an SAF Document Uri (e.g. user selected Google Drive via document picker)
     */
    fun readJsonFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader(Charsets.UTF_8).readText()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getSuggestedFileName(): String {
        val dateStr = SimpleDateFormat("yyyy_MM_dd", Locale.getDefault()).format(Date())
        return "Ilayankudi_TABill_Backup_$dateStr.json"
    }
}
