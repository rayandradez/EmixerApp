package com.example.emixerapp.manager

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.reaj.emixer.IMessageService
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) // Usa RobolectricTestRunner para simular o ambiente Android em testes de unidade.
@Config(manifest = Config.NONE, sdk = [28]) // Configura Robolectric para não usar um manifesto real e simular Android 9 (API 28).
class AidlServiceManagerTest {

    @Mock // Anota o campo para que o Mockito crie um mock para ele.
    private lateinit var context: Context // Declara um mock do Context do Android.

    @Mock // Anota o campo para que o Mockito crie um mock para ele.
    private lateinit var messageService: IMessageService // Declara um mock da interface IMessageService.

    @Mock // Anota o campo para que o Mockito crie um mock para ele.
    private lateinit var binder: IBinder // Declara um mock da interface IBinder.

    private lateinit var aidlServiceManager: AidlServiceManager // Declara o objeto AidlServiceManager que será testado.
    private lateinit var closeable: AutoCloseable // Declara um AutoCloseable para fechar os mocks após os testes.

    @Before // Indica que este método deve ser executado antes de cada método de teste.
    fun setUp() {
        // Substitui initMocks por openMocks e armazena o AutoCloseable para liberar recursos depois.
        closeable = MockitoAnnotations.openMocks(this)

        // Inicializa o AidlServiceManager usando o contexto mockado.
        aidlServiceManager = AidlServiceManager(context)

        // Configura o mock do IMessageService para retornar a interface quando asInterface é chamado.
        `when`(IMessageService.Stub.asInterface(binder)).thenReturn(messageService)
    }

    @After // Indica que este método deve ser executado após cada método de teste.
    fun tearDown() {
        // Fecha os mocks para liberar recursos e evitar vazamentos de memória.
        closeable.close()
    }

    @Test // Indica que este método é um teste de unidade.
    fun bindService_success() {
        // Configura o mock do contexto para simular a conexão do serviço.
        doAnswer { invocation ->
            val serviceConnection = invocation.arguments[1] as ServiceConnection // Captura o ServiceConnection passado.
            serviceConnection.onServiceConnected(null, binder) // Simula a conexão do serviço.
            true
        }.`when`(context).bindService(any(Intent::class.java), any(ServiceConnection::class.java), eq(Context.BIND_AUTO_CREATE))

        var isConnected = false // Variável para rastrear se a conexão foi estabelecida.
        val onServiceConnected: (IMessageService) -> Unit = { isConnected = true } // Callback para quando o serviço é conectado.
        val onServiceDisconnected: () -> Unit = {} // Callback vazio para quando o serviço é desconectado.

        aidlServiceManager.bindService(onServiceConnected, onServiceDisconnected) // Chama o método para vincular o serviço.

        assert(isConnected) // Verifica se a conexão foi estabelecida.
        verify(context).bindService(any(Intent::class.java), any(ServiceConnection::class.java), eq(Context.BIND_AUTO_CREATE)) // Verifica se bindService foi chamado no contexto.
    }

    @Test // Indica que este método é um teste de unidade.
    fun isServiceBound_initiallyFalse() {
        // Verifica se o serviço não está vinculado inicialmente.
        assert(!aidlServiceManager.isServiceBound())
    }

    @Test // Indica que este método é um teste de unidade.
    fun getMessageService_returnsNullWhenNotBound() {
        // Verifica se getMessageService retorna null quando o serviço não está vinculado.
        assert(aidlServiceManager.getMessageService() == null)
    }
}
