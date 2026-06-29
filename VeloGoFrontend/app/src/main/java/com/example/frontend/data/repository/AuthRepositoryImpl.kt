package com.example.frontend.data.repository

import com.example.frontend.data.local.pref.SessionManager
import com.example.frontend.data.mapper.toDomain
import com.example.frontend.data.remote.api.AuthApi
import com.example.frontend.data.remote.dto.*
import com.example.frontend.domain.model.User
import com.example.frontend.domain.repository.AuthRepository
import com.example.frontend.ui.util.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val sessionManager: SessionManager
) : AuthRepository {

    private val gson = Gson()

    override fun signIn(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        try {
            val response = api.signIn(SignInRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val userResponse = response.body()!!
                val user = userResponse.toDomain()
                
                if (!user.isUser && !user.isAdmin) {
                    emit(Resource.Error("Access Denied: Only customer or admin accounts can sign in here"))
                    return@flow
                }
                
                val accessToken = userResponse.accessToken ?: ""
                val refreshToken = userResponse.refreshToken ?: ""
                val role = when {
                    user.isAdmin -> "admin"
                    user.isVendor -> "vendor"
                    else -> "user"
                }
                sessionManager.saveSession(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    userId = user.id,
                    username = user.username,
                    email = user.email,
                    role = role,
                    phoneNumber = userResponse.phoneNumber,
                    adress = userResponse.adress
                )
                emit(Resource.Success(user))
            } else {
                val errorMsg = parseError(response.errorBody()?.string())
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error", e))
        }
    }

    override fun signUp(username: String, email: String, password: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            val response = api.signUp(SignUpRequest(username, email, password))
            if (response.isSuccessful && response.body() != null) {
                val baseResponse = response.body()!!
                emit(Resource.Success(baseResponse.message ?: "Sign up successful"))
            } else {
                val errorMsg = parseError(response.errorBody()?.string())
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error", e))
        }
    }

    override fun signInWithGoogle(
        name: String,
        email: String,
        photo: String,
        isVendor: Boolean
    ): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        try {
            val request = GoogleAuthRequest(name, email, photo)
            val response = if (isVendor) {
                api.vendorSignInWithGoogle(request)
            } else {
                api.signInWithGoogle(request)
            }

            if (response.isSuccessful && response.body() != null) {
                val userResponse = response.body()!!
                val user = userResponse.toDomain()

                if (isVendor && !user.isVendor) {
                    emit(Resource.Error("Access Denied: Only vendor accounts can sign in here"))
                    return@flow
                }
                if (!isVendor && !user.isUser && !user.isAdmin) {
                    emit(Resource.Error("Access Denied: Only customer or admin accounts can sign in here"))
                    return@flow
                }

                val setCookies = response.headers().values("Set-Cookie")
                var extractedAccessToken = userResponse.accessToken
                if (extractedAccessToken.isNullOrEmpty()) {
                    for (cookie in setCookies) {
                        val token = extractCookieValue(cookie, "access_token")
                        if (!token.isNullOrEmpty()) {
                            extractedAccessToken = token
                            break
                        }
                    }
                }

                val accessToken = extractedAccessToken ?: ""
                val refreshToken = userResponse.refreshToken ?: ""
                val role = when {
                    isVendor -> "vendor"
                    user.isAdmin -> "admin"
                    else -> "user"
                }

                sessionManager.saveSession(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    userId = user.id,
                    username = user.username,
                    email = user.email,
                    role = role,
                    phoneNumber = userResponse.phoneNumber,
                    adress = userResponse.adress
                )
                emit(Resource.Success(user))
            } else {
                val errorMsg = parseError(response.errorBody()?.string())
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error", e))
        }
    }

    override fun signOut(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            sessionManager.clearSession()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Sign out error", e))
        }
    }

    override fun getSessionDetails(): Flow<User?> {
        return sessionManager.sessionDetailsFlow.map { session ->
            session?.let {
                User(
                    id = it.userId,
                    username = it.username,
                    email = it.email,
                    phoneNumber = it.phoneNumber,
                    adress = it.adress,
                    profilePicture = null,
                    isUser = it.role == "user",
                    isAdmin = it.role == "admin",
                    isVendor = it.role == "vendor",
                    createdAt = null,
                    updatedAt = null
                )
            }
        }
    }

    override fun vendorSignIn(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        try {
            val response = api.vendorSignIn(SignInRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val userResponse = response.body()!!
                val user = userResponse.toDomain()
                
                if (!user.isVendor) {
                    emit(Resource.Error("Access Denied: Only vendor accounts can sign in here"))
                    return@flow
                }
                
                // Save tokens and session
                val accessToken = userResponse.accessToken ?: ""
                val refreshToken = userResponse.refreshToken ?: ""
                sessionManager.saveSession(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    userId = user.id,
                    username = user.username,
                    email = user.email,
                    role = "vendor",
                    phoneNumber = userResponse.phoneNumber,
                    adress = userResponse.adress
                )
                emit(Resource.Success(user))
            } else {
                val errorMsg = parseError(response.errorBody()?.string())
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error", e))
        }
    }

    override fun vendorSignUp(username: String, email: String, password: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            val response = api.vendorSignUp(SignUpRequest(username, email, password))
            if (response.isSuccessful && response.body() != null) {
                val baseResponse = response.body()!!
                emit(Resource.Success(baseResponse.message ?: "Sign up successful"))
            } else {
                val errorMsg = parseError(response.errorBody()?.string())
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error", e))
        }
    }

    private fun parseError(errorJson: String?): String {
        if (errorJson.isNullOrEmpty()) return "An unknown error occurred"
        return try {
            val baseResponse = gson.fromJson(errorJson, BaseResponse::class.java)
            baseResponse.message ?: "An error occurred"
        } catch (e: Exception) {
            "An error occurred"
        }
    }

    private fun extractCookieValue(cookieHeader: String, cookieName: String): String? {
        val cookies = cookieHeader.split(";")
        for (cookie in cookies) {
            val trimmed = cookie.trim()
            if (trimmed.startsWith("$cookieName=", ignoreCase = true)) {
                return trimmed.substringAfter("=")
            }
        }
        return null
    }
}
