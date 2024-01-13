package com.example.otcam.Utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.example.polarecgdata.utils.remoteTree
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import timber.log.Timber

class PermissionUtils(private val activity: Activity) {

    private lateinit var permission: Array<String>

    fun grantPermission(grantPermissionCallBack: GrantPermissionCallBack) {
      /*  permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } else {
            arrayOf(
              Manifest.permission.ACCESS_COARSE_LOCATION,
             Manifest.permission.WRITE_EXTERNAL_STORAGE,
             Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }*/

        permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API level 33 and above
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // API level 31 and 32
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE

            )
        } else {
            // API level 30 and below
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }


        permission = retrievePermissions(activity)


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
                            remoteTree.log(1, "deniedPermissionResponses ::: ${i.permissionName}")
                            Timber.plant(remoteTree)
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
            }).withErrorListener {
                Toast.makeText(
                    activity,
                    "Something Wrong",
                    Toast.LENGTH_SHORT
                ).show()
            }.onSameThread().check()
    }

    /*private fun retrievePermissions(context: Context): Array<String> {
        try {
            return context
                .packageManager
                .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
                .requestedPermissions
        } catch (e: PackageManager.NameNotFoundException) {
            remoteTree.log(1, "retrievePermissions catch ::: ${e.message}")
            throw RuntimeException("This should have never happened.", e)
        }
    }*/


    private fun retrievePermissions(context: Context): Array<String> {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            val requestedPermissions = packageInfo.requestedPermissions
            val filteredPermissions = requestedPermissions.filter {
                Log.e("dsdsdds", "retrievePermissions---- -- $it")
                it.startsWith(context.packageName)
            }.toTypedArray()
            return filteredPermissions
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("dsdsdds", "retrievePermissions---- -- $e")
            // If package information is not found, log an error and throw a runtime exception
            remoteTree.log(1, "retrievePermissions catch ::: ${e.message}")
            throw RuntimeException("This should have never happened.", e)
        }
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
}