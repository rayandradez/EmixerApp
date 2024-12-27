package com.example.emixerapp

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.emixerapp.ui.components.viewModels.MainViewModel
import com.example.mvvmapp.R
import com.example.mvvmapp.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var receiver: AirplaneModeBroadcastReceiver


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
        val viewModel: MainViewModel by viewModels()

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
        unregisterReceiver(receiver)
    }
}

