// AudioManagerTest.kt

package com.example.emixerapp.manager

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.reaj.emixer.IMessageService

// Especifica que a classe deve usar o RobolectricTestRunner para executar os testes
@RunWith(RobolectricTestRunner::class)

// Configura o ambiente de teste sem manifest e com a versão 28 do SDK
@Config(manifest = Config.NONE, sdk = [28])
class AudioManagerTest {

    // Declara uma instância de AudioManager para ser inicializada posteriormente
    private lateinit var audioManager: AudioManager

    // Mock do AidlServiceManager para simular seu comportamento durante os testes
    @Mock
    private lateinit var aidlServiceManager: AidlServiceManager

    // Mock do IMessageService para simular seu comportamento durante os testes
    @Mock
    private lateinit var messageService: IMessageService

    // Este método é executado antes de cada teste para configurar o ambiente de teste
    @Before
    fun setUp() {
        // Inicializa os objetos mock
        MockitoAnnotations.initMocks(this)

        // Configura o comportamento do mock do AidlServiceManager
        `when`(aidlServiceManager.isServiceBound()).thenReturn(true)
        `when`(aidlServiceManager.getMessageService()).thenReturn(messageService)

        // Inicializa o AudioManager com o mock do AidlServiceManager
        audioManager = AudioManager(aidlServiceManager)
    }

    // Teste para verificar se a configuração do valor de bass funciona corretamente
    @Test
    fun setBass_success() {
        val bassValue = 10
        // Configura o mock do IMessageService para retornar true quando setBass for chamado
        `when`(messageService.setBass(bassValue)).thenReturn(true)

        // Chama o método setBass no AudioManager
        audioManager.setBass(bassValue)

        // Verifica se setBass foi chamado no IMessageService com o parâmetro correto
        verify(messageService).setBass(bassValue)
    }

    // Teste para verificar se a configuração do valor de mid funciona corretamente
    @Test
    fun setMid_success() {
        val midValue = 50
        // Configura o mock do IMessageService para retornar true quando setMid for chamado
        `when`(messageService.setMid(midValue)).thenReturn(true)

        // Chama o método setMid no AudioManager
        audioManager.setMid(midValue)

        // Verifica se setMid foi chamado no IMessageService com o parâmetro correto
        verify(messageService).setMid(midValue)
    }

    // Teste para verificar se a configuração do valor de treble funciona corretamente
    @Test
    fun setTreble_success() {
        val trebleValue = 75
        // Configura o mock do IMessageService para retornar true quando setTreble for chamado
        `when`(messageService.setTreble(trebleValue)).thenReturn(true)

        // Chama o método setTreble no AudioManager
        audioManager.setTreble(trebleValue)

        // Verifica se setTreble foi chamado no IMessageService com o parâmetro correto
        verify(messageService).setTreble(trebleValue)
    }

    // Teste para verificar se a configuração do valor de volume principal funciona corretamente
    @Test
    fun setMainVolume_success() {
        val volumeValue = 80
        // Configura o mock do IMessageService para retornar true quando setMainVolume for chamado
        `when`(messageService.setMainVolume(volumeValue)).thenReturn(true)

        // Chama o método setMainVolume no AudioManager
        audioManager.setMainVolume(volumeValue)

        // Verifica se setMainVolume foi chamado no IMessageService com o parâmetro correto
        verify(messageService).setMainVolume(volumeValue)
    }

    // Teste para verificar se a configuração do valor de pan funciona corretamente
    @Test
    fun setPan_success() {
        val panValue = -20
        // Configura o mock do IMessageService para retornar true quando setPan for chamado
        `when`(messageService.setPan(panValue)).thenReturn(true)

        // Chama o método setPan no AudioManager
        audioManager.setPan(panValue)

        // Verifica se setPan foi chamado no IMessageService com o parâmetro correto
        verify(messageService).setPan(panValue)
    }
}

