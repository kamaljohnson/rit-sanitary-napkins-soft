package com.xborggames.ivending

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_send_money_from_wallet_pt2.*
import kotlinx.android.synthetic.main.activity_upipayment.*
import kotlinx.android.synthetic.main.activity_upipayment.amount_text
import kotlinx.android.synthetic.main.activity_upipayment.done_button
import kotlinx.android.synthetic.main.activity_upipayment.send_button
import kotlinx.android.synthetic.main.activity_upipayment.transaction_status
import kotlinx.android.synthetic.main.activity_upipayment.wallet_balance_text

class UPIPaymentActivity : AppCompatActivity() {

    var bank_upi_id: String = ""

    val UPI_PAYMENT:Int = 0

    private lateinit var database: DatabaseReference
    private lateinit var postReference: DatabaseReference
    private lateinit var user_ref: DatabaseReference
    private lateinit var transaction_ref: DatabaseReference
    private lateinit var transaction: SendMoneyFromWallet.Transactions
    var transaction_id = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upipayment)

        val uid = FirebaseAuth.getInstance().uid ?: ""
        user_ref = FirebaseDatabase.getInstance().reference.child("users").child(uid)
        postReference = FirebaseDatabase.getInstance().reference.child("banks").child("company")
        transaction_ref = FirebaseDatabase.getInstance().reference.child("transactions")


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
            payUsingUpi(amount_text.text.toString(), bank_upi_id, "kamal", "napkin payment")
        }
    }

    public override fun onStart() {
        super.onStart()

        done_button.visibility = View.INVISIBLE
        send_button.visibility = View.INVISIBLE
        transaction_status.text = "invalid"

        database = FirebaseDatabase.getInstance().reference

        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val bank = dataSnapshot.getValue(Bank::class.java)
                if (bank != null) {
                    bank_upi_id = bank.upi.toString()
                    bank_upi_text.text = "to: " + bank_upi_id;
                    transaction_status.text = "valid"
                    transaction_id = transaction_ref.push().key.toString()
                    val uid = FirebaseAuth.getInstance().uid ?: ""
                    transaction = SendMoneyFromWallet.Transactions(
                        from = uid,
                        to = bank.upi,
                        status = "valid"
                    )
                    transaction_ref.child(transaction_id).setValue(transaction)
                }
            }
        }
        postReference.addValueEventListener(postListener)

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

        if(bank_upi_id != "")
        {
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
            transaction_ref.child(transaction_id).addValueEventListener(postListener_transaction )
        }

    }

    fun payUsingUpi(amount:String, upiId:String, name:String, note:String) {

        var uri:Uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("pn", name)
            .appendQueryParameter("tn", note)
            .appendQueryParameter("am", amount)
            .appendQueryParameter("cu", "INR")
            .build()

        val upiPayIntent :Intent = Intent(Intent.ACTION_VIEW)
        upiPayIntent.setData(uri)
        val chooser:Intent = Intent.createChooser(upiPayIntent, "Pay with")

        if(null != chooser.resolveActivity(packageManager)){
            startActivityForResult(chooser, UPI_PAYMENT)
        } else {
            Toast.makeText(this, "No UPI app found, please install one to continue", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == UPI_PAYMENT) {
            if(Activity.RESULT_OK == resultCode || resultCode == 11) {
                if(data != null) {
                    val text = data.getStringExtra("response")
                    transaction.status = "pending"
                    transaction.amount = amount_text.text.toString().toFloat()
                    transaction_ref.child(transaction_id).setValue(transaction)
                }
            }
        }
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
}
