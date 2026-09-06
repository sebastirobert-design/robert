package com.example.util

object Localization {

    fun get(key: String, isTamil: Boolean): String {
        val ta = isTamil
        return when (key) {
            "app_title" -> if (ta) "பயணப்படி & நாள்காட்டி (TA Bill & Diary)" else "TA Bill & Tour Diary"
            "tab_home" -> if (ta) "முகப்பு" else "Home"
            "tab_diary" -> if (ta) "படிவம் 1: நாள்காட்டி" else "Form 1: Diary"
            "tab_ta_bill" -> if (ta) "படிவம் 2: பயணப்படி பட்டியல்" else "Form 2: TA Bill"
            "tab_schools" -> if (ta) "பள்ளிகள்" else "Schools"
            "tab_officer" -> if (ta) "அலுவலர்" else "Officer"
            "tab_settings" -> if (ta) "அமைப்புகள்" else "Settings"
            
            "quick_entry" -> if (ta) "+ புதிய பயணம் (Quick Tour)" else "+ New Tour"
            "quick_visit" -> if (ta) "பள்ளிப் பார்வை" else "School Visit"
            "quick_cl" -> if (ta) "தற்செயல் விடுப்பு (CL)" else "Casual Leave"
            "quick_holiday" -> if (ta) "விடுமுறை" else "Holiday"
            "quick_office" -> if (ta) "அலுவலகப்பணி" else "HQ Office Duty"
            
            "total_tours" -> if (ta) "பயணங்கள்" else "Total Tours"
            "total_km" -> if (ta) "மொத்த கி.மீ" else "Total KM"
            "total_bus_fare" -> if (ta) "பேருந்து கட்டணம்" else "Bus Fare"
            "total_da" -> if (ta) "தினப்படி (DA)" else "Total DA"
            "total_terminal" -> if (ta) "முனையக் கட்டணம்" else "Terminal Charges"
            "grand_total" -> if (ta) "மொத்த தொகை (Grand Total)" else "Grand Total"
            
            "select_date" -> if (ta) "தேதி தேர்வு" else "Select Date"
            "departure_station" -> if (ta) "புறப்படும் இடம்" else "Departure Station"
            "departure_time" -> if (ta) "புறப்படும் நேரம்" else "Departure Time"
            "destination" -> if (ta) "செல்லும் இடம் / பள்ளி" else "Destination School/Place"
            "arrival_time" -> if (ta) "சென்றடைந்த நேரம்" else "Arrival Time"
            "return_departure_time" -> if (ta) "திரும்ப புறப்படும் நேரம்" else "Return Departure Time"
            "return_arrival_time" -> if (ta) "தலைமையிடம் சேரும் நேரம்" else "Return Arrival Time"
            "purpose" -> if (ta) "பயணத்தின் நோக்கம்" else "Purpose of Journey"
            "mode" -> if (ta) "பயண வகை" else "Mode of Journey"
            "distance_km" -> if (ta) "தொலைவு (கி.மீ)" else "Distance (KM)"
            "bus_fare" -> if (ta) "பேருந்து கட்டணம் (₹)" else "Bus Fare (₹)"
            "da_rate" -> if (ta) "தினப்படி விகிதம் (DA Rate ₹)" else "DA Rate (₹)"
            "da_amount" -> if (ta) "தினப்படி தொகை (DA Amount ₹)" else "DA Amount (₹)"
            "terminal_charges" -> if (ta) "முனையக் கட்டணம் (17a + 17b ₹)" else "Terminal Charges (₹)"
            "return_journey_toggle" -> if (ta) "மறுபயணம் (Return Journey) தானாக சேர்க்க" else "Auto-create Return Journey"
            "save_tour" -> if (ta) "பயணத்தை சேமிக்க" else "Save Tour"
            "cancel" -> if (ta) "ரத்து செய்" else "Cancel"
            "delete" -> if (ta) "நீக்கு" else "Delete"
            "edit" -> if (ta) "திருத்து" else "Edit"
            "print_pdf" -> if (ta) "PDF உருவாக்கு / Print" else "Print / PDF"
            "share_whatsapp" -> if (ta) "WhatsApp பகிர்" else "Share WhatsApp"
            "search_schools" -> if (ta) "119 பள்ளிகளைத் தேடுக..." else "Search 119 schools..."
            "beo_title" -> if (ta) "வட்டார கல்வி அலுவலர்" else "Block Educational Officer"
            "ilayankudi_hq" -> if (ta) "இளையான்குடி, சிவகங்கை மாவட்டம்" else "Ilayankudi, Sivagangai Dt."
            
            else -> key
        }
    }
}
