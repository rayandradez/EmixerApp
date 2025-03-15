package com.example.emixerapp.ui.components.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reaj.emixer.R
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.widget.ImageView


class AppListAdapter(private val appList: List<ApplicationInfo>, private val packageManager: PackageManager) :
    RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appName: TextView = view.findViewById(R.id.appName)
        val appIcon: ImageView = view.findViewById(R.id.appIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appInfo = appList[position]
        holder.appName.text = packageManager.getApplicationLabel(appInfo)
        holder.appIcon.setImageDrawable(packageManager.getApplicationIcon(appInfo))
    }

    override fun getItemCount(): Int = appList.size
}

