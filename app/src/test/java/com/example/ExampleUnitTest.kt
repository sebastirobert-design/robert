package com.example

import com.example.data.model.TourEntry
import com.example.util.PrintExportHelper
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testForm1CsvGeneration_has9ColumnsAndUtf8Bom() {
    val travelTour = TourEntry(
        officerId = 1L,
        monthYear = "2026-07",
        orderIndex = 1,
        dayOfMonth = 2,
        departureStation = "தலைமையிடம்",
        departureDate = "02.07.2026",
        departureHour = "08:00 AM",
        arrivalStation = "வ.வண்டல்",
        arrivalDate = "02.07.2026",
        arrivalHour = "09:00 AM",
        purposeOfJourney = "பள்ளிபார்வை",
        kindOfJourney = "பேருந்து",
        distanceKm = 20,
        busFare = 15.0,
        daRate = 300.0,
        daAmount = 210.0,
        terminalCharge17a = 20.0,
        terminalCharge17b = 20.0,
        grandTotal = 265.0
    )

    val holidayTour = TourEntry(
        officerId = 1L,
        monthYear = "2026-07",
        orderIndex = 2,
        dayOfMonth = 5,
        isNonTravel = true,
        nonTravelType = "விடுமுறை",
        departureStation = "தலைமையிடம்",
        departureDate = "05.07.2026",
        arrivalStation = "தலைமையிடம்",
        arrivalDate = "05.07.2026",
        purposeOfJourney = "விடுமுறை",
        distanceKm = 0
    )

    val csv = PrintExportHelper.generateForm1Csv(listOf(travelTour, holidayTour))

    // 1. Must start with UTF-8 BOM (\uFEFF)
    assertTrue("CSV must start with UTF-8 BOM", csv.startsWith('\uFEFF'))

    val lines = csv.removePrefix("\uFEFF").split("\r\n").filter { it.isNotBlank() }
    // Header + Onward + Return for travel day + 1 for holiday = 4 rows
    assertEquals(4, lines.size)

    // Header has 9 columns
    val headerCols = lines[0].split(",")
    assertEquals(9, headerCols.size)
    assertTrue(headerCols[0].contains("1. Departure Station"))
    assertTrue(headerCols[8].contains("9. Distance (km)"))

    // Travel day return leg has empty purpose
    val returnRow = lines[2]
    assertTrue(returnRow.contains("\"04:10 PM\""))
    assertTrue(returnRow.contains("\"05:45 PM\""))

    // Holiday has no return leg
    val holidayRow = lines[3]
    assertTrue(holidayRow.contains("விடுமுறை"))
  }

  @Test
  fun testForm2CsvGeneration_has20ColumnsAndUtf8Bom() {
    val travelTour = TourEntry(
        officerId = 1L,
        monthYear = "2026-07",
        orderIndex = 1,
        dayOfMonth = 2,
        departureStation = "தலைமையிடம்",
        departureDate = "02.07.2026",
        departureHour = "08:00 AM",
        arrivalStation = "வ.வண்டல்",
        arrivalDate = "02.07.2026",
        arrivalHour = "09:00 AM",
        purposeOfJourney = "பள்ளிபார்வை",
        kindOfJourney = "பேருந்து",
        distanceKm = 20,
        busFare = 15.0,
        daRate = 300.0,
        daAmount = 210.0,
        terminalCharge17a = 20.0,
        terminalCharge17b = 20.0,
        grandTotal = 265.0
    )

    val returnTour = TourEntry(
        officerId = 1L,
        monthYear = "2026-07",
        orderIndex = 2,
        dayOfMonth = 2,
        departureStation = "வ.வண்டல்",
        departureDate = "02.07.2026",
        departureHour = "04:10 PM",
        arrivalStation = "தலைமையிடம்",
        arrivalDate = "02.07.2026",
        arrivalHour = "05:45 PM",
        kindOfJourney = "பேருந்து",
        distanceKm = 20,
        busFare = 15.0,
        terminalCharge17a = 20.0,
        terminalCharge17b = 20.0,
        grandTotal = 55.0,
        isReturnLeg = true
    )

    val csv = PrintExportHelper.generateForm2Csv(
        tourRecords = listOf(travelTour, returnTour),
        currentMonth = "July 2026"
    )

    // 1. Must start with UTF-8 BOM
    assertTrue("CSV must start with UTF-8 BOM", csv.startsWith('\uFEFF'))

    val rawLines = csv.removePrefix("\uFEFF").split("\r\n")
    // Top detail 1 + Top detail 2 + (Empty line) + Headers + Onward + Return
    assertTrue(rawLines.contains("")) // verifies empty line spacing

    val nonBlankLines = rawLines.filter { it.isNotBlank() }
    // 1. Office establishment line
    assertTrue(nonBlankLines[0].contains("Travelling Allowance Bill of the Establishment of"))
    assertTrue(nonBlankLines[0].contains("July 2026"))

    // 2. Officer & Basic Pay line
    assertTrue(nonBlankLines[1].contains("Travelling allowance Bill of S. PAUL DAVID ROSARIO, B.E.O., ILAYANKUDI, SIVAGANGAI DT."))
    assertTrue(nonBlankLines[1].contains("BASICPAY-Rs: 100600"))

    // 3. Table Headers (21 columns for Form 2 TA bill)
    val headerCols = nonBlankLines[2].split(",")
    assertEquals(21, headerCols.size)
    assertTrue(headerCols[0].contains("1. Dep. Station"))
    assertTrue(headerCols[6].contains("7. Kind of Journey"))
    assertTrue(headerCols[7].contains("8. Purpose of Journey"))
    assertTrue(headerCols[8].contains("9. No. of km"))
    assertTrue(headerCols[12].contains("13. Bus Fare Amount"))
    assertTrue(headerCols[13].contains("14. Road Distance"))
    assertTrue(headerCols[14].contains("15. DA Rate"))
    assertTrue(headerCols[15].contains("16. DA Amount"))
    assertTrue(headerCols[16].contains("17(a). Terminal Charges"))
    assertTrue(headerCols[17].contains("17(b). Terminal Charges"))
    assertTrue(headerCols[18].contains("18. Incidental"))
    assertTrue(headerCols[19].contains("19. TOTAL"))
    assertTrue(headerCols[20].contains("20. Remarks"))

    // Verify Onward row has mode at index 6 and purpose at index 7
    val onwardCols = nonBlankLines[3].split(",")
    assertEquals(21, onwardCols.size)
    assertTrue(onwardCols[6].contains("பேருந்து"))
    assertTrue(onwardCols[7].contains("பள்ளிபார்வை"))

    // 4. Return leg for travel tour has DA rate empty, DA amount 0, returnTotal = 15 + 20 + 20 = 55
    val returnRow = nonBlankLines[4]
    assertTrue(returnRow.contains("\"04:10 PM\""))
    assertTrue(returnRow.contains("\"05:45 PM\""))
    assertTrue(returnRow.contains("\"55\""))
  }

  @Test
  fun testTaEligibilityFilters() {
    val holidayTour = TourEntry(
      departureStation = "தலைமையிடம்",
      arrivalStation = "விடுமுறை",
      kindOfJourney = "பேருந்து",
      isNonTravel = true
    )
    assertFalse(holidayTour.isTaEligible)

    val clTour = TourEntry(
      departureStation = "தலைமையிடம்",
      arrivalStation = "தற்செயல் விடுப்பு",
      kindOfJourney = "பேருந்து",
      isNonTravel = true
    )
    assertFalse(clTour.isTaEligible)

    val officeTour = TourEntry(
      departureStation = "தலைமையிடம்",
      arrivalStation = "அலுவலகப் பணி",
      kindOfJourney = "பேருந்து",
      isNonTravel = true
    )
    assertFalse(officeTour.isTaEligible)

    val busTour = TourEntry(
      departureStation = "தலைமையிடம்",
      arrivalStation = "வ.வண்டல்",
      kindOfJourney = "பேருந்து",
      distanceKm = 20,
      busFare = 15.0
    )
    assertTrue(busTour.isTaEligible)

    val otherModeTour = TourEntry(
      departureStation = "தலைமையிடம்",
      arrivalStation = "வ.வண்டல்",
      kindOfJourney = "நடை",
      distanceKm = 5
    )
    assertFalse(otherModeTour.isTaEligible)
  }
}
