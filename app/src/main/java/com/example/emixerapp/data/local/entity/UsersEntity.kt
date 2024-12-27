package com.example.emixerapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.emixerapp.data.model.UserModel
import java.util.UUID

@Entity
data class UsersEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "iconIndex")
    val iconIndex: Int,

    @ColumnInfo(name = "bass")
    val bass: Int,

    @ColumnInfo(name = "middle")
    val middle: Int,

    @ColumnInfo(name = "high")
    val high: Int,

    @ColumnInfo(name = "mainVolume")
    val mainVolume: Int, // Corrigido: Removida a duplicação do atributo

    @ColumnInfo(name = "pan")
    val pan: Int

)

// Extensão para converter para UserModel, se necessário
fun UsersEntity.toUserModel(): UserModel {
    return UserModel(
        id = id,
        name = name,
        iconIndex = iconIndex,
        bass = bass,
        middle = middle,
        high = high,
        mainVolume = mainVolume,
        pan = pan
    )
}
