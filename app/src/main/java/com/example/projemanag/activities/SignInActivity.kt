package com.example.projemanag.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.projemanag.R
import com.example.projemanag.databinding.ActivitySignInBinding
import com.example.projemanag.firebase.FireStoreClass
import com.example.projemanag.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInActivity : BaseActivity() {
    private var binding:ActivitySignInBinding? = null
    private lateinit var  auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupActionBar()
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        binding?.btnSignIn?.setOnClickListener{
            signInRegisteredUser()
        }
    }
    //A function for actionBar Setup.
    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarSignInActivity)

        var actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_android_back_24dp)
        }
        binding?.toolbarSignInActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    //A function for Sign-In using the registered user using the email and password.
    private fun signInRegisteredUser(){
        // Here we get the text from editText and trim the space
        val email=binding?.etEmail?.text.toString().trim { it <= ' ' }
        val password=binding?.etPassword?.text.toString().trim { it <= ' ' }
        if(validateForm(email, password)) {
            // Show the progress dialog.
            showProgressDialog()
            // Sign-In using FirebaseAuth
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                hideProgressDialog()
                if (task.isSuccessful) {
//                    val user = auth.currentUser
//                    startActivity(Intent(this,MainActivity::class.java))
                    FireStoreClass().loadUserData(this@SignInActivity)
                } else {
                    Toast.makeText(baseContext, "Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    //A function to validate the entries of a user
    private fun validateForm(email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter an email address")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password")
                false
            }
            else -> {
                true
            }
        }
    }
    //A function to get the user details from the Firestore database after authentication
    fun signInSuccess(user: User) {
        hideProgressDialog()
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }
}