package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AppSettings
import com.example.data.model.OfficerProfile
import com.example.data.model.School
import com.example.data.model.TourEntry
import com.example.util.DriveBackupHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TA Bill & Diary", appName)
  }

  @Test
  fun `test DriveBackupHelper serialization and parsing`() {
    val officers = listOf(
      OfficerProfile(
        id = 1L,
        officerSlot = 1,
        name = "வட்டாரக் கல்வி அலுவலர் 1",
        designation = "வட்டாரக் கல்வி அலுவலர்",
        basicPay = 100600.0
      )
    )

    val settings = AppSettings(
      id = 1,
      language = "ta",
      defaultDaRate = 300.0,
      defaultDaAmount = 210.0
    )

    val schools = listOf(
      School(
        id = 10L,
        serialNo = 1,
        nameTa = "ஊ.ஒ.தொ.பள்ளி, காரக்கோட்டை",
        villageTa = "காரக்கோட்டை",
        distanceFromHqKm = 10,
        defaultBusFare = 10
      )
    )

    val tours = listOf(
      TourEntry(
        id = 101L,
        officerId = 1L,
        monthYear = "2026-07",
        dayOfMonth = 3,
        departureStation = "தலைமையிடம்",
        arrivalStation = "காரக்கோட்டை",
        distanceKm = 10,
        busFare = 10.0,
        grandTotal = 250.0
      )
    )

    val jsonString = DriveBackupHelper.createBackupJson(
      officers = officers,
      settings = settings,
      schools = schools,
      tourEntries = tours
    )

    assertTrue("Backup JSON must contain app identifier", jsonString.contains("ILAYANKUDI_BEO_TA_BILL_APP"))
    assertTrue("Backup JSON must contain officer name", jsonString.contains("வட்டாரக் கல்வி அலுவலர் 1"))
    assertTrue("Backup JSON must contain school name", jsonString.contains("காரக்கோட்டை"))

    val parseResult = DriveBackupHelper.parseBackupJson(jsonString)
    assertTrue("Parsing backup must succeed", parseResult.isSuccess)

    val backupData = parseResult.getOrThrow()
    assertEquals(1, backupData.officerCount)
    assertEquals(1, backupData.schoolCount)
    assertEquals(1, backupData.tourCount)
    assertEquals("வட்டாரக் கல்வி அலுவலர் 1", backupData.officers[0].name)
    assertEquals("காரக்கோட்டை", backupData.schools[0].villageTa)
    assertEquals(10, backupData.tourEntries[0].distanceKm)
    assertEquals("ta", backupData.settings?.language)
  }
}
