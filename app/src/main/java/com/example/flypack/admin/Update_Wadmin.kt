package com.example.flypack.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flypack.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class Update_Wadmin : AppCompatActivity() {

    private lateinit var checkBox: CheckBox
    private lateinit var button: Button
    private lateinit var btnDelete: Button

    // Declarar las referencias a los elementos de la interfaz de usuario
    private lateinit var workerType: TextView
    private lateinit var txtName: EditText
    private lateinit var txtFirstName: EditText
    private lateinit var txtAge: EditText
    private lateinit var txtSex: Spinner
    private lateinit var txtUser: EditText

    // Declarar la referencia a la base de datos
    private lateinit var database: FirebaseDatabase
    private lateinit var dbReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wadmin_update)

        checkBox = findViewById(R.id.checkBox)
        button = findViewById(R.id.button)
        btnDelete = findViewById(R.id.delete)

        // Inicializar Firebase
        database = FirebaseDatabase.getInstance()
        dbReference = database.reference.child("Worker")

        // Obtener la referencia de Firebase Auth
        val auth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = auth.currentUser

        // Obtener la referencia a los campos de la interfaz de usuario
        workerType = findViewById(R.id.txt_Name_user21)
        txtName = findViewById(R.id.txt_name)
        txtFirstName = findViewById(R.id.txt_fisrt_name)
        txtAge = findViewById(R.id.txt_age)
        txtSex = findViewById(R.id.txt_sex)
        txtUser = findViewById(R.id.txt_user)

        // Deshabilitar el botón de registro al inicio
        button.isEnabled = false
        btnDelete.isEnabled = false

        // Listener del CheckBox para habilitar/deshabilitar el botón de registro
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            button.isEnabled = isChecked
            btnDelete.isEnabled = isChecked
        }

        currentUser?.let { user ->
            val userRef: DatabaseReference = dbReference.child(user.uid)

            userRef.addValueEventListener(object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val workertype: String? = dataSnapshot.child("WorkerType").getValue(String::class.java)
                    val name: String? = dataSnapshot.child("Name").getValue(String::class.java)
                    val firstName: String? = dataSnapshot.child("LastName").getValue(String::class.java)
                    val age: String? = dataSnapshot.child("Age").getValue(String::class.java)
                    val sex: String? = dataSnapshot.child("Gender").getValue(String::class.java)
                    val userName: String? = dataSnapshot.child("UserName").getValue(String::class.java)

                    workerType.text = workertype
                    txtName.setText(name)
                    txtFirstName.setText(firstName)
                    txtAge.setText(age)
                    txtSex.setSelection(getIndex(txtSex, sex ?: ""))
                    txtUser.setText(userName)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Snackbar.make(findViewById(android.R.id.content), "Error", Snackbar.LENGTH_SHORT).show()
                }
            })
        }

        button.setOnClickListener {
            // Función de actualización de datos
            updateData()
        }

        btnDelete.setOnClickListener {
            // Función de eliminación de usuario
            deleteCurrentUser(currentUser?.uid)
        }

        // Adaptador para el Spinner de Sexo
        val spinnerSexAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.spinner_sex_values,
            android.R.layout.simple_spinner_item
        )
        spinnerSexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        txtSex.adapter = spinnerSexAdapter
    }

    private fun updateData() {
        // Obtener los nuevos valores de los campos de la interfaz de usuario
        val newName = txtName.text.toString()
        val newFirstName = txtFirstName.text.toString()
        val newAge = txtAge.text.toString()
        val newSex = txtSex.selectedItem.toString()
        val newUser = txtUser.text.toString()


        // Verificar que el username no supere los 13 caracteres
        if (newUser.length > 10) {
            Toast.makeText(this, "El nombre de usuario no puede tener más de 10 caracteres", Toast.LENGTH_LONG).show()
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
            val userReference = dbReference.child(userID)

            // Crear un mapa de datos para almacenar los nuevos valores
            val updatesMap = HashMap<String, Any>()
            updatesMap["Name"] = newName
            updatesMap["FirstName"] = newFirstName
            updatesMap["Age"] = newAge
            updatesMap["Gender"] = newSex
            updatesMap["UserName"] = newUser

            // Actualizar los valores en la base de datos
            userReference.updateChildren(updatesMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Los datos se actualizaron correctamente
                        Snackbar.make(findViewById(android.R.id.content), "Datos actualizados correctamente", Snackbar.LENGTH_SHORT).show()
                    } else {
                        // Ocurrió un error al actualizar los datos
                        val errorMessage = task.exception?.message ?: "Error desconocido al actualizar los datos"
                        Snackbar.make(findViewById(android.R.id.content), "Error al actualizar los datos: $errorMessage", Snackbar.LENGTH_SHORT).show()
                    }
                }
        }
    }

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

    fun back_user_status(view: View) {
        onBackPressed()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, Rud_Wadmin::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
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
}
