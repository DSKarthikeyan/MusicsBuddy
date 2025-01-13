package com.dsk.musicbuddy.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.viewpager.widget.ViewPager
import com.dsk.musicbuddy.R
import com.dsk.musicbuddy.databinding.ActivityMainBinding
import com.dsk.musicbuddy.ui.adapter.ViewStatePageAdapter
import com.dsk.musicbuddy.ui.view.LibraryFragment
import com.dsk.musicbuddy.ui.view.PlayListFragment
import com.dsk.musicbuddy.ui.viewmodel.MusicPlayerViewModel
import com.dsk.musicbuddy.util.PermissionHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var textViewSettings: TextView
    private lateinit var fabPlayList: FloatingActionButton
    private lateinit var fabShuffle: FloatingActionButton

    private val musicPlayerViewModel: MusicPlayerViewModel by viewModels()
    private var isSettingsDialogShown = false

    private val permissionRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        handlePermissionsResult(permissions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")

        if (isSettingsDialogShown) {
            isSettingsDialogShown = false
            updateUIBasedOnPermissions()
        } else {
            checkAndRequestPermissions()
        }
    }

    private fun setupUI() {
        tabLayout = binding.tabs
        viewPager = binding.viewPager
        setUpViewPager()

        textViewSettings = binding.textViewSettings
        textViewSettings.setOnClickListener { PermissionHelper.showSettingsDialog(this) }
        enableEdgeToEdge()
    }

    private fun setUpViewPager() {
        binding.viewPager.adapter = ViewStatePageAdapter(supportFragmentManager).apply {
            addFragmentTabDetails(LibraryFragment(), getString(R.string.text_music))
            addFragmentTabDetails(PlayListFragment(), getString(R.string.text_playLists))
        }
        binding.tabs.setupWithViewPager(binding.viewPager)
    }

    private fun enableEdgeToEdge() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        window.decorView.setOnApplyWindowInsetsListener { _, insets ->
            val statusBarInsets = insets.systemWindowInsetTop
            binding.tabs.updatePadding(top = statusBarInsets)
            insets
        }
    }

    private fun checkAndRequestPermissions() {
        val requiredPermissions = PermissionHelper.getRequiredPermissions()
        val deniedPermissions = requiredPermissions.filterNot {
            PermissionHelper.checkPermission(this, it)
        }

        if (deniedPermissions.isEmpty() && checkWritePermission(this)) {
            onAllPermissionsGranted()
        } else {
            permissionRequestLauncher.launch(requiredPermissions)
        }
    }

    private fun checkWritePermission(context: Activity): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED
        }
        if (!permission) {
            Log.d(TAG, "WRITE_SETTINGS permission is not granted.")
        }
        return permission
    }

    private fun handlePermissionsResult(permissions: Map<String, Boolean>) {
        val grantedPermissions = permissions.filterValues { it }.keys
        val deniedPermissions = permissions.filterValues { !it }.keys

        if (grantedPermissions.isEmpty() || !checkWritePermission(this)) {
            onAllPermissionsDenied()
            return
        }

        when {
            grantedPermissions.containsAll(PermissionHelper.getRequiredPermissions().toSet()) -> {
                onAllPermissionsGranted()
            }
            grantedPermissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE) &&
                    grantedPermissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                onReadWritePermissionGranted()
            }
            grantedPermissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                onReadPermissionGranted()
            }
            grantedPermissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                onWritePermissionGranted()
            }
        }

        if (deniedPermissions.isNotEmpty()) {
            onPartialPermissionsDenied(deniedPermissions)
        }
    }

    private fun updateUIBasedOnPermissions() {
        val requiredPermissions = PermissionHelper.getRequiredPermissions()
        val deniedPermissions = requiredPermissions.filterNot {
            PermissionHelper.checkPermission(this, it)
        }

        if (deniedPermissions.isEmpty()) {
            if (checkWritePermission(this)) {
                onAllPermissionsGranted()
            } else {
                showWritePermissionDialog()
            }
        } else {
            showSystemSettingsMessage()
        }
    }

    private fun onAllPermissionsGranted() {
        Log.d(TAG, "All permissions granted.")
        hideSystemSettingsMessage()
        initializeFeatures()
    }

    private fun onReadWritePermissionGranted() {
        Log.d(TAG, "Both READ and WRITE permissions granted.")
        hideSystemSettingsMessage()
        initializeFeatures()
    }

    private fun onReadPermissionGranted() {
        Log.d(TAG, "READ permission granted.")
        showLimitedFeaturesWarning("Write access is required for full functionality.")
    }

    private fun onWritePermissionGranted() {
        Log.d(TAG, "WRITE permission granted.")
        showLimitedFeaturesWarning("Read access is required for full functionality.")
    }

    private fun onPartialPermissionsDenied(deniedPermissions: Collection<String>) {
        Log.d(TAG, "Partial permissions denied: $deniedPermissions")
        showSystemSettingsMessage()
    }

    private fun onAllPermissionsDenied() {
        Log.d(TAG, "All permissions denied.")
        if (!isSettingsDialogShown) {
            PermissionHelper.showSettingsDialog(this)
            isSettingsDialogShown = true
        } else {
            showSystemSettingsMessage()
        }
    }

    private fun initializeFeatures() {
        Log.d(TAG, "Initializing full features.")
        setupUI()
    }

    private fun showSystemSettingsMessage() {
        textViewSettings.visibility = View.VISIBLE
        textViewSettings.text = getString(R.string.text_app_settings) + " " +
                getString(R.string.text_go_to_settings)
    }

    private fun hideSystemSettingsMessage() {
        textViewSettings.visibility = View.GONE
    }

    private fun showWritePermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("System Settings Permission")
            .setMessage("Write access to system settings is required for full functionality. Please grant this permission.")
            .setPositiveButton("Grant") { _, _ ->
                isSettingsDialogShown = true
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .setCancelable(false)
            .show()
    }

    private fun showLimitedFeaturesWarning(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Limited Functionality")
            .setMessage(message)
            .setPositiveButton("Go to Settings") { _, _ ->
                PermissionHelper.showSettingsDialog(this)
                isSettingsDialogShown = true
            }
            .setNegativeButton("Cancel", null)
            .setCancelable(false)
            .show()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val MULTIPLE_PERMISSIONS = 7
    }
}
