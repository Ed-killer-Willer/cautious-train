package com.example.flypack.admin

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*

import androidx.appcompat.app.AppCompatActivity
import com.example.flypack.R

import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import java.util.regex.Pattern

class Create_Wadmin : AppCompatActivity() {

    private lateinit var txt_Name: EditText
    private lateinit var txt_Last_Name: EditText
    private lateinit var txt_Age: EditText
    private lateinit var spinner1: Spinner
    private lateinit var txt_Email: EditText
    private lateinit var txt_User: EditText
    private lateinit var txt_Password: EditText
    private lateinit var txt_Password_Repeat: EditText
    private lateinit var spinner2: Spinner
    private lateinit var checkBox: CheckBox
    private lateinit var button: Button

    private lateinit var progressDialog: ProgressDialog

    private lateinit var dbReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wadmin_create)

        txt_Name = findViewById(R.id.txt_name)
        txt_Last_Name = findViewById(R.id.txt_fisrt_name)
        txt_Age = findViewById(R.id.txt_age)
        spinner1 = findViewById(R.id.spinner1)
        txt_Email = findViewById(R.id.txt_email)
        txt_User = findViewById(R.id.txt_user)
        txt_Password = findViewById(R.id.txt_password)
        txt_Password_Repeat = findViewById(R.id.txt_password_repeat)
        spinner2 = findViewById(R.id.spinner2)
        checkBox = findViewById(R.id.checkBox)
        button = findViewById(R.id.button)

        button.isEnabled = false

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            button.isEnabled = isChecked
        }

        val spinnerSexAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.spinner_sex_values,
            R.layout.style_spinner_sex
        )
        spinnerSexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner1.adapter = spinnerSexAdapter

        val spinnerWorkerTypeAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.type_worker,
            R.layout.style_spinner_package
        )
        spinnerWorkerTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner2.adapter = spinnerWorkerTypeAdapter

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        dbReference = database.reference.child("Worker")

        button.setOnClickListener {
            register()
        }
    }

    private fun isPasswordSecure(password: String): Boolean {
        val pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=!])(?=\\S+\$).{8,}\$")
        return pattern.matcher(password).matches()
    }

    private fun verifyEmail(user: FirebaseUser?) {
        user?.sendEmailVerification()
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
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

        spinner1.setSelection(0)
        spinner2.setSelection(0)
        progressDialog.dismiss()
    }

    private fun action() {
        clearFields()
        progressDialog.dismiss() // Aquí colocas el dismiss después de limpiar los campos
        startActivity(Intent(this, Main_Wadmin::class.java))
    }


    fun back_to_wadmin_main(view: View) {
        startActivity(Intent(this, Main_Wadmin::class.java))
    }

    private fun register() {
        // Verificar si todos los campos están completos y válidos antes de mostrar el diálogo de progreso
        if (areFieldsValid()) {
            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Registrando Usuario...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            val name = txt_Name.text.toString()
            val lastName = txt_Last_Name.text.toString()
            val age = txt_Age.text.toString()
            val gender = spinner1.selectedItem.toString()
            val email = txt_Email.text.toString()
            val username = txt_User.text.toString()
            val password1 = txt_Password.text.toString()
            val password2 = txt_Password_Repeat.text.toString()
            val workerType = spinner2.selectedItem.toString()

            auth.createUserWithEmailAndPassword(email, password1)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user: FirebaseUser? = auth.currentUser
                        verifyEmail(user)

                        val userBD = dbReference.child(user?.uid!!)
                        userBD.apply {
                            child("Name").setValue(name)
                            child("LastName").setValue(lastName)
                            child("Age").setValue(age)
                            child("Gender").setValue(gender)
                            child("Email").setValue(email)
                            child("UserName").setValue(username)
                            child("WorkerType").setValue(workerType)
                        }

                        progressDialog.dismiss() // Ocultar el diálogo de progreso después de completar el registro
                        action()
                    } else {
                        progressDialog.dismiss() // Ocultar el diálogo de progreso en caso de error
                        Snackbar.make(findViewById(android.R.id.content), "Error al registrar el usuario: ${task.exception?.message}", Snackbar.LENGTH_SHORT).show()
                    }
                }
        }
    }


    private fun areFieldsValid(): Boolean {
        val name = txt_Name.text.toString()
        val lastName = txt_Last_Name.text.toString()
        val age = txt_Age.text.toString()
        val gender = spinner1.selectedItem.toString()
        val email = txt_Email.text.toString()
        val username = txt_User.text.toString()
        val password1 = txt_Password.text.toString()
        val password2 = txt_Password_Repeat.text.toString()
        val workerType = spinner2.selectedItem.toString()

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(age) || TextUtils.isEmpty(gender) ||
            TextUtils.isEmpty(email) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password1) || TextUtils.isEmpty(password2) ||
            TextUtils.isEmpty(workerType)
        ) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            return false
        }

        if (username.length > 10) {
            Toast.makeText(this, "El nombre de usuario no puede tener más de 10 caracteres", Toast.LENGTH_LONG).show()
            return false
        }

        val userAge = age.toIntOrNull()
        if (userAge == null || userAge < 18) {
            Toast.makeText(this, "La edad debe ser mayor o igual a 18", Toast.LENGTH_LONG).show()
            return false
        }

        if (password1 != password2 || password1.length < 10 || !isPasswordSecure(password1)) {
            Toast.makeText(this, "Las contraseñas deben tener al menos 10 caracteres y contener al menos una letra mayúscula, una letra minúscula, un número y un carácter especial", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

}
