package com.xborggames.ivending

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_pin_code_view.*

class PinCodeViewActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var postReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_code_view)

        val uid = FirebaseAuth.getInstance().uid ?: ""

        postReference = FirebaseDatabase.getInstance().reference.child("users").child(uid)
    }

    public override fun onStart() {
        super.onStart()

        database = FirebaseDatabase.getInstance().reference

        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                pincode_text.text = "ERROR"
            }

            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val user = dataSnapshot.getValue(User::class.java)
                if (user != null) {
                    if(user.pin != "-1") {
                        pincode_text.text = user.pin.toString()
                    }
                    else {
                        pincode_text.text = "- - - - - -"
                    }
                }
            }
        }
        postReference.addValueEventListener(postListener)

    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()

    }
}
