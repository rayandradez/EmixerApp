// InfoFragment.kt
package com.example.emixerapp.ui.screens

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.emixerapp.ui.components.adapters.AppListAdapter
import com.example.emixerapp.ui.components.adapters.SimpleListAdapter
import com.reaj.emixer.MainActivity
import com.reaj.emixer.databinding.FragmentInfoBinding

/**
 * Fragmento para exibir informações sobre as tarefas em execução e os aplicativos instalados.
 */
class InfoFragment : Fragment() {

    private var _binding: FragmentInfoBinding? = null // Variável para armazenar a instância do ViewBinding
    private val binding get() = _binding!! // Obtém a instância do ViewBinding, garantindo que não seja nula

    /**
     * Infla o layout do fragmento.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInfoBinding.inflate(inflater, container, false) // Infla o layout usando ViewBinding
        return binding.root // Retorna a view raiz do layout
    }

    /**
     * Configura a UI e obtém os dados para exibir.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtém as instâncias do ActivityManager e PackageManager da MainActivity
        val mainActivity = activity as? MainActivity
        val packageManager = mainActivity?.packageManager
        val activityManager = mainActivity?.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager

        // Listando as Tarefas
        val appTasks = activityManager?.appTasks
        val taskNames = appTasks?.map { it.taskInfo.baseActivity?.className ?: "Unknown Task" } ?: emptyList()

        val tasksAdapter = SimpleListAdapter(taskNames) // Cria um adaptador para a lista de tarefas
        binding.recyclerViewTasks.adapter = tasksAdapter // Define o adaptador para o RecyclerView de tarefas
        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(requireContext()) // Define o LayoutManager para o RecyclerView de tarefas

        // Listando os Aplicativos
        val installedApps = packageManager?.getInstalledApplications(PackageManager.GET_META_DATA) ?: emptyList() // Obtém a lista de aplicativos instalados
        val appsAdapter = if (packageManager != null) {
            AppListAdapter(installedApps, packageManager) // Cria um adaptador para a lista de aplicativos
        } else {
            // Se o PackageManager for nulo, cria um adaptador com uma lista vazia e um PackageManager falso
            AppListAdapter(emptyList(), requireContext().packageManager)
        }
        binding.recyclerViewApps.adapter = appsAdapter // Define o adaptador para o RecyclerView de aplicativos
        binding.recyclerViewApps.layoutManager = LinearLayoutManager(requireContext()) // Define o LayoutManager para o RecyclerView de aplicativos
    }

    /**
     * Limpa a referência do ViewBinding para evitar vazamentos de memória.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Define a variável _binding como nula
    }
}
