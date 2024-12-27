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

@Dao
interface UsersDao {
    @Query("SELECT * FROM UsersEntity")
    fun getAllUsers(): Flow<List<UsersEntity>>

    @Insert
    suspend fun insertUser(user: UsersEntity)

    @Update
    suspend fun updateUser(user: UsersEntity)

    @Delete
    suspend fun deleteUser(user: UsersEntity)

}