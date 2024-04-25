package com.example.flypack.all

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.flypack.R
import com.example.flypack.admin.Session_Admin
import com.example.flypack.user.Session_User
import com.example.flypack.worker.elements.Session_Worker

class User_All : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        Thread.sleep(1300)
        setTheme(R.style.Theme_SplashScreen)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.all_user)
    }
    // Funciones de los botones
    fun layout_user_session (view: View){
        val intent=Intent(this,Session_User::class.java).apply{}
        startActivity(intent)}

    fun layout_worker_session (view: View){
        val intent=Intent(this, Session_Worker::class.java).apply{}
        startActivity(intent)}

    fun layout_admin_session (view: View){
        val intent=Intent(this,Session_Admin::class.java).apply{}
        startActivity(intent)}

    fun close(view: View) {
        super.onBackPressed()
        finish()
    }




}