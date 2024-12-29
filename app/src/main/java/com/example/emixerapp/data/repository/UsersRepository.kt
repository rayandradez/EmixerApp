package com.reaj.emixer.data.repository

import android.util.Log
import com.reaj.emixer.data.local.dao.UsersDao
import com.reaj.emixer.data.local.entity.UsersEntity
import kotlinx.coroutines.flow.Flow

class UsersRepository(private val usersDao: UsersDao) {

    // Retorna todos os usuários como um Flow<List<UsersEntity>>
    fun getAllUsers(): Flow<List<UsersEntity>> {
        return usersDao.getAllUsers() // Certifique-se de que o DAO também retorna o tipo correto
    }

    // Adicionar um novo usuário
    suspend fun addUser(user: UsersEntity) {
        Log.e("DATABASE", "IS CREATING NEW USER " )
        usersDao.insertUser(user)
    }

    // Atualizar um usuário
    suspend fun updateUser(user: UsersEntity) {
        usersDao.updateUser(user)
    }

    // Remover um usuário
    suspend fun deleteUser(user: UsersEntity) {
        usersDao.deleteUser(user)
    }
}
