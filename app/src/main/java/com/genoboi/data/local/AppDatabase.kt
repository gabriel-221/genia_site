package com.genoboi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.genoboi.data.local.dao.AnimalDao
import com.genoboi.data.local.dao.CicloCioDao
import com.genoboi.data.local.dao.EventoReprodutivoDao
import com.genoboi.data.local.dao.ProdutorDao
import com.genoboi.data.local.entity.AnimalEntity
import com.genoboi.data.local.entity.CicloCioEntity
import com.genoboi.data.local.entity.EventoReprodutivoEntity
import com.genoboi.data.local.entity.ProdutorEntity

@Database(
    entities = [
        AnimalEntity::class,
        EventoReprodutivoEntity::class,
        CicloCioEntity::class,
        ProdutorEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun animalDao(): AnimalDao
    abstract fun eventoDao(): EventoReprodutivoDao
    abstract fun cicloDao(): CicloCioDao
    abstract fun produtorDao(): ProdutorDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS `produtores` (
                        `supabaseId` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `senhaHash` TEXT NOT NULL,
                        `nome` TEXT NOT NULL,
                        `nomeFazenda` TEXT NOT NULL DEFAULT '',
                        `municipio` TEXT NOT NULL DEFAULT '',
                        `estado` TEXT NOT NULL DEFAULT 'CE',
                        `cpf` TEXT NOT NULL DEFAULT '',
                        `telefone` TEXT NOT NULL DEFAULT '',
                        PRIMARY KEY(`supabaseId`)
                    )"""
                )
                database.execSQL("ALTER TABLE `animais` ADD COLUMN `produtorSupabaseId` TEXT")
                database.execSQL("ALTER TABLE `animais` ADD COLUMN `disponivelMatch` INTEGER NOT NULL DEFAULT 1")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `animais` ADD COLUMN `producaoLeiteDiaria` REAL NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "genoboi.db"
                )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                    // Cobre versões anteriores à 4 (ex: device com v3 instalada):
                    // apaga o banco local e recria — dados voltam pelo sync Supabase.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
