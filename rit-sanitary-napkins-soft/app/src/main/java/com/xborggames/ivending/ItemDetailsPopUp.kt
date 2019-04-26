package com.xborggames.ivending

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_item_details_pop_up.*
import kotlinx.android.synthetic.main.activity_item_details_pop_up.wallet_balance_text

class ItemDetailsPopUp : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var postReference: DatabaseReference

    internal lateinit var myDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_details_pop_up)


        val uid = FirebaseAuth.getInstance().uid ?: ""

        postReference = FirebaseDatabase.getInstance().reference
            .child("users").child(uid)


        buy_button.setOnClickListener {
            if(item_cost_text.text != "0") {
                getPinFromCloud()
            }
        }
    }

    fun getPinFromCloud() {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/pin")

//        if(wallet_balance_text.text.toString().toInt() < item_cost_text.text.toString().toInt())
//        {
//            Toast.makeText(this, "not enough balance in your wallet", Toast.LENGTH_LONG).show()
//            val intent = Intent(this, WalletTopUpActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//        }
//        {
            ref.setValue("-1")
                .addOnSuccessListener {
                    Toast.makeText(this, "sending request for pin", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, PinCodeViewActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "there was an error in saving the data to the cloud", Toast.LENGTH_SHORT)
                        .show()
                }
//        }
    }

    public override fun onStart() {
        super.onStart()

        database = FirebaseDatabase.getInstance().reference

        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val user = dataSnapshot.getValue(User::class.java)
                if (user != null) {
                    if(user.cicost == -1f){
                        return
                    }
                    item_cost_text.text = "₹ "+ user.cicost.toString()
                    wallet_balance_text.text = "₹ "+ user.wallet.toString()
                }
            }
        }
        postReference.addValueEventListener(postListener)

    }

    fun ShowDialog() {
        myDialog = Dialog(this)
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        myDialog.setContentView(R.layout.activity_pin_code_view)
        myDialog.setTitle("Pin Code")
        myDialog.show()
    }
}
