package com.example.emixerapp

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.emixerapp.data.local.database.AppDatabase
import com.example.emixerapp.data.model.UserModel
import com.example.emixerapp.data.repository.UsersRepository
import com.example.emixerapp.ui.components.viewModels.MainViewModel
import com.example.emixerapp.ui.components.viewModels.MainViewModelFactory
import com.example.mvvmapp.R
import com.example.mvvmapp.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding   // Binding para acessar os componentes da UI
    private lateinit var navController: NavController   // Controlador de navegação para gerenciar as transições entre fragments
    private lateinit var viewModel: MainViewModel   // ViewModel para gerenciar dados e lógica de negócios
    private lateinit var receiver: AirplaneModeBroadcastReceiver    // Receptor para escutar mudanças no modo avião


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilita a exibição de borda a borda, removendo as áreas de inserção da UI do sistema.
        enableEdgeToEdge()

        // Infla o layout para a activity.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        // Registra um launcher para solicitar permissão de leitura de contatos.
        val requestPermissionLauncher =
            this.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                // Ação a ser tomada com base na concessão da permissão
                if (isGranted) {
                    // Define as colunas a serem consultadas no banco de dados de contatos
                    val projection = arrayOf(
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME,

                    )

                    // Consulta os contatos do usuário
                    contentResolver.query(
                        ContactsContract.Contacts.CONTENT_URI,
                        projection,
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        // Obtém os índices das colunas relevantes
                        val idContactColumn = cursor.getColumnIndex(
                            ContactsContract.Contacts._ID
                        )
                        val nameContactColumn = cursor.getColumnIndex(
                            ContactsContract.Contacts.DISPLAY_NAME
                        )
                        val profile = mutableListOf<Profile>()  // Lista para armazenar perfis de contatos
                        while (cursor.moveToNext()) {
                            // Extrai os dados do contato
                            val id = cursor.getLong(idContactColumn)
                            val name = cursor.getString(nameContactColumn)
                            val uri =
                                ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id)

                            profile.add(Profile(id, name, uri)) // Adiciona o perfil à lista

                            // Cria um UserModel e atualiza no ViewModel
                            val user = UserModel(name = name, iconIndex = 0)
                            viewModel.updateUser(user)

                        }

                    }
                } else {
                    // Exibe um Toast informando que a importação do perfil falhou
                    Toast.makeText(
                        this@MainActivity,
                        "Unable to import your profile",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        // Verifica o estado da permissão de leitura de contatos
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {}    // Permissão já concedida; nenhuma ação necessária

            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.READ_CONTACTS
            ) -> {
                // Aqui você pode mostrar uma explicação ao usuário sobre por que a permissão é necessária
            }

            else -> {
                // Solicita a permissão ao usuário
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_CONTACTS
                )
            }
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


        // Inicializar o ViewModel com o Factory
        val factory = MainViewModelFactory(usersRepository)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)



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
                    Log.e("MAIN_ACTIVITY", "TESTING VIEW MODEL: " + data)
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
}

// Classe de dados para representar um perfil de contato
data class Profile(
    val id: Long,   // ID do contato
    val name: String,   // Nome do contato
    val uri: Uri    // URI do contato
)