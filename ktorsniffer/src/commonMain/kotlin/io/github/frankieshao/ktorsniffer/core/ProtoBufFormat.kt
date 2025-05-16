package io.github.frankieshao.ktorsniffer.core

import io.ktor.util.reflect.TypeInfo

/**
 * @author Frank
 * @created 5/16/25
 * Wrapper class for ProtoBuf serialization configuration.
 * Allows customization of how ProtoBuf bodies are formatted for logging.
 * @property bodyFormatter Function to convert a ProtoBuf body to a String for logging.
 */
class ProtoBuf(
    var bodyFormatter: ProtoBufBodyFormatter = { _, body -> body.toString() }
)

/**
 * Type alias for a function that formats a ProtoBuf body for logging.
 * @param typeInfo The type information of the body (may be null).
 * @param body The actual body object to format.
 * @return The formatted string representation of the body.
 */
typealias ProtoBufBodyFormatter = (typeInfo: TypeInfo?, body: Any) -> String