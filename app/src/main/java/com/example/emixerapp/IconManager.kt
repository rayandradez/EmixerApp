package com.example.emixerapp

import com.example.mvvmapp.R

object IconManager {
    val iconDrawables = arrayOf(
        R.drawable.accordion_b,
        R.drawable.drumsb,
        R.drawable.guitar_b,
        R.drawable.headsetb,
        R.drawable.hornb
    )

    fun getDrawableResource(index: Int): Int {
        return iconDrawables.getOrNull(index) ?: R.drawable.ic_launcher_foreground // Default if index is out of bounds
    }
}
