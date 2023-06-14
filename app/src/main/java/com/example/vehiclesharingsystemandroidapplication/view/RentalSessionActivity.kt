package com.example.vehiclesharingsystemandroidapplication.view

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.model.RentalSession
import com.example.vehiclesharingsystemandroidapplication.model.Vehicle
import com.example.vehiclesharingsystemandroidapplication.service.DriverService
import com.example.vehiclesharingsystemandroidapplication.service.Session
import com.example.vehiclesharingsystemandroidapplication.view.data.Result
import com.example.vehiclesharingsystemandroidapplication.view.ui.VolleyListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class RentalSessionActivity : AppCompatActivity(),VolleyListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var distanceTravelledTextView:TextView
    private lateinit var unlockCarButton:Button
    private lateinit var lockCarButton:Button
    private lateinit var price:TextView
    private lateinit var amountToPayTextView:TextView
    private lateinit var vehicle:Vehicle
    private lateinit var currentRentalSession:RentalSession
    private lateinit var temporaryStateOfCurrentRentalSession: RentalSession
    private lateinit var session: Session
    private lateinit var loading:ProgressBar
    private lateinit var functioningModeLabel:TextView
    private var state:String = "IDLE"
    private var discoveredDevice:BluetoothDevice? = null
    private var timeout:Boolean = false
    private lateinit var resultLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private var count: Boolean = true
    private var getState: Boolean = true
    private var currentVehicleControllerState: String = "NORMAL"
    private var socket: BluetoothSocket? = null
    private var btDevFilter:IntentFilter? = null
    private val generalUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rental_session)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        unlockCarButton = findViewById(R.id.unlockVehicleButton)
        unlockCarButton.isEnabled = false
        lockCarButton = findViewById(R.id.lockVehicleButton)
        lockCarButton.isEnabled = false

        loading = findViewById(R.id.loadingAtRentalSession)
        functioningModeLabel = findViewById(R.id.emergencyFunctioningModeLabel)
        session = Session(this)
        currentRentalSession = RentalSession.fromJSONString(session.getCurrentRentalSession()!!)
        vehicle = currentRentalSession.vehicle

        price = findViewById(R.id.priceTextViewRentalSession)
        distanceTravelledTextView = findViewById(R.id.distanceTravelledTextView)
        amountToPayTextView = findViewById(R.id.amountToPayTextView)

        lifecycleScope.launch {
            connectToVehicle()
        }

        if(session.getActiveSubscription() != null) {
            price.visibility = INVISIBLE
            amountToPayTextView.visibility = INVISIBLE
            val priceLabelRentalSession = findViewById<TextView>(R.id.priceLabelRentalSession)
            val amountToPayLabel = findViewById<TextView>(R.id.amountToPayLabel)
            priceLabelRentalSession.visibility = INVISIBLE
            amountToPayLabel.visibility = INVISIBLE
        }

        lifecycleScope.launch {
        startRentalInformationLiveUpdates()
        }

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode == Activity.RESULT_OK){
                val data: Intent? = result.data
                if(data!!.getStringExtra("status").toString() == this.getString(R.string.PAYMENT_OK)){
                    data.getStringExtra("encryptedCardNumber")?.let { card_nb ->
                        DriverService.payForRentalSession(session,
                        this,
                        currentRentalSession.id,
                        card_nb,
                        currentRentalSession.cost,
                        this as VolleyListener)
                    }
                }
            }
        }

        val finishRentalSessionButton = findViewById<Button>(R.id.finishRentalSessionButton)
        finishRentalSessionButton.setOnClickListener {
            count=false
            val now = LocalDateTime.now()
            temporaryStateOfCurrentRentalSession = currentRentalSession
            temporaryStateOfCurrentRentalSession.cost =
                Duration.between(currentRentalSession.startTime,now).toMinutes().toDouble()* currentRentalSession.vehicle.price.value
            temporaryStateOfCurrentRentalSession.endTime = now
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnCompleteListener {
                temporaryStateOfCurrentRentalSession.vehicle.location =  LatLng(
                    it.result.latitude,
                    it.result.longitude
                )
                DriverService.endDriverRentalSession(
                    session,
                    this,
                    temporaryStateOfCurrentRentalSession,
                    this as VolleyListener,
                )
            }
        }

        unlockCarButton.setOnClickListener {
           lifecycleScope.launch {
               sendMessageToVehicleController("OPEN")
           }
        }

        lockCarButton.setOnClickListener {
            lifecycleScope.launch {
                sendMessageToVehicleController("CLOSE")
            }
        }

    }

    private fun connectToVehicle(){
        loading.visibility = VISIBLE
        socket = getBluetoothSocket()
        if(socket == null) {
            if (state == "SCAN_STARTED") {
                lifecycleScope.launch {
                    startCountingToTimeout()
                }
            }else{
                Toast.makeText(
                    this, "Could not find the vehicle." +
                            " Make sure you are not more than 2 meters away from it" +
                            " and try again", Toast.LENGTH_LONG
                ).show()
            }
        }
        //not else
        if(socket != null){
            openBluetoothConnection()
        }
    }

    private fun getKey(): SecretKey {
        val encodedKey = "V2n/zFD7TNiH/TZaHhcI8lnsW3uN3/+aNLkoXg0246A="
        val decodedKey = Base64.getDecoder().decode(encodedKey)
         return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
    }

    private fun getIv():IvParameterSpec{
        val iv = "vuFJ5vKBYi3hUAPD5t3mFg=="
        val decodedIV = Base64.getDecoder().decode(iv)
        return IvParameterSpec(decodedIV)
    }

    private fun getReservationToken():String{
        val key: SecretKey = getKey()
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key,getIv())
        val cipherText = cipher.doFinal(currentRentalSession.id.toByteArray())
        return Base64.getEncoder()
            .encodeToString(cipherText)
    }

    private fun sendMessageToVehicleController(message: String){
        val jsonObject = JSONObject()
        jsonObject.put("token",getReservationToken())
        jsonObject.put("operation",message)

        try{
            val dos = DataOutputStream(socket!!.outputStream)
            dos.writeChars(jsonObject.toString()+"\n")

            val dis = DataInputStream(socket!!.inputStream)
            val cmd = StringBuilder()
            while(cmd.toString()!= "terminate"){
                cmd.delete(0, cmd.length)
                var c: Char
                while (dis.readChar().also { c = it }.code > 0 && c != '\n') {
                    cmd.append(c)
                }
                if(cmd.toString()!="terminate") {
                    Toast.makeText(this, cmd.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }catch (e:Exception){
            getState = false
            e.printStackTrace()
            unlockCarButton.isEnabled = false
            lockCarButton.isEnabled = false
            Toast.makeText(this, "Connection with vehicle interrupted.\n Reestablishing connection...", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch {
                connectToVehicle()
            }

        }
    }

//    private fun listenToMessagesFromController(){
//        try{
//            val dis = DataInputStream(socket!!.inputStream)
//            val cmd = StringBuilder()
//            var c: Char
//            while (dis.readChar().also { c = it }.code > 0 && c != '\n') {
//                //println(c)
//                cmd.append(c)
//            }
//            if(!cmd.toString().contains("LIMP_MODE")){
//                if(cmd.toString() !="terminate"){
//                    Toast.makeText(this, cmd.toString(), Toast.LENGTH_SHORT).show()
//                }
//            }else{
//                functioningModeLabel.text = cmd.toString()
//            }
//
//        }catch(e:Exception){
//            e.printStackTrace()
//            unlockCarButton.isEnabled = false
//            lockCarButton.isEnabled = false
//            Toast.makeText(this, "Connection with vehicle interrupted.\n Reestablishing connection...", Toast.LENGTH_SHORT).show()
//            lifecycleScope.launch {
//                connectToVehicle()
//            }
//        }
//    }

    private suspend fun getVehicleStateFromController(){
        while(getState){
            try{
                val dos = DataOutputStream(socket!!.outputStream)
                dos.writeChars("GET_STATE"+"\n")
                val dis = DataInputStream(socket!!.inputStream)
                val cmd = StringBuilder()
                while(cmd.toString()!= "terminate"){
                    cmd.delete(0, cmd.length)
                    var c: Char
                    while (dis.readChar().also { c = it }.code > 0 && c != '\n') {
                        cmd.append(c)
                    }
                    val res = cmd.toString()
                    if(res != "terminate") {
                        val controllerStateJSONObject = JSONObject(res)
                        val controllerState = controllerStateJSONObject.getString("state")
                        if (controllerState != currentVehicleControllerState) {
                            currentVehicleControllerState = controllerState
                            if(controllerState!="NORMAL") {
                                functioningModeLabel.text = currentVehicleControllerState +
                                " has been enforced on:\n" +
                                        controllerStateJSONObject.getString("issueTime") +
                                        "\nfor the following reason:\n" +
                                        controllerStateJSONObject.getString("reason")
                                Toast.makeText(this, "Please pull over." +
                                        " Your vehicle will enter limp mode soon.",
                                    Toast.LENGTH_LONG).show()
                            }else{
                                functioningModeLabel.text = "Regular mode reactivated"
                                Toast.makeText(this, "Regular mode reactivated.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }catch (e:Exception){
                getState = false
                e.printStackTrace()
                unlockCarButton.isEnabled = false
                lockCarButton.isEnabled = false
                Toast.makeText(this, "Connection with vehicle interrupted.\n Reestablishing connection...", Toast.LENGTH_SHORT).show()
                lifecycleScope.launch {
                    connectToVehicle()
                }

            }
            delay(60000L)
        }
    }

    private fun openBluetoothConnection(){
        loading.visibility = INVISIBLE
        try{
            if(socket!=null){
                if(!socket!!.isConnected) {
                    socket!!.connect()
                }
                val dis = DataInputStream(socket!!.inputStream)
                val cmd = StringBuilder()
                var c: Char
                while (dis.readChar().also { c = it }.code > 0 && c != '\n') {
                    println(c)
                    cmd.append(c)
                }
                val remoteJSONObject = JSONObject(cmd.toString())
                if(remoteJSONObject.getString("connectionStatus") == "CONNECTED"){
                    Toast.makeText(
                        this, "Vehicle successfully identified", Toast.LENGTH_LONG)
                        .show()
                    unlockCarButton.isEnabled = true
                    lockCarButton.isEnabled = true
                    getState = true
                    val controllerStateJSONObject = JSONObject(remoteJSONObject.getString("controllerState"))
                    val controllerState = controllerStateJSONObject.getString("state")
                    if (controllerState != currentVehicleControllerState) {
                        currentVehicleControllerState = controllerState
                        if (controllerState != "NORMAL") {
                            currentVehicleControllerState = controllerState
                            functioningModeLabel.text = currentVehicleControllerState +
                                    " has been enforced on:\n" +
                                    controllerStateJSONObject.getString("issueTime") +
                                    "\nfor the following reason:\n" +
                                    controllerStateJSONObject.getString("reason")
                            Toast.makeText(
                                this, "Please pull over." +
                                        " Your vehicle will enter limp mode soon.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            functioningModeLabel.text = "Regular mode reactivated"
                            Toast.makeText(this, "Regular mode reactivated.", Toast.LENGTH_LONG).show()
                        }
                    }

//                    lifecycleScope.launch {
//                        listenToMessagesFromController()
//                    }

                    lifecycleScope.launch {
                        getVehicleStateFromController()
                    }

                }else{
                    Toast.makeText(
                        this, "Vehicle controller identified, but could not connect to it.\n"
                                +"Trying to reconnect...",
                        Toast.LENGTH_LONG
                    ).show()
                    lifecycleScope.launch {
                        delay(15000)
                        connectToVehicle()
                    }
                }
            }
        }catch (e:Exception){
            getState = false
            e.printStackTrace()
            Toast.makeText(
                this, "Vehicle controller identified, but could not connect to it.\n"
                        +"Trying to reconnect...",
                Toast.LENGTH_LONG
            ).show()
            lifecycleScope.launch {
                delay(15000)
                connectToVehicle()
            }
        }

    }


    private suspend fun startCountingToTimeout(){
        delay(12000)
        timeout = true
        Toast.makeText(this@RentalSessionActivity, "DONE COUNTING", Toast.LENGTH_SHORT).show()
        if (discoveredDevice == null) {
            loading.visibility = INVISIBLE
            Toast.makeText(
                this, "Could not connect to the locking system." +
                        " Make sure you are not more than 2 meters away from the vehicle" +
                        " and try again", Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun getBluetoothSocket(): BluetoothSocket? {
        val bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val mBluetoothAdapter = bluetoothManager.adapter
        mBluetoothAdapter.cancelDiscovery()
        val pairedDevices = mBluetoothAdapter.bondedDevices
        // If there are paired devices
        if (pairedDevices.size > 0) {
            // Loop through paired devices
            for (device in pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                if (device.name.equals("DESKTOP-V27P7JH", ignoreCase = true)) {
                    return try {
                        device.createRfcommSocketToServiceRecord(generalUuid)
                    } catch (e: IOException) {
                        null
                    }
                }
            }
        }
        btDevFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, btDevFilter)
        state = if(mBluetoothAdapter.startDiscovery()){
            Toast.makeText(this, "Searching the vehicle", Toast.LENGTH_SHORT).show()
            "SCAN_STARTED"
        }else{
            "COULD_NOT_START_SCAN"
        }

        return null

    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device:BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    //Toast.makeText(this@RentalSessionActivity, device?.name, Toast.LENGTH_SHORT).show()
                    if (device?.name.equals("DESKTOP-V27P7JH", ignoreCase = true)) {
                         discoveredDevice = device
                        socket = discoveredDevice?.createRfcommSocketToServiceRecord(generalUuid)
                        openBluetoothConnection()
                    }
                }
            }
        }
    }

    private suspend fun startRentalInformationLiveUpdates() {
        price.text = vehicle.price.toString()
        distanceTravelledTextView.text = "-1"

        while (count) {
            distanceTravelledTextView.text = (distanceTravelledTextView.text.toString()
                .toInt() + 1).toString()
            amountToPayTextView.text =
                (Duration.between(currentRentalSession.startTime, LocalDateTime.now())
                    .toMinutes() * currentRentalSession.vehicle.price.value).toString()
            delay(10000L) //for demonstration purposes only. set delay to 60000L in production.
        }
    }




    override fun requestFinished(result: Result<Any>) {
        if (result is Result.Success) {
            val resultData = result.data as String
            val resultJSONObject = JSONObject(resultData)
            if (resultJSONObject.getString("source") == "rentalSessionEndCall"){
                val response = resultJSONObject.getString("response")
                if (response.contains("SUCCESS")) {
                    currentRentalSession = temporaryStateOfCurrentRentalSession
                    session.setCurrentRentalSession(currentRentalSession.toJsonString())
                    socket?.close()
                    if (session.getActiveSubscription() == null) {
                        val intent = Intent(this, PaymentActivity::class.java)
                        intent.putExtra("amount",currentRentalSession.cost)
                        resultLauncher.launch(intent)
                    } else {
                        DriverService.payForRentalSession(session,
                            this,
                            currentRentalSession.id,
                            "",
                            currentRentalSession.cost,
                            this as VolleyListener)
                    }
                } else {
                    println(resultData)
                    Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
                }
            }else{
                if (resultJSONObject.getString("response") == "SUCCESS") {
                    val intent = Intent(this, AfterRentalSessionCompletionActivity::class.java)
                    startActivity(intent)

                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }

        } else {
            Toast.makeText(this,result.toString(),Toast.LENGTH_LONG).show()
        }
    }
    override fun onDestroy() {
        try {
            if(btDevFilter!=null) {
                unregisterReceiver(receiver)
            }
        }catch (e: IllegalArgumentException){
            e.printStackTrace()
        }
        socket?.close()
        super.onDestroy()

}
}


