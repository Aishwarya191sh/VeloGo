package com.example.frontend.data.remote.interceptor

import com.example.frontend.data.local.pref.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val (accessToken, refreshToken) = runBlocking {
            val access = sessionManager.accessTokenFlow.first()
            val refresh = sessionManager.refreshTokenFlow.first()
            access to refresh
        }

        val requestBuilder = originalRequest.newBuilder()
        if (accessToken != null && refreshToken != null && originalRequest.header("Authorization") == null) {
            requestBuilder.header("Authorization", "Bearer $refreshToken,$accessToken")
        }

        return chain.proceed(requestBuilder.build())
    }
}
