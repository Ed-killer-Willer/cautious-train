package com.example.flypack.worker.elements

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.flypack.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator

private const val REQUEST_CODE_PERMISSIONS = 101

class Scan : AppCompatActivity() {

    // Variable para almacenar el tipo de trabajador
    private var workerType: String? = null

    // Constantes que representan el orden de escaneo
    private val STEPS_ORDER = listOf("Registro", "Transporte", "Seguridad", "Abordaje", "Entrega")

    // Variable para almacenar el índice del paso actual
    private var currentStepIndex = 0


    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var Qr: ImageView
    private lateinit var profileWorker: ImageView

    private lateinit var dbReference: DatabaseReference
    private lateinit var textView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_scan__worker)

        // Initialize Firebase Auth
        auth = Firebase.auth
        // Initialize Firebase Database
        databaseReference = Firebase.database.reference
        dbReference = Firebase.database.reference.child("Worker") // Inicializar dbReference con la referencia correcta

        Qr = findViewById(R.id.imageQR)
        textView = findViewById(R.id.typeWorker)
        profileWorker = findViewById(R.id.profile_worker)

        // Solicitar permisos de cámara al iniciar la actividad
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CODE_PERMISSIONS
            )
        }

        // Agregar el listener de clics a la imagen Qr

        Qr.setOnClickListener {
            // Iniciar la cámara para escanear el código QR
            startScan()


        }


        // Obtener el tipo de trabajador y asignarlo a la vista
        val currentUser: FirebaseUser? = auth.currentUser
        currentUser?.let { user ->
            val userRef: DatabaseReference = dbReference.child(user.uid)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    workerType = dataSnapshot.child("WorkerType").getValue(String::class.java)
                    val salutation = "$workerType"
                    textView.text = salutation
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores de lectura de datos si es necesario
                }
            })
        }
    }

    private fun checkIfUserScannedNextStep(nextStep: String) {
        val currentUser: FirebaseUser? = auth.currentUser
        currentUser?.let { user ->
            val userRef: DatabaseReference = dbReference.child(user.uid)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    workerType = dataSnapshot.child("WorkerType").getValue(String::class.java)
                    // Verificar si el tipo de trabajador coincide con el siguiente paso en el proceso de escaneo
                    if (workerType == STEPS_ORDER[currentStepIndex]) {
                        // No necesitas hacer nada aquí, el escaneo se inició desde el listener de clics de la imagen QR
                    } else {
                        // Mostrar un mensaje indicando que el usuario debe escanear el siguiente paso
                        val nextStep = STEPS_ORDER[currentStepIndex]
                        Toast.makeText(this@Scan, "Por favor, escanea el código QR de $nextStep", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores de lectura de datos si es necesario
                }
            })
        }
    }

    private fun sendQRContentToDatabase(qrContent: String) {
        try {
            // Verificar si el contenido del código QR es un UID válido
            if (isValidUid(qrContent)) {
                val userRef = databaseReference.child("User").child(qrContent)

                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        if (dataSnapshot.exists()) {
                            // El usuario está registrado en la base de datos
                            val userName = dataSnapshot.child("UserName").getValue(String::class.java)
                            // Mostrar el nombre de usuario en el Toast
                            Toast.makeText(this@Scan, "Usuario escaneado: $userName", Toast.LENGTH_SHORT).show()
                            // Verificar si el UID ya ha sido escaneado por este tipo de trabajador
                            val scannedByWorker = dataSnapshot.child(workerType ?: "").getValue(Boolean::class.java)
                            if (scannedByWorker == true) {
                                // El UID ya ha sido escaneado por este tipo de trabajador
                                Toast.makeText(this@Scan, "Este usuario ya ha sido escaneado por un $workerType", Toast.LENGTH_SHORT).show()
                            } else {
                                // El UID no ha sido escaneado por este tipo de trabajador, agregar el valor
                                dataSnapshot.ref.child(workerType ?: "").setValue(true)
                                Toast.makeText(this@Scan, "Se agregó el dato al usuario existente", Toast.LENGTH_SHORT).show()

                                // Verificar si el tipo de trabajador coincide con el siguiente paso en el proceso de escaneo
                                if (workerType == STEPS_ORDER[currentStepIndex]) {
                                    // Avanzar al siguiente paso si coincide
                                    currentStepIndex++
                                    if (currentStepIndex < STEPS_ORDER.size) {
                                        // Mostrar un mensaje indicando el siguiente paso
                                        val nextStep = STEPS_ORDER[currentStepIndex]
                                        checkIfUserScannedNextStep(nextStep)
                                    } else {
                                        // Se ha completado el proceso de escaneo
                                        Toast.makeText(this@Scan, "Proceso de escaneo completado", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    // Mostrar un mensaje indicando que el usuario debe escanear el siguiente paso
                                    val nextStep = STEPS_ORDER[currentStepIndex]
                                    checkIfUserScannedNextStep(nextStep)
                                }
                            }
                        } else {
                            // El usuario no está registrado en la base de datos
                            Snackbar.make(findViewById(android.R.id.content), "El usuario no está registrado", Snackbar.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Manejar errores de lectura de datos si es necesario
                        Snackbar.make(findViewById(android.R.id.content), "Error al obtener datos del usuario", Snackbar.LENGTH_SHORT).show()
                    }
                })
            } else {
                // El contenido del código QR no es válido
                Snackbar.make(findViewById(android.R.id.content), "Contenido de código QR no válido", Snackbar.LENGTH_SHORT).show()
            }
        } catch (e: DatabaseException) {
            // Manejar la excepción de Firebase Database
            Log.e("DatabaseException", "Error en la base de datos: ${e.message}")
            Snackbar.make(findViewById(android.R.id.content), "Usuario Inexistente", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun startScan() {
        IntentIntegrator(this)
            .setBeepEnabled(false)
            .initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("Status_User", "Actividad activa: ${javaClass.simpleName}")

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                // Mostrar Snackbar con mensaje de escaneo cancelado en la parte inferior
                Snackbar.make(findViewById(android.R.id.content), "Escaneo cancelado", Snackbar.LENGTH_SHORT).show()
            } else {
                if (result.contents.isNotEmpty()) {
                    // El contenido del código QR es válido, enviarlo a Firebase Realtime Database
                    sendQRContentToDatabase(result.contents)
                } else {
                    // El contenido del código QR no es válido
                    Snackbar.make(findViewById(android.R.id.content), "Contenido de código QR no válido", Snackbar.LENGTH_SHORT).show()
                }
            }
        } else {
            // Mostrar Snackbar indicando que ocurrió un error al escanear el código QR
            Snackbar.make(findViewById(android.R.id.content), "Error al escanear el código QR", Snackbar.LENGTH_SHORT).show()
        }
    }

    fun go_worker_profile (view: View){
        val intent= Intent(this, Profile::class.java).apply{}
        startActivity(intent)
        finish()
    }

    fun back_worker_session (view: View){
        val intent= Intent(this, Session_Worker::class.java).apply{}
        startActivity(intent)
        finish()
    }

    private fun isValidUid(uid: String): Boolean {
        // Puedes implementar aquí la lógica para validar si el UID es válido
        // Por ejemplo, puedes verificar si tiene la longitud adecuada o si sigue un formato específico
        // Aquí tienes un ejemplo simple que verifica si la longitud del UID es mayor que 0
        return uid.isNotEmpty()
    }
}
