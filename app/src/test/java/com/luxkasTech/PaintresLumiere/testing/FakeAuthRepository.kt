package com.luxkasTech.PaintresLumiere.testing

import com.luxkasTech.PaintresLumiere.data.auth.AuthRepository
import kotlinx.coroutines.CompletableDeferred

/** Records calls and lets a test decide when and how each one completes. */
class FakeAuthRepository : AuthRepository {
    var loginCalls = mutableListOf<Pair<String, String>>()
    var loginResult: CompletableDeferred<Unit> = CompletableDeferred(Unit)

    override suspend fun login(email: String, password: String) {
        loginCalls += email to password
        loginResult.await()
    }
}
