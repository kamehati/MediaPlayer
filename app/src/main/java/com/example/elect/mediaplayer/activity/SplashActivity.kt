package com.example.elect.mediaplayer.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.motion.widget.MotionLayout
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.databinding.ActivitySplashBinding
import com.example.elect.mediaplayer.extensions.hasPermission
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySplashBinding

    private var handlePermission: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermission()

        if(hasPermission()){
            runApp()
        }
    }

    private fun checkPermission(){

        handlePermission = registerForActivityResult(

            ActivityResultContracts.RequestPermission()
        ) { permission->
            if (permission) {
                this.recreate()
            } else {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.permission_message)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->

                        val detail = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        detail.apply {

                            addCategory(Intent.CATEGORY_DEFAULT)

                            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                            data = Uri.fromParts("package", packageName, null)
                        }
                        startActivity(detail)

                        Toast.makeText(
                            applicationContext,
                            getString(R.string.permission_ok),
                            Toast.LENGTH_SHORT
                        ).show()

                        dialog.dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.permission_deny),
                            Toast.LENGTH_SHORT
                        ).show()

                        finish()

                        moveTaskToBack(true)
                        dialog.dismiss()
                    }.create()
                    .show()
            }
        }


        if (!hasPermission()) {
            handlePermission?.launch(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun runApp(){

        binding.root.addTransitionListener(
            object : MotionLayout.TransitionListener{

                override fun onTransitionStarted(
                    motionLayout: MotionLayout?,
                    startId: Int,
                    endId: Int
                ) { }


                override fun onTransitionChange(
                    motionLayout: MotionLayout?,
                    startId: Int,
                    endId: Int,
                    progress: Float
                ) { }


                override fun onTransitionCompleted(
                    motionLayout: MotionLayout?,
                    currentId: Int
                ) {
                    startActivity(
                        Intent(
                            this@SplashActivity,
                            MainActivity::class.java
                        )
                    )

                    finish()
                }

                override fun onTransitionTrigger(
                    motionLayout: MotionLayout?,
                    triggerId: Int,
                    positive: Boolean,
                    progress: Float
                ) { }
            }
        )
    }
}