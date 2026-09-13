package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schools")
data class School(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serialNo: Int = 0,
    val code: String = "",
    val nameEn: String = "",
    val nameTa: String = "",
    val category: String = "BEO_I", // BEO_I, BEO_II, BEO_III, OTHER
    val villageEn: String = "",
    val villageTa: String = "",
    val distanceFromHqKm: Int = 15,
    val defaultBusFare: Int = 15,
    val isFrequent: Boolean = false
) {
    fun getStationOrVillageName(isTamil: Boolean = true): String {
        return if (isTamil) {
            if (villageTa.isNotBlank()) villageTa.trim() else extractVillageName(nameTa)
        } else {
            if (villageEn.isNotBlank()) villageEn.trim() else extractVillageName(nameEn)
        }
    }

    companion object {
        fun extractVillageName(rawName: String): String {
            if (rawName.isBlank()) return ""
            var clean = rawName.trim()

            // Handle office/parenthesized text e.g. "சிவகங்கை (டிஇஓ / கலெக்டரேட் / ஆய்வு)" -> "சிவகங்கை"
            if (clean.contains("(") && clean.contains(")")) {
                val beforeParen = clean.substringBefore("(").trim()
                if (beforeParen.isNotEmpty()) return beforeParen
            }

            // If has comma e.g. "ஹமீதியா தொடக்கப்பள்ளி, சாலையூர்" or "HAMEEDIA PRIMARY SCHOOL, SALAIYUR"
            if (clean.contains(",")) {
                val afterComma = clean.substringAfterLast(",").trim()
                if (afterComma.isNotEmpty()) return afterComma
            }

            // Remove common school prefixes in Tamil
            val prefixes = listOf(
                "ஊ.ஒ.தொ.பள்ளி ",
                "ஊ.ஒ.ந.பள்ளி ",
                "ஊராட்சி ஒன்றிய தொடக்கப் பள்ளி ",
                "ஊராட்சி ஒன்றிய நடுநிலைப் பள்ளி ",
                "ஊராட்சி ஒன்றிய நடுநிலைப் பள்ளிவடக்கு ",
                "ஊராட்சி ஒன்றிய தொடக்கப்பள்ளி ",
                "ஊராட்சி ஒன்றிய நடுநிலைப்பள்ளி ",
                "ஆர்சி தொடக்கப் பள்ளி ",
                "ஆர்.சி தொடக்கப்பள்ளி ",
                "ஆர்.சி நடுநிலைப்பள்ளி ",
                "ரோமன் கத்தோலிக்க நடுநிலைப் பள்ளி ",
                "ரோமன் கத்தோலிக்க தொடக்கப் பள்ளி ",
                "புனித அந்தோணியார் நடுநிலைப் பள்ளி ",
                "புனித அந்தோணியார் தொடக்கப் பள்ளி ",
                "புனித அந்தோனியார் தொடக்கப்பள்ளி ",
                "புனித அந்தோனியார் நடுநிலைப்பள்ளி ",
                "தூய மரியன்னை நடுநிலைப் பள்ளி ",
                "புனித மேரி நடுநிலைப்பள்ளி ",
                "புனித மேரி நடுநிலைப் பள்ளி ",
                "புனித மேரி தொடக்கப்பள்ளி ",
                "புனித ஜேம்ஸ் தொடக்கப் பள்ளி ",
                "புனித ஜேம்ஸ் தொடக்கப்பள்ளி ",
                "புனித சேவியர் தொடக்கப் பள்ளி ",
                "புனித சேவியர் தொடக்கப்பள்ளி ",
                "புனித ஜே நடுநிலைப் பள்ளி ",
                "புனித ஜே நடுநிலைப்பள்ளி ",
                "கிறிஸ்து மெமோரியல் தொடக்கப் பள்ளி ",
                "கிறிஸ்ட் தொடக்கப்பள்ளி ",
                "சிஎஸ்ஐ தொடக்கப் பள்ளி ",
                "சி.எஸ்.ஐ தொடக்கப்பள்ளி ",
                "பாத்திமா தொடக்கப் பள்ளி ",
                "பாத்திமா தொடக்கப்பள்ளி ",
                "பாக்கியம் தொடக்கப் பள்ளி ",
                "பாக்கியம் தொடக்கப்பள்ளி ",
                "அசோகா தொடக்கப் பள்ளி ",
                "அசோகா தொடக்கப்பள்ளி ",
                "முத்து விநாயகர் நடுநிலைப் பள்ளி ",
                "முத்து விநாயகர் நடுநிலைப்பள்ளி ",
                "திருவள்ளுவர் தொடக்கப் பள்ளி ",
                "திருவள்ளூர் தொடக்கப்பள்ளி ",
                "விஜய தாஸ் தொடக்கப் பள்ளி ",
                "விஜயதாஸ் தொடக்கப்பள்ளி ",
                "எஸ்எம்வேலம்மாள் தொடக்கப் பள்ளி ",
                "எஸ்.எம்.வேலம்மாள் தொடக்கப்பள்ளி ",
                "செங்கோல் வித்யாசாலா தொடக்கப் பள்ளி ",
                "செங்கோல் வித்யாசாலா தொடக்கப்பள்ளி ",
                "வீரமாமுனிவர் நடுநிலைப் பள்ளி ",
                "வீரமாமுனிவர் நடுநிலைப்பள்ளி ",
                "நூரியா தொடக்கப் பள்ளி ",
                "நூரியா தொடக்கப்பள்ளி ",
                "எஸ்என் தொடக்கப் பள்ளி ",
                "எஸ்.என் தொடக்கப்பள்ளி ",
                "ரஹ்மானியா தொடக்கப் பள்ளி ",
                "ரஹ்மானியா தொடக்கப்பள்ளி ",
                "மேலப்பள்ளிவாசல் தொடக்கப் பள்ளி ",
                "மேலப்பள்ளிவாசல் தொடக்கப்பள்ளி ",
                "MNAS நடுநிலைப் பள்ளி ",
                "எம்.என்.ஏ.எஸ் நடுநிலைப்பள்ளி ",
                "இலயோலா தொடக்கப் பள்ளி ",
                "லயோலா தொடக்கப்பள்ளி ",
                "ஹமீதியா தொடக்கப் பள்ளி ",
                "ஹமீதியா தொடக்கப்பள்ளி ",
                "தொடக்கப் பள்ளி ",
                "நடுநிலைப் பள்ளி ",
                "தொடக்கப்பள்ளி ",
                "நடுநிலைப்பள்ளி "
            )
            for (p in prefixes) {
                if (clean.startsWith(p)) {
                    clean = clean.removePrefix(p).trim()
                    break
                }
            }

            // Also check English prefixes
            val enPrefixes = listOf(
                "PUPS ", "PUMS ", "RCPS ", "RC PRIMARY SCHOOL ", "RC MID SCHOOL ",
                "ST. ANTONY'S MIDDLE SCHOOL ", "ST. ANTONY'S PRIMARY SCHOOL ",
                "ST. MARY MID.SCH ", "St.MARY'S MIDDLE SCHOOL ", "ST JAMES PRIMARY SCHOOL ",
                "St.XAVIER PRIMARY SCHOOL ", "ST.J.MID.SCHOOL ", "CHR.SOUTH INDIA PS ",
                "CHRIST M.PRI.SCHOOL ", "FATHIMA PRI SCHOOL ", "PACKIAM PRI SCHOOL ",
                "ASHOKA PRI SCHOOL ", "MUTHU VINAYAGAR MIDDLE SCHOOL ", "THIRUVALLUR PRIMARY SCHOOL ",
                "VIJAYA DOSS PRIMARY SCHOOL ", "S.M.VELAMMAL PS ", "SENGOL VIDYASALA PRIMARY SCHOOL ",
                "VEERAMAMUNIVAR MIDDLE SCHOOL ", "NOORIA.PRI.SCHOOL ", "S.N.PRI SCHOOL ",
                "RAHMANIA PRI SCHOOL ", "MELAPALLIVASAL PRI SCHOOL ", "M N A S MIDDLE SCHOOL ",
                "LOYOLA PRIMARY SCHOOL ", "HAMEEDIA PRIMARY SCHOOL "
            )
            for (p in enPrefixes) {
                if (clean.startsWith(p, ignoreCase = true)) {
                    clean = clean.substring(p.length).trim()
                    break
                }
            }

            return clean
        }
    }
}
