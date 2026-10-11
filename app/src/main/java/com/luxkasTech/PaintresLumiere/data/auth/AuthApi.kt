package com.luxkasTech.PaintresLumiere.data.auth

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

interface AuthApi {
    @POST("login")
    suspend fun login(@Body body: LoginRequest): AccessTokenResponse

    @POST("signup")
    suspend fun signUp(@Body body: SignUpRequest): AccessTokenResponse

    @POST("auth/google")
    suspend fun authenticateWithGoogle(@Body body: GoogleAuthRequest): AccessTokenResponse

    @POST("logout")
    suspend fun logout()

    @DELETE("users/me")
    suspend fun deleteAccount()
}
