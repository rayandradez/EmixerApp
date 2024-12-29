package com.reaj.emixer

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Teste instrumentado, que será executado em um dispositivo Android.
 *
 * Este teste verifica o nome do pacote do aplicativo para garantir que o contexto
 * do aplicativo esteja configurado corretamente. É um teste básico para verificar
 * a configuração do ambiente de teste.
 *
 * Veja a [documentação de testes](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Obtém o contexto do aplicativo que está sendo testado.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        // Verifica se o nome do pacote do aplicativo corresponde ao esperado.
        assertEquals("com.reaj.emixer", appContext.packageName)
    }
}