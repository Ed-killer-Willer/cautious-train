package com.example.flypack.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.flypack.R
import com.example.flypack.all.User_All
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Main_Admin : AppCompatActivity (){

    //Variables obtenidas de Firebase
    private lateinit var txtUser: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbReference: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_main)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbReference = database.reference.child("Admin")

        // Referencias Username
        txtUser = findViewById(R.id.txt_Name_user)


        val currentUser: FirebaseUser? = auth.currentUser
        currentUser?.let { user ->
            val userRef: DatabaseReference = dbReference.child(user.uid)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val username: String? = dataSnapshot.child("UserName").getValue(String::class.java)
                    val salutation = "¡Hi $username!"
                    //Mostrar Username
                    txtUser.text = salutation
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores de lectura de datos si es necesario
                }
            })
        }


    }


    fun back_all_session (view: View){
        val intent= Intent(this, User_All::class.java).apply{}
        startActivity(intent)}

    fun go_to_admin_main_wadmin (view: View){
        val intent= Intent(this,Main_Wadmin::class.java).apply{}
        startActivity(intent)}

    fun go_to_admin_main_uadmin (view: View){
        val intent= Intent(this,Main_Uadmin::class.java).apply{}
        startActivity(intent)}

    }







