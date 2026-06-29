package com.example.frontend.di

import com.example.frontend.data.repository.AuthRepositoryImpl
import com.example.frontend.domain.repository.AuthRepository
import com.example.frontend.data.repository.SearchRepositoryImpl
import com.example.frontend.domain.repository.SearchRepository
import com.example.frontend.data.repository.BookingRepositoryImpl
import com.example.frontend.domain.repository.BookingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        searchRepositoryImpl: SearchRepositoryImpl
    ): SearchRepository

    @Binds
    @Singleton
    abstract fun bindBookingRepository(
        bookingRepositoryImpl: BookingRepositoryImpl
    ): BookingRepository
}
