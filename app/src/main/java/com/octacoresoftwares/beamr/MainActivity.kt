package com.octacoresoftwares.beamr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment

private const val CAMERA_REQUEST = 20

class MainActivity : AppCompatActivity() {

    private var cameraPermissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        cameraPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

        if (!cameraPermissionGranted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST
            )
        } else {
            initializeGraph()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeGraph()
                }
            }
        }
    }

    private fun initializeGraph() {
        val graphHost = NavHostFragment.create(R.navigation.app_navigation)
        supportFragmentManager.beginTransaction()
            .replace(R.id.app_host_fragment, graphHost)
            .setPrimaryNavigationFragment(graphHost)
            .commit()
    }
}