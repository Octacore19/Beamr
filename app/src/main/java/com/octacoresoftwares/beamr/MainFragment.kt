package com.octacoresoftwares.beamr

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_main.*


class MainFragment : Fragment() {

    private var hasCameraFlash = false
    private var flashLightStatus = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
            = inflater.inflate(R.layout.fragment_main, container, false)

    override fun onResume() {
        super.onResume()
        hasCameraFlash = requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

        switch_view.setOnClickListener {
            if (hasCameraFlash) {
                if (flashLightStatus)
                    flashLightOff()
                else
                    flashLightOn()
            } else {
                Toast.makeText(
                    requireContext(), "No flash available on your device",
                    Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private fun flashLightOn() {
        val cameraManager = requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager?
        try {
            val cameraId = cameraManager!!.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, true)
            flashLightStatus = true
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun flashLightOff() {
        val cameraManager = requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager?
        try {
            val cameraId = cameraManager!!.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, false)
            flashLightStatus = false
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
}