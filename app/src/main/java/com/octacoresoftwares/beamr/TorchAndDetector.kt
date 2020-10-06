package com.octacoresoftwares.beamr

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import com.squareup.seismic.ShakeDetector

class TorchAndDetector(private val context: Context) : ShakeDetector.Listener {
    private var hasCameraFlash = false
    private var flashLightStatus = false

    private var shakeDetector: ShakeDetector? = null
    private var sensorManager: SensorManager? = null
    private var cameraManager: CameraManager? = null
    private var vibrator: Vibrator? = null

    private var cameraId = ""

    fun initTorchAndDetector() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        shakeDetector = ShakeDetector(this)
        hasCameraFlash =
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        cameraId = cameraManager!!.cameraIdList[0]
    }

    fun setSensitivity(intensity: Intensity) {
        shakeDetector?.setSensitivity(intensity.value)
    }

    fun startDetector() {
        shakeDetector?.start(sensorManager)
    }

    fun stopDetector() {
        shakeDetector?.stop()
        try {
            cameraManager?.setTorchMode(cameraId, false)
            flashLightStatus = false
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun flashLightOn() {
        try {
            cameraManager?.setTorchMode(cameraId, true)
            vibrate()
            flashLightStatus = true
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun flashLightOff() {
        try {
            cameraManager?.setTorchMode(cameraId, false)
            vibrate()
            flashLightStatus = false
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(
                    100,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            //deprecated in API 26
            vibrator?.vibrate(100)
        }
    }

    override fun hearShake() {
        if (hasCameraFlash) {
            if (flashLightStatus)
                flashLightOff()
            else
                flashLightOn()
        } else {
            Log.e("TouchAndDetector", "Flash not present")
            Toast.makeText(context, "Device does not have Flash", Toast.LENGTH_LONG).show()
        }
    }
}

enum class Intensity(val value: Int) {
    LOW(15),
    MEDIUM(14),
    HIGH(13)
}