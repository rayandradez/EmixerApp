// MainActivity.kt
package com.reaj.emixer

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
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

    private var messageService: IMessageService? = null  // Interface AIDL para comunicação com o serviço
    private var isBound = false // Variável para rastrear se o serviço está vinculado

    // Objeto que gerencia a conexão com o serviço
    private val connection: ServiceConnection = object : ServiceConnection {
        // Chamado quando a conexão com o serviço é estabelecida
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            messageService = IMessageService.Stub.asInterface(service)  // Obtém a interface AIDL do serviço
            isBound = true // Define a variável isBound como true
        }

        // Chamado quando a conexão com o serviço é perdida
        override fun onServiceDisconnected(className: ComponentName?) {
            messageService = null  // Limpa a interface AIDL
            isBound = false // Define a variável isBound como false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate")

        // Inicia o serviço explicitamente se ele ainda não estiver rodando
        if (!isServiceRunning(MessageService::class.java)) {
            val serviceIntent = Intent(this, MessageService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
            Log.d("MainActivity", "Serviço iniciado explicitamente")
        }

        // Recupera informações sobre as tarefas em execução
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appTasks = activityManager.appTasks
        for (appTask in appTasks) {
            val taskInfo = appTask.taskInfo
            Log.d("MainActivity", "Task: ${taskInfo.baseActivity?.className}")
        }

        // Recupera informações sobre os aplicativos instalados
        val packageManager = packageManager
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        for (appInfo in installedApps) {
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            Log.d("MainActivity", "App: $appName")
        }

        // Obrir a instância do Firebase Analytics.
        analytics = Firebase.analytics

        // Habilita a exibição de borda a borda, removendo as áreas de inserção da UI do sistema.
        enableEdgeToEdge()

        // Infla o layout para a activity.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Set Light/Dark Mode
        applySavedTheme()

        // Cria uma Intent para o serviço
        val intent = Intent(this, MessageService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        // Define um listener para lidar com assetSmallIcon áreas de inserção da janela (barras do sistema, como status e navegação).
        // Isso garante que o conteúdo seja desenhado corretamente, evitando sobreposição com elementos da UI do sistema.
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Adjust screen brightness
        val layoutParams = window.attributes
        layoutParams.screenBrightness = 0.5f // Example brightness level
        window.attributes = layoutParams
        Log.d("MainActivity", "Screen brightness set to: ${layoutParams.screenBrightness}")


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


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        // Setup bottom navigation
        binding.bottomNavigation.setupWithNavController(navController)
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.welcome -> {
                    navController.popBackStack(R.id.welcome, false)
                    navController.navigate(R.id.welcome)
                    binding.bottomNavigation.menu.findItem(R.id.welcome).isChecked = true
                    binding.bottomNavigation.menu.findItem(R.id.settings).isChecked = false
                    true
                }
                R.id.settings -> {
                    navController.navigate(R.id.settings)
                    binding.bottomNavigation.menu.findItem(R.id.welcome).isChecked = false
                    binding.bottomNavigation.menu.findItem(R.id.settings).isChecked = true
                    true
                }
                else -> false
            }
        }

        // Ensure the correct item is checked on initial load
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.welcome -> binding.bottomNavigation.menu.findItem(R.id.welcome).isChecked = true
                R.id.settings -> binding.bottomNavigation.menu.findItem(R.id.settings).isChecked = true
            }
        }


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

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "onPause")
    }


    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onStop")
        // Desregistra o receptor para evitar vazamentos de memória
        unregisterReceiver(receiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy")
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    // Metódo para verificar o tema
    private fun applySavedTheme() {
        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        // O valor padrão para o modo noturno é -1 (MODE_NIGHT_FOLLOW_SYSTEM).
        // Se o usuário nunca configurou, ele seguirá o sistema.
        // Se 0 (NO) ou 1 (YES) estiver salvo, ele usará essa preferência.
        val savedNightMode = sharedPrefs.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(savedNightMode)
    }


    // Método para verificar se o serviço está rodando
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        // Obtém a lista de processos em execução e a torna imutável
        val runningProcesses = manager.runningAppProcesses.toList()
        for (processInfo in runningProcesses) {
            // Verifica se o serviço está rodando
            if (processInfo.processName == packageName) {
                Log.d("MainActivity", "Serviço já está rodando")
                return true // Se o serviço já estiver rodando, retorna true
            }
        }
        Log.d("MainActivity", "Serviço não está rodando")
        return false // Se o serviço não estiver rodando, retorna false
    }
}


// Classe de dados para representar um perfil de contato
data class Profile(
    val id: Long,   // ID do contato
    val name: String,   // Nome do contato
    val uri: Uri    // URI do contato
)