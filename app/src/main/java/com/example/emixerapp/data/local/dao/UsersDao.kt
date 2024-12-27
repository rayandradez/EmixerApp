package com.example.emixerapp.data.local.dao

import android.util.Log
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.emixerapp.data.local.entity.UsersEntity
import kotlinx.coroutines.flow.Flow

// Define uma interface DAO (Data Access Object) para interagir com a tabela de usuários no banco de dados
@Dao
interface UsersDao {

    // Consulta para obter todos os usuários da tabela UsersEntity
    @Query("SELECT * FROM UsersEntity")
    fun getAllUsers(): Flow<List<UsersEntity>>

    // Insere um novo usuário na tabela; operação suspensa para ser chamada em um coroutine
    @Insert
    suspend fun insertUser(user: UsersEntity)

    // Atualiza um usuário existente na tabela; operação suspensa para ser chamada em um coroutine
    @Update
    suspend fun updateUser(user: UsersEntity)

    // Deleta um usuário da tabela; operação suspensa para ser chamada em um coroutine
    @Delete
    suspend fun deleteUser(user: UsersEntity)

}