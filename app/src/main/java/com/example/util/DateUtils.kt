package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    // தமிழ் மாதப் பெயர்கள் (Tamil Month Names)
    val monthNamesTamil = listOf(
        "ஜனவரி", "பிப்ரவரி", "மார்ச்", "ஏப்ரல்", "மே", "ஜூன்",
        "ஜூலை", "ஆகஸ்ட்", "செப்டம்பர்", "அக்டோபர்", "நவம்பர்", "டிசம்பர்"
    )

    // தமிழ் கிழமைப் பெயர்கள் (Tamil Day Names: 0=Sunday to 6=Saturday)
    val dayNamesTamil = listOf(
        "ஞாயிற்றுக்கிழமை", // 0: Sunday (Calendar.SUNDAY = 1)
        "திங்கட்கிழமை",    // 1: Monday (Calendar.MONDAY = 2)
        "செவ்வாய்க்கிழமை",  // 2: Tuesday (Calendar.TUESDAY = 3)
        "புதன்கிழமை",       // 3: Wednesday (Calendar.WEDNESDAY = 4)
        "வியாழக்கிழமை",     // 4: Thursday (Calendar.THURSDAY = 5)
        "வெள்ளிக்கிழமை",    // 5: Friday (Calendar.FRIDAY = 6)
        "சனிக்கிழமை"       // 6: Saturday (Calendar.SATURDAY = 7)
    )

    val dayNamesEnglish = listOf(
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )

    val monthNamesEnglish = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    fun getCurrentCalendar(): Calendar = Calendar.getInstance()

    fun getMonthYear(cal: Calendar): String {
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        return String.format(Locale.US, "%04d-%02d", year, month)
    }

    // இயல்பான நடப்புத் தேதி முறை (Natural current date mode: selectedDate = new Date())
    fun getCurrentMonthYear(): String {
        return getMonthYear(Calendar.getInstance())
    }

    // displayDate = ${day}.${month}.${year}
    fun formatDisplayDate(cal: Calendar): String {
        val day = String.format(Locale.US, "%02d", cal.get(Calendar.DAY_OF_MONTH))
        val month = String.format(Locale.US, "%02d", cal.get(Calendar.MONTH) + 1)
        val year = cal.get(Calendar.YEAR)
        return "$day.$month.$year"
    }

    // displayDay = dayNames[selectedDate.getDay()]
    fun getDayNameTamil(cal: Calendar): String {
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 (Sunday) to 7 (Saturday)
        val index = (dayOfWeek - 1).coerceIn(0, 6)
        return dayNamesTamil[index]
    }

    fun getDayNameEnglish(cal: Calendar): String {
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val index = (dayOfWeek - 1).coerceIn(0, 6)
        return dayNamesEnglish[index]
    }

    fun formatDate(year: Int, month: Int, day: Int): String {
        return String.format(Locale.US, "%02d.%02d.%04d", day, month, year)
    }

    fun parseDayOfMonth(dateStr: String): Int {
        return try {
            val parts = dateStr.split(".")
            if (parts.isNotEmpty()) parts[0].toInt() else 1
        } catch (e: Exception) {
            1
        }
    }

    fun parseYearMonth(monthYear: String): Pair<Int, Int> {
        return try {
            val parts = monthYear.split("-")
            val y = parts.getOrNull(0)?.toIntOrNull() ?: 2026
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 7
            Pair(y, m)
        } catch (e: Exception) {
            Pair(2026, 7)
        }
    }

    fun getTamilMonthDisplay(monthYear: String): String {
        return try {
            val parts = monthYear.split("-")
            val year = parts.getOrNull(0) ?: "2026"
            val month = parts.getOrNull(1)?.toIntOrNull() ?: 1
            val monthNameTa = if (month in 1..12) monthNamesTamil[month - 1] else "மாதம்"
            "$monthNameTa $year"
        } catch (e: Exception) {
            monthYear
        }
    }

    fun getEnglishMonthDisplay(monthYear: String): String {
        return try {
            val parts = monthYear.split("-")
            val year = parts.getOrNull(0) ?: "2026"
            val month = parts.getOrNull(1)?.toIntOrNull() ?: 1
            val monthNameEn = if (month in 1..12) monthNamesEnglish[month - 1] else "Month"
            "$monthNameEn $year"
        } catch (e: Exception) {
            monthYear
        }
    }

    fun getBillMonthHeader(monthYear: String): String {
        return try {
            val parts = monthYear.split("-")
            val year = parts.getOrNull(0)?.takeLast(2) ?: "26"
            val month = parts.getOrNull(1)?.toIntOrNull() ?: 1
            val monthShort = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec").getOrElse(month - 1) { "Jan" }
            "$monthShort-$year"
        } catch (e: Exception) {
            "Month"
        }
    }

    fun getDayOfWeekTamil(dateStr: String): String {
        return try {
            val date = dateFormatter.parse(dateStr) ?: return ""
            val cal = Calendar.getInstance().apply { time = date }
            getDayNameTamil(cal)
        } catch (e: Exception) {
            ""
        }
    }

    fun getDayOfWeekEnglish(dateStr: String): String {
        return try {
            val date = dateFormatter.parse(dateStr) ?: return ""
            val cal = Calendar.getInstance().apply { time = date }
            getDayNameEnglish(cal)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Strictly formats/normalizes time to "hh:mm a" format.
     * Ensures:
     * - Hour is strictly 2 digits: "01" to "12"
     * - Minute is strictly 2 digits: "00" to "59"
     * - AM/PM is strictly 2 characters: "AM" or "PM"
     * Examples:
     * "8:00 AM" -> "08:00 AM"
     * "4:10 pm" -> "04:10 PM"
     * "9:00" -> "09:00 AM"
     * "16:15" -> "04:15 PM"
     */
    fun formatStrictTime(raw: String, defaultTime: String = "09:00 AM"): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return defaultTime

        val regex = Regex("""^(\d{1,2})(?::(\d{1,2}))?\s*([aApP][mM])?$""")
        val match = regex.find(trimmed)
        if (match != null) {
            val (hStr, mStr, amPmStr) = match.destructured
            var h = hStr.toIntOrNull() ?: 9
            val m = mStr.toIntOrNull() ?: 0
            var amPm = amPmStr.uppercase(Locale.US)

            if (amPm.isEmpty()) {
                if (h in 12..23) {
                    amPm = "PM"
                    if (h > 12) h -= 12
                } else if (h == 0) {
                    h = 12
                    amPm = "AM"
                } else {
                    amPm = if (h >= 12) "PM" else "AM"
                }
            } else {
                if (h > 12) h = 12
                if (h < 1) h = 12
            }
            val validHour = h.coerceIn(1, 12)
            val validMin = m.coerceIn(0, 59)
            return String.format(Locale.US, "%02d:%02d %s", validHour, validMin, if (amPm == "PM") "PM" else "AM")
        }
        return defaultTime
    }

    /**
     * Parses time string into (hour12: 1..12, minute: 0..59, isPm: Boolean)
     */
    fun parseTimeComponents(timeStr: String): Triple<Int, Int, Boolean> {
        val regex = Regex("""^(\d{1,2}):(\d{2})\s*([aApP][mM])?$""")
        val match = regex.find(timeStr.trim())
        if (match != null) {
            val (hStr, mStr, amPm) = match.destructured
            val h = (hStr.toIntOrNull() ?: 8).coerceIn(1, 12)
            val m = (mStr.toIntOrNull() ?: 0).coerceIn(0, 59)
            val isPm = amPm.equals("PM", ignoreCase = true)
            return Triple(h, m, isPm)
        }
        return Triple(8, 0, false)
    }

    /**
     * Converts 12-hour components to 24-hour integer (0..23)
     */
    fun getHour24(h12: Int, isPm: Boolean): Int {
        val clampedH = h12.coerceIn(1, 12)
        return if (isPm) {
            if (clampedH == 12) 12 else clampedH + 12
        } else {
            if (clampedH == 12) 0 else clampedH
        }
    }
}

