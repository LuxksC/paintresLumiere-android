package com.luxkasTech.PaintresLumiere.data.profile

import com.luxkasTech.PaintresLumiere.core.network.UnknownTolerantEnumSerializer
import kotlinx.serialization.Serializable

/** Account type from `GET /profile`. Unknown values never grant admin access. */
@Serializable(with = UserType.Serializer::class)
enum class UserType(val raw: String) {
    ADMIN("admin"),
    CLIENT("client"),
    UNKNOWN("");

    object Serializer : UnknownTolerantEnumSerializer<UserType>("UserType", entries, UNKNOWN, { it.raw })
}

@Serializable
data class UserProfile(
    val id: String,
    val type: UserType = UserType.UNKNOWN,
    val name: String,
    val lastname: String? = null,
    val phone: String? = null,
    val email: String,
    val cpf: String? = null,
    val cnpj: String? = null,
    val image: String? = null,
) {
    val displayName: String get() = listOfNotNull(name, lastname?.takeIf { it.isNotBlank() }).joinToString(" ")
}
