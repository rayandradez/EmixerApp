package com.example.emixerapp.ui.components.viewModels

import androidx.lifecycle.ViewModel
import com.example.emixerapp.data.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class MainUiState(
    val user: UserModel? = null,
    val usersList: List<UserModel> = emptyList()
)

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun addUser(newUser: UserModel) {
        _uiState.update { currentState ->
            currentState.copy(usersList = currentState.usersList + newUser)
        }
    }

    //Other functions, if needed, to update the user list or individual users.  These should all update _uiState.
}
