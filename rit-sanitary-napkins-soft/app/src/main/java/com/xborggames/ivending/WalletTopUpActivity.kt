package com.xborggames.ivending

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_wallet_top_up.*

class WalletTopUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_top_up)

        topup_booth_button.setOnClickListener {
            val intent = Intent(this, UserQRCodeActivity::class.java)
            startActivity(intent)
        }
        online_payment_button.setOnClickListener() {
            Toast.makeText(this, "this feature is not yet implemented", Toast.LENGTH_SHORT).show()

            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
            ref.child("wallet").setValue(10)
        }
    }
}
