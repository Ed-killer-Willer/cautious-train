package com.example.flypack.admin

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flypack.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class Read_Wadmin : AppCompatActivity() {

    private lateinit var txtworkertype: TextView
    private lateinit var txtUser: TextView
    private lateinit var txtName: TextView
    private lateinit var txtFirstName: TextView
    private lateinit var txtAge: TextView
    private lateinit var txtSex: TextView
    private lateinit var txtEmail: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wadmin_read)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbReference = database.reference.child("Worker")

        txtworkertype = findViewById(R.id.txt_Name_user21)
        txtUser = findViewById(R.id.txt_user)
        txtName = findViewById(R.id.txt_name)
        txtFirstName = findViewById(R.id.txt_fisrt_name)
        txtAge = findViewById(R.id.txt_age)
        txtSex = findViewById(R.id.txt_sex)
        txtEmail = findViewById(R.id.txt_email)

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
                    val userName: String? = dataSnapshot.child("UserName").getValue(String::class.java)
                    val workerType: String? = dataSnapshot.child("WorkerType").getValue(String::class.java)

                    txtworkertype.text = workerType
                    txtUser.text = userName
                    txtName.text = name
                    txtFirstName.text = lastName
                    txtAge.text = age
                    txtSex.text = gender
                    txtEmail.text = email
                }



                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@Read_Wadmin, "Error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    fun back_user_status(view: android.view.View) {
        onBackPressed()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, Rud_Wadmin::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
