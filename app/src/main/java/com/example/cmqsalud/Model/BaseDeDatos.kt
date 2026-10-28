package com.example.cmqsalud.Model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Usuario::class, DocumentoEntity::class],
    version = 3, // Subimos a versión 3 por la nueva columna fotoBase64
    exportSchema = false
)
abstract class CmqDatabase : RoomDatabase() {
    abstract fun cmqDao(): CmqDao

    companion object {
        @Volatile
        private var INSTANCE: CmqDatabase? = null

        fun getDatabase(context: Context): CmqDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CmqDatabase::class.java,
                    "cmq_salud_local_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}