package com.example.vehiclesharingsystemandroidapplication.model

import android.graphics.Bitmap

data class IdentityValidationDocument(
    var photos: ArrayList<Bitmap>,
    var type: DocumentType,
    var status: DocumentValidationStatus
)
