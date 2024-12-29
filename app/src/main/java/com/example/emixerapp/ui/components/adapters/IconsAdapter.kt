package com.example.emixerapp.ui.components.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvmapp.R
import com.example.mvvmapp.databinding.AdapterIconsBinding

/**
 * Adaptador para RecyclerView que exibe uma lista de ícones.
 * Permite a seleção de um único ícone.
 */
class IconsAdapter(private val dataSet: ArrayList<Int>) :
    RecyclerView.Adapter<IconsAdapter.ViewHolder>() {

    // Callback para notificar a activity quando um ícone for clicado.
    var onItemClick: ((Int) -> Unit)? = null
    // Variável para rastrear a posição do item atualmente selecionado.
    var selectedPosition = -1

        /**
        * ViewHolder para cada item na RecyclerView.
        * Cada ViewHolder contém uma referência à view de um item e seu listener.
        */
        inner class ViewHolder(val binding: AdapterIconsBinding) : RecyclerView.ViewHolder(binding.root) {
            init {
                // Define o listener de clique para cada item.
                itemView.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        // Alterna a seleção do item (selecione ou desmarque).
                        selectedPosition = if (selectedPosition == position) -1 else position
                        // Notifica o adapter de que os dados foram alterados.
                        notifyDataSetChanged()
                        // Chama o callback onItemClick, passando a posição do item clicado.
                        onItemClick?.invoke(position)
                    }
                }
            }

            /**
             * Vincula os dados do ícone à view.
             * @param iconResource O recurso do ícone a ser exibido.
             * @param isSelected Indica se o item está atualmente selecionado.
             */
            fun bind(iconResource: Int, isSelected: Boolean) {
                binding.iconImageView.setImageResource(iconResource)
                // Define o background do ImageView com base no estado de seleção.
                binding.iconImageView.setBackgroundResource(
                    if (isSelected) R.drawable.circle_selected_icon else R.drawable.transparent_background
                )
            }
        }

        // Infla o layout para criar um novo ViewHolder.
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = AdapterIconsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        // Vincula os dados do ícone ao ViewHolder correspondente.
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(dataSet[position], position == selectedPosition) // Call the bind function here
        }

        // Retorna o número total de itens no dataset.
        override fun getItemCount(): Int = dataSet.size
    }
