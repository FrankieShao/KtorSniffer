package io.github.frankieshao.ktorsniffer

/**
 * Platform expect object for logging at different levels.
 * The actual implementation is provided in each platform module (e.g., Android, iOS).
 * This allows the core module to log messages in a platform-appropriate way.
 */
expect object Logger {
    fun e(error: String)
    fun w(warning: String)
    fun i(info: String)
    fun d(debug: String)
}

/**
 * The default log tag used for all KtorSniffer logs.
 */
const val TAG = "KtorSniffer"



