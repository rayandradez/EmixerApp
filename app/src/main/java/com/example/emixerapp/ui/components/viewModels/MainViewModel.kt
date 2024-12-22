package com.example.emixerapp.ui.components.viewModels

import androidx.lifecycle.ViewModel
import com.example.emixerapp.data.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class MainUiState(
    val user: UserModel = UserModel("Rayanne", 1, 0, 0),
    val usersList: List<UserModel> = arrayListOf()
)

class MainViewModel : ViewModel() {

    private val _user = MutableStateFlow(MainUiState())
    val userState: StateFlow<MainUiState> = _user.asStateFlow()

    // TODO - Added comment about the StateFlow

    private val _usersList = MutableStateFlow(MainUiState())
    val userListState: StateFlow<MainUiState> = _usersList.asStateFlow()

    // TODO - Add functions update users data

    fun addUser(newUser: UserModel) {
        _usersList.update { currentState ->
            currentState.copy(usersList = currentState.usersList + newUser)
        }
    }
}
