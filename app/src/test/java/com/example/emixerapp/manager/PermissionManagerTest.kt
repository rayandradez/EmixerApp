package com.example.emixerapp.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) // Usa o RobolectricTestRunner para executar testes que dependem de componentes Android.
@Config(manifest = Config.NONE, sdk = [28]) // Configura o Robolectric para não usar um manifesto real e simular Android 9 (API 28).
class PermissionManagerTest {

    @Mock // Anota o campo para que o Mockito crie um mock para ele.
    private lateinit var context: Context // Declara um mock do Context do Android.

    @Mock // Anota o campo para que o Mockito crie um mock para ele.
    private lateinit var activity: FragmentActivity // Declara um mock da FragmentActivity do Android.

    private lateinit var permissionManager: PermissionManager // Declara o objeto PermissionManager que será testado.

    @Before // Indica que este método deve ser executado antes de cada método de teste.
    fun setUp() {
        MockitoAnnotations.openMocks(this) // Inicializa os mocks criados pelo Mockito.
        permissionManager = PermissionManager(context, activity) // Cria uma instância do PermissionManager usando os mocks.
    }

    @Test // Indica que este método é um teste de unidade.
    fun checkAudioPermissions_whenPermissionNotGranted_requestsPermission() {
        // Configura o mock para retornar PERMISSION_DENIED quando checkSelfPermission é chamado para RECORD_AUDIO.
        `when`(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO))
            .thenReturn(PackageManager.PERMISSION_DENIED)

        // Chama o método que deve verificar e solicitar a permissão.
        permissionManager.checkAudioPermissions(1001)

        // Verifica se requestPermissions foi chamado na activity com RECORD_AUDIO e o código de requisição 1001.
        verify(activity).requestPermissions(
            eq(arrayOf(Manifest.permission.RECORD_AUDIO)),
            eq(1001)
        )
    }

    @Test // Indica que este método é um teste de unidade.
    fun checkAudioPermissions_whenPermissionGranted_doesNotRequestPermission() {
        // Configura o mock para retornar PERMISSION_GRANTED quando checkSelfPermission é chamado para RECORD_AUDIO.
        `when`(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO))
            .thenReturn(PackageManager.PERMISSION_GRANTED)

        // Chama o método que deve verificar a permissão.
        permissionManager.checkAudioPermissions(1001)

        // Verifica que requestPermissions nunca foi chamado, já que a permissão já foi concedida.
        verify(activity, never()).requestPermissions(any(), anyInt())
    }
}
