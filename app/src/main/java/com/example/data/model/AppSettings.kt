package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,
    val language: String = "ta", // "ta" (Tamil) or "en" (English)
    val defaultDaRate: Double = 300.0,
    val defaultDaAmount: Double = 210.0, // 70% of 300 = 210 for regular tour day
    val defaultTerminal17a: Double = 20.0,
    val defaultTerminal17b: Double = 20.0,
    val defaultIncidental: Double = 0.0,
    val baseHeadquartersEn: String = "Headquarters (Ilayankudi)",
    val baseHeadquartersTa: String = "தலைமையிடம் (இளையான்குடி)",
    val selectedOfficerId: Long = 1L
)
