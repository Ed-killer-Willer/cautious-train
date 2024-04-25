package com.example.flypack.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flypack.R
import com.example.flypack.admin.Rud_Uadmin
import com.example.flypack.userssearch.User

class Adapter_User(
    private var listaUsuarios: ArrayList<User>,
    private val listener: Rud_Uadmin // Aquí se define el listener como la actividad Rud_Uadmin
): RecyclerView.Adapter<Adapter_User.ViewHolder>() {

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        // Referencias a los elementos de la interfaz de usuario
        val tvNombre: TextView = itemView.findViewById(R.id.NombreUsuario)
        val tvUsername: TextView = itemView.findViewById(R.id.Username)
        val tvEmail: TextView = itemView.findViewById(R.id.Email)
        val profileIcon: ImageView = itemView.findViewById(R.id.profile)
        val editIcon: ImageView = itemView.findViewById(R.id.edit)


        init {
            // Configura el listener para el clic en el ícono de perfil
            profileIcon.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Cuando se hace clic en el ícono de perfil, se llama al método onItemClick de la actividad Rud_Uadmin
                    listener.onItemClick(position)
                }
            }
        }

        init {
            // Configura el listener para el clic en el ícono de perfil
            editIcon.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Cuando se hace clic en el ícono de perfil, se llama al método onItemClick de la actividad Rud_Uadmin
                    listener.onItemClicks(position)
                }
            }
        }
    }

    // Crea y devuelve una instancia de ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_rv_usuario, parent, false)
        return ViewHolder(itemView)
    }

    // Vincula los datos del usuario en la posición dada con los elementos de la interfaz de usuario en el ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val usuario = listaUsuarios[position]

        holder.tvNombre.text = usuario.nombre
        holder.tvUsername.text = usuario.username
        holder.tvEmail.text = usuario.email
    }

    // Devuelve el número total de elementos en el conjunto de datos
    override fun getItemCount(): Int {
        return listaUsuarios.size
    }

    // Método para filtrar la lista de usuarios
    fun filtrar(listaFiltrada: ArrayList<User>) {
        this.listaUsuarios = listaFiltrada
        notifyDataSetChanged()
    }

}
