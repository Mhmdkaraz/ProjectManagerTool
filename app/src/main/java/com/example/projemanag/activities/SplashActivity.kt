package com.example.projemanag.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.projemanag.databinding.ActivitySplashBinding
import com.example.projemanag.firebase.FireStoreClass


class SplashActivity : AppCompatActivity() {
    private var binding: ActivitySplashBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        // This is used to hide the status bar and make the splash screen as a full screen activity.
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        // This is used to get the file from the assets folder and set it to the title textView.
        val typeFace: Typeface = Typeface.createFromAsset(assets, "carbon bl.ttf")
        binding?.tvAppName?.typeface = typeFace

        // Adding the handler to after the a task after some delay.
        Handler(Looper.getMainLooper()).postDelayed({
            // Here if the user is signed in once and not signed out again from the app. So next time while coming into the app
            // we will redirect him to MainScreen or else to the Intro Screen as it was before.

            // Get the current user id
            val currentUserID = FireStoreClass().getCurrentUserId()
            if (currentUserID.isNotEmpty()) {// Start the Main Activity
                startActivity(Intent(this, MainActivity::class.java))
            } else {// Start the Intro Activity
                startActivity(Intent(this, IntroActivity::class.java))
            }
            finish()// Call this when your activity is done and should be closed.
        }, 2500)// Here we pass the delay time in milliSeconds after which the splash activity will disappear.

    }
}