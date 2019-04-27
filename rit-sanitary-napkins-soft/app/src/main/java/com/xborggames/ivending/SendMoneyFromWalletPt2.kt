package com.xborggames.ivending

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Debug
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_send_money_from_wallet_pt2.*
import java.io.Console

class SendMoneyFromWalletPt2 : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var postReference: DatabaseReference

    var transaction_id = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_money_from_wallet_pt2)

        transaction_id = intent.getStringExtra("transaction_id")
        postReference = FirebaseDatabase.getInstance().reference.child("transactions").child(transaction_id)

        amount_text.afterTextChanged {
            if(transaction_status.text != "INVALID" && amount_text.text.toString() != "") {
                if(amount_text.text.toString().toInt() > 0) {
                    send_button.visibility = View.VISIBLE
                }
            }
            else
            {
                send_button.visibility = View.INVISIBLE
            }
        }

        send_button.setOnClickListener {
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("transactions/$transaction_id")
            ref.child("amount").setValue(amount_text.text.toString().toFloat())
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
                    transaction_status.text = transaction.status
                }
            }
        }
        postReference.addValueEventListener(postListener)
    }

    fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }
        })
    }
}
