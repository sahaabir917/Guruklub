package com.gmpire.guruklub.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.*
import android.os.Build.VERSION.RELEASE
import android.os.Build.VERSION.SDK_INT
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

object DeviceInfoHelper {

    val model = deviceModel

    val hardware: String? = HARDWARE

    val board: String? = BOARD

    val bootloader: String? = BOOTLOADER

    val user: String? = USER

    val host: String? = HOST

    val version: String? = RELEASE

    val apiLevel = SDK_INT

    val id: String? = ID

    val time = TIME

    val fingerPrint: String? = FINGERPRINT

    val display: String? = DISPLAY

    private val deviceModel
        @SuppressLint("DefaultLocale")
        get() = capitalize(
            if (MODEL.toLowerCase().startsWith(MANUFACTURER.toLowerCase())) {
                MODEL
            } else {
                "$MANUFACTURER $MODEL"
            }
        )


    private fun capitalize(str: String) = str.apply {
        if (isNotEmpty()) {
            first().run { if (isLowerCase()) toUpperCase() }
        }
    }

    private val Context.imei
        @SuppressLint("HardwareIds", "MissingPermission")
        get() = telephonyManager?.run {
            if (isReadPhoneStatePermissionGranted()) {
                if (SDK_INT >= VERSION_CODES.O) {
                    imei
                } else {
                    deviceId
                }
            } else 100
        } ?: 100

    private fun Context.isReadPhoneStatePermissionGranted() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

    private val Context.telephonyManager
        get() = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?


    @SuppressLint("MissingPermission")
    fun getDeviceId(context: Context): String? {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            val deviceId: String
            deviceId = if (SDK_INT >= VERSION_CODES.Q) {
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            } else {
                val mTelephony: TelephonyManager =
                    context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                if (mTelephony.getDeviceId() != null) {
                    mTelephony.getDeviceId()
                } else {
                    Settings.Secure.getString(
                        context.contentResolver,
                        Settings.Secure.ANDROID_ID
                    )
                }
            }
            return deviceId
        }
        return ""
    }

}