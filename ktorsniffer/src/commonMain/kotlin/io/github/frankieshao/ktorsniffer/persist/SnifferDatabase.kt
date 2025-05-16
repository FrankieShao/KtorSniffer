package io.github.frankieshao.ktorsniffer.persist

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import io.github.frankieshao.ktorsniffer.model.MapListStringConverter
import io.github.frankieshao.ktorsniffer.model.NetworkLog

/**
 * Room database definition for storing network logs.
 *
 * - Uses NetworkLog as the only entity.
 * - Applies MapListStringConverter for serializing header maps.
 * - The database is constructed using platform-specific logic via expect/actual.
 */
@Database(entities = [NetworkLog::class], version = 2)
@TypeConverters(MapListStringConverter::class)
@ConstructedBy(SnifferDatabaseConstructor::class)
abstract class SnifferDatabase : RoomDatabase() {
    abstract fun networkLogDao(): NetworkLogDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object SnifferDatabaseConstructor : RoomDatabaseConstructor<SnifferDatabase> {
    override fun initialize(): SnifferDatabase
}

/**
 * The default file name for the sniffer database.
 */
internal const val DB_FILE_NAME = "sniffer.db"

