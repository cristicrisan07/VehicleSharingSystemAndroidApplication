package com.example.vehiclesharingsystemandroidapplication.model


data class Driver(
    var account: ApplicationAccount,
    var firstName: String,
    var lastName: String,
    var requiredDocuments: ArrayList<IdentityValidationDocument>,
    var subscription: ActiveSubscription
)
