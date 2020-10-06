package com.octacoresoftwares.beamr

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.preference.*


class MainFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var prefs: SharedPreferences
    private var cameraEnabled = MutableLiveData<STATUS>()

    private val app by lazy {
        (requireActivity().application as App)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        cameraEnabled.value = checkPermission()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_REQUEST -> {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        cameraEnabled.value = STATUS.ENABLED
                    } else {
                        cameraEnabled.value = STATUS.RETRY
                    }
                return
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        cameraEnabled.observe({ lifecycle }) { value ->
            if (value != null)
                when (value) {
                    STATUS.ENABLED -> {
                        enableMain(rootKey)
                    }

                    STATUS.DISABLED -> {
                        enablePermission(rootKey)
                    }

                    STATUS.RETRY -> {
                        enablePermission(rootKey, R.string.permission_summary_long)
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

        when (key) {
            getString(R.string.service_switch_key) -> {
                val pref = findPreference<SwitchPreferenceCompat>(key)
                if (pref?.isChecked!!) {
                    startServiceWithoutExtra()
                } else {
                    stopService()
                }
            }

            getString(R.string.sensitivity_key) -> {
                val pref = findPreference<ListPreference>(key)
                pref?.summary = pref?.value!!
                startServiceWithExtra(pref.value)
            }

            getString(R.string.theme_key) -> {
                val pref = findPreference<SwitchPreferenceCompat>(key)
                if (pref?.isChecked!!) {
                    app.setDarkMode(true)
                } else {
                    app.setDarkMode(false)
                }
            }
        }
    }

    private fun enablePermission(rootKey: String?) {
        setPreferencesFromResource(R.xml.permission_preference, rootKey)
        val pref = findPreference<Preference>(getString(R.string.permission_key))
        pref?.setOnPreferenceClickListener {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST)
            return@setOnPreferenceClickListener true
        }
    }

    @Suppress("SameParameterValue")
    private fun enablePermission(rootKey: String?, @StringRes withSummary: Int) {
        setPreferencesFromResource(R.xml.permission_preference, rootKey)
        val pref = findPreference<Preference>(getString(R.string.permission_key))
        pref?.setOnPreferenceClickListener {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST)
            return@setOnPreferenceClickListener true
        }
        pref?.setSummary(withSummary)
    }

    private fun enableMain(rootKey: String?) {
        setPreferencesFromResource(R.xml.main_preference, rootKey)

        val start = prefs.getBoolean(getString(R.string.service_switch_key), false)
        val value = prefs.getString(getString(R.string.sensitivity_key), "")
        setSensitivityInitialSummary()

        val torchIntent = Intent(requireContext(), TorchService::class.java)
        if (start) {
            if (value!!.isNotEmpty()) {
                torchIntent.putExtra(INTENSITY_EXTRA_KEY, value)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(torchIntent)
            } else {
                requireContext().startService(torchIntent)
            }
        } else {
            requireContext().stopService(torchIntent)
        }
    }

    private fun startServiceWithoutExtra() {
        if (isServiceRunning()) {
            stopService()
        }

        if (checkPermission() == STATUS.ENABLED) {
            val intent = Intent(requireContext(), TorchService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(intent)
            } else {
                requireContext().startService(intent)
            }
        }
    }

    private fun startServiceWithExtra(value: String) {
        if (isServiceRunning()) {
            stopService()
        }

        if (checkPermission() == STATUS.ENABLED) {
            val intent = Intent(requireContext(), TorchService::class.java)
            intent.putExtra(INTENSITY_EXTRA_KEY, value)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(intent)
            } else {
                requireContext().startService(intent)
            }
        }
    }

    private fun stopService() {
        requireContext().stopService(Intent(requireContext(), TorchService::class.java))
    }

    private fun isServiceRunning(): Boolean {
        val manager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (TorchService::class.java.name == service.service.className)
                return true
        }
        return false
    }

    private fun checkPermission(): STATUS {
        return when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                STATUS.ENABLED
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                STATUS.RETRY
            }
            else -> {
                STATUS.DISABLED
            }
        }
    }

    private fun setSensitivityInitialSummary() {
        val listPref = findPreference<ListPreference>(getString(R.string.sensitivity_key))
        listPref?.summary = listPref?.value
    }
}

private const val CAMERA_REQUEST = 20

enum class STATUS(val value: Int) {
    ENABLED(0),
    RETRY(1),
    DISABLED(-1)
}