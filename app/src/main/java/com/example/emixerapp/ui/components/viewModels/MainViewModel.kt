package com.example.emixerapp.ui.components.viewModels

import androidx.lifecycle.ViewModel
import com.example.emixerapp.data.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Data class representing the UI state for the main screen.
 * Contains the currently selected user and the list of all users.
 */
data class MainUiState(
    var user: UserModel? = null,  // Usuário atualmente selecionado.
    val usersList: List<UserModel> = emptyList()  // Lista de todos os usuários.
)

/**
 * ViewModel para gerenciar o estado da UI da aplicação principal.
 * Usa MutableStateFlow para emitir mudanças no estado da UI de forma eficiente.
 */
class MainViewModel : ViewModel() {

    // Estado da UI, usando MutableStateFlow para emitir mudanças reativamente.
    private val _uiState = MutableStateFlow(MainUiState())
    // StateFlow imutável para acesso externo ao estado da UI.
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    /**
     * Adiciona um novo usuário à lista de usuários.
     * @param newUser O novo usuário a ser adicionado.
     */
    fun addUser(newUser: UserModel) {
        _uiState.update { currentState ->
            // Copia o estado atual e adiciona o novo usuário à lista.
            currentState.copy(usersList = currentState.usersList + newUser)
        }
    }

    /**
     * Define o usuário atual.
     * @param user O usuário a ser definido como atual.  Pode ser null.
     */
    fun setCurrentUser(user: UserModel?) {
        _uiState.value.user = user
    }

    /**
     * Atualiza um usuário existente na lista ou adiciona um novo usuário se o ID não existir.
     * @param user O usuário a ser atualizado ou adicionado. Pode ser null para remover um usuário.
     */
    fun updateUser(user: UserModel?) {
        _uiState.update { currentState ->
            val updatedList = currentState.usersList.toMutableList()
            val index = updatedList.indexOfFirst { it.id == user?.id }
            if (index != -1) {
                // Atualiza o usuário existente se o ID for encontrado.
                if (user != null) {
                    updatedList[index] = user
                } // Remove se user for null
            } else {
                // Adiciona o usuário se o ID não for encontrado.
                if (user != null) {
                    updatedList.add(user)
                }
            }
            // Copia o estado atual, atualizando a lista de usuários.
            currentState.copy(usersList = updatedList)
        }
    }



}
