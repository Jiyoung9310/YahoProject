package com.android.yaho.di

import android.content.Context
import com.android.yaho.local.YahoPreference
import com.android.yaho.local.YahoPreferenceImpl
import com.android.yaho.local.cache.LiveClimbingCache
import com.android.yaho.local.cache.MountainListCache

import com.android.yaho.repository.*
import com.android.yaho.viewmodel.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

private const val YAHO_PREFERENCE = "yaho_preference"
@ExperimentalCoroutinesApi
val appModule = module {

    single<ContextDelegate> { ContextDelegateImpl(androidContext()) }
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single {
        androidApplication().getSharedPreferences(
            YAHO_PREFERENCE,
        Context.MODE_PRIVATE)
    }
//    single { YahoRoomDatabase.getInstance(androidApplication()) }
    single<YahoPreference> { YahoPreferenceImpl(get()) }
    single { MountainListCache() }
    single { LiveClimbingCache() }
//    single { ClimbingSaveHelper(get()) }

    factory <LoginRepository> { LoginRepositoryImpl(get(), get()) }
    factory <MountainRepository> { MountainRepositoryImpl(get()) }
    factory <UserDataRepository> { UserDataRepositoryImpl(get()) }
    factory <ClimbingRepository> { ClimbingRepositoryImpl(get()) }

    viewModel { LoginViewModel(get()) }
    viewModel { MainViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { ReadyViewModel(get(), get(), get()) }
    viewModel { ClimbingViewModel(get()) }
    viewModel { ClimbingDoneViewModel(get()) }
    viewModel { ClimbingDetailViewModel(get()) }
}