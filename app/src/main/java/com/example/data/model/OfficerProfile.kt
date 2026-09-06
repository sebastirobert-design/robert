package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "officer_profiles")
data class OfficerProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val officerSlot: Int = 1, // 1, 2, 3
    val name: String = "S. PAUL DAVID ROSARIO",
    val designation: String = "Block Educational Officer (வட்டார கல்வி அலுவலர்)",
    val shortDesignation: String = "B.E.O.",
    val headquarters: String = "இளையான்குடி (Ilayankudi)",
    val district: String = "சிவகங்கை மாவட்டம் (Sivagangai Dt.)",
    val basicPay: Double = 100600.0,
    val isActive: Boolean = true
)
