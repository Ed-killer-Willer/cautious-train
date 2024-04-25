package com.example.flypack.test

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.flypack.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Pruebas_Image : AppCompatActivity() {

    private lateinit var selectImageBtn: Button
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_activity_test)

        // Inicializar los elementos de diseño
        selectImageBtn = findViewById(R.id.selectImageBtn)

        // Configuración del listener del botón
        selectImageBtn.setOnClickListener {
            selectImage() // Función para seleccionar una imagen
        }
    }

    private fun uploadImage(imageUri: Uri) {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Subiendo imagen...") // Mostrar un diálogo de progreso mientras se carga la imagen
        progressDialog.setCancelable(false)
        progressDialog.show()

        val filename = generateFilename()

        // Obtener una referencia al almacenamiento de Firebase utilizando el nombre generado
        val storageReference = FirebaseStorage.getInstance().getReference(filename)

        // Subir la imagen al almacenamiento de Firebase
        storageReference.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // Obtener la URL de descarga de la imagen
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    saveImageUrl(imageUrl)
                }
            }
            .addOnFailureListener { exception ->
                showErrorMessage(exception.message)
            }
    }

    private fun saveImageUrl(imageUrl: String) {
        // Guardar la URL en la base de datos en tiempo real de Firebase
        val databaseReference = FirebaseDatabase.getInstance().getReference("Packages")
        val imageId = databaseReference.push().key
        val imageMap = HashMap<String, String>()
        imageMap["url"] = imageUrl
        imageId?.let {
            databaseReference.child(it).setValue(imageMap)
                .addOnSuccessListener {
                    showSuccessMessage()
                }
                .addOnFailureListener { exception ->
                    showErrorMessage(exception.message)
                }
        }
    }

    private fun generateFilename(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault())
        val currentDate = formatter.format(Date())
        return "Package_$currentDate" // Nombre de la imagen
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*" // Especificar que solo se pueden seleccionar imágenes
        startActivityForResult(intent, 100) // Iniciar la actividad para seleccionar una imagen
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val imageUri = data?.data // Obtener el URI de la imagen seleccionada
            imageUri?.let {
                uploadImage(it) // Subir la imagen seleccionada
            }
        }
    }

    private fun showSuccessMessage() {
        Snackbar.make(findViewById(android.R.id.content), "Imagen cargada correctamente", Snackbar.LENGTH_SHORT).show()
        progressDialog.dismiss() // Ocultar el diálogo de progreso
    }

    private fun showErrorMessage(message: String?) {
        Snackbar.make(findViewById(android.R.id.content), "Error al cargar la imagen: $message", Snackbar.LENGTH_SHORT).show()
        progressDialog.dismiss() // Ocultar el diálogo de progreso
    }
}

