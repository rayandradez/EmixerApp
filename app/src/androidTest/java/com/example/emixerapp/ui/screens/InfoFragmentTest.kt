package com.reaj.emixer

import androidx.navigation.Navigation.findNavController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.emixerapp.ui.screens.InfoFragment

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule

/**
 * Teste instrumentado, que será executado em um dispositivo Android.
 *
 * Este teste verifica o nome do pacote do aplicativo para garantir que o contexto
 * do aplicativo esteja configurado corretamente. É um teste básico para verificar
 * a configuração do ambiente de teste
 *
 * Veja a [documentação de testes](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    var activityRule: ActivityScenarioRule<MainActivity> = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testNavigateToInfoFragment() {
        // Navega para o SettingsFragment usando o BottomNavigationView
        onView(withId(R.id.settings)).perform(click())

        // Verifica se o SettingsFragment está exibido
        onView(withId(R.id.settings))
            .check(matches(isDisplayed()))

        // Rola até o botão antes de clicar
        onView(withId(R.id.btnTestTasks)).perform(scrollTo(), click())

        // Verifica se o InfoFragment está exibido
        onView(withId(R.id.recyclerViewTasks))
            .check(matches(isDisplayed()))

        // Verifica se o RecyclerView contém itens
        onView(withId(R.id.recyclerViewTasks))
            .check(matches(hasMinimumChildCount(1)))
    }
}
