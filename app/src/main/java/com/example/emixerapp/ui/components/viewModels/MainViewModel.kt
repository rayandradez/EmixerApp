package com.example.emixerapp.ui.components.viewModels

import androidx.lifecycle.ViewModel
import com.example.emixerapp.data.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow

// TODO - Explain the data Class
data class MainUiState(
    val user: UserModel = UserModel("Rayanne", "", 0, 0, 0),
    val usersList: List<UserModel> = arrayListOf()
)

// TODO - Explain the view model
class MainViewModel: ViewModel() {

    // TODO - Added comment about the StateFlow
    private val _user = MutableStateFlow(MainUiState())
    val userState: StateFlow<MainUiState> = _user.asStateFlow()

    private val _users_list = MutableStateFlow(MainUiState())
    val userListState: StateFlow<MainUiState> = _users_list.asStateFlow()

    // TODO - Add functions update users data
    fun updateUsersList(usersList: List<UserModel>) {
        _users_list.value = _users_list.value.copy(usersList = usersList)
    }

}