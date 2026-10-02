package com.hephaes200.historia.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Capitulo::class, Pregunta::class], version = 11, exportSchema = false)
abstract class HistoriaDatabase : RoomDatabase() {

    abstract fun historiaDao(): HistoriaDao

    companion object {
        @Volatile
        private var INSTANCE: HistoriaDatabase? = null

        fun getDatabase(context: Context): HistoriaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HistoriaDatabase::class.java,
                    "historia_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}