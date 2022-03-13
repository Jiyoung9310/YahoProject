package com.climbing.yaho.screen

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.climbing.yaho.BuildConfig
import com.climbing.yaho.R
import com.climbing.yaho.base.BindingActivity
import com.climbing.yaho.databinding.ActivityIntroBinding
import com.climbing.yaho.local.YahoPreference
import com.climbing.yaho.ui.LocationPermissionDialog
import com.climbing.yaho.viewmodel.IntroViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class IntroActivity : BindingActivity<ActivityIntroBinding>(ActivityIntroBinding::inflate) {

    @Inject
    lateinit var yahoPreference: YahoPreference
    private val viewModel by viewModels<IntroViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(!yahoPreference.isConfirm) {
            LocationPermissionDialog(this) {
                yahoPreference.isConfirm = true
                viewModel.checkPermissions()
            }.show()
        } else {
            viewModel.checkPermissions()
        }

        initObserver()
    }

    private fun initObserver() {
        viewModel.goToHome.observe(this) {
            // go to home screen
            startActivity(Intent(this@IntroActivity, HomeActivity::class.java))
            finish()
        }

        viewModel.goToLogin.observe(this) {
            // go to home screen
            startActivity(Intent(this@IntroActivity, LoginActivity::class.java))
            finish()
        }

        viewModel.checkPermissions.observe(this) {
            if(!it) {
                ActivityCompat.requestPermissions(
                    this,
                    ReadyActivity.PERMISSIONS,
                    ReadyActivity.PERMISSION_REQUEST_CODE
                )
            } else {
                viewModel.startIDCheck()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == ReadyActivity.PERMISSION_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> {
                    // If user interaction was interrupted, the permission request is cancelled and you receive empty arrays.

                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    // Permission was granted.
                    viewModel.startIDCheck()
                }
                else -> {
                    Snackbar.make(
                        binding.root, R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(
                            R.string.settings
                        ) { // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID, null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }

        } else {
            Toast.makeText(this, "앱 사용을 위해서 위치 정보가 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}