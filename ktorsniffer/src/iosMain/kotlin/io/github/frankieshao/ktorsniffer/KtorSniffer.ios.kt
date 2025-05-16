package io.github.frankieshao.ktorsniffer

import androidx.room.Room
import io.github.frankieshao.ktorsniffer.persist.DB_FILE_NAME
import io.github.frankieshao.ktorsniffer.persist.SnifferDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.frankieshao.ktorsniffer.persist.NetworkLogDao
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * @author Frank
 * @created 5/8/25
 */
actual object KtorSniffer {

    private val database: SnifferDatabase by lazy {
        val dbFilePath = "${documentDirectory()}/$DB_FILE_NAME"
        val room = Room.databaseBuilder<SnifferDatabase>(
            name = dbFilePath,
        ).setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
        room
    }

    actual fun getNetworkLogDao(): NetworkLogDao {
        return database.networkLogDao()
    }

}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory) { "Unable to get document directory in Ios" }.path!!
}
