package com.luxkasTech.PaintresLumiere.testing

import com.luxkasTech.PaintresLumiere.data.profile.ProfileRepository
import com.luxkasTech.PaintresLumiere.data.profile.UserProfile

class FakeProfileRepository(
    var result: Result<UserProfile> = Result.success(UserProfile(id = "1", name = "Lucas", email = "lucas@example.com")),
) : ProfileRepository {
    var calls = 0
    override suspend fun getProfile(): UserProfile {
        calls++
        return result.getOrThrow()
    }
}
