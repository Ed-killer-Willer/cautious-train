package com.example.flypack.user


import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.flypack.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class Create_User : AppCompatActivity() {

    //Declarations of variables

    private lateinit var txt_Name: EditText
    private lateinit var txt_Last_Name: EditText
    private lateinit var txt_Age: EditText
    private lateinit var spinner1: Spinner
    private lateinit var txt_Email: EditText
    private lateinit var txt_User: EditText
    private lateinit var txt_Password: EditText
    private lateinit var txt_Password_Repeat: EditText
    private lateinit var spinner2: Spinner
    private lateinit var txt_Ticket: EditText
    private lateinit var txt_Description: EditText
    private lateinit var checkBox: CheckBox
    private lateinit var button: Button
    private lateinit var btn_Image_Package: Button

    private lateinit var progressDialog: ProgressDialog

    //Variables de Firebase
    private lateinit var dbReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    private var selectedImageUri: Uri? = null // Variable para almacenar la URI de la imagen seleccionada

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_create)

        //Inicializar Firebase
        FirebaseApp.initializeApp(this)

        // Asociación de variables a elementos de la interfaz
        txt_Name = findViewById(R.id.txt_name)
        txt_Last_Name = findViewById(R.id.txt_last_name)
        txt_Age = findViewById(R.id.txt_age)
        spinner1 = findViewById(R.id.spinner1)
        txt_Email = findViewById(R.id.txt_email)
        txt_User = findViewById(R.id.txt_user)
        txt_Password = findViewById(R.id.txt_password)
        txt_Password_Repeat = findViewById(R.id.txt_password_repeat)
        spinner2 = findViewById(R.id.spinner2)
        txt_Ticket = findViewById(R.id.txt_ticket)
        btn_Image_Package = findViewById(R.id.botonSeleccionarImagen)
        txt_Description = findViewById(R.id.txt_description)
        checkBox = findViewById(R.id.checkBox)
        button = findViewById(R.id.button)

        // Deshabilitar el botón de registro al inicio
        button.isEnabled = false

        // Listener del CheckBox para habilitar/deshabilitar el botón de registro
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            button.isEnabled = isChecked
        }

        // Adaptador para el Spinner de Sexo
        val spinnerSexAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.spinner_sex_values,
            R.layout.style_spinner_sex
        )
        spinnerSexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner1.adapter = spinnerSexAdapter

        // Adaptador para el Spinner de Paquete
        val spinnerPackageAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.spinner_package_values,
            R.layout.style_spinner_package
        )
        spinnerPackageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner2.adapter = spinnerPackageAdapter

        // Inicialización de Firebase Auth y Database
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        dbReference = database.reference.child("User")

        // Configuración del listener del botón de selección de imagen
        btn_Image_Package = findViewById(R.id.botonSeleccionarImagen)
        btn_Image_Package.setOnClickListener {
            selectImage() // Función para seleccionar una imagen
        }

        // Configuración del listener del botón de registro
        button.setOnClickListener {
            register()
        }
    }

    private fun uploadImage() {
        // Verificar si hay una imagen seleccionada
        selectedImageUri?.let { uri ->
            val filename = generateFilename()

            val storageReference = FirebaseStorage.getInstance().getReference("packages/$filename") // Se crea la carpeta "packages" en Firebase Storage

            storageReference.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { url ->
                        val imageUrl = url.toString()
                        saveUserDetails(imageUrl) // Guardar la URL de la imagen junto con los detalles del usuario
                        progressDialog.dismiss() // Cerrar el ProgressDialog después de completar el registro

                    }
                }
                .addOnFailureListener { exception ->
                    showErrorMessage(exception.message)
                    progressDialog.dismiss() // Cerrar el ProgressDialog después de completar el registro

                }
        } ?: run {
            // Si no hay imagen seleccionada, mostrar mensaje sobre campos vacíos
            if (areFieldsEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), "Por favor complete todos los campos", Snackbar.LENGTH_SHORT).show()
            } else {
                // Si hay campos completos pero falta la imagen, mostrar mensaje sobre la imagen faltante
                Snackbar.make(findViewById(android.R.id.content), "Por favor seleccione una imagen", Snackbar.LENGTH_SHORT).show()
            }
            progressDialog.dismiss()
        }
    }




    private fun saveUserDetails(imageUrl: String) {

        val name = txt_Name.text.toString()
        val lastName = txt_Last_Name.text.toString()
        val age = txt_Age.text.toString()
        val gender = spinner1.selectedItem.toString()
        val email = txt_Email.text.toString()
        val username = txt_User.text.toString()
        val password1 = txt_Password.text.toString()
        val password2 = txt_Password_Repeat.text.toString()
        val packageType = spinner2.selectedItem.toString()
        val ticket = txt_Ticket.text.toString()
        val description = txt_Description.text.toString()

        // Verificar que ningún campo esté vacío
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(age) || TextUtils.isEmpty(gender) ||
            TextUtils.isEmpty(email) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password1) || TextUtils.isEmpty(password2) ||
            TextUtils.isEmpty(packageType) ||TextUtils.isEmpty(ticket) || TextUtils.isEmpty(description)
        ) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar la cantidad de caracteres del boleto
        if (ticket.length != 6) {
            Toast.makeText(this, "El boleto debe tener 6 caracteres", Toast.LENGTH_LONG).show()
            return
        }

        // Verificar que el username no supere los 13 caracteres
        if (username.length > 10) {
            Toast.makeText(this, "El nombre de usuario no puede tener más de 13 caracteres", Toast.LENGTH_LONG).show()
            return
        }

        // Verificar que la descripción no sea mayor a 28 caracteres
        if (description.length > 28) {
            Toast.makeText(this, "La descripción no puede tener más de 28 caracteres", Toast.LENGTH_LONG).show()
            return
        }

        // Verificar que la edad sea mayor o igual a 18
        val userAge = age.toIntOrNull()
        if (userAge == null || userAge < 18) {
            Toast.makeText(this, "La edad debe ser mayor o igual a 18", Toast.LENGTH_LONG).show()
            return
        }

        // Verificar que las contraseñas sean seguras y coincidan
        if (password1 != password2 || password1.length < 10 || !isPasswordSecure(password1)) {
            Toast.makeText(this, "Las contraseñas deben tener al menos 10 caracteres y contener al menos una letra mayúscula, una letra minúscula, un número y un carácter especial", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear el usuario en Firebase Auth
        auth.createUserWithEmailAndPassword(email, password1)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registro exitoso
                    val user: FirebaseUser? = auth.currentUser
                    verifyEmail(user)

                    // Guardar información registrada en la base de datos
                    val userBD = dbReference.child(user?.uid!!)
                    userBD.apply {
                        child("Name").setValue(name)
                        child("LastName").setValue(lastName)
                        child("Age").setValue(age)
                        child("Gender").setValue(gender)
                        child("Email").setValue(email)
                        child("UserName").setValue(username)
                        child("Package").setValue(packageType)
                        child("Ticket").setValue(ticket)
                        child("Description").setValue(description)
                        child("ImageUrl").setValue(imageUrl) // Guardar la URL de la imagen junto con los detalles del usuario
                    }

                    // Acción después del registro exitoso
                    action()
                } else {
                    // Si el registro falla, mostrar un mensaje de error
                    Snackbar.make(findViewById(android.R.id.content), "Error al registrar el usuario: ${task.exception?.message}", Snackbar.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            }
    }



    // Función para verificar si una contraseña es segura
    private fun isPasswordSecure(password: String): Boolean {
        val pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=!])(?=\\S+\$).{8,}\$")
        return pattern.matcher(password).matches()
    }

    // Método para enviar correo de verificación
    private fun verifyEmail(user: FirebaseUser?) {
        user?.sendEmailVerification()
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Mostrar Toast con mensaje de correo de verificación enviado
                    Toast.makeText(this, "Correo de verificación enviado", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(
                        this,
                        "Error al enviar el correo de verificación: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
    private fun clearFields() {
        txt_Name.text.clear()
        txt_Last_Name.text.clear()
        txt_Age.text.clear()
        txt_Email.text.clear()
        txt_User.text.clear()
        txt_Password.text.clear()
        txt_Password_Repeat.text.clear()
        txt_Ticket.text.clear()
        txt_Description.text.clear()


        // También puedes reiniciar los spinners si es necesario
        spinner1.setSelection(0)
        spinner2.setSelection(0)
    }


    // Método para ir a la sesión de usuario después del registro exitoso
    private fun action() {
        clearFields()
        startActivity(Intent(this, Session_User::class.java))
    }

    fun user_back_Main_Uadmin (view: View){
        startActivity(Intent(this, Session_User::class.java))
    }

    private fun generateFilename(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault())
        val currentDate = formatter.format(Date())
        return "Package_$currentDate"
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            selectedImageUri?.let {
                showSuccessMessage() // Mostrar el mensaje de "Imagen cargada correctamente" aunque aún no se haya subido la imagen
            }
        }
    }

    private fun showSuccessMessage() {
        Snackbar.make(findViewById(android.R.id.content), "Imagen seleccionada correctamente", Snackbar.LENGTH_LONG).show()
    }

    private fun showErrorMessage(message: String?) {
        Snackbar.make(findViewById(android.R.id.content), "Error al cargar la imagen: $message", Snackbar.LENGTH_LONG).show()
        progressDialog.dismiss()
    }

    private fun register() {
        // Mostrar mensaje de diálogo de "Registrando"

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Registrando Usuario...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        uploadImage() // Subir la imagen cuando se presiona el botón de registro

    }

    private fun areFieldsEmpty(): Boolean {
        // Verificar si algún campo de texto está vacío
        return TextUtils.isEmpty(txt_Name.text) || TextUtils.isEmpty(txt_Last_Name.text) || TextUtils.isEmpty(txt_Age.text) ||
                TextUtils.isEmpty(txt_Email.text) || TextUtils.isEmpty(txt_User.text) || TextUtils.isEmpty(txt_Password.text) ||
                TextUtils.isEmpty(txt_Password_Repeat.text) || TextUtils.isEmpty(txt_Ticket.text) || TextUtils.isEmpty(btn_Image_Package.text) ||
                TextUtils.isEmpty(txt_Description.text)
    }

}

