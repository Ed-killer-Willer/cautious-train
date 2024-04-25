package com.example.flypack.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.flypack.R
import com.example.flypack.admin.Session_Admin


class Recovery_Admin: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_recovery)
    }
    fun back_adin_session (view: View){
        val intent= Intent(this, Session_Admin::class.java).apply{}
        startActivity(intent)}
}