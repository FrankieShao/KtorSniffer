package io.github.frankieshao.ktorsniffer

import io.github.frankieshao.ktorsniffer.persist.NetworkLogDao

/**
 * @author Frank
 * @created 5/8/25
 */
expect object KtorSniffer {
    /**
     * Returns the platform-specific NetworkLogDao instance.
     */
    fun getNetworkLogDao(): NetworkLogDao
}