package com.example.moneymanager

import android.app.Application
import androidx.room.Room
import com.example.moneymanager.data.AppDatabase

class MyApp : Application() {

    lateinit var db: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "money_manager_db"
        )
            // ✅ ALL required migrations registered
            .addMigrations(
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7
            )
            .build()
    }
}
