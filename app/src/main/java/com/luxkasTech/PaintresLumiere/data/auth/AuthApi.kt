package com.luxkasTech.PaintresLumiere.data.auth

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("login")
    suspend fun login(@Body body: LoginRequest): AccessTokenResponse
}
