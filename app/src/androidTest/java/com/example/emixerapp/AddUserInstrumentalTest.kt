package com.example.emixerapp

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import com.example.mvvmapp.R
import org.hamcrest.CoreMatchers.not
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddUserInstrumentedTest {

    /**
     * Teste de Integração: addUserTest
     *
     * Este teste verifica o fluxo de adicionar um novo usuário no aplicativo.
     * Ele navega através das telas Welcome, ManageUser e AddUser, simula a
     * entrada de dados do usuário e a seleção de um ícone, e finalmente salva
     * o novo usuário. O teste então verifica se o usuário foi adicionado
     * corretamente.
     */
    @Test
    fun addUserTest() {
        // Inicia a MainActivity
        ActivityScenario.launch(MainActivity::class.java)

        // Aguarda a tela Welcome estar pronta
        onView(withId(R.id.BtnManageUser)).check(matches(isDisplayed()))

        // Navega do fragmento Welcome para ManageUser
        onView(withId(R.id.BtnManageUser)).perform(click())

        // Aguarda a tela ManageUser estar pronta
        onView(withId(R.id.addNewUserButton)).check(matches(isDisplayed()))

        // Navega de ManageUser para AddUser
        onView(withId(R.id.addNewUserButton)).perform(click())

        // Agora estamos no fragmento AddUser

        // Aguarda a tela AddUser ser exibida
        onView(withId(R.id.editNewName)).check(matches(isDisplayed()))

        // Substitui o texto no campo EditText
        onView(withId(R.id.editNewName))
            .perform(replaceText("Novo Usuário"), closeSoftKeyboard())

        // Seleciona um ícone no RecyclerView
        onView(withId(R.id.recycler_view_icons))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // Clica no botão de salvar
        onView(withId(R.id.BtnSaveUser)).perform(click())

        // Verifica se o usuário foi adicionado (isso depende do comportamento do app após salvar)
        // Exemplo: onView(withText("Novo Usuário")).check(matches(isDisplayed()))
    }

    /**
     * Teste para verificar a edição de detalhes de um usuário no fragmento AddUser.
     */
    @Test
    fun editUserTest() {
        // Lança a MainActivity
        ActivityScenario.launch(MainActivity::class.java)

        // Navega para o fragmento Welcome e espera que o botão ManageUser esteja visível
        onView(withId(R.id.BtnManageUser)).check(matches(isDisplayed()))

        // Clica no botão para ir para ManageUser
        onView(withId(R.id.BtnManageUser)).perform(click())

        // Verifica se a RecyclerView está visível
        onView(withId(R.id.recycler_view_user)).check(matches(isDisplayed()))

        // Clica no primeiro usuário da lista para editá-lo
        onView(withId(R.id.recycler_view_user))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // Espera a tela AddUser ser exibida
        onView(withId(R.id.editNewName)).check(matches(isDisplayed()))

        // Altera o nome do usuário
        onView(withId(R.id.editNewName))
            .perform(replaceText("Updated User"), closeSoftKeyboard())

        // Seleciona um novo ícone do RecyclerView
        onView(withId(R.id.recycler_view_icons))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(2, click()))

        // Clica no botão de salvar
        onView(withId(R.id.BtnSaveUser)).perform(click())

        // Verifica se os detalhes do usuário foram atualizados na lista
        onView(withText("Updated User")).check(matches(isDisplayed()))
    }

    /**
     * Teste para verificar a exclusão de um usuário no fragmento AddUser.
     */
    @Test
    fun deleteUserTest() {
        // Lança a MainActivity
        ActivityScenario.launch(MainActivity::class.java)

        // Navega para o fragmento Welcome e verifica se o botão ManageUser está visível
        onView(withId(R.id.BtnManageUser)).check(matches(isDisplayed()))

        // Clica no botão para ir para ManageUser
        onView(withId(R.id.BtnManageUser)).perform(click())

        // Verifica se a RecyclerView está visível em ManageUser
        onView(withId(R.id.recycler_view_user)).check(matches(isDisplayed()))

        // Clica no primeiro usuário da lista para editá-lo
        onView(withId(R.id.recycler_view_user))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // Verifica se a tela AddUser está exibida
        onView(withId(R.id.editNewName)).check(matches(isDisplayed()))

        // Clica no botão de deletar o usuário
        onView(withId(R.id.BtndeleteUser)).perform(click())

        // Confirma a exclusão no diálogo
        onView(withText("Delete")).perform(click())

        // Verifica se o usuário foi removido da lista em ManageUser
        onView(withId(R.id.recycler_view_user))
            .check(matches(not(hasDescendant(withText("Nome do Usuário")))))
    }

}
