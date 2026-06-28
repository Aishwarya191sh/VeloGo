package com.example.frontend.data.remote.api

import com.example.frontend.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/signin")
    suspend fun signIn(
        @Body request: SignInRequest
    ): Response<UserResponse>

    @POST("api/auth/signup")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): Response<BaseResponse<Any>>

    @POST("api/auth/google")
    suspend fun signInWithGoogle(
        @Body request: GoogleAuthRequest
    ): Response<UserResponse>

    @POST("api/vendor/vendorsignin")
    suspend fun vendorSignIn(
        @Body request: SignInRequest
    ): Response<UserResponse>

    @POST("api/vendor/vendorsignup")
    suspend fun vendorSignUp(
        @Body request: SignUpRequest
    ): Response<BaseResponse<Any>>

    @POST("api/vendor/vendorgoogle")
    suspend fun vendorSignInWithGoogle(
        @Body request: GoogleAuthRequest
    ): Response<UserResponse>

    @POST("api/auth/refreshToken")
    suspend fun refreshToken(): Response<TokenResponse>
}
