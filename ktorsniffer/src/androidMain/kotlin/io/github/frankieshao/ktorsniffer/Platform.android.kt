package io.github.frankieshao.ktorsniffer

actual object Logger {

    actual fun e(error: String) {
        android.util.Log.e(TAG, error)
    }

    actual fun w(warning: String) {
        android.util.Log.w(TAG, warning)
    }

    actual fun i(info: String) {
        android.util.Log.i(TAG, info)
    }

    actual fun d(debug: String) {
        android.util.Log.d(TAG, debug)
    }

}


