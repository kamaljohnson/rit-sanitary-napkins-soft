package com.xborggames.ivending

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*

@Suppress("UNREACHABLE_CODE")
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
                    if(!it.isSuccessful) {
                        Toast.makeText(this, "registration was unsuccessful", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }
                    //else if successful
                    Toast.makeText(this, "registration was successful", Toast.LENGTH_SHORT).show()
                    saveUserToFirebaseDatabase()
                }
        }

        already_have_account_textview.setOnClickListener {
            //TODO create a login activity and navigate to it
        }

    }

    private fun saveUserToFirebaseDatabase() {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val wallet = 0

        val user = User(uid, username_edit_text.text.toString(), wallet)

        ref.setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "user data saved to the cloud", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "there was an error in saving the data to the cloud", Toast.LENGTH_SHORT).show()
            }

    }
}

class User(
    val uid: String ?= "",
    val username: String ?= "",
    val wallet: Int ?= 0
)