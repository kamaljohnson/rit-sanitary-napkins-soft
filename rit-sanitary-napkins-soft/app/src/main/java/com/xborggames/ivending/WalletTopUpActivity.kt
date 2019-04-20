package com.xborggames.ivending

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_wallet_top_up.*

class WalletTopUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_top_up)

        topup_booth_button.setOnClickListener {
            Toast.makeText(this, "the button is pressed", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, UserQRCodeActivity::class.java)
            startActivity(intent)
        }
    }
}
