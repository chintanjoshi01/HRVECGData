package com.example.otcam.Utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class PermissionUtils(var activity: Activity) {


    private lateinit var permission: Array<String>

    fun grantPermission(grantPermissionCallBack: GrantPermissionCallBack) {
        permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
            arrayOf(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
            )
        }


        Dexter.withContext(activity)
            .withPermissions(
                *permission
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        grantPermissionCallBack.onGranted()
                    }
                    if (report.isAnyPermissionPermanentlyDenied) {
                        report.deniedPermissionResponses
                        for (i in report.deniedPermissionResponses) {
                            Log.e("fhhfihda", "De---- -- ${i.permissionName}")
                        }
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener { error: DexterError? ->
                Toast.makeText(
                    activity,
                    "Something Wrong",
                    Toast.LENGTH_SHORT
                ).show()
            }.onSameThread().check()
    }

    interface GrantPermissionCallBack {
        fun onGranted()
        fun onNotGranted()

    }


    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Need Permissions")
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton("GOTO SETTINGS") { dialog: DialogInterface, which: Int ->
            dialog.cancel()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri =
                Uri.fromParts("package", activity.getPackageName(), null)
            intent.data = uri
            activity.startActivityForResult(intent, 101)
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog: DialogInterface, which: Int -> dialog.cancel() }
        builder.show()
    }


    private fun showWSettingsDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Write Setting Permissions")
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton("GOTO SETTINGS") { dialog: DialogInterface, which: Int ->
            dialog.cancel()
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + activity.packageName)
            activity.startActivityForResult(intent, 1)
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog: DialogInterface, which: Int -> dialog.cancel() }
        builder.show()
    }

    fun checkGPSEnable() {
        val wifiManager = activity.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val builder = androidx.appcompat.app.AlertDialog.Builder(activity)
            builder.setTitle("Location Services Disabled")
            builder.setMessage("Please enable location services to use this feature.")
            builder.setPositiveButton("Enable") { dialog, which ->

                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity.startActivity(intent)
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                activity.finishAffinity()
            }
            val dialog = builder.create()
            dialog.show()
        } else if (!wifiManager.isWifiEnabled()) {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS);
            activity.startActivity(intent);
        } else {
            deviceWifiConnected()
        }
    }

    fun deviceWifiConnected() {
        val ssid = "InTH"
        val builder = androidx.appcompat.app.AlertDialog.Builder(activity)
        if (isWifiConnected(activity, ssid)) {
            return
        } else {
            builder.setTitle("Wi-Fi not connected")
            builder.setCancelable(false)
            builder.setMessage("Please connect to a InTH Network ")
            builder.setPositiveButton("Wi-Fi settings") { dialog, which ->
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                activity.startActivity(intent)
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                activity.finishAffinity()
            }
            builder.show()

        }
    }


    private fun isWifiConnected(context: Context, ssid: String): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        if (!networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return false
        }
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo ?: return false
        val connectedSsid = wifiInfo.ssid ?: return false
        val firstFourChars = ssid.substring(0, 6)
        return connectedSsid.startsWith("\"$firstFourChars") && wifiManager.isWifiEnabled
    }

    private fun F_CheckIP(context: Context): Boolean {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            wifiManager.setWifiEnabled(true)
        }
        val intToIp = intToIp(wifiManager.connectionInfo.ipAddress)
        return intToIp.startsWith("192.168.25.") || intToIp.startsWith("192.168.26.") || intToIp.startsWith(
            "192.168.27."
        ) || intToIp.startsWith("192.168.28.") || intToIp.startsWith("192.168.29.") || intToIp.startsWith(
            "192.168.30."
        ) || intToIp.startsWith("192.168.31.") || intToIp.startsWith("192.168.34.") || intToIp.startsWith(
            "192.168.33."
        )
    }

    private fun intToIp(i: Int): String {
        return (i and 255).toString() + "." + (i shr 8 and 255) + "." + (i shr 16 and 255) + "." + (i shr 24 and 255)
    }

    @SuppressLint("SuspiciousIndentation")
    fun checkWifiGPSOn(activity: Activity): Boolean {
        val wifiManager = activity.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


    fun checkReadStorage() {

    }


}