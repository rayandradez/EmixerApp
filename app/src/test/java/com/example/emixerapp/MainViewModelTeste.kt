package com.example.emixerapp

import com.example.emixerapp.data.model.UserModel
import com.example.emixerapp.data.repository.UsersRepository
import com.example.emixerapp.ui.components.viewModels.MainViewModel
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first

@ExperimentalCoroutinesApi
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: MainViewModel
    private lateinit var usersRepository: UsersRepository

    @Before
    fun setup() {
        // Define o dispatcher de teste
        Dispatchers.setMain(testDispatcher)

        // Mock do UsersRepository
        usersRepository = mockk(relaxed = true)

        // Inicializa o ViewModel com o repositório mockado
        viewModel = MainViewModel(usersRepository)
    }

    @After
    fun tearDown() {
        // Reseta Dispatchers.Main para o original
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_shouldHaveEmptyUserList() {
        // Verifica: A lista de usuários deve estar vazia na inicialização
        // Este teste é importante para garantir que o estado inicial do ViewModel
        // seja consistente e que não haja usuários pré-existentes.
        val usersList = viewModel.uiState.value.usersList
        assertTrue(usersList.isEmpty())
    }

    @Test
    fun addUser_shouldAddUserToList() = runTest {
        // Arrange: cria um novo usuário
        val newUser = UserModel(id = "1", name = "Test User", iconIndex = 0)

        // Act: adiciona o usuário
        viewModel.addUser(newUser)

        // Verifica: o usuário é adicionado à lista de usuários do ViewModel
        // Este teste é crucial para verificar a funcionalidade de adicionar novos usuários,
        // assegurando que a lista de usuários seja atualizada corretamente.
        val usersList = viewModel.uiState.first().usersList
        assertEquals(1, usersList.size)
        assertEquals("Test User", usersList[0].name)
    }

    @Test
    fun addMultipleUsers_shouldContainAllUsers() {
        // Arrange: Cria múltiplos usuários
        val user1 = UserModel(id = "1", name = "User One", iconIndex = 0)
        val user2 = UserModel(id = "2", name = "User Two", iconIndex = 1)

        // Act: Adiciona ambos os usuários
        viewModel.addUser(user1)
        viewModel.addUser(user2)

        // Verifica: A lista de usuários deve conter ambos os usuários
        // Este teste é importante para garantir que múltiplos usuários possam ser
        // adicionados e que a lista mantenha todos corretamente.
        val usersList = viewModel.uiState.value.usersList
        assertEquals(2, usersList.size)
        assertTrue(usersList.contains(user1))
        assertTrue(usersList.contains(user2))
    }

    @Test
    fun addAndRemoveUser_shouldHandleCorrectly() {
        // Arrange: Cria um usuário
        val user = UserModel(id = "1", name = "User", iconIndex = 0)

        // Act: Adiciona e então remove o usuário
        viewModel.addUser(user)
        viewModel.deleteUser(user)

        // Verifica: A lista de usuários deve estar vazia
        // Este teste verifica a capacidade do ViewModel de remover usuários
        // corretamente, garantindo que a lista seja atualizada adequadamente
        // após operações de adição e remoção.
        val usersList = viewModel.uiState.value.usersList
        assertTrue(usersList.isEmpty())
    }
}
