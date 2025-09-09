package com.example.emixerapp.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Gerencia a verificação e solicitação de permissões necessárias para o aplicativo em tempo de execução.
 *
 * @param context O contexto da aplicação.
 * @param activity A atividade que está solicitando as permissões, necessária para registrar o launcher.
 */
class PermissionManager(private val context: Context, private val activity: ComponentActivity) {

    /**
     * Define a lista de permissões necessárias com base na versão do SDK do Android.
     * A permissão POST_NOTIFICATIONS só é necessária a partir do Android 13 (API 33, TIRAMISU).
     */
    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO, // Para acessar os arquivos de música
            Manifest.permission.READ_CONTACTS,    // Para importar perfis de contatos
            Manifest.permission.POST_NOTIFICATIONS // Para exibir a notificação do serviço
        )
    } else {
        arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_CONTACTS
        )
    }

    /**
     * Prepara o launcher que solicita as permissões ao sistema operacional e lida com o resultado.
     */
    private val requestPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                if (isGranted) {
                    Log.d("PermissionManager", "Permissão concedida: $permissionName")
                } else {
                    Log.w("PermissionManager", "Permissão negada: $permissionName")
                    // Aqui você poderia exibir uma mensagem ao usuário explicando por que a permissão é necessária.
                }
            }
        }

    /**
     * Verifica quais das permissões necessárias ainda não foram concedidas e, se houver alguma,
     * lança o diálogo de solicitação de permissão para o usuário.
     */
    fun checkPermissions() {
        // Filtra a lista 'requiredPermissions' para encontrar apenas as que ainda não foram concedidas.
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        // Se a lista de permissões a serem solicitadas não estiver vazia, lança o diálogo.
        if (permissionsToRequest.isNotEmpty()) {
            Log.d("PermissionManager", "Solicitando permissões: ${permissionsToRequest.joinToString()}")
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            Log.d("PermissionManager", "Todas as permissões necessárias já foram concedidas.")
        }
    }
}
