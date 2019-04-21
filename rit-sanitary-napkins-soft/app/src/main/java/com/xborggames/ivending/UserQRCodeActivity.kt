package com.xborggames.ivending

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_user_qrcode.*

class UserQRCodeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_qrcode)

        val uid = FirebaseAuth.getInstance().uid
        val encoder = BarcodeEncoder()
        val bitmap = encoder.encodeBitmap(uid.toString(), BarcodeFormat.QR_CODE,
            500, 500)
        qrcode_imageview.setImageBitmap(bitmap)
    }
}
