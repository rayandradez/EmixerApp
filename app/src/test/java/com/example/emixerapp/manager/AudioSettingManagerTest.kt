package com.example.emixerapp.manager

import android.widget.SeekBar
import com.example.emixerapp.ui.screens.UserPage
import com.reaj.emixer.IMessageService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.ref.WeakReference

@RunWith(RobolectricTestRunner::class) // Usa RobolectricTestRunner para simular o ambiente Android em testes de unidade.
@Config(manifest = Config.NONE, sdk = [28]) // Configura Robolectric para não usar um manifesto real e simular Android 9 (API 28).
class AudioSettingManagerTest {

    @Mock // Anota o campo para que o Mockito crie um mock para ele.
    private lateinit var userPage: UserPage // Declara um mock da classe UserPage.

    @Mock // Anota o campo para que o Mockito crie um mock para ele.
    private lateinit var messageService: IMessageService // Declara um mock da interface IMessageService.

    @Mock // Anota o campo para que o Mockito crie um mock para ele.
    private lateinit var bassSeekBar: SeekBar // Declara um mock da classe SeekBar para controle de bass.

    @Mock // Anota o campo para que o Mockito crie um mock para ele.
    private lateinit var midSeekBar: SeekBar // Declara um mock da classe SeekBar para controle de mid.

    @Mock // Anota o campo para que o Mockito crie um mock para ele.
    private lateinit var highSeekBar: SeekBar // Declara um mock da classe SeekBar para controle de high.

    @Mock // Anota o campo para que o Mockito crie um mock para ele.
    private lateinit var mainVolumeSeekBar: SeekBar // Declara um mock da classe SeekBar para controle de volume principal.

    @Mock // Anota o campo para que o Mockito crie um mock para ele.
    private lateinit var panSeekBar: SeekBar // Declara um mock da classe SeekBar para controle de pan.

    private lateinit var audioSettingsManager: AudioSettingsManager // Declara o objeto AudioSettingsManager que será testado.

    // Variáveis para verificar mudanças nos settings
    private var settingsChangedCalled = false
    private var bassChangedValue = -1
    private var midChangedValue = -1
    private var trebleChangedValue = -1
    private var mainVolumeChangedValue = -1
    private var panChangedValue = -1

    @Before // Indica que este método deve ser executado antes de cada método de teste.
    fun setUp() {
        MockitoAnnotations.openMocks(this) // Inicializa os mocks criados pelo Mockito.

        // Cria uma instância do AudioSettingsManager com callbacks para capturar mudanças.
        audioSettingsManager = AudioSettingsManager(
            WeakReference(userPage),
            messageService,
            true,
            { settingsChangedCalled = true }, // Callback para mudança de configuração.
            { bassChangedValue = it }, // Callback para mudança de valor do bass.
            { midChangedValue = it }, // Callback para mudança de valor do mid.
            { trebleChangedValue = it }, // Callback para mudança de valor do treble.
            { mainVolumeChangedValue = it }, // Callback para mudança de valor do volume principal.
            { panChangedValue = it } // Callback para mudança de valor do pan.
        )

        // Configura o progresso inicial dos SeekBars usando Mockito.
        `when`(bassSeekBar.progress).thenReturn(10)
        `when`(midSeekBar.progress).thenReturn(20)
        `when`(highSeekBar.progress).thenReturn(30)
        `when`(mainVolumeSeekBar.progress).thenReturn(40)
        `when`(panSeekBar.progress).thenReturn(50)
    }

    @Test // Indica que este método é um teste de unidade.
    fun testBassSeekBarListener() {
        // Configura os listeners dos SeekBars.
        audioSettingsManager.setupSeekBarListeners(
            bassSeekBar, midSeekBar, highSeekBar, mainVolumeSeekBar, panSeekBar
        )

        // Captura o listener do bassSeekBar.
        val listener = captureSeekBarChangeListener(bassSeekBar)
        listener.onProgressChanged(bassSeekBar, 10, true) // Simula mudança de progresso no bassSeekBar.

        assert(settingsChangedCalled) // Verifica se a mudança de configuração foi chamada.
        assert(bassChangedValue == 10) // Verifica se o valor do bass foi atualizado corretamente.
    }

