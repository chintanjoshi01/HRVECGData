package com.example.polarecgdata

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.polarecgdata.database.DataModel
import com.example.polarecgdata.database.DatabaseHelper
import com.example.polarecgdata.databinding.ActivityMainBinding
import com.example.polarecgdata.utils.ID
import com.example.polarecgdata.utils.NAME
import com.example.polarecgdata.utils.SharedPreferencesUtils
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.provider.Settings
import com.example.polarecgdata.utils.DeviceDetails
import com.example.polarecgdata.utils.TimberRemoteTree
import com.example.polarecgdata.utils.devicePhoneId
import com.example.polarecgdata.utils.remoteTree

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val PERMISSION_REQUEST_CODE: Int = 101
    private val STORAGE_PERMISSION_CODE = 1
    private lateinit var sharedPreferencesUtils: SharedPreferencesUtils
    private lateinit var databaseHelper: DatabaseHelper
    val executor: ExecutorService = Executors.newSingleThreadExecutor()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        devicePhoneId = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
         val deviceDetails = DeviceDetails(devicePhoneId)
        remoteTree =  TimberRemoteTree(deviceDetails)
        databaseHelper = DatabaseHelper.getInstance(this)!!
        initBottomNavBar()
        sharedPreferencesUtils = SharedPreferencesUtils(this)
        reqPermission()
    }

    fun initBottomNavBar() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment) as NavHostFragment
        binding.bottomNavigation.setupWithNavController(navHostFragment.navController)
    }

    private fun reqPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(
                    arrayOf(
                        android.Manifest.permission.BLUETOOTH_SCAN,
                        android.Manifest.permission.BLUETOOTH_CONNECT
                    ), PERMISSION_REQUEST_CODE
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
                requestPermissions(
                    arrayOf(
                        android.Manifest.permission.BLUETOOTH_SCAN,
                        android.Manifest.permission.BLUETOOTH_CONNECT,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ), PERMISSION_REQUEST_CODE
                )
            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_CODE
                )
            }
        } else {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.execute {
            databaseHelper.dao?.update(
                DataModel(
                    ID,
                    NAME, "", "", "", "", "", ""
                )
            )
            executor.shutdown()
        }
    }


}