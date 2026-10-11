package com.luxkasTech.PaintresLumiere.data.profile

import com.luxkasTech.PaintresLumiere.core.network.apiCall
import retrofit2.http.GET
import javax.inject.Inject
import javax.inject.Singleton

interface ProfileApi {
    @GET("profile")
    suspend fun getProfile(): UserProfile
}

/** The signed-in user's account data — iOS `ProfileService` (card #88). Throws `ApiError`. */
interface ProfileRepository {
    suspend fun getProfile(): UserProfile
}

@Singleton
class DefaultProfileRepository @Inject constructor(
    private val api: ProfileApi,
) : ProfileRepository {
    override suspend fun getProfile(): UserProfile = apiCall { api.getProfile() }
}
