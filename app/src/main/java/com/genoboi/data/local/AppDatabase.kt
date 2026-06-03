package com.genoboi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.genoboi.data.local.dao.AnimalDao
import com.genoboi.data.local.dao.CicloCioDao
import com.genoboi.data.local.dao.EventoReprodutivoDao
import com.genoboi.data.local.entity.AnimalEntity
import com.genoboi.data.local.entity.CicloCioEntity
import com.genoboi.data.local.entity.EventoReprodutivoEntity

@Database(
    entities = [
        AnimalEntity::class,
        EventoReprodutivoEntity::class,
        CicloCioEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun animalDao(): AnimalDao
    abstract fun eventoDao(): EventoReprodutivoDao
    abstract fun cicloDao(): CicloCioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "genoboi.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
