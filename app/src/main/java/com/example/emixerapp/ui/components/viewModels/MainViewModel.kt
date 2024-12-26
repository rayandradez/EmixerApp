package com.example.emixerapp.ui.components.viewModels

import androidx.lifecycle.ViewModel
import com.example.emixerapp.data.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class MainUiState(
    var user: UserModel? = null,
    val usersList: List<UserModel> = emptyList()
)
// testing
class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun addUser(newUser: UserModel) {
        _uiState.update { currentState ->
            currentState.copy(usersList = currentState.usersList + newUser)
        }
    }

    fun setCurrentUser(user: UserModel?) {
        _uiState.value.user = user
    }

    fun updateUser(user: UserModel?) {
        _uiState.update { currentState ->
            val updatedList = currentState.usersList.toMutableList()
            val index = updatedList.indexOfFirst { it.id == user?.id }
            if (index != -1) {
                if (user != null) {
                    updatedList[index] = user
                } // Replace the existing user with updated user
            } else {
                if (user != null) {
                    updatedList.add(user)
                } // Add if it's a new user
            }
            currentState.copy(usersList = updatedList)
        }
    }
}