    @Test // Indica que este método é um teste de unidade.
    fun testMidSeekBarListener() {
        // Configura os listeners dos SeekBars.
        audioSettingsManager.setupSeekBarListeners(
            bassSeekBar, midSeekBar, highSeekBar, mainVolumeSeekBar, panSeekBar
        )

        // Captura o listener do midSeekBar.
        val listener = captureSeekBarChangeListener(midSeekBar)
        listener.onProgressChanged(midSeekBar, 20, true) // Simula mudança de progresso no midSeekBar.

        assert(settingsChangedCalled) // Verifica se a mudança de configuração foi chamada.
        assert(midChangedValue == 20) // Verifica se o valor do mid foi atualizado corretamente.
    }

    @Test // Indica que este método é um teste de unidade.
    fun testTrebleSeekBarListener() {
        // Configura os listeners dos SeekBars.
        audioSettingsManager.setupSeekBarListeners(
            bassSeekBar, midSeekBar, highSeekBar, mainVolumeSeekBar, panSeekBar
        )

        // Captura o listener do highSeekBar.
        val listener = captureSeekBarChangeListener(highSeekBar)
        listener.onProgressChanged(highSeekBar, 30, true) // Simula mudança de progresso no highSeekBar.

        assert(settingsChangedCalled) // Verifica se a mudança de configuração foi chamada.
        assert(trebleChangedValue == 30) // Verifica se o valor do treble foi atualizado corretamente.
    }

    @Test // Indica que este método é um teste de unidade.
    fun testMainVolumeSeekBarListener() {
        // Configura os listeners dos SeekBars.
        audioSettingsManager.setupSeekBarListeners(
            bassSeekBar, midSeekBar, highSeekBar, mainVolumeSeekBar, panSeekBar
        )

        // Captura o listener do mainVolumeSeekBar.
        val listener = captureSeekBarChangeListener(mainVolumeSeekBar)
        listener.onProgressChanged(mainVolumeSeekBar, 40, true) // Simula mudança de progresso no mainVolumeSeekBar.

        assert(settingsChangedCalled) // Verifica se a mudança de configuração foi chamada.
        assert(mainVolumeChangedValue == 40) // Verifica se o valor do volume principal foi atualizado corretamente.
    }

    @Test // Indica que este método é um teste de unidade.
    fun testPanSeekBarListener() {
        // Configura os listeners dos SeekBars.
        audioSettingsManager.setupSeekBarListeners(
            bassSeekBar, midSeekBar, highSeekBar, mainVolumeSeekBar, panSeekBar
        )

        // Captura o listener do panSeekBar.
        val listener = captureSeekBarChangeListener(panSeekBar)
        listener.onProgressChanged(panSeekBar, 50, true) // Simula mudança de progresso no panSeekBar.

        assert(settingsChangedCalled) // Verifica se a mudança de configuração foi chamada.
        assert(panChangedValue == 50) // Verifica se o valor do pan foi atualizado corretamente.
    }

    // Método auxiliar para capturar o OnSeekBarChangeListener de um SeekBar.
    private fun captureSeekBarChangeListener(seekBar: SeekBar): SeekBar.OnSeekBarChangeListener {
        val listenerCaptor = argumentCaptor<SeekBar.OnSeekBarChangeListener>() // Cria um captor para OnSeekBarChangeListener.
        verify(seekBar).setOnSeekBarChangeListener(listenerCaptor.capture()) // Verifica e captura o listener configurado.
        return listenerCaptor.firstValue // Retorna o primeiro valor capturado.
    }
}
