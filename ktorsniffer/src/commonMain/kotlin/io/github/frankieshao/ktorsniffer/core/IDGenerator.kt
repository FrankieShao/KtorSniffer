package io.github.frankieshao.ktorsniffer.core

import kotlinx.datetime.Clock
import kotlin.random.Random

/**
 * @author Frank
 * @created 5/15/25
 * Generates a unique ID for a NetworkLog entry.
 * The ID is composed of the current timestamp and a random 5-digit number.
 * This ensures uniqueness across logs generated in quick succession.
 */
fun generateId(): String {
    val timestamp = Clock.System.now().toEpochMilliseconds()
    val random = Random.nextInt(10000, 99999)
    return "$timestamp-$random"
}