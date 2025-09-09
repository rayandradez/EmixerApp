package com.reaj.emixer

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.example.emixerapp.manager.AidlServiceManager
import com.reaj.emixer.data.local.database.AppDatabase
import com.reaj.emixer.data.repository.UsersRepository
import com.reaj.emixer.databinding.ActivityMainBinding
import com.reaj.emixer.ui.components.viewModels.MainViewModel
import com.reaj.emixer.ui.components.viewModels.MainViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var viewModel: MainViewModel
    private lateinit var analytics: FirebaseAnalytics

    private val receiver = AirplaneModeBroadcastReceiver()
    private var isReceiverRegistered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate")

        AidlServiceManager.initialize(applicationContext)

        if (!isServiceRunning(MessageService::class.java)) {
            val serviceIntent = Intent(this, MessageService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
            Log.d("MainActivity", "Serviço iniciado explicitamente")
        }

        AidlServiceManager.bindService()

        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appTasks = activityManager.appTasks
        for (appTask in appTasks) {
            val taskInfo = appTask.taskInfo
            Log.d("MainActivity", "Task: ${taskInfo.baseActivity?.className}")
        }

        val packageManager = packageManager
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        for (appInfo in installedApps) {
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            Log.d("MainActivity", "App: $appName")
        }

        analytics = Firebase.analytics
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applySavedTheme()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val layoutParams = window.attributes
        layoutParams.screenBrightness = 0.5f
        window.attributes = layoutParams
        Log.d("MainActivity", "Screen brightness set to: ${layoutParams.screenBrightness}")


        val database = AppDatabase.getDatabase(applicationContext)
        val usersRepository = UsersRepository(database.usersDao())
        val factory = MainViewModelFactory(usersRepository)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.welcome -> {
                    navController.popBackStack(R.id.welcome, false)
                    navController.navigate(R.id.welcome)
                    true
                }
                R.id.settings -> {
                    navController.navigate(R.id.settings)
                    true
                }
                else -> false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.welcome -> binding.bottomNavigation.menu.findItem(R.id.welcome).isChecked = true
                R.id.settings -> binding.bottomNavigation.menu.findItem(R.id.settings).isChecked = true
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { data ->
                    Log.i("MAIN_ACTIVITY", "TESTING VIEW MODEL: $data")
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        navController = findNavController(R.id.navHostFragmentContainerView)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "onStart")
        if (!isReceiverRegistered) {
            val filter = IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            registerReceiver(receiver, filter)
            isReceiverRegistered = true
            Log.d("MainActivity", "AirplaneModeReceiver registrado.")
        }
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
        if (isReceiverRegistered) {
            unregisterReceiver(receiver)
            isReceiverRegistered = false
            Log.d("MainActivity", "AirplaneModeReceiver teve seu registro cancelado.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy")
        AidlServiceManager.unbindService()
    }

    private fun applySavedTheme() {
        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val savedNightMode = sharedPrefs.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(savedNightMode)
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Log.d("MainActivity", "Serviço já está rodando")
                return true
            }
        }
        Log.d("MainActivity", "Serviço não está rodando")
        return false
    }
}

// Classe de dados para representar um perfil de contato
data class Profile(
    val id: Long,
    val name: String,
    val uri: Uri
)
