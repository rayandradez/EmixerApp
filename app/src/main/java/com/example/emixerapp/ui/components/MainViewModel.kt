package com.example.emixerapp.ui.components

import androidx.lifecycle.ViewModel
import com.example.emixerapp.data.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow

// TODO - Explain the data Class
data class DiceUiState(
    val user: UserModel? = UserModel("Rayanne", "", 0, 0, 0),
)

// TODO - Explain the view model
class MainViewModel: ViewModel() {

    // TODO - Added comment about the StateFlow
    private val _user = MutableStateFlow(DiceUiState())
    val userState: StateFlow<DiceUiState> = _user.asStateFlow()

    // TODO - Add functions update users data

}