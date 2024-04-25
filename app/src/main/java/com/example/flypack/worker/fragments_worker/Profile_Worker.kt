package com.example.flypack.worker.fragments_worker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.flypack.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class Profile_Worker : Fragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile__worker, container, false)


        val backButton: ImageButton = view.findViewById(R.id.btn_back_user_status)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbReference = database.reference.child("Worker")

        txtworkertype = view.findViewById(R.id.txt_Name_user21)
        txtUser = view.findViewById(R.id.txt_user)
        txtName = view.findViewById(R.id.txt_name)
        txtFirstName = view.findViewById(R.id.txt_fisrt_name)
        txtAge = view.findViewById(R.id.txt_age)
        txtSex = view.findViewById(R.id.txt_sex)
        txtEmail = view.findViewById(R.id.txt_email)

        backButton.setOnClickListener {
            exitFragment()
        }

        val currentUser: FirebaseUser? = auth.currentUser
        currentUser?.let { user ->
            val userRef: DatabaseReference = dbReference.child(user.uid)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val workertype: String? = dataSnapshot.child("WorkerType").getValue(String::class.java)
                    val username: String? = dataSnapshot.child("UserName").getValue(String::class.java)
                    val name: String? = dataSnapshot.child("Name").getValue(String::class.java)
                    val firstName: String? = dataSnapshot.child("LastName").getValue(String::class.java)
                    val age: String? = dataSnapshot.child("Age").getValue(String::class.java)
                    val gender: String? = dataSnapshot.child("Gender").getValue(String::class.java)
                    val email: String? = dataSnapshot.child("Email").getValue(String::class.java)

                    txtworkertype.text =  workertype
                    txtUser.text = username
                    txtName.text = name
                    txtFirstName.text = firstName
                    txtAge.text = age
                    txtSex.text = gender
                    txtEmail.text = email
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                }
            })
        }
        return view

    }
    private fun exitFragment() {
        // Terminar la actividad actual (el fragmento)
        activity?.finish()
    }

}
