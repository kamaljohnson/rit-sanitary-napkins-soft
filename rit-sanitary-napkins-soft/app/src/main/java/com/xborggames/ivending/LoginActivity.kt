package com.xborggames.ivending

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_button.setOnClickListener {
            val username = username_edit_text.text.toString()
            val email = email_edit_text.text.toString()
            val password = password_edit_text.text.toString()

            if(username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "username, email or password can not be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //firebase authentication to create a user with email and password
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if(!it.isSuccessful) return@addOnCompleteListener

                    //else if successful
                    already_have_account_textview.text = it.result!!.user.uid
                }
        }

        already_have_account_textview.setOnClickListener {
            //TODO create a login activity and navigate to it
        }

    }
}
