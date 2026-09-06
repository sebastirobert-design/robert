package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppSettings
import com.example.data.model.OfficerProfile
import com.example.data.model.School
import com.example.data.model.TourEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface TaBillDao {

    // ==================== TOUR ENTRIES ====================
    @Query("SELECT * FROM tour_entries WHERE officerId = :officerId AND monthYear = :monthYear ORDER BY dayOfMonth ASC, orderIndex ASC, id ASC")
    fun getTourEntriesForMonth(officerId: Long, monthYear: String): Flow<List<TourEntry>>

    @Query("SELECT * FROM tour_entries ORDER BY id DESC")
    fun getAllTourEntries(): Flow<List<TourEntry>>

    @Query("SELECT DISTINCT monthYear FROM tour_entries WHERE officerId = :officerId ORDER BY monthYear DESC")
    fun getDistinctMonths(officerId: Long): Flow<List<String>>

    @Query("SELECT * FROM tour_entries WHERE id = :id LIMIT 1")
    suspend fun getTourEntryById(id: Long): TourEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTourEntry(entry: TourEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTourEntries(entries: List<TourEntry>): List<Long>

    @Update
    suspend fun updateTourEntry(entry: TourEntry)

    @Delete
    suspend fun deleteTourEntry(entry: TourEntry)

    @Query("DELETE FROM tour_entries WHERE id = :id")
    suspend fun deleteTourEntryById(id: Long)

    @Query("DELETE FROM tour_entries WHERE tripGroupId = :tripGroupId AND tripGroupId != ''")
    suspend fun deleteTourEntriesByTripGroup(tripGroupId: String)

    @Query("DELETE FROM tour_entries WHERE officerId = :officerId AND monthYear = :monthYear")
    suspend fun deleteTourEntriesForMonth(officerId: Long, monthYear: String)

    // ==================== SCHOOLS ====================
    @Query("SELECT * FROM schools ORDER BY serialNo ASC, nameEn ASC")
    fun getAllSchools(): Flow<List<School>>

    @Query("SELECT * FROM schools WHERE nameEn LIKE '%' || :query || '%' OR nameTa LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%' ORDER BY serialNo ASC")
    fun searchSchools(query: String): Flow<List<School>>

    @Query("SELECT * FROM schools WHERE id = :id LIMIT 1")
    suspend fun getSchoolById(id: Long): School?

    @Query("SELECT COUNT(*) FROM schools")
    suspend fun getSchoolCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchool(school: School): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchools(schools: List<School>)

    @Update
    suspend fun updateSchool(school: School)

    @Delete
    suspend fun deleteSchool(school: School)

    // ==================== OFFICERS ====================
    @Query("SELECT * FROM officer_profiles ORDER BY officerSlot ASC")
    fun getAllOfficers(): Flow<List<OfficerProfile>>

    @Query("SELECT * FROM officer_profiles WHERE id = :id LIMIT 1")
    suspend fun getOfficerById(id: Long): OfficerProfile?

    @Query("SELECT * FROM officer_profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveOfficer(): Flow<OfficerProfile?>

    @Query("SELECT COUNT(*) FROM officer_profiles")
    suspend fun getOfficerCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfficer(officer: OfficerProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfficers(officers: List<OfficerProfile>)

    @Update
    suspend fun updateOfficer(officer: OfficerProfile)

    @Query("UPDATE officer_profiles SET isActive = CASE WHEN id = :selectedId THEN 1 ELSE 0 END")
    suspend fun setActiveOfficer(selectedId: Long)

    // ==================== APP SETTINGS ====================
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<AppSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: AppSettings)
}
