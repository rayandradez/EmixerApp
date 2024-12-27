package com.example.emixerapp.ui.components.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.emixerapp.data.repository.UsersRepository

// Fábrica de ViewModel para criar instâncias de MainViewModel
class MainViewModelFactory(
    private val usersRepository: UsersRepository    // Repositório de usuários injetado na fábrica
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST") // Suprime o aviso de cast não seguro
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Verifica se a classe do ViewModel solicitado é do tipo MainViewModel
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            // Cria uma nova instância de MainViewModel passando o repositório de usuários
            return MainViewModel(usersRepository) as T
        }
        // Lança uma exceção caso a classe do ViewModel não seja reconhecida
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
