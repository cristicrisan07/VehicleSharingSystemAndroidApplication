package com.example.vehiclesharingsystemandroidapplication.view

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.service.DtoConverter
import com.example.vehiclesharingsystemandroidapplication.service.Session
import com.example.vehiclesharingsystemandroidapplication.service.SingletonRQ
import com.example.vehiclesharingsystemandroidapplication.view.ui.ListAdapter
import com.example.vehiclesharingsystemandroidapplication.view.ui.selectSubscription.RentalSubscriptionViewModel
import org.json.JSONObject
import java.nio.charset.Charset
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class SubscriptionSelectionActivity : AppCompatActivity() {

    private var listViewReference: ListView? = null
    private var listAdapter: ListAdapter? = null
    private var subscriptionsArrayList: ArrayList<RentalSubscriptionViewModel>? = ArrayList()
    private var selectedSubscriptionIndex: Int? = null

    val dtoConverter = DtoConverter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription_selection)

        val session = Session(this)
        val username =session.getUsername()
        val token = session.getToken()

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if(result.resultCode == Activity.RESULT_OK){
                val data: Intent? = result.data
                if(data!!.getStringExtra("status").toString() == this.getString(R.string.PAYMENT_OK)){
                    selectedSubscriptionIndex?.let {value->
                        subscriptionsArrayList?.get(value)?.rentalSubscription?.let { rentalSubscription ->
                            data.getStringExtra("encryptedCardNumber")?.let { it ->
                                addSubscriptionToDriver(username!!,
                                    rentalSubscription.id, it,token!!,
                                    rentalSubscription.rentalPrice.value.toString()
                                )
                            }
                        }
                    }
                    Toast.makeText(applicationContext,"COOL!",Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(applicationContext,"An error occurred during the payment process. Please try again!",Toast.LENGTH_LONG).show()
                }
            }
        }

        val goToPaymentButton = findViewById<Button>(R.id.goPaySubscriptionButton)
        goToPaymentButton.setOnClickListener {
            val intent = Intent(this,PaymentActivity::class.java)
            resultLauncher.launch(intent)
        }
        getAvailableSubscriptions(token!!)

    }

    private fun getAvailableSubscriptions(token:String){
        val stringRequest: JsonArrayRequest = object: JsonArrayRequest(
            Method.GET,
            this.getString(R.string.getAllSubscriptions),
            null,
            { response->
                if(response.length() == 0){
                    Toast.makeText(applicationContext, "No subscriptions available", Toast.LENGTH_SHORT).show()
                }
                else {
                    for (i in 0 until response.length()) {
                        subscriptionsArrayList?.add(
                            RentalSubscriptionViewModel(
                                dtoConverter.fromDTOtoSubscription(
                                    response.getJSONObject(i)
                                ), false
                            )
                        )
                    }
                    listViewReference = findViewById(R.id.subscription_selection_list_view)
                    listAdapter = subscriptionsArrayList?.let { ListAdapter(this, it) }
                    listViewReference?.adapter = listAdapter

                    listViewReference!!.setOnItemClickListener { parent,view,position,id ->
                        subscriptionsArrayList!![position].selected=!subscriptionsArrayList!![position].selected
                        selectedSubscriptionIndex = position
                        for(i in 0 until subscriptionsArrayList!!.size){
                            if(i != position){
                                subscriptionsArrayList!![i].selected = false
                            }
                        }
                        listAdapter?.notifyDataSetChanged()
                    }
                }
            },
            {error->
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }){

            override fun getHeaders(): MutableMap<String, String> {
                val params=HashMap<String,String>()
                params["Content-Type"] = "application/json"
                params["Accept"] = "application/json"
                params["Authorization"] = "Bearer $token"
                return params
            }

        }
        SingletonRQ.getInstance(this@SubscriptionSelectionActivity).addToRequestQueue(stringRequest)
    }

    private fun addSubscriptionToDriver(username:String, subscriptionId: String, encryptedCardNumber: String, paymentValue: String, token: String){
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST,
            this.getString(R.string.addSubscriptionToDriver),
            {response ->
                if(response=="SUCCESS")
                {
                    //setResult(Activity.RESULT_OK)
                    Toast.makeText(this,"Subscription successfully bought!",Toast.LENGTH_LONG).show()
                }
                else{
                    if(response=="DRIVER_NOT_FOUND")
                    {
                        Toast.makeText(applicationContext,"$response. Please contact support.",
                            Toast.LENGTH_LONG).show()
                       // setResult(Activity.RESULT_CANCELED)
                    }
                }
               // this.finish()
            },
            {
                Toast.makeText(this, "Could not subscribe!", Toast.LENGTH_LONG).show()
            }) {

            override fun getBody(): ByteArray {
                val subscriptionContractDTO = JSONObject()
                subscriptionContractDTO.put("driverUsername",username)
                subscriptionContractDTO.put("subscriptionId",subscriptionId)
                subscriptionContractDTO.put("encryptedCardNumber",encryptedCardNumber)
                subscriptionContractDTO.put("value",paymentValue)
                return subscriptionContractDTO.toString().toByteArray(Charset.forName("utf-8"))

            }

            override fun getHeaders(): MutableMap<String, String> {
                val params=HashMap<String,String>()
                params["Content-Type"] = "application/json"
                params["Accept"] = "application/json"
                params["Authorization"] = "Bearer $token"
                return params
            }

        }
        SingletonRQ.getInstance(this@SubscriptionSelectionActivity).addToRequestQueue(stringRequest)
    }
}



