package com.example.moneymanager.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ExpenseEntry::class, BudgetEntry::class],
    version = 5
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

        // ✅ REQUIRED — EVEN IF EMPTY
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No schema change
            }
        }
    }
}

