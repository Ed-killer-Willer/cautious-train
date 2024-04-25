package com.example.flypack.user

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.flypack.R
import com.example.flypack.all.User_All

import android.text.TextUtils
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class Session_User : AppCompatActivity() {

    //var auth


    // Variables
    private lateinit var txtUser: EditText
    private lateinit var txtPassword: EditText
    private lateinit var auth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    //----------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_session)

//Variables creadas vs existentes en el layout
        txtUser=findViewById(R.id.txt_user)
        txtPassword=findViewById(R.id.btn_user2)
        auth=FirebaseAuth.getInstance()
    }


    //Metodos para Elementos del layout

    fun go_to_user_status (view: View){
        if (txtUser.text.isNullOrEmpty() || txtPassword.text.isNullOrEmpty()) {
            Toast.makeText(this, "Debes llenar todos los campos", Toast.LENGTH_SHORT).show()
        } else {
            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Iniciando sesión...")
            progressDialog.setCancelable(false)
            progressDialog.show()
            loginUser()
        }
    }



    private fun loginUser(){
        val user:String=txtUser.text.toString()
        val password:String=txtPassword.text.toString()

        if (!TextUtils.isEmpty(user)&&!TextUtils.isEmpty(password)){

            auth.signInWithEmailAndPassword(user,password)
                .addOnCompleteListener(this){
                        task ->
                    if (task.isSuccessful){

                        action()
                        clearFields()
                        progressDialog.dismiss() // Cerrar el ProgressDialog después de completar el registro


                    }else{
                        progressDialog.dismiss() // Cerrar el ProgressDialog después de completar el registro
                        Toast.makeText(this, "Error ren la autenticacion", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun action()
    {

      progressDialog.dismiss()

        startActivity(Intent(this,Status_User::class.java))
    }
    private fun clearFields() {
        txtUser.text.clear()
        txtPassword.text.clear()
    }

    //----------------------------------------------------------------

    fun user_go_recovery_password (view: View){
        val intent= Intent(this, Recovery_User::class.java).apply{}
        startActivity(intent)}

    fun go_to_register_user (view: View){
        val intent= Intent(this, Create_User::class.java).apply{}
        startActivity(intent)}

    fun user_back_user_all(view: View) {
        val intent = Intent(this, User_All::class.java).apply{}
        startActivity(intent)
        finish()}
}