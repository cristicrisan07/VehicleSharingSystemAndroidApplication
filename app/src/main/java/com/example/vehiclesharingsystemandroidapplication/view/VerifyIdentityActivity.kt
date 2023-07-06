package com.example.vehiclesharingsystemandroidapplication.view

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider.getUriForFile
import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.service.DriverService
import com.example.vehiclesharingsystemandroidapplication.service.Session
import com.example.vehiclesharingsystemandroidapplication.view.data.Result
import com.example.vehiclesharingsystemandroidapplication.view.ui.VolleyListener
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference


class VerifyIdentityActivity : AppCompatActivity(),VolleyListener {
    private var encodedPhotoFront: String? = null
    private var encodedPhotoBack: String? = null
    private var encodedPhotoID: String? = null
    private lateinit var submitButton:Button
    private lateinit var takeFrontPhotoButton:Button
    private lateinit var takeBackPhotoButton:Button
    private lateinit var takeIDPhotoButton:Button
    private lateinit var session:Session
    private lateinit var imageUri:Uri

    private var resultLauncherBack: ActivityResultLauncher<Intent>? = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode== RESULT_OK){
            val contentResolver = contentResolver
            try {
               val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                } else {
                    val source: ImageDecoder.Source =
                        ImageDecoder.createSource(contentResolver, imageUri)
                    ImageDecoder.decodeBitmap(source)
                }
                encodedPhotoBack = bitmap?.let {
                        it -> encodeImage(it)
                }
                if(bitmap!=null){
                    takeBackPhotoButton.isEnabled = false
                    takeBackPhotoButton.text = this.getString(R.string.take_photo_back_button_taken)
                    if(encodedPhotoFront !=null && encodedPhotoID !=null){
                        submitButton.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var resultLauncherID: ActivityResultLauncher<Intent>? = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode==RESULT_OK){
            val contentResolver = contentResolver
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                } else {
                    val source: ImageDecoder.Source =
                        ImageDecoder.createSource(contentResolver, imageUri)
                    ImageDecoder.decodeBitmap(source)
                }
                encodedPhotoID = bitmap?.let {
                        it -> encodeImage(it)
                }
                if(bitmap!=null){
                    takeIDPhotoButton.isEnabled = false
                    takeIDPhotoButton.text = this.getString(R.string.take_photo_front_button_taken)
                    if(encodedPhotoFront !=null && encodedPhotoBack !=null){
                        submitButton.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var resultLauncherFront: ActivityResultLauncher<Intent>? = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
         if(result.resultCode== RESULT_OK){
            val contentResolver = contentResolver
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                } else {
                    val source: ImageDecoder.Source =
                        ImageDecoder.createSource(contentResolver, imageUri)
                    ImageDecoder.decodeBitmap(source)
                }
                encodedPhotoFront = bitmap?.let {
                        it -> encodeImage(it)
                }
                takeFrontPhotoButton.isEnabled = false
                takeFrontPhotoButton.text = this.getString(R.string.take_photo_front_button_taken)
                if(encodedPhotoBack!=null && encodedPhotoID !=null){
                    submitButton.isEnabled = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
         }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_identity)

        getPermissions()

        session = Session(this)

        takeFrontPhotoButton = findViewById(R.id.takePhotoFrontButton)
        takeFrontPhotoButton.setOnClickListener {
            openCameraForPicture("front")
        }
        takeBackPhotoButton = findViewById(R.id.takePhotoBackButton)
        takeBackPhotoButton.setOnClickListener {
            openCameraForPicture("back")
        }
        takeIDPhotoButton = findViewById(R.id.takePhotoIDButton)
        takeIDPhotoButton.setOnClickListener {
            openCameraForPicture("id")
        }

        submitButton = findViewById(R.id.submitButton)
        submitButton.isEnabled = false
        submitButton.setOnClickListener {
                if(encodedPhotoFront!= null && encodedPhotoBack !=null && encodedPhotoID != null){
                    DriverService.sendDrivingLicense(session,
                    this,encodedPhotoFront!!,encodedPhotoBack!!,encodedPhotoID!!,this as VolleyListener)
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }else{
                    Toast.makeText(this, "Please take a photo of all documents before submitting", Toast.LENGTH_SHORT).show()
                }
           }
        if(session.getDocumentsValidationStatus() == "INVALID") {
            findViewById<TextView>(R.id.identityValidationNeededTextView).text = "Previously submitted pictures are not valid." + this.getString(R.string.identity_verification_needed)

        }

        }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 2
        private const val STORAGE_PERMISSION_REQUEST_CODE = 3
    }

    private fun openCameraForPicture(side: String){

            if (ActivityCompat.checkSelfPermission(this@VerifyIdentityActivity,
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED ) {

                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (intent.resolveActivity(packageManager) != null){
                    if(side == "front"){
                        imageUri = createImageFile("front")
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                        resultLauncherFront?.launch(intent)
                    }else{
                        if(side == "back") {
                            imageUri = createImageFile("back")
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                            resultLauncherBack?.launch(intent)
                        }else{
                            imageUri = createImageFile("id")
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                            resultLauncherID?.launch(intent)
                        }
                    }
                }
            }else{
                Toast.makeText(this,"Please allow the app to access the camera" +
                        " and the local storage",Toast.LENGTH_LONG).show()
                getPermissions()
            }

    }

    private fun getPermissions(){
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )

        }
        if(ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        }
        return
    }

    private fun createImageFile(name: String): Uri {
        val imagePath = File(this.cacheDir, "images")
        imagePath.mkdirs()
        val newFile = File(imagePath, "$name.jpg")
        return getUriForFile(this, "com.mydomain.fileprovider", newFile)
    }

    private fun encodeImage(bm: Bitmap): String? {
        val barr = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, barr)
        return Base64.encodeToString(barr.toByteArray(), Base64.DEFAULT)
    }

    override fun requestFinished(result: Result<Any>) {
        if (result is Result.Success) {
            session.setDocumentSubmissionStatus(true)
            session.setDocumentsValidationStatus("PENDING_VALIDATION")
            startActivity(Intent(this,MapsActivity::class.java))

            finish()
            setResult(RESULT_OK)
        }else{
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            Toast.makeText(this,result.toString(), Toast.LENGTH_LONG).show()
        }
    }

}
