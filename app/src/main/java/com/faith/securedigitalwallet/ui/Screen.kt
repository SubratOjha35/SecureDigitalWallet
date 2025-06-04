package com.faith.securedigitalwallet.ui

sealed class Screen {
    object Start : Screen()
    object BankProfiles : Screen()
    object WebLoginProfiles : Screen()
    object LicProfiles : Screen()
    object ResetPassword : Screen()
}