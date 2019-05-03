package com.xborggames.ivending

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_feedback.*
import kotlinx.android.synthetic.main.activity_feedback.send_button
import java.text.SimpleDateFormat
import java.util.*


class FeedbackActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var transaction_ref: DatabaseReference

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        val uid = FirebaseAuth.getInstance().uid ?: ""


        send_button.setOnClickListener {
            if(message_text.text.toString() != "") {
                val uid = FirebaseAuth.getInstance().uid
                val ref = FirebaseDatabase.getInstance().getReference("feedbacks")
                val feedback_id = ref.push().key.toString()
                ref.child(feedback_id).child("uid").setValue(uid)

                val cal = Calendar.getInstance()
                val date = cal.time
                val dateFormat = SimpleDateFormat("HH:mm:ss")
                val formattedDate = dateFormat.format(date)
                ref.child(feedback_id).child("time stamp").setValue(dateFormat)
                ref.child(feedback_id).child("message").setValue(message_text.text.toString())
                Toast.makeText(this, "Thank you for the feedback", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please fill in your feedback", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
