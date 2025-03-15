package com.example.emixerapp.ui.screens

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.reaj.emixer.databinding.FragmentInfoBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.emixerapp.ui.components.adapters.AppListAdapter
import com.example.emixerapp.ui.components.adapters.SimpleListAdapter
import com.reaj.emixer.R

class InfoFragment : Fragment() {

    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val packageManager = requireContext().packageManager

        // Listando as Tarefas
        val activityManager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appTasks = activityManager.appTasks
        val taskNames = appTasks.map { it.taskInfo.baseActivity?.className ?: "Unknown Task" }

        val tasksAdapter = SimpleListAdapter(taskNames)
        binding.recyclerViewTasks.adapter = tasksAdapter
        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(requireContext())



        // Listando os Aplicativos
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val appsAdapter = AppListAdapter(installedApps, packageManager)
        binding.recyclerViewApps.adapter = appsAdapter
        binding.recyclerViewApps.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
