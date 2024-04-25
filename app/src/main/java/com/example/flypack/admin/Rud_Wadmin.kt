package com.example.flypack.admin

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flypack.R
import com.example.flypack.adapters.Adapter_Worker
import com.example.flypack.databinding.WadminRudBinding
import com.example.flypack.userssearch.Worker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Rud_Wadmin : AppCompatActivity() {

    //Variables obtenidas de Firebase
    private lateinit var txtUser: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbReference: DatabaseReference

    // Declaración de variables
    private lateinit var binding: WadminRudBinding
    private lateinit var adaptador: Adapter_Worker
    private lateinit var dbref: DatabaseReference
    private var listaUsuarios = ArrayList<Worker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WadminRudBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbReference = database.reference.child("Admin")

        // Referencias Username
        txtUser = findViewById(R.id.hi_adm)


        val currentUser: FirebaseUser? = auth.currentUser
        currentUser?.let { user ->
            val userRef: DatabaseReference = dbReference.child(user.uid)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val username: String? = dataSnapshot.child("UserName").getValue(String::class.java)
                    val salutation = "¡Hi $username Admin!"
                    //Mostrar Username
                    txtUser.text = salutation
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores de lectura de datos si es necesario
                }
            })
        }


        // Inicialización del adaptador y configuración del RecyclerView
        adaptador = Adapter_Worker(listaUsuarios,this)
        binding.rvListaW.adapter = adaptador

        // Referencia a la base de datos Firebase
        dbref = FirebaseDatabase.getInstance().getReference("Worker")

        // Configuración del RecyclerView y búsqueda de usuarios
        setupRecyclerView()
        buscarUsuarios()

        // Escucha los cambios en el texto del EditText de búsqueda
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun afterTextChanged(p0: Editable?) {
                filtrar(p0.toString())
            }
        })
    }

    // Método para buscar usuarios en la base de datos Firebase
    private fun buscarUsuarios() {
        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaUsuarios.clear()
                for (userSnapshot in snapshot.children) {
                    val nombre = userSnapshot.child("Name").getValue(String::class.java)
                    val cargo = userSnapshot.child("WorkerType").getValue(String::class.java)
                    val email = userSnapshot.child("Email").getValue(String::class.java)

                    if (nombre != null) {
                        listaUsuarios.add(Worker(nombre, cargo ?: "", email))
                    }
                }
                adaptador.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error aquí
            }
        })
    }

    // Método para configurar el RecyclerView
    private fun setupRecyclerView() {
        binding.rvListaW.layoutManager = LinearLayoutManager(this)
        adaptador = Adapter_Worker(listaUsuarios,this)
        binding.rvListaW.adapter = adaptador
    }

    // Método para filtrar la lista de usuarios
    private fun filtrar(texto: String) {
        val listaFiltrada = ArrayList<Worker>()
        for (usuario in listaUsuarios) {
            if (usuario.nombre.toLowerCase().contains(texto.toLowerCase()) ||
                usuario.username.toLowerCase().contains(texto.toLowerCase()) ||
                (usuario.email != null && usuario.email!!.toLowerCase().contains(texto.toLowerCase()))) {
                listaFiltrada.add(usuario)
            }
        }
        adaptador.filtrar(listaFiltrada)
    }

    fun admin_back_to_main_uadmin(view: View) {
        onBackPressed()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, Main_Wadmin::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    // Método que se llama cuando se hace clic en un elemento del RecyclerView
    fun onItemClick(position: Int) {
        val usuario = listaUsuarios[position]
        val intent = Intent(this, Read_Wadmin::class.java)
        intent.putExtra("usuario", usuario)
        startActivity(intent)

    }


    // Método que se llama cuando se hace clic en un elemento del RecyclerView
    fun onItemClicks(position: Int) {
        val usuario = listaUsuarios[position]
        val intent = Intent(this, Update_Wadmin::class.java)
        intent.putExtra("usuario", usuario)
        startActivity(intent)
    }
}
