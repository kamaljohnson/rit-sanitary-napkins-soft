package com.xborggames.ivending

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_wallet_top_up.*

class WalletTopUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_top_up)

        receive_money_from_wallet.setOnClickListener {
            val intent = Intent(this, ReceiveMoneyFromWallet::class.java)
            startActivity(intent)
        }

        send_money_from_wallet.setOnClickListener {
            val intent = Intent(this, SendMoneyFromWallet::class.java)
            startActivity(intent)
        }

        online_payment_button.setOnClickListener() {
            val intent = Intent(this, UPIPaymentActivity::class.java)
            startActivity(intent)
        }
    }
}
