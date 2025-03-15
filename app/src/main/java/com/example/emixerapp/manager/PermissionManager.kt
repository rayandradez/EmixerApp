// PermissionManager.kt
package com.example.emixerapp.manager
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Gerencia as permissões necessárias para o aplicativo, como permissões de áudio.
 *
 * @param context O contexto da aplicação.
 * @param activity A Activity onde as permissões serão solicitadas.
 */
class PermissionManager(private val context: Context, private val activity: FragmentActivity) {

    /**
     * Verifica e solicita as permissões de áudio necessárias.
     *
     * @param permissionRequestCode O código de requisição para a permissão.
     */
    fun checkAudioPermissions(permissionRequestCode: Int) {
        // Determina qual permissão usar com base na versão do Android
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO // Permissão para Android 13 (Tiramisu) e superior
        } else {
            Manifest.permission.RECORD_AUDIO // Permissão para versões mais antigas do Android
        }

        // Verifica se a permissão já foi concedida
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            // Se a permissão não foi concedida, solicita ao usuário
            ActivityCompat.requestPermissions(activity, arrayOf(permission), permissionRequestCode)
        }
    }
}
