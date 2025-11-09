package com.example.auratrackr.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.auratrackr.data.local.dao.AppUsageDao
import com.example.auratrackr.data.local.dao.BlockedAppDao
import com.example.auratrackr.data.local.entity.AppUsageEntity
import com.example.auratrackr.data.local.entity.BlockedAppEntity
import java.time.LocalDate

/**
 * The main Room database class for the application.
 *
 * This class brings together all the entities (tables), DAOs (database queries),
 * and type converters required for the local data layer.
 *
 * @property version The version of the database. This must be incremented whenever the
 * schema (the structure of the tables) changes. A proper migration strategy should
 * be provided for each version bump in a production application.
 */
@Database(
    entities = [
        BlockedAppEntity::class,
        AppUsageEntity::class
    ],
    version = 2,
    // ✅ FIX: Changed to false to resolve the KSP warning. This is the simplest
    // and most common solution for apps that don't require schema history.
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AuraTrackrDatabase : RoomDatabase() {

    /**
     * Provides an abstract method to get the DAO for blocked app settings.
     * Room will generate the implementation for this.
     */
    abstract fun blockedAppDao(): BlockedAppDao

    /**
     * Provides an abstract method to get the DAO for daily app usage stats.
     * Room will generate the implementation for this.
     */
    abstract fun appUsageDao(): AppUsageDao
}

/**
 * A type converter for Room to handle the conversion between [LocalDate] and [String].
 * This allows Room to store `LocalDate` objects in the database as a simple string.
 */
class DateConverter {
    /**
     * Converts a [String] from the database into a [LocalDate] object.
     * @param dateString The date as a string (e.g., "2025-08-08").
     * @return A [LocalDate] object, or `null` if the input string is null.
     */
    @TypeConverter
    fun toDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }

    /**
     * Converts a [LocalDate] object into a [String] for database storage.
     * @param date The [LocalDate] object to convert.
     * @return The date formatted as a string, or `null` if the input date is null.
     */
    @TypeConverter
    fun fromDate(date: LocalDate?): String? {
        return date?.toString()
    }
}
