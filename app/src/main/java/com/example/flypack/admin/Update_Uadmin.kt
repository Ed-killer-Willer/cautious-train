package com.example.flypack.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.flypack.R
import com.example.flypack.user.Session_User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Update_Uadmin : AppCompatActivity() {

    private lateinit var checkBox: CheckBox
    private lateinit var button: Button
    private lateinit var btnDelete: Button

    // Declarar las referencias a los elementos de la interfaz de usuario
    private lateinit var txtName: EditText
    private lateinit var txt_FirstName: EditText
    private lateinit var txtAge: EditText
    private lateinit var Spinner1: Spinner
    private lateinit var txtUser: EditText
    private lateinit var Spinner2: Spinner
    private lateinit var btnTickete: EditText
    private lateinit var txtDescription: EditText
    private lateinit var btnImagePackage: Button

    // Declarar la referencia a la base de datos
    private lateinit var database: FirebaseDatabase
    private lateinit var dbReference: DatabaseReference

    private var selectedImageUri: Uri? = null // Variable para almacenar la URI de la imagen seleccionada

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.uadmin_update)

        checkBox = findViewById(R.id.checkBox)
        button = findViewById(R.id.button)
        btnDelete = findViewById(R.id.delete)


        // Inicializar Firebase
        database = FirebaseDatabase.getInstance()
        dbReference = database.reference.child("User")

        // Obtener la referencia de Firebase Auth
        val auth = FirebaseAuth.getInstance()
        val currentUserID = auth.currentUser?.uid

        // Obtener la referencia a los campos de la interfaz de usuario
        txtName = findViewById(R.id.txt_name)
        txt_FirstName = findViewById(R.id.txt_fisrt_name)
        txtAge = findViewById(R.id.txt_age)
        Spinner1 = findViewById(R.id.spinner1)
        txtUser = findViewById(R.id.txt_user)
        Spinner2 = findViewById(R.id.spinner2)
        btnTickete = findViewById(R.id.btn_Ticket)
        txtDescription = findViewById(R.id.txt_description)
        btnImagePackage = findViewById(R.id.btn_image_package)


        // Deshabilitar el botón de registro al inicio
        button.isEnabled = false
        btnDelete.isEnabled = false


        // Listener del CheckBox para habilitar/deshabilitar el botón de registro
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            button.isEnabled = isChecked
            btnDelete.isEnabled = isChecked
        }

        // Adaptador para el Spinner de Sexo
        val spinnerSexAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.spinner_sex_values,
            R.layout.style_spinner_sex
        )
        spinnerSexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        Spinner1.adapter = spinnerSexAdapter

        // Adaptador para el Spinner de Paquete
        val spinnerPackageAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.spinner_package_values,
            R.layout.style_spinner_package
        )
        spinnerPackageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        Spinner2.adapter = spinnerPackageAdapter

        btnImagePackage.setOnClickListener {
            // Abre el gestor de archivos del teléfono
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*" // Esto especifica que solo se seleccionen imágenes
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Obtener los datos del usuario actual de la base de datos
        currentUserID?.let { userID ->
            dbReference.child(userID).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Obtener los valores de los campos del usuario desde la base de datos
                        val name = snapshot.child("Name").getValue(String::class.java)
                        val firstname = snapshot.child("LastName").getValue(String::class.java)
                        val age = snapshot.child("Age").getValue(String::class.java)
                        val spinn1 = snapshot.child("Gender").getValue(String::class.java)
                        val user = snapshot.child("UserName").getValue(String::class.java)
                        val spinn2 = snapshot.child("Package").getValue(String::class.java)
                        val ticket = snapshot.child("Ticket").getValue(String::class.java)
                        val description = snapshot.child("Description").getValue(String::class.java)
                        val urlimage = snapshot.child("ImageUrl").getValue(String::class.java)

                        // Actualizar los campos de la interfaz de usuario con los valores obtenidos
                        txtName.setText(name)
                        txt_FirstName.setText(firstname)
                        txtAge.setText(age)
                        spinn1?.let { Spinner1.setSelection(getIndex(Spinner1, it)) }
                        txtUser.setText(user)
                        spinn2?.let { Spinner2.setSelection(getIndex(Spinner2, it)) }
                        btnTickete.setText(ticket)
                        txtDescription.setText(description)
                        urlimage?.let { btnImagePackage.setText(it) }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar el error
                }
            })
        }

        // Agrega el resto de tu lógica aquí, como la actualización de los datos en la base de datos cuando se presiona el botón.
        button.setOnClickListener {
            updateData()
        }

        btnDelete.setOnClickListener {
            deleteCurrentUser(currentUserID)
        }
    }



    private val PICK_IMAGE_REQUEST = 71

    // Sobrescribe el método onActivityResult para manejar la respuesta de la selección de imagen
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            // Obtén la URI de la imagen seleccionada
            selectedImageUri = data.data

            // Llama a la función para cargar la imagen en Firebase Storage
            uploadImage()
        }
    }

    private fun uploadImage() {
        // Verificar si hay una imagen seleccionada
        selectedImageUri?.let { uri ->
            val filename = generateFilename()

            val storageReference = FirebaseStorage.getInstance().getReference("packages/$filename")

            storageReference.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    // Imagen cargada exitosamente, obtenemos la URL de descarga
                    storageReference.downloadUrl.addOnSuccessListener { url ->
                        val imageUrl = url.toString()
                        saveUserDetails(imageUrl) // Guardar la URL de la imagen en la base de datos
                    }
                }
                .addOnFailureListener { exception ->
                    showErrorMessage(exception.message)
                }
        } ?: run {
            showErrorMessage("No se seleccionó ninguna imagen")
        }
    }

    private fun generateFilename(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault())
        val currentDate = formatter.format(Date())
        return "Package_$currentDate"
    }

    private fun saveUserDetails(imageUrl: String) {
        // Obtener el ID del usuario actual
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid

        currentUserID?.let { userID ->
            // Obtener una referencia al nodo del usuario en la base de datos
            val userReference = dbReference.child(userID)

            // Actualizar la URL de la imagen del usuario en la base de datos
            userReference.child("ImageUrl").setValue(imageUrl)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Imagen actualizada", Toast.LENGTH_SHORT).show()
                        // La URL de la imagen se almacenó correctamente en la base de datos
                        // Puedes realizar alguna acción adicional si es necesario
                    } else {
                        // Ocurrió un error al almacenar la URL de la imagen en la base de datos
                        // Puedes mostrar un mensaje de error o realizar alguna acción de manejo de errores
                        val errorMessage = task.exception?.message ?: "Error desconocido al guardar los detalles del usuario"
                        showErrorMessage(errorMessage)
                    }
                }
        }
    }

    private fun showErrorMessage(message: String?) {
        Snackbar.make(findViewById(android.R.id.content), "Error al cargar la imagen: $message", Snackbar.LENGTH_LONG).show()
    }

    private fun updateData() {
        // Obtener los nuevos valores de los campos de la interfaz de usuario
        val newName = txtName.text.toString()
        val newFirstName = txt_FirstName.text.toString()
        val newAge = txtAge.text.toString()
        val newGender = Spinner1.selectedItem.toString()
        val newUser = txtUser.text.toString()
        val newPackage = Spinner2.selectedItem.toString()
        val newTicket = btnTickete.text.toString()
        val newDescription = txtDescription.text.toString()

        //Restricciones de update

        // Verificar la cantidad de caracteres del boleto
        if (newTicket.length != 6) {
            Toast.makeText(this, "El boleto debe tener 6 caracteres", Toast.LENGTH_LONG).show()
            return
        }

        // Verificar que el username no supere los 13 caracteres
        if (newUser.length > 10) {
            Toast.makeText(this, "El nombre de usuario no puede tener más de 10 caracteres", Toast.LENGTH_LONG).show()
            return
        }

        // Verificar que la descripción no sea mayor a 28 caracteres
        if (newDescription.length > 28) {
            Toast.makeText(this, "La descripción no puede tener más de 28 caracteres", Toast.LENGTH_LONG).show()
            return
        }

        // Verificar que la edad sea mayor o igual a 18
        val userAge = newAge.toIntOrNull()
        if (userAge == null || userAge < 18) {
            Toast.makeText(this, "La edad debe ser mayor o igual a 18", Toast.LENGTH_LONG).show()
            return
        }


        // Obtener la referencia al usuario actual de la base de datos
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid

        currentUserID?.let { userID ->
            // Verificar si hay una imagen seleccionada
            selectedImageUri?.let { uri ->
                val filename = generateFilename()

                val storageReference = FirebaseStorage.getInstance().getReference("packages/$filename")

                storageReference.putFile(uri)
                    .addOnSuccessListener { taskSnapshot ->
                        // Imagen cargada exitosamente, obtenemos la URL de descarga
                        storageReference.downloadUrl.addOnSuccessListener { url ->
                            val imageUrl = url.toString()

                            // Crear un mapa de datos para almacenar los nuevos valores
                            val updatesMap = HashMap<String, Any>()
                            updatesMap["Name"] = newName
                            updatesMap["LastName"] = newFirstName
                            updatesMap["Age"] = newAge
                            updatesMap["Gender"] = newGender
                            updatesMap["UserName"] = newUser
                            updatesMap["Package"] = newPackage
                            updatesMap["Ticket"] = newTicket
                            updatesMap["Description"] = newDescription
                            updatesMap["ImageUrl"] = imageUrl // Guardar la URL de la imagen

                            // Actualizar los valores en la base de datos
                            val userReference = dbReference.child(userID)
                            userReference.updateChildren(updatesMap)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // Los datos se actualizaron correctamente
                                        // Mostrar un mensaje de éxito o realizar alguna acción adicional
                                        Snackbar.make(findViewById(android.R.id.content), "Datos actualizados correctamente", Snackbar.LENGTH_SHORT).show()
                                    } else {
                                        // Ocurrió un error al actualizar los datos
                                        // Mostrar un mensaje de error o realizar alguna acción de manejo de errores
                                        val errorMessage = task.exception?.message ?: "Error desconocido al actualizar los datos"
                                        Snackbar.make(findViewById(android.R.id.content), "Error al actualizar los datos: $errorMessage", Snackbar.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        showErrorMessage(exception.message)
                    }
            } ?: run {
                // No se seleccionó ninguna imagen, actualizar solo los otros campos
                val updatesMap = HashMap<String, Any>()
                updatesMap["Name"] = newName
                updatesMap["LastName"] = newFirstName
                updatesMap["Age"] = newAge
                updatesMap["Gender"] = newGender
                updatesMap["UserName"] = newUser
                updatesMap["Package"] = newPackage
                updatesMap["Ticket"] = newTicket
                updatesMap["Description"] = newDescription

                // Actualizar los valores en la base de datos
                val userReference = dbReference.child(userID)
                userReference.updateChildren(updatesMap)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Los datos se actualizaron correctamente
                            // Mostrar un mensaje de éxito o realizar alguna acción adicional
                            Snackbar.make(findViewById(android.R.id.content), "Datos actualizados correctamente", Snackbar.LENGTH_SHORT).show()
                        } else {
                            // Ocurrió un error al actualizar los datos
                            // Mostrar un mensaje de error o realizar alguna acción de manejo de errores
                            val errorMessage = task.exception?.message ?: "Error desconocido al actualizar los datos"
                            Snackbar.make(findViewById(android.R.id.content), "Error al actualizar los datos: $errorMessage", Snackbar.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }


    // Método para eliminar el usuario actual
    private fun deleteCurrentUser(currentUserID: String?) {
        currentUserID?.let { userID ->
            // Eliminar el usuario de la base de datos
            dbReference.child(userID).removeValue()
                .addOnSuccessListener {
                    // Mostrar un mensaje de éxito
                    Snackbar.make(findViewById(android.R.id.content), "Usuario eliminado correctamente", Snackbar.LENGTH_SHORT).show()
                    // Redirigir a la pantalla de inicio o realizar alguna otra acción necesaria
                    finish()

                    // Después de eliminar al usuario de la base de datos, eliminar la autenticación
                    deleteFirebaseAuthUser()
                }
                .addOnFailureListener { exception ->
                    // Mostrar un mensaje de error en caso de fallo
                    Snackbar.make(findViewById(android.R.id.content), "Error al eliminar el usuario: ${exception.message}", Snackbar.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteFirebaseAuthUser() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // El usuario se eliminó exitosamente
                    Snackbar.make(findViewById(android.R.id.content), "Usuario eliminado correctamente", Snackbar.LENGTH_SHORT).show()
                    // Realiza alguna acción adicional si es necesario
                } else {
                    // Ocurrió un error al eliminar al usuario
                    val errorMessage = task.exception?.message ?: "Error desconocido al eliminar el usuario"
                    // Muestra un mensaje de error o realiza alguna acción de manejo de errores
                }
            }
    }

    // Método para obtener el índice de un elemento en un Spinner
    private fun getIndex(spinner: Spinner, value: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == value) {
                return i
            }
        }
        return 0
    }

    fun back_user_status(view: View) {
        onBackPressed()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, Rud_Uadmin::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

}