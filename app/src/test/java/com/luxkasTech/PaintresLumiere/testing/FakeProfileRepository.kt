package com.luxkasTech.PaintresLumiere.testing

import com.luxkasTech.PaintresLumiere.data.profile.ProfileRepository
import com.luxkasTech.PaintresLumiere.data.profile.UserProfile

class FakeProfileRepository(
    var result: Result<UserProfile> = Result.success(UserProfile(id = "1", name = "Lucas", email = "lucas@example.com")),
) : ProfileRepository {
    var calls = 0
    val updates = mutableListOf<com.luxkasTech.PaintresLumiere.data.profile.UpdateProfileRequest>()
    var updateResult: Result<UserProfile>? = null

    override suspend fun updateProfile(request: com.luxkasTech.PaintresLumiere.data.profile.UpdateProfileRequest): UserProfile {
        updates += request
        return (updateResult ?: result.map { it.copy(name = request.name ?: it.name, phone = request.phone ?: it.phone) }).getOrThrow()
    }
    override suspend fun getProfile(): UserProfile {
        calls++
        return result.getOrThrow()
    }
}
