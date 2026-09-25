package com.example

import com.example.data.model.School
import com.example.util.SchoolAiValidator
import com.example.util.SchoolCsvHelper
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SchoolCsvAndAiValidatorTest {

    @Test
    fun testGenerateSampleCsvTemplate_hasCorrectHeadersAndSampleRows() {
        val template = SchoolCsvHelper.generateSampleSchoolCsvTemplate()
        assertTrue(template.startsWith("\uFEFF"))
        assertTrue(template.contains("S.No"))
        assertTrue(template.contains("பள்ளி பெயர் (தமிழ்)"))
        assertTrue(template.contains("தலைமையிட தூரம் (KM)"))
        assertTrue(template.contains("பேருந்து கட்டணம் (Rs)"))
        assertTrue(template.contains("ஆண்டிபட்டி"))
    }

    @Test
    fun testExportSchoolsToCsv_correctlyOutputsAllFields() {
        val schools = listOf(
            School(serialNo = 1, code = "33240100101", nameTa = "பள்ளி 1", nameEn = "School 1", villageTa = "ஊர் 1", villageEn = "Village 1", distanceFromHqKm = 10, defaultBusFare = 10, category = "BEO_I"),
            School(serialNo = 2, code = "33240100201", nameTa = "பள்ளி 2", nameEn = "School 2", villageTa = "ஊர் 2", villageEn = "Village 2", distanceFromHqKm = 20, defaultBusFare = 15, category = "BEO_II")
        )
        val csv = SchoolCsvHelper.exportSchoolsToCsv(schools)
        assertTrue(csv.contains("School 1"))
        assertTrue(csv.contains("School 2"))
        assertTrue(csv.contains("10"))
        assertTrue(csv.contains("15"))
    }

    @Test
    fun testParseSchoolsFromCsv_parsesSuccessfully() {
        val sampleCsv = """
            S.No,பள்ளி பெயர் (தமிழ்),School Name (English),ஊரின் பெயர் (தமிழ்),Town/Village (English),தலைமையிட தூரம் (KM),பேருந்து கட்டணம் (Rs),பிரிவு,UDISE Code
            1,ஊராட்சி ஒன்றிய தொடக்கப் பள்ளி ஆண்டிபட்டி,PUPS ANDIPATTI,ஆண்டிபட்டி,ANDIPATTI,6,10,BEO_I,33240100101
            2,ஊராட்சி ஒன்றிய நடுநிலைப் பள்ளி மயிலாடும்பாறை,PUMS MAYILADUMPARAI,மயிலாடும்பாறை,MAYILADUMPARAI,14,15,BEO_II,33240100201
            3,வட்டாரக் கல்வி அலுவலகம்,BEO OFFICE HEADQUARTERS,தலைமையிடம்,HEADQUARTERS,0,0,OTHER,33240100000
        """.trimIndent()

        val result = SchoolCsvHelper.parseSchoolsFromCsv(sampleCsv)
        assertEquals(3, result.successCount)
        assertEquals(3, result.schools.size)

        val first = result.schools[0]
        assertEquals(1, first.serialNo)
        assertEquals("ஊராட்சி ஒன்றிய தொடக்கப் பள்ளி ஆண்டிபட்டி", first.nameTa)
        assertEquals("ஆண்டிபட்டி", first.villageTa)
        assertEquals(6, first.distanceFromHqKm)
        assertEquals(10, first.defaultBusFare)
        assertEquals("BEO_I", first.category)
        assertEquals("33240100101", first.code)

        val second = result.schools[1]
        assertEquals(2, second.serialNo)
        assertEquals(14, second.distanceFromHqKm)
        assertEquals(15, second.defaultBusFare)
        assertEquals("BEO_II", second.category)

        val third = result.schools[2]
        assertEquals(0, third.distanceFromHqKm)
        assertEquals(0, third.defaultBusFare)
        assertEquals("OTHER", third.category)
    }

    @Test
    fun testAiSchoolValidator_evaluatesCorrectly() = runBlocking {
        val testSchools = listOf(
            School(id = 1, serialNo = 1, nameTa = "பள்ளி 1", villageTa = "ஊர் 1", distanceFromHqKm = 5, defaultBusFare = 10, category = "BEO_I", code = "33240100101"),
            School(id = 2, serialNo = 2, nameTa = "பள்ளி 2", villageTa = "ஊர் 2", distanceFromHqKm = 15, defaultBusFare = 15, category = "BEO_I", code = "33240100201"),
            School(id = 3, serialNo = 3, nameTa = "பள்ளி 3", villageTa = "ஊர் 3", distanceFromHqKm = 25, defaultBusFare = 20, category = "BEO_II", code = "33240100301"),
            School(id = 4, serialNo = 4, nameTa = "தலைமையக அலுவலகம்", villageTa = "தலைமையிடம்", distanceFromHqKm = 0, defaultBusFare = 0, category = "OTHER", code = "33240100000")
        )

        val report = SchoolAiValidator.auditSchools(testSchools)
        assertNotNull(report)
        assertEquals(4, report.totalSchools)
        assertEquals(2, report.localRadiusCount) // 5 km and 0 km are <= 8 km
        assertEquals(2, report.outstationCount) // 15 km and 25 km are > 8 km
        assertTrue(report.qualityScore >= 80)
        assertTrue(report.isOverallValid)
        assertTrue(report.aiNarrativeSummaryTa.isNotEmpty())
        assertTrue(report.aiRecommendationsTa.isNotEmpty())
    }

    @Test
    fun testAiSchoolValidator_detectsFareAnomalies() = runBlocking {
        val schoolsWithAnomalies = listOf(
            // Distance 25 km but fare is 0 (anomaly)
            School(id = 1, serialNo = 1, nameTa = "பள்ளி 1", villageTa = "ஊர் 1", distanceFromHqKm = 25, defaultBusFare = 0, category = "BEO_I", code = "33240100101"),
            // Distance 0 km but fare is 50 (anomaly)
            School(id = 2, serialNo = 2, nameTa = "பள்ளி 2", villageTa = "ஊர் 2", distanceFromHqKm = 0, defaultBusFare = 50, category = "BEO_I", code = "33240100201"),
            // Missing village name (anomaly)
            School(id = 3, serialNo = 3, nameTa = "பள்ளி 3", villageTa = "", villageEn = "", distanceFromHqKm = 10, defaultBusFare = 10, category = "BEO_I", code = "33240100301")
        )

        val report = SchoolAiValidator.auditSchools(schoolsWithAnomalies)
        assertTrue(report.issues.isNotEmpty())
        val hasFareAnomaly = report.issues.any { it.issueType == SchoolAiValidator.IssueType.FARE_ANOMALY }
        val hasZeroDistAnomaly = report.issues.any { it.issueType == SchoolAiValidator.IssueType.ZERO_DISTANCE_FARE }
        val hasMissingTown = report.issues.any { it.issueType == SchoolAiValidator.IssueType.MISSING_TOWN }

        assertTrue("Should detect fare anomaly for 0 fare at 25km", hasFareAnomaly)
        assertTrue("Should detect zero distance with high fare", hasZeroDistAnomaly)
        assertTrue("Should detect missing town name", hasMissingTown)
    }
}
