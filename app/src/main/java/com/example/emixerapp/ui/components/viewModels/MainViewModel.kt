package com.reaj.emixer.ui.components.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reaj.emixer.data.local.entity.UsersEntity
import com.reaj.emixer.data.local.entity.toUserModel
import com.reaj.emixer.data.model.UserModel
import com.reaj.emixer.data.repository.UsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val user: UserModel? = null,  // Usuário atualmente selecionado. Declarado como val.
    val usersList: List<UserModel> = emptyList()  // Lista de todos os usuários.
)

class MainViewModel(private val usersRepository: UsersRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    var showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    init {
        usersRepository.getAllUsers().onEach { usersList: List<UsersEntity> ->
            _uiState.update { currentState ->
                // Mapeia UsersEntity para UserModel e atualiza a lista
                val userModels = usersList.map { it.toUserModel() }
                // Garante que o usuário selecionado (se houver) ainda esteja na nova lista de usuários
                val currentUser = currentState.user?.let { current -> userModels.find { it.id == current.id } ?: userModels.firstOrNull() }
                    ?: userModels.firstOrNull() // Se não havia um usuário selecionado, pega o primeiro da lista
                currentState.copy(usersList = userModels, user = currentUser)
            }
        }.launchIn(viewModelScope)
        _showSettings.value = false
    }

    fun addUserDB(user: UsersEntity) {
        viewModelScope.launch {
            usersRepository.addUser(user)
        }
    }

    fun updateUserDb(user: UsersEntity) {
        viewModelScope.launch {
            usersRepository.updateUser(user)
        }
    }

    fun setShowSettings() {
        _showSettings.update { !it }
    }

    /**
     * Define o usuário atual no estado da UI.
     * @param user O usuário a ser definido como atual. Pode ser null.
     */
    fun setCurrentUser(user: UserModel?) {
        _uiState.update { currentState ->
            currentState.copy(user = user)
        }
    }

    /**
     * Atualiza um usuário existente na lista ou adiciona um novo usuário se o ID não existir.
     * Isso também atualiza o usuário selecionado se ele for o que está sendo modificado.
     * @param user O usuário a ser atualizado ou adicionado.
     */
    fun updateUser(user: UserModel?) {
        if (user == null) {
            Log.w("MainViewModel", "updateUser called with null user. No action taken.")
            return
        }

        viewModelScope.launch {
            val existingUser = _uiState.value.usersList.find { it.id == user.id }

            if (existingUser != null) {
                // Atualiza o usuário existente
                usersRepository.updateUser(user.toUsersEntity())
            } else {
                // Adiciona um novo usuário
                usersRepository.addUser(user.toUsersEntity())
            }

            // A atualização da _uiState.usersList será handled pelo `getAllUsers().onEach` no init block.
            // Mas precisamos garantir que o usuário selecionado também seja atualizado imediatamente
            // se for o mesmo que está sendo modificado.
            _uiState.update { currentState ->
                val updatedList = currentState.usersList.toMutableList()
                val index = updatedList.indexOfFirst { it.id == user.id }
                if (index != -1) {
                    updatedList[index] = user
                } else {
                    updatedList.add(user)
                }

                // Se o usuário que está sendo atualizado/adicionado é o atualmente selecionado,
                // atualiza também o 'user' no MainUiState.
                val newSelectedUser = if (currentState.user?.id == user.id) user else currentState.user
                currentState.copy(usersList = updatedList, user = newSelectedUser)
            }
        }
    }


    fun deleteUser(user: UserModel?) {
        if (user == null) {
            Log.w("MainViewModel", "deleteUser called with null user. No action taken.")
            return
        }

        viewModelScope.launch {
            usersRepository.deleteUser(user.toUsersEntity())

            // A atualização da _uiState.usersList será handled pelo `getAllUsers().onEach` no init block.
            // Mas precisamos garantir que o usuário selecionado também seja atualizado se ele for o deletado.
            _uiState.update { currentState ->
                val updatedList = currentState.usersList.toMutableList().apply {
                    removeIf { it.id == user.id }
                }

                // Se o usuário deletado era o selecionado, define o selecionado como null ou o primeiro da nova lista
                val newSelectedUser = if (currentState.user?.id == user.id) {
                    updatedList.firstOrNull() // Seleciona o primeiro da lista restante
                } else {
                    currentState.user // Mantém o usuário selecionado atual
                }

                currentState.copy(usersList = updatedList, user = newSelectedUser)
            }
        }
    }
}

// Adicione esta função de extensão para converter UserModel para UsersEntity
fun UserModel.toUsersEntity(): UsersEntity {
    return UsersEntity(
        id = this.id,
        name = this.name,
        iconIndex = this.iconIndex,
        bass = this.bass,
        middle = this.middle,
        high = this.high,
        mainVolume = this.mainVolume,
        pan = this.pan
    )
}
