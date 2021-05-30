package com.climbing.yaho.di

import android.content.Context
import com.climbing.yaho.local.YahoPreference
import com.climbing.yaho.local.YahoPreferenceImpl
import com.climbing.yaho.local.cache.LiveClimbingCache
import com.climbing.yaho.local.cache.MountainListCache
import com.climbing.yaho.repository.*
import com.climbing.yaho.viewmodel.*
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
    viewModel { HomeViewModel(get()) }
    viewModel { ReadyViewModel(get(), get(), get()) }
    viewModel { ClimbingViewModel(get()) }
    viewModel { ClimbingDoneViewModel(get(), get(), get()) }
    viewModel { ClimbingDetailViewModel(get(), get(), get()) }
    viewModel { RecordListViewModel(get(), get()) }
}