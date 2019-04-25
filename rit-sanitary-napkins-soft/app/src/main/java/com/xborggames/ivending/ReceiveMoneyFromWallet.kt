package com.xborggames.ivending

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_receive_modny_from_wallet.*

class ReceiveMoneyFromWallet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive_modny_from_wallet)

        val uid = FirebaseAuth.getInstance().uid
        val encoder = BarcodeEncoder()
        val bitmap = encoder.encodeBitmap(uid.toString(), BarcodeFormat.QR_CODE,
            500, 500)
        qrcode_imageview.setImageBitmap(bitmap)
    }
}
