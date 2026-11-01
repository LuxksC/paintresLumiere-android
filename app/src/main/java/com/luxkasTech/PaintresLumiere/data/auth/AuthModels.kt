package com.luxkasTech.PaintresLumiere.data.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class AccessTokenResponse(val accessToken: String)
