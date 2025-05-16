package io.github.frankieshao.ktorsniffer

import platform.Foundation.NSLog

actual object Logger {
    actual fun e(error: String) {
        NSLog("[$TAG] ERROR: $error")
    }

    actual fun w(warning: String) {
        NSLog("[$TAG] WARNING: $warning")
    }

    actual fun i(info: String) {
        NSLog("[$TAG] INFO: $info")
    }

    actual fun d(debug: String) {
        NSLog("[$TAG] DEBUG: $debug")
    }

}

