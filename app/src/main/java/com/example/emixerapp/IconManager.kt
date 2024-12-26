package com.example.emixerapp

import com.example.mvvmapp.R

/**
 * Gerenciador de ícones para o aplicativo.  Fornece acesso fácil aos recursos de desenho de ícones.
 */
object IconManager {

    /**
     * Array contendo os recursos de desenho dos ícones.
     * Os índices deste array correspondem aos índices usados no método `getDrawableResource()`.
     */
    val iconDrawables = arrayOf(
        R.drawable.accordion_b,
        R.drawable.drumsb,
        R.drawable.guitar_b,
        R.drawable.headsetb,
        R.drawable.hornb
    )

    /**
     * Retorna o recurso de desenho do ícone para o índice especificado.
     *
     * @param index O índice do ícone no array `iconDrawables`.
     * @return O recurso de desenho do ícone. Se o índice estiver fora dos limites do array,
     *         retorna o ícone padrão `R.drawable.ic_launcher_foreground`.
     */
    fun getDrawableResource(index: Int): Int {
        return iconDrawables.getOrNull(index) ?: R.drawable.ic_launcher_foreground // Default if index is out of bounds
    }
}
