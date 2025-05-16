package io.github.frankieshao.ktorsniffer

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.frankieshao.ktorsniffer.persist.DB_FILE_NAME
import io.github.frankieshao.ktorsniffer.persist.NetworkLogDao
import io.github.frankieshao.ktorsniffer.persist.SnifferDatabase
import kotlinx.coroutines.Dispatchers

/**
 * @author Frank
 * @created 5/8/25
 */

/**
 * Android actual implementation of the KtorSniffer expect object.
 *
 * This object is responsible for initializing and providing access to the SnifferDatabase and its DAO on Android.
 *
 * IMPORTANT: You must call [init] from your Application or Activity before using [getNetworkLogDao],
 * otherwise an exception will be thrown.
 */
actual object KtorSniffer {

    private var database: SnifferDatabase? = null

    /**
     * Initializes the SnifferDatabase for Android.
     *
     * This should be called once, typically from your Application or Activity's onCreate().
     *
     * @param context The Android context (Application or Activity).
     */
    fun init(context: Context) {
        val dbFile = context.applicationContext.getDatabasePath(DB_FILE_NAME)
        database = Room
            .databaseBuilder<SnifferDatabase>(
                context = context.applicationContext,
                name = dbFile.absolutePath,
            ).setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    actual fun getNetworkLogDao(): NetworkLogDao {
        return requireNotNull(database) { "SnifferDatabase not initialized" }.networkLogDao()
    }

}