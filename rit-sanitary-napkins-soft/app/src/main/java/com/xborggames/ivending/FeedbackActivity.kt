package com.xborggames.ivending

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_feedback.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


class FeedbackActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var transaction_ref: DatabaseReference

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        val uid = FirebaseAuth.getInstance().uid ?: ""


        send_button.setOnClickListener {
            if(message_text.text.toString() != "") {
                val uid = FirebaseAuth.getInstance().uid
                val ref = FirebaseDatabase.getInstance().getReference("feedbacks")

                val feedback:Feedback = Feedback(
                    catagory = if(editText.text.toString() == "") "general" else editText.text.toString(),
                    uid = uid,
                    message = message_text.text.toString(),
                    data = LocalDateTime.now().toString()
                )

                ref.push().setValue(feedback)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Thank you for the feedback", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "there was an error in sending the feedback", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please fill in your feedback", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
