package com.example.moneymanager.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ExpenseEntry::class, BudgetEntry::class],
    version = 7
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao

    companion object {

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE budget_table ADD COLUMN periodStart INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE budget_table ADD COLUMN periodEnd INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // no change
            }
        }

        // 🔑 IMPORTANT: Multi-user support
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE expense_table ADD COLUMN userId TEXT NOT NULL DEFAULT ''"
                )
            }
        }
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE budget_table ADD COLUMN userId TEXT NOT NULL DEFAULT ''"
                )
            }
        }
    }
}

