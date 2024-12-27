package com.example.emixerapp.ui.components.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emixerapp.data.local.entity.UsersEntity
import com.example.emixerapp.data.local.entity.toUserModel
import com.example.emixerapp.data.model.UserModel
import com.example.emixerapp.data.repository.UsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
class MainViewModel(private val usersRepository: UsersRepository) : ViewModel() {

    // Estado da UI, usando MutableStateFlow para emitir mudanças reativamente.
    private val _uiState = MutableStateFlow(MainUiState())

    // StateFlow imutável para acesso externo ao estado da UI.
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // Inicializa o estado com a lista de usuários do banco de dados
    init {
        usersRepository.getAllUsers().onEach { usersList: List<UsersEntity> ->
            _uiState.update { currentState ->
                currentState.copy(usersList = usersList.map { it.toUserModel() })
            }
        }.launchIn(viewModelScope)
    }

    // Adiciona um novo usuário
    fun addUserDB(user: UsersEntity) {
        viewModelScope.launch {
            usersRepository.addUser(user)
        }
    }

    // Atualiza um usuário existente
    fun updateUserDb(user: UsersEntity) {
        viewModelScope.launch {
            usersRepository.updateUser(user)
        }
    }

    // Remove um usuário
    fun deleteUser(user: UsersEntity) {
        viewModelScope.launch {
            usersRepository.deleteUser(user)
        }
    }

    // Define o usuário atual
    fun setCurrentUser(user: UsersEntity?) {
        _uiState.update { currentState ->
            currentState.copy(user = user?.toUserModel())
        }
    }

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
                    updateUserDb(
                        UsersEntity(
                            user.id,
                            user.name,
                            user.iconIndex,
                            user.bass,
                            user.middle,
                            user.high,
                            user.mainVolume,
                            user.pan
                        )
                    )
                } // Remove se user for null
            } else {
                // Adiciona o usuário se o ID não for encontrado.
                if (user != null) {
                    addUserDB(
                        UsersEntity(
                            user.id,
                            user.name,
                            user.iconIndex,
                            user.bass,
                            user.middle,
                            user.high,
                            user.mainVolume,
                            user.pan
                        )
                    )
                    updatedList.add(user)
                }
            }
            // Copia o estado atual, atualizando a lista de usuários.
            currentState.copy(usersList = updatedList)
        }
    }

    fun deleteUser(user: UserModel?) {
        _uiState.update { currentState ->
            val updatedList = currentState.usersList.toMutableList()
            val index = updatedList.indexOfFirst { it.id == user?.id }
            if (index != -1) {
                // Atualiza o usuário existente se o ID for encontrado.
                if (user != null) {
                    updatedList.remove(user)
                } // Remove se user for null
            }

            // Copia o estado atual, atualizando a lista de usuários.
            currentState.copy(usersList = updatedList)
        }
    }

}
