package com.luxkasTech.PaintresLumiere.testing

import com.luxkasTech.PaintresLumiere.data.auth.AuthRepository
import kotlinx.coroutines.CompletableDeferred

/** Records calls and lets a test decide when and how each one completes. */
class FakeAuthRepository : AuthRepository {
    val loginCalls = mutableListOf<Pair<String, String>>()
    var loginResult: CompletableDeferred<Unit> = CompletableDeferred(Unit)

    data class SignUpCall(
        val name: String,
        val email: String,
        val password: String,
        val phone: String?,
        val cpf: String?,
        val cnpj: String?,
    )

    val signUpCalls = mutableListOf<SignUpCall>()
    var signUpResult: CompletableDeferred<Unit> = CompletableDeferred(Unit)

    var logoutCalls = 0
    var deleteCalls = 0
    var deleteResult: CompletableDeferred<Unit> = CompletableDeferred(Unit)

    override suspend fun logout() {
        logoutCalls++
    }

    override suspend fun deleteAccount() {
        deleteCalls++
        deleteResult.await()
    }

    override suspend fun login(email: String, password: String) {
        loginCalls += email to password
        loginResult.await()
    }

    override suspend fun signUp(name: String, email: String, password: String, phone: String?, cpf: String?, cnpj: String?) {
        signUpCalls += SignUpCall(name, email, password, phone, cpf, cnpj)
        signUpResult.await()
    }
}
