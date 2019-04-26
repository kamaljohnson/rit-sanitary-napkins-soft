package com.xborggames.ivending

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_send_money_from_wallet_pt2.*

class SendMoneyFromWalletPt2 : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var postReference: DatabaseReference

    var transaction_id = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_money_from_wallet_pt2)

        val uid = FirebaseAuth.getInstance().uid ?: ""

        transaction_id = intent.getStringExtra("transaction_id")
        postReference = FirebaseDatabase.getInstance().reference.child("transactions").child(transaction_id)
        if(transaction_status.text != "INVALID" && amount_text.text.toString().toInt() > 0) {
            send_button.visibility = View.VISIBLE
        }
        else
        {
            send_button.visibility = View.INVISIBLE
        }

    }

    override fun onStart() {
        super.onStart()
        database = FirebaseDatabase.getInstance().reference

        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val transaction = dataSnapshot.getValue(SendMoneyFromWallet.Transactions::class.java)
                if (transaction != null) {
                    if(transaction.status == "invalid") {
                        transaction_status.text = "INVALID"
                    }else {
                        transaction_status.text = "VALID"
                    }
                }
            }
        }
        postReference.addValueEventListener(postListener)
    }
}
