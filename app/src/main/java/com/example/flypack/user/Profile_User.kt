package com.example.flypack.user

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flypack.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class Profile_User : AppCompatActivity() {

    private lateinit var txtName: TextView
    private lateinit var txtFirstName: TextView
    private lateinit var txtAge: TextView
    private lateinit var txtSex: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtUser: TextView
    private lateinit var txtSpinner2: TextView
    private lateinit var txtBoleto: TextView
    private lateinit var btnImage: TextView
    private lateinit var txtDescription: TextView
    private lateinit var txtUsername: TextView
    private var imageUrl: String? = null // Variable para almacenar la URL de la imagen

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbReference: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_user)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbReference = database.reference.child("User")

        txtName = findViewById(R.id.txt_name)
        txtFirstName = findViewById(R.id.txt_fisrt_name)
        txtAge = findViewById(R.id.txt_age)
        txtSex = findViewById(R.id.txt_sex)
        txtEmail = findViewById(R.id.txt_email)
        txtUser = findViewById(R.id.txt_user)
        txtSpinner2 = findViewById(R.id.spinner2)
        txtBoleto = findViewById(R.id.btn_Ticket)
        btnImage = findViewById(R.id.btn_image_package)
        txtDescription = findViewById(R.id.txt_description)
        txtUsername = findViewById(R.id.txt_Name_user)



        val currentUser: FirebaseUser? = auth.currentUser
        currentUser?.let { user ->
            val userRef: DatabaseReference = dbReference.child(user.uid)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val name: String? = dataSnapshot.child("Name").getValue(String::class.java)
                    val lastName: String? = dataSnapshot.child("LastName").getValue(String::class.java)
                    val age: String? = dataSnapshot.child("Age").getValue(String::class.java)
                    val gender: String? = dataSnapshot.child("Gender").getValue(String::class.java)
                    val email: String? = dataSnapshot.child("Email").getValue(String::class.java)
                    val username: String? = dataSnapshot.child("UserName").getValue(String::class.java)
                    val packageType: String? = dataSnapshot.child("Package").getValue(String::class.java)
                    val ticket: String? = dataSnapshot.child("Ticket").getValue(String::class.java)
                    val description: String? = dataSnapshot.child("Description").getValue(String::class.java)
                    imageUrl = dataSnapshot.child("ImageUrl").getValue(String::class.java)

                    txtName.text = name
                    txtFirstName.text = lastName
                    txtAge.text = age
                    txtSex.text = gender
                    txtEmail.text = email
                    txtUser.text = username
                    txtSpinner2.text = packageType
                    txtBoleto.text = ticket
                    txtDescription.text = description
                    txtEmail.text = email
                    txtUsername.text = username
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores de lectura de datos si es necesario
                }
            })
        }
    }



    fun open_image(view: View) {
        imageUrl?.let { url ->
            if (url.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } else {
                Toast.makeText(this, "No se encontró ninguna URL de imagen disponible", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun back_user_status(view: View) {
        onBackPressed()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, Status_User::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

}
