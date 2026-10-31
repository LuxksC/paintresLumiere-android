package com.luxkasTech.PaintresLumiere.data.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class AccessTokenResponse(val accessToken: String)

@Serializable
data class SignUpRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val cpf: String? = null,
    val cnpj: String? = null,
)
