package com.safecircle.app.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.safecircle.app.data.local.SafeCircleDatabase
import com.safecircle.app.data.local.dao.LocationDao
import com.safecircle.app.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SafeCircleDatabase =
        Room.databaseBuilder(context, SafeCircleDatabase::class.java, "safecircle.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideUserDao(db: SafeCircleDatabase): UserDao = db.userDao()

    @Provides
    fun provideLocationDao(db: SafeCircleDatabase): LocationDao = db.locationDao()
}
