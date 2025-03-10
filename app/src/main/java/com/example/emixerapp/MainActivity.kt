package com.reaj.emixer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.emixerapp.MessageService
import com.reaj.emixer.data.local.database.AppDatabase
import com.reaj.emixer.data.repository.UsersRepository
import com.reaj.emixer.ui.components.viewModels.MainViewModel
import com.reaj.emixer.ui.components.viewModels.MainViewModelFactory
import com.reaj.emixer.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding   // Binding para acessar os componentes da UI
    private lateinit var navController: NavController   // Controlador de navegação para gerenciar as transições entre fragments
    private lateinit var viewModel: MainViewModel   // ViewModel para gerenciar dados e lógica de negócios
    private lateinit var receiver: AirplaneModeBroadcastReceiver    // Receptor para escutar mudanças no modo avião
    private lateinit var analytics: FirebaseAnalytics // Firebase para rastrear eventos do usuário


    private var messageService: IMessageService? = null
    private var isBound = false

    // Declara uma variável para a conexão com o serviço
    private val connection: ServiceConnection = object : ServiceConnection {
        // Chamado quando a conexão com o serviço é estabelecida
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // Obtém a interface do serviço a partir do IBinder
            messageService = IMessageService.Stub.asInterface(service)
            isBound = true
        }

        // Chamado quando a conexão com o serviço é desconectada
        override fun onServiceDisconnected(className: ComponentName) {
            // Define que o serviço não está mais disponível
            messageService = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obrir a instância do Firebase Analytics.
        analytics = Firebase.analytics

        // Habilita a exibição de borda a borda, removendo as áreas de inserção da UI do sistema.
        enableEdgeToEdge()

        // Infla o layout para a activity.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = Intent(this, MessageService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        // Define um listener para lidar com as áreas de inserção da janela (barras do sistema, como status e navegação).
        // Isso garante que o conteúdo seja desenhado corretamente, evitando sobreposição com elementos da UI do sistema.
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Inicializa o receptor para mudanças no modo avião e registra para receber esses eventos.
        receiver = AirplaneModeBroadcastReceiver()
        IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED).also {
            registerReceiver(receiver, it)
        }


        // Obtém uma instância do MainViewModel usando o delegado viewModels().  Este é um padrão
        // do AndroidX que simplifica a criação e gerenciamento de ViewModels.  O uso de
        // viewModels() garante que:
        //   - Um único MainViewModel é criado e associado a esta Activity.
        //   - O ViewModel sobrevive a mudanças de configuração (como rotações de tela).
        //   - O ViewModel é automaticamente destruído quando a Activity é destruída, prevenindo vazamentos de memória.
        //   - O ViewModel é criado usando a injeção de dependências (se configurado), garantindo a testabilidade.
        // Inicializar o banco de dados e o repositório
        val database = AppDatabase.getDatabase(applicationContext)
        val usersRepository = UsersRepository(database.usersDao())


        // Inicializar o ViewModel com o Factory compartilhado com a Activity
        val factory = MainViewModelFactory(usersRepository)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]


        // Inicia uma corrotina dentro do escopo do ciclo de vida da activity.
        lifecycleScope.launch {
            // repeatOnLifecycle garante que a corrotina esteja ativa apenas enquanto o ciclo de vida estiver no estado STARTED ou superior.
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Coleta o fluxo de uiState do viewModel. Isso será executado sempre que o uiState mudar.
                viewModel.uiState.collect { data ->
                    // Atualize os elementos da UI com base nos dados recebidos.  Por exemplo:
                    // binding.textViewUserName.text = data.user.name
                    // binding.imageViewUserAvatar.setImageURI(data.user.avatarUrl)
                    // ... atualize outros elementos da UI conforme necessário ...

                    // Registra os dados recebidos para fins de depuração.
                    Log.i("MAIN_ACTIVITY", "TESTING VIEW MODEL: " + data)
                }
            }
        }
    }


    // Lidar com a ação de navegação para cima, tipicamente usada para navegação para trás em uma configuração de componente de navegação.
    override fun onSupportNavigateUp(): Boolean {
        // Obtém o NavController associado ao fragmento de host de navegação.
        navController = findNavController(R.id.navHostFragmentContainerView)
        // Tenta navegar para cima. Se bem-sucedido, retorna true; caso contrário, delega para a superclasse.
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onStop() {
        super.onStop()
        // Desregistra o receptor para evitar vazamentos de memória
        unregisterReceiver(receiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    fun getMessageService(): IMessageService? {
        return messageService
    }
}

// Classe de dados para representar um perfil de contato
data class Profile(
    val id: Long,   // ID do contato
    val name: String,   // Nome do contato
    val uri: Uri    // URI do contato
)