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
    val category: String = "PUPS", // PUPS, PUMS, AIDED_PRIMARY, AIDED_MIDDLE, OTHER
    val villageEn: String = "",
    val villageTa: String = "",
    val distanceFromHqKm: Int = 15,
    val defaultBusFare: Int = 15,
    val isFrequent: Boolean = false
)
