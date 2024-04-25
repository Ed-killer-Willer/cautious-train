package com.example.flypack.user

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flypack.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Recovery_User : AppCompatActivity() {

    private lateinit var txt_Email: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var dbReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_recovery)

        txt_Email = findViewById(R.id.txt_user)
        auth = FirebaseAuth.getInstance()
        dbReference = FirebaseDatabase.getInstance().reference.child("User")
    }

    fun back_worker_session(view: View) {
        onBackPressed()
    }

    fun send(view: View) {
        val email = txt_Email.text.toString()

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        dbReference.orderByChild("Email").equalTo(email).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(this@Recovery_User) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this@Recovery_User,
                                    "Correo electrónico enviado correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                clearFields()
                                startActivity(Intent(this@Recovery_User, Session_User::class.java))
                            } else {
                                Toast.makeText(
                                    this@Recovery_User,
                                    "Error al enviar el correo electrónico",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        this@Recovery_User,
                        "No se encontró ningún usuario con este correo electrónico",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@Recovery_User,
                    "Error de base de datos: ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun clearFields() {
        txt_Email.text.clear()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, Session_User::class.java).apply {}
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
