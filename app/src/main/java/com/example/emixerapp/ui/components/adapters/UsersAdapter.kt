package com.reaj.emixer.ui.components.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reaj.emixer.data.model.UserModel
import com.reaj.emixer.R
import com.reaj.emixer.databinding.AdapterUserBinding

/**
 * Adaptador para RecyclerView que exibe uma lista de usuários.
 * Cada item da lista é uma instância de [UserModel].
 */
class UsersAdapter(var dataSet: ArrayList<UserModel>) :
    RecyclerView.Adapter<UsersAdapter.ViewHolder>() {

    // Callback para notificar a activity quando um usuário for clicado.
    var onItemClick: ((UserModel) -> Unit)? = null

    // Infla o layout para cada item da lista e retorna um novo ViewHolder.
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = AdapterUserBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ViewHolder(binding)
    }

    // Vincula os dados do usuário ao ViewHolder correspondente.
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentItem = dataSet[position]
        viewHolder.binding(currentItem)   // Chama a função bind para vincular os dados.
    }

    // Retorna o número total de itens na lista de usuários.
    override fun getItemCount() = dataSet.size



    // Classe ViewHolder para manter a vinculação da view.
    inner class ViewHolder(val binding: AdapterUserBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            // Define o listener de clique para cada item da lista.
            itemView.setOnClickListener {
                // Chama o callback onItemClick, passando o objeto UserModel do item clicado.
                onItemClick?.invoke(dataSet[adapterPosition])
            }
        }

        /**
         * Vincula os dados do usuário à view.
         * @param user O objeto UserModel a ser exibido.
         */
        fun binding(user: UserModel) {
            binding.userNameTextView.text = user.name

            // Obtém o recurso de desenho do ícone usando o IconManager.
            binding.userIconImageView.setImageResource(user.iconIndex)


        }
    }
}
