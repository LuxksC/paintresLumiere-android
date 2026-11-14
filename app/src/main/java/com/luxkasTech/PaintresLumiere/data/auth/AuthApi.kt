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

    /** Backend card PL-96 (not live yet). Always 200, whether the email exists or not. */
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest)

    /** Backend card PL-97 (not live yet). Doesn't sign in — the user logs in with the new password. */
    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest)

    /** Proposed `PUT /profile/password` — no backend card yet; the `mock` flavor serves it. */
    @retrofit2.http.PUT("profile/password")
    suspend fun changePassword(@Body body: ChangePasswordRequest)

    @POST("logout")
    suspend fun logout()

    @DELETE("users/me")
    suspend fun deleteAccount()
}
