package com.example.emixerapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.emixerapp.data.local.dao.UsersDao
import com.example.emixerapp.data.local.entity.UsersEntity


@Database(entities = [UsersEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        // Instância singleton do banco de dados
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Obtém a instância do banco de dados. Se já existir, retorna a existente;
         * caso contrário, cria uma nova.
         *
         * @param context O contexto da aplicação.
         * @return A instância do banco de dados.
         */
        fun getDatabase(context: Context): AppDatabase {
            // Verifica se a instância já existe
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database" // Nome do banco de dados
                )
                    .fallbackToDestructiveMigration() // Remove os dados antigos se a estrutura mudar
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // Método para acessar o DAO de usuários
    abstract fun usersDao(): UsersDao
}