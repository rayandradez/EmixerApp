package com.example.emixerapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.emixerapp.data.model.UserModel
import java.util.UUID

// Define uma entidade de banco de dados chamada UsersEntity, representando a tabela de usuários
@Entity
data class UsersEntity(

    // Declara o campo id como a chave primária da tabela
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    // Declara o campo name que armazena o nome do usuário
    @ColumnInfo(name = "name")
    val name: String,

    // Declara o campo iconIndex que armazena o índice do ícone do usuário
    @ColumnInfo(name = "iconIndex")
    val iconIndex: Int,

    // Declara o campo bass que armazena o nível de graves do usuário
    @ColumnInfo(name = "bass")
    val bass: Int,

    // Declara o campo middle que armazena o nível de médios do usuário
    @ColumnInfo(name = "middle")
    val middle: Int,

    // Declara o campo high que armazena o nível de agudos do usuário
    @ColumnInfo(name = "high")
    val high: Int,

    // Declara o campo mainVolume que armazena o volume principal do usuário
    @ColumnInfo(name = "mainVolume")
    val mainVolume: Int, // Corrigido: Removida a duplicação do atributo

    // Declara o campo pan que armazena o efeito de pan do usuário
    @ColumnInfo(name = "pan")
    val pan: Int

)

// Função de extensão para converter uma instância de UsersEntity em UserModel, se necessário
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
