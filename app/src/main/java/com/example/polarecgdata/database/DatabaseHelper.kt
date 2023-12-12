package com.example.proctocam.Database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(
    entities = [DataModel::class, DataModelUpdateData::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ],
    exportSchema = true,

    )
abstract class DatabaseHelper : RoomDatabase() {
    abstract val dao: DaoAc?

    companion object {
        private var instance: DatabaseHelper? = null

        @Synchronized
        fun getInstance(context: Context?): DatabaseHelper? {
            if (instance == null) {
                instance = databaseBuilder(
                    context!!,
                    DatabaseHelper::class.java, "database"
                ).allowMainThreadQueries().build()
            }

            return instance
        }

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE content RENAME COLUMN remarks TO symptoms")
            }
        }
    }


}

