package com.xborggames.ivending

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_in.signin_email_edit_text
import kotlinx.android.synthetic.main.activity_sign_in.sign_in
import kotlinx.android.synthetic.main.activity_sign_in.signin_password_edit_text

class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        sign_in.setOnClickListener {
            val email = signin_email_edit_text.text.toString()
            val password = signin_password_edit_text.text.toString()

            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "email or password can not be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //firebase authentication to signing in a user with email and password
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if(!it.isSuccessful) {
                        Toast.makeText(this, "sign in unsuccessful", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }
                    //else if successful
                    Toast.makeText(this, "sign in was successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
        }

        create_an_account_text.setOnClickListener() {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}
