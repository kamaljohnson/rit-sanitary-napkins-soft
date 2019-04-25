package com.xborggames.ivending

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*

@Suppress("UNREACHABLE_CODE")
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sign_in.setOnClickListener {
            val username = username_edit_text.text.toString()
            val email = signin_email_edit_text.text.toString()
            val password = signin_password_edit_text.text.toString()

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
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

    }

    private fun saveUserToFirebaseDatabase() {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val wallet = 0

        val user = User (
            uid = uid,
            username = username_edit_text.text.toString(),
            wallet = 0,
            mid = "",
            pin = ""
        )
        val machine = Machines()

        ref.setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "user data saved to the cloud", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "there was an error in saving the data to the cloud", Toast.LENGTH_SHORT).show()
            }

        val mid = machine.mid
        val ref2 = FirebaseDatabase.getInstance().getReference("/machines/$mid")
        ref2.setValue(machine)
            .addOnSuccessListener {
                Toast.makeText(this, "user data saved to the cloud", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "there was an error in saving the data to the cloud", Toast.LENGTH_SHORT).show()
            }
    }
}

class User(
    val uid: String ?= "",              //the user id of the current user
    val username: String ?= "",         //the user name of the user
    val wallet: Int ?= 0,               //the wallet amount of the user
    val mid: String ?= "",              //id of the machine the user is using now
    val pin: String ?= "",              //the pin the user can use to activate the machine
    val cicost: Float ?= 0f             //the cicost of the item provided by the currently scanned machine
)

class Machines(
    val mid:String ?= "tempId",
    val pgcode:String ?= "0",
    val sales:Int ?= 0,
    val items:Int ?= 0,
    val item_cost:Float ?= 10f
)