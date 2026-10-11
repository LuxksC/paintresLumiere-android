package com.luxkasTech.PaintresLumiere.data.profile

import com.luxkasTech.PaintresLumiere.core.network.apiCall
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Body of `PUT /profile` (backend card PL-90). A null field is omitted = "don't change"; an empty
 * string = "clear". Email, password, type and image are not editable here.
 */
@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val lastname: String? = null,
    val phone: String? = null,
    val cpf: String? = null,
    val cnpj: String? = null,
)

interface ProfileApi {
    @GET("profile")
    suspend fun getProfile(): UserProfile

    /** Not live yet (PL-90); returns the full updated profile. */
    @PUT("profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): UserProfile
}

/** The signed-in user's account data — iOS `ProfileService` (card #88). Throws `ApiError`. */
interface ProfileRepository {
    suspend fun getProfile(): UserProfile
    suspend fun updateProfile(request: UpdateProfileRequest): UserProfile
}

@Singleton
class DefaultProfileRepository @Inject constructor(
    private val api: ProfileApi,
) : ProfileRepository {
    override suspend fun getProfile(): UserProfile = apiCall { api.getProfile() }
    override suspend fun updateProfile(request: UpdateProfileRequest): UserProfile = apiCall { api.updateProfile(request) }
}
