package com.luxkasTech.PaintresLumiere.core.network

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Decodes an API enum without ever throwing on a value the app doesn't know yet — it falls back to
 * [unknown] instead (iOS decision AD-005, `UnknownCaseDecodable`).
 *
 * Without this, one new category or color added on the backend fails the decode of the *whole*
 * product list and takes the catalog down for every product. **Every enum decoded from the API
 * must use this serializer**, including the cart, order and payment enums still to come.
 */
open class UnknownTolerantEnumSerializer<T : Enum<T>>(
    serialName: String,
    private val values: List<T>,
    private val unknown: T,
    private val rawValue: (T) -> String,
) : KSerializer<T> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName, PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): T {
        val raw = decoder.decodeString()
        return values.firstOrNull { it != unknown && rawValue(it) == raw } ?: unknown
    }

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeString(rawValue(value))
    }
}
