package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserAccountEntity::class,
        AppLimitEntity::class,
        PauseLogEntity::class,
        PlantEntity::class,
        WalletTransactionEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class PausaDatabase : RoomDatabase() {
    abstract fun pausaDao(): PausaDao

    companion object {
        @Volatile
        private var INSTANCE: PausaDatabase? = null

        fun getDatabase(context: Context): PausaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PausaDatabase::class.java,
                    "pausa_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
