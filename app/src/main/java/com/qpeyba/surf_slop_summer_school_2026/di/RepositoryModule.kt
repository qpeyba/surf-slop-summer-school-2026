package com.qpeyba.surf_slop_summer_school_2026.di

import com.qpeyba.surf_slop_summer_school_2026.data.mapper.BookingMapper
import com.qpeyba.surf_slop_summer_school_2026.data.mapper.InstructorMapper
import com.qpeyba.surf_slop_summer_school_2026.data.mapper.ProfileMapper
import com.qpeyba.surf_slop_summer_school_2026.data.mapper.SlotMapper
import com.qpeyba.surf_slop_summer_school_2026.data.repository.AuthRepositoryImpl
import com.qpeyba.surf_slop_summer_school_2026.data.repository.BookingsRepositoryImpl
import com.qpeyba.surf_slop_summer_school_2026.data.repository.InstructorsRepositoryImpl
import com.qpeyba.surf_slop_summer_school_2026.data.repository.ProfileRepositoryImpl
import com.qpeyba.surf_slop_summer_school_2026.data.repository.SlotsRepositoryImpl
import com.qpeyba.surf_slop_summer_school_2026.domain.repository.AuthRepository
import com.qpeyba.surf_slop_summer_school_2026.domain.repository.BookingsRepository
import com.qpeyba.surf_slop_summer_school_2026.domain.repository.InstructorsRepository
import com.qpeyba.surf_slop_summer_school_2026.domain.repository.ProfileRepository
import com.qpeyba.surf_slop_summer_school_2026.domain.repository.SlotsRepository
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
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindSlotsRepository(impl: SlotsRepositoryImpl): SlotsRepository

    @Binds
    @Singleton
    abstract fun bindBookingsRepository(impl: BookingsRepositoryImpl): BookingsRepository

    @Binds
    @Singleton
    abstract fun bindInstructorsRepository(impl: InstructorsRepositoryImpl): InstructorsRepository
}
