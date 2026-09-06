package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tour_entries")
data class TourEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Grouping & Sorting
    val officerId: Long = 1L,
    val monthYear: String = "2026-07", // YYYY-MM format for easy querying
    val orderIndex: Int = 0,
    val dayOfMonth: Int = 1,
    
    // Entry Type
    val isNonTravel: Boolean = false,
    val nonTravelType: String = "", // "தற்செயல்விடுப்பு" (CL), "விடுமுறை" (Holiday), "அலுவலகப்பணி" (Office Duty), "கூட்டம்" (Meeting)
    
    // Columns 1, 2, 3 (DEPARTURE)
    val departureStation: String = "தலைமையிடம்",
    val departureDate: String = "01.07.2026",
    val departureHour: String = "08:00 AM",
    
    // Columns 4, 5, 6 (ARRIVAL)
    val arrivalStation: String = "",
    val arrivalDate: String = "01.07.2026",
    val arrivalHour: String = "09:00 AM",
    
    // Column 7: Purpose of Journey
    val purposeOfJourney: String = "பள்ளிபார்வை", // பள்ளிபார்வை, கலந்தாய்வு, வீரசிங்கம் கேஸ், மீட்டிங், பயிற்சி, etc.
    
    // Column 8: Kind of Journey
    val kindOfJourney: String = "பேருந்து", // பேருந்து (Bus), ரயில் (Train), சொந்த வாகனம் (Own Vehicle)
    
    // Column 9: No. of KM
    val distanceKm: Int = 0,
    
    // Columns 10, 11, 12: Railway / Steamer Fare
    val railClass: String = "",
    val railNoOfFares: String = "",
    val railAmount: Double = 0.0,
    
    // Column 13: Bus Fare
    val busFare: Double = 0.0,
    
    // Columns 14, 15: Distance travelled by road for which mileage is admissible
    val roadMileageOrdinary: String = "",
    val roadMileageSpecial: String = "",
    
    // Columns 16A, 16B, 16C: Daily Allowance
    val daDays: String = "", // e.g. "1" or ""
    val daRate: Double = 300.0,
    val daAmount: Double = 0.0, // e.g. 210.0
    
    // Columns 17a, 17b: Terminal Charges
    val terminalCharge17a: Double = 0.0, // e.g. 20.0
    val terminalCharge17b: Double = 0.0, // e.g. 20.0
    
    // Column 18: Incidental Charges
    val incidentalCharges: Double = 0.0,
    
    // Column 19: Grand Total
    val grandTotal: Double = 0.0,
    
    // Column 20: Remarks
    val remarks: String = "",
    
    // Linked Trip Support
    val tripGroupId: String = "", // Links outbound and return legs
    val isReturnLeg: Boolean = false
) {
    /**
     * Recomputes Grand Total accurately:
     * Bus Fare + Rail Amount + DA Amount + Terminal 17a + Terminal 17b + Incidental
     */
    fun calculatedGrandTotal(): Double {
        return busFare + railAmount + daAmount + terminalCharge17a + terminalCharge17b + incidentalCharges
    }
}
