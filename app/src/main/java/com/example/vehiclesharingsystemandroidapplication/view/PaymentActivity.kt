package com.example.vehiclesharingsystemandroidapplication.view

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.android.volley.toolbox.StringRequest
import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.service.Session
import com.example.vehiclesharingsystemandroidapplication.service.SingletonRQ
import org.json.JSONObject
import java.nio.charset.Charset
import java.time.LocalDateTime
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import kotlin.random.Random

class PaymentActivity : AppCompatActivity() {
    enum class PaymentStatus {
        SUCCESS, FAILURE
    }
    private var status = PaymentStatus.FAILURE
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val pay=findViewById<Button>(R.id.proceedButton)
            pay.setOnClickListener {
                var valid = true
                val cardNumberPlaintext = findViewById<TextView>(R.id.cardnumbertextbox).text
                if (cardNumberPlaintext.length != 16 || findViewById<TextView>(
                        R.id.CVVTextBox
                    ).text.length != 3
                ) { valid = false }
                val str =
                    findViewById<TextView>(R.id.monthTextView).text.toString() + "-20" + findViewById<TextView>(
                        R.id.yearTextview
                    ).text.toString()

                if(str.take(2).toInt() in 1..12) {
                    if (LocalDateTime.now().year == str.takeLast(4).toInt()) {
                        if (LocalDateTime.now().monthValue < str.take(2).toInt())
                            valid = false
                    } else if (LocalDateTime.now().year > str.takeLast(4).toInt()) {
                        valid = false
                    }
                }else valid=false
                if (valid) {
                    val chance = Random.nextInt(9)
                    status = if (chance < 2)
                        PaymentStatus.FAILURE
                    else PaymentStatus.SUCCESS
                    if (status == PaymentStatus.SUCCESS) {
                        Toast.makeText(applicationContext, "Payment Successful", Toast.LENGTH_LONG).show()
                        val keygen = KeyGenerator.getInstance("AES")
                        keygen.init(256)
                        val key: SecretKey = keygen.generateKey()
                        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
                        cipher.init(Cipher.ENCRYPT_MODE, key)
                        val encryptedCardNumber: ByteArray = cipher.doFinal(cardNumberPlaintext.toString().toByteArray())

                        val resultIntent = Intent()
                        resultIntent.putExtra("status",this.getString(R.string.PAYMENT_OK))
                        resultIntent.putExtra("encryptedCardNumber",encryptedCardNumber.toString())
                        setResult(Activity.RESULT_OK,resultIntent)
                        this.finish()
                    }
                else{
                    Toast.makeText(this, "Payment Error. Check your data.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}