package com.xborggames.ivending

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_wallet_top_up.*

class HomeActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var postReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val uid = FirebaseAuth.getInstance().uid ?: ""

        postReference = FirebaseDatabase.getInstance().reference
            .child("users").child(uid)

        topup_button.setOnClickListener {
            val intent = Intent(this, WalletTopUpActivity::class.java)
            startActivity(intent)
        }

    }

    public override fun onStart() {
        super.onStart()

        database = FirebaseDatabase.getInstance().reference

        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val user = dataSnapshot.getValue(User::class.java)
                if (user != null) {
                    wallet_balance_text.text = "₹ "+ user.wallet.toString()
                }
            }
        }
        postReference.addValueEventListener(postListener)

    }
}
