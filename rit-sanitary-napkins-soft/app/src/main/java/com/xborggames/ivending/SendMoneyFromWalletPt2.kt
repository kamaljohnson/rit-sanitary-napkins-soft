package com.xborggames.ivending

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_send_money_from_wallet_pt2.*

enum class TransactionStatus {
    SUCCESSFUL,
    VALID,
    INVALID,
    PENDING,
    INSUFFICIENT_FUND,
    FAILED,
    CANCELLED
}

class SendMoneyFromWalletPt2 : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var transaction_ref: DatabaseReference
    private lateinit var user_ref: DatabaseReference

    var transaction_id = ""
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_money_from_wallet_pt2)

        val uid = FirebaseAuth.getInstance().uid ?: ""
        user_ref = FirebaseDatabase.getInstance().reference.child("users").child(uid)

        transaction_id = intent.getStringExtra("transaction_id")
        transaction_ref = FirebaseDatabase.getInstance().reference.child("transactions").child(transaction_id)

        amount_text.afterTextChanged {
            if(transaction_status.text != "invalid" && amount_text.text.toString() != "") {
                if(amount_text.text.toString().toInt() > 0 && transaction_status.text == "valid") {
                    send_button.visibility = View.VISIBLE
                } else {
                    send_button.visibility = View.INVISIBLE
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
            done_button.visibility = View.VISIBLE
            send_button.visibility = View.INVISIBLE
        }

        done_button.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        database = FirebaseDatabase.getInstance().reference

        done_button.visibility = View.INVISIBLE
        send_button.visibility = View.INVISIBLE

        val postListener_transaction = object : ValueEventListener {
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
        transaction_ref.addValueEventListener(postListener_transaction )

        val postListener_user_data = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)

                if(user != null) {
                    wallet_balance_text.text = "₹ "+ user.wallet.toString()
                }
            }
        }
        user_ref.addValueEventListener(postListener_user_data)
    }

    private fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
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

//    private fun TextView.afterTextChangedTextView(afterTextChanged: (String) -> Unit) {
//        this.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//            }
//
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//            }
//
//            override fun afterTextChanged(editable: Editable?) {
//                afterTextChanged.invoke(editable.toString())
//            }
//        })
//    }
}
