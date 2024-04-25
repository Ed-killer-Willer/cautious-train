package com.example.flypack.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flypack.R
import com.example.flypack.admin.Rud_Wadmin
import com.example.flypack.userssearch.Worker

class Adapter_Worker(
    private var listaUsuarios: ArrayList<Worker>,
    private val listener: Rud_Wadmin // Aquí se define el listener como la actividad Rud_Wadmin
): RecyclerView.Adapter<Adapter_Worker.ViewHolder>() {

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        // Referencias a los elementos de la interfaz de usuario
        val tvNombre: TextView = itemView.findViewById(R.id.NombreUsuario)
        val tvUsername: TextView = itemView.findViewById(R.id.Username)
        val tvEmail: TextView = itemView.findViewById(R.id.Email)
        val profileIconW: ImageView = itemView.findViewById(R.id.profile)
        val editIconW: ImageView = itemView.findViewById(R.id.edit)


        init {
            // Configura el listener para el clic en el ícono de perfil
            profileIconW.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Cuando se hace clic en el ícono de perfil, se llama al método onItemClick de la actividad Rud_Uadmin
                    listener.onItemClick(position)
                }
            }
        }

        init {
            // Configura el listener para el clic en el ícono de perfil
            editIconW.setOnClickListener {
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
    fun filtrar(listaFiltrada: ArrayList<Worker>) {
        this.listaUsuarios = listaFiltrada
        notifyDataSetChanged()
    }

}
