package com.example.emixerapp.data.model

import android.os.Parcel
import android.os.Parcelable
import java.util.UUID

/**
 * Classe de dados representando um usuário no aplicativo. Implementa Parcelable para passagem
 * eficiente de dados entre componentes Android.  Parcelable usa Parcel para serializar e
 * desserializar objetos, permitindo que eles sejam passados através de Intents e salvos no estado
 * do sistema, tornando a comunicação entre componentes mais eficiente.
 */
data class UserModel(
    val id: UUID = UUID.randomUUID(), // Identificador único para o usuário. Gerado automaticamente se não fornecido.
    var name: String = "",           // Nome do usuário (mutável).
    var iconIndex: Int = 0,          // Índice do ícone associado ao usuário (mutável).
    var bass: Int = 0,               // Nível de graves (mutável).
    var middle: Int = 0,             // Nível de médios (mutável).
    var high: Int = 0,              // Nível de agudos (mutável).
    var mainVolume: Int = 50,        // Nível de volume principal (mutável). Valor padrão 50.
    var pan: Int = 50               // Nível de panorâmica (mutável). Valor padrão 50.
) : Parcelable {

    /**
     * Construtor usado para criar um UserModel a partir de um Parcel. Necessário para a
     * implementação de Parcelable.
     */
    constructor(parcel: Parcel) : this(
        UUID.fromString(parcel.readString()), // Lê o ID do Parcel.
        parcel.readString() ?: "",     // Lê o nome do Parcel. Usa ?: "" para lidar com possíveis valores nulos.
        parcel.readInt(),                    // Lê o índice do ícone do Parcel.
        parcel.readInt(),                    // Lê o nível de graves do Parcel.
        parcel.readInt(),                    // Lê o nível de médios do Parcel.
        parcel.readInt(),                    // Lê o nível de agudos do Parcel.
        parcel.readInt(),                    // Lê o volume principal do Parcel.
        parcel.readInt()                     // Lê o nível de panorâmica do Parcel.
    )

    /**
     * Escreve o conteúdo deste UserModel em um Parcel. Necessário para a
     * implementação de Parcelable.
     */
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id.toString())     // Escreve o ID no Parcel.
        parcel.writeString(name)              // Escreve o nome no Parcel.
        parcel.writeInt(iconIndex)            // Escreve o índice do ícone no Parcel.
        parcel.writeInt(bass)                 // Escreve o nível de graves no Parcel.
        parcel.writeInt(middle)               // Escreve o nível de médios no Parcel.
        parcel.writeInt(high)                 // Escreve o nível de agudos no Parcel.
        parcel.writeInt(mainVolume)           // Escreve o volume principal no Parcel.
        parcel.writeInt(pan)                  // Escreve o nível de panorâmica no Parcel.
    }

    /**
     * Descreve o conteúdo deste UserModel. Necessário para a implementação de Parcelable.
     * Retorna 0 para simplificar.
     */
    override fun describeContents(): Int {
        return 0
    }

    /**
     * Objeto companion contendo o CREATOR, necessário para a implementação de Parcelable.
     * Parcelable permite a serialização de objetos para envio entre processos.  O CREATOR
     * permite que o sistema Android reconstrua instâncias de UserModel a partir de dados
     * serializados em um Parcel, por exemplo, quando uma Activity é recriada após uma
     * rotação de tela.
     */
    companion object CREATOR : Parcelable.Creator<UserModel> {
        override fun createFromParcel(parcel: Parcel): UserModel {
            return UserModel(parcel)
        }

        override fun newArray(size: Int): Array<UserModel?> {
            return arrayOfNulls(size)
        }
    }
}
