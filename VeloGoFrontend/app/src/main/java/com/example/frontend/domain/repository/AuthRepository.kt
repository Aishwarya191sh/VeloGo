package com.example.frontend.domain.repository

import com.example.frontend.domain.model.User
import com.example.frontend.ui.util.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun signIn(email: String, password: String): Flow<Resource<User>>
    fun signUp(username: String, email: String, password: String): Flow<Resource<String>>
    fun signInWithGoogle(name: String, email: String, photo: String, isVendor: Boolean): Flow<Resource<User>>
    fun signOut(): Flow<Resource<Unit>>
    fun getSessionDetails(): Flow<User?>
    fun vendorSignIn(email: String, password: String): Flow<Resource<User>>
    fun vendorSignUp(username: String, email: String, password: String): Flow<Resource<String>>
}
