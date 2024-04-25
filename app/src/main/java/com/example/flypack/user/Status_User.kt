package com.example.flypack.user

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.flypack.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class Status_User : AppCompatActivity() {


    //Variables de QR


    private lateinit var qrContainer: ConstraintLayout
    private lateinit var qrImageView: ImageView
    private var isQRCodeVisible = true

    //Variables obtenidas de Firebase
    private lateinit var txtUser: TextView

    //Variables de Estado
    private lateinit var register: ImageView
    private lateinit var transport: ImageView
    private lateinit var security: ImageView
    private lateinit var approach: ImageView
    private lateinit var delivery: ImageView


    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbReference: DatabaseReference


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_status)

        // Sección para obtener el dato de QR

        //Fin del dato QR

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbReference = database.reference.child("User")


        // Referencias a QR
        qrImageView = findViewById(R.id.CodeQR)
        qrContainer = findViewById(R.id.qrContainer)

        // Referencias Username
        txtUser = findViewById(R.id.txt_Name_user)

        // Referencias Estado
        register = findViewById(R.id.Register)
        transport = findViewById(R.id.Transport)
        security = findViewById(R.id.Security)
        approach = findViewById(R.id.Approach)
        delivery = findViewById(R.id.Delivery)


        // Configurar OnClickListener para ocultar el código QR cuando se toque cualquier parte de la pantalla
        qrContainer.setOnClickListener {
            toggleQRCodeVisibility(it)
        }
        // Mostrar el código QR
        showQRCode()

        val currentUser: FirebaseUser? = auth.currentUser
        currentUser?.let { user ->
            val userRef: DatabaseReference = dbReference.child(user.uid)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val username: String? = dataSnapshot.child("UserName").getValue(String::class.java)

                    val RG: Boolean? = dataSnapshot.child("Registro").getValue(Boolean::class.java)
                    val TP: Boolean? = dataSnapshot.child("Transporte").getValue(Boolean::class.java)
                    val SG: Boolean? = dataSnapshot.child("Seguridad").getValue(Boolean::class.java)
                    val AP: Boolean? = dataSnapshot.child("Abordaje").getValue(Boolean::class.java)
                    val DV: Boolean? = dataSnapshot.child("Entrega").getValue(Boolean::class.java)

                    val salutation = "¡Hi $username!"


                    // Verificar el valor del Registro
                    val backgroundResourceRG = if (RG == true) {
                        R.drawable.circle_navy // Recurso de fondo cuando todas las variables están activadas
                    } else {
                        R.drawable.circle_grey // Recurso de fondo cuando al menos una de las variables está desactivada
                    }
                    // Aplicar el recurso de fondo al ImageView
                    register.setBackgroundResource(backgroundResourceRG)


                    // Verificar el valor del transporte
                    val backgroundResourceTP = if (TP == true) {
                        R.drawable.circle_navy // Recurso de fondo cuando todas las variables están activadas
                    } else {
                        R.drawable.circle_grey // Recurso de fondo cuando al menos una de las variables está desactivada
                    }
                    // Aplicar el recurso de fondo al ImageView
                    transport.setBackgroundResource(backgroundResourceTP)


                    // Verificar el valor del seguridad
                    val backgroundResourceSG = if (SG == true) {
                        R.drawable.circle_navy // Recurso de fondo cuando todas las variables están activadas
                    } else {
                        R.drawable.circle_grey // Recurso de fondo cuando al menos una de las variables está desactivada
                    }
                    // Aplicar el recurso de fondo al ImageView
                    security.setBackgroundResource(backgroundResourceSG)


                    // Verificar el valor del abordaje
                    val backgroundResourceAP = if (AP == true) {
                        R.drawable.circle_navy // Recurso de fondo cuando todas las variables están activadas
                    } else {
                        R.drawable.circle_grey // Recurso de fondo cuando al menos una de las variables está desactivada
                    }
                    // Aplicar el recurso de fondo al ImageView
                    approach.setBackgroundResource(backgroundResourceAP)


                    // Verificar el valor del recibido
                    val backgroundResourceDV = if (DV == true) {
                        R.drawable.circle_navy // Recurso de fondo cuando todas las variables están activadas
                    } else {
                        R.drawable.circle_grey // Recurso de fondo cuando al menos una de las variables está desactivada
                    }
                    // Aplicar el recurso de fondo al ImageView
                    delivery.setBackgroundResource(backgroundResourceDV)


                    //Mostrar Username
                    txtUser.text = salutation


                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores de lectura de datos si es necesario
                }
            })
        }


    }



    // Método para mostrar u ocultar el código QR
    fun toggleQRCodeVisibility(view: View) {
        if (isQRCodeVisible) {
            qrContainer.visibility = View.GONE
            qrImageView.visibility = View.GONE
            isQRCodeVisible = false
        } else {
            qrContainer.visibility = View.VISIBLE
            qrImageView.visibility = View.VISIBLE
            isQRCodeVisible = true
        }

    }


    fun descargar(view: View) {
        download()
    }

    private fun download() {
        val currentUser: FirebaseUser? = auth.currentUser
        val userId = currentUser?.uid

        userId?.let {
            dbReference.child(it).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userId = snapshot.key
                    userId?.let {
                        // Generar el código QR con el ID de usuario
                        val barcodeEncoder = BarcodeEncoder()
                        val bitmap: Bitmap = barcodeEncoder.encodeBitmap(userId, BarcodeFormat.QR_CODE, 750, 750)

                        // Generar un nombre único para la imagen
                        val fileName = generateUniqueFileName()

                        // Guardar la imagen en la carpeta "Descargas" del almacenamiento externo público
                        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        val file = File(storageDir, fileName)
                        try {
                            val outputStream = FileOutputStream(file)
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            outputStream.close()
                            mostrarToast("Descarga exitosa.")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            mostrarToast("Error en la descarga")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar el error
                    mostrarToast("Error en la descarga")
                }
            })
        }
    }

    // Función para generar un nombre único para la imagen
    private fun generateUniqueFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(java.util.Date())
        return "qr_code_image_$timestamp.png"
    }

    // Método para mostrar un Toast
    private fun mostrarToast(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    // Método para mostrar el código QR del ID de usuario
    private fun showQRCode() {
        val currentUser: FirebaseUser? = auth.currentUser
        val userId = currentUser?.uid

        userId?.let {
            // Obtener el ID de usuario de la base de datos
            dbReference.child(it).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Generar el código QR con el ID de usuario
                    val userId = snapshot.key
                    userId?.let {
                        generateQRCode(it)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar el error
                }
            })
        }
    }

    // Método para generar el código QR
    private fun generateQRCode(userId: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(userId, BarcodeFormat.QR_CODE, 750, 750)

            // Mostrar el código QR en el ImageView
            qrImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun back_user_session (view: View){
        onBackPressed()}
    fun go_user_profile (view: View){
        val intent= Intent(this,Profile_User::class.java).apply{}
        startActivity(intent)}

    override fun onBackPressed() {
        super.onBackPressed()
        val intent= Intent(this,Session_User::class.java).apply{}
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

}

