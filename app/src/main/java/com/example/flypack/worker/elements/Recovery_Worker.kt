    package com.example.flypack.worker.elements

    import android.content.Intent
    import android.os.Bundle
    import android.text.TextUtils
    import android.view.View
    import android.widget.EditText
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import com.example.flypack.R
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.database.DataSnapshot
    import com.google.firebase.database.DatabaseError
    import com.google.firebase.database.DatabaseReference
    import com.google.firebase.database.FirebaseDatabase
    import com.google.firebase.database.ValueEventListener

    class Recovery_Worker : AppCompatActivity() {

        private lateinit var txt_Email: EditText
        private lateinit var auth: FirebaseAuth
        private lateinit var dbReference: DatabaseReference

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.worker_recovery)

            txt_Email = findViewById(R.id.txt_user)
            auth = FirebaseAuth.getInstance()
            dbReference = FirebaseDatabase.getInstance().reference.child("Worker")
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
                            .addOnCompleteListener(this@Recovery_Worker) { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this@Recovery_Worker,
                                        "Correo electrónico enviado correctamente",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    clearFields()
                                    startActivity(Intent(this@Recovery_Worker, Session_Worker::class.java))
                                } else {
                                    Toast.makeText(
                                        this@Recovery_Worker,
                                        "Error al enviar el correo electrónico",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(
                            this@Recovery_Worker,
                            "No se encontró ningún usuario con este correo electrónico",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@Recovery_Worker,
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
            val intent = Intent(this, Session_Worker::class.java).apply {}
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
