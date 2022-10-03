package com.example.projemanag.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import com.example.projemanag.databinding.ActivityIntroBinding

class IntroActivity : BaseActivity() {
    private var binding:ActivityIntroBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // This is used to get the file from the assets folder and set it to the title textView.
        val typeface:Typeface =
            Typeface.createFromAsset(assets,"carbon bl.ttf")
        binding?.tvAppNameIntro?.typeface = typeface

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

        binding?.btnSignUpIntro?.setOnClickListener{ // Launch the sign up screen.
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        binding?.btnSignInIntro?.setOnClickListener{ // Launch the sign in screen.
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}