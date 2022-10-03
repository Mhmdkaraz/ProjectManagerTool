package com.example.projemanag.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.adapters.BoardItemsAdapter
import com.example.projemanag.databinding.ActivityMainBinding
import com.example.projemanag.firebase.FireStoreClass
import com.example.projemanag.models.Board
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging

@Suppress("DEPRECATION")
class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var binding: ActivityMainBinding? = null
    /**
     * A companion object to declare the constants.
     */
    companion object{
        //A unique code for starting the activity for result
        const val MY_PROFILE_REQUEST_CODE:Int = 11
        const val CREATE_BOARD_REQUEST_CODE = 12
    }
    // A global variable for User Name
    private lateinit var mUserName:String
    private lateinit var mSharedPreferences:SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()
        mSharedPreferences = this.getSharedPreferences(Constants.PROJEMANAG_PREFERENCES,Context.MODE_PRIVATE)
        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED,false)

        if(tokenUpdated){
            showProgressDialog()
            FireStoreClass().loadUserData(this,true)
        }else{
           FirebaseMessaging.getInstance()
               .token.addOnSuccessListener(this@MainActivity) {
                   updateFCMToken(it)
               }
        }


        FireStoreClass().loadUserData(this,true)
        // Assign the NavigationView.OnNavigationItemSelectedListener to navigation view.
        binding?.navView?.setNavigationItemSelectedListener(this)

        binding?.appBarMain?.fabCreateBoard?.setOnClickListener{
            val intent = Intent(this,CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME,mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
    }

    override fun onBackPressed() {
        if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        } else {
            // A double back press function is added in Base Activity.
            doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                startActivityForResult(Intent(this,MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out -> {
                // Here sign outs the user from firebase in this device.
                FirebaseAuth.getInstance().signOut()
                mSharedPreferences.edit().clear().apply()
                // Send the user to the intro screen of the application.
                val intent = Intent(this,IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE){
            // Get the user updated details.
            FireStoreClass().loadUserData(this)
        }else if(resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE){
            // Get the latest boards list.
            FireStoreClass().getBoardsList(this)
        }
        else{
            Log.e("Cancelled", "Cancelled")
        }
    }
    /**
     * A function to setup action bar
     */
    private fun setupActionBar() {
        setSupportActionBar(binding?.appBarMain?.toolbarMainActivity)
        binding?.appBarMain?.toolbarMainActivity?.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        binding?.appBarMain?.toolbarMainActivity?.setNavigationOnClickListener {
            toggleDrawer()
        }
    }
    /**
     * A function for opening and closing the Navigation Drawer.
     */
    private fun toggleDrawer() {
        if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        } else {
            binding?.drawerLayout?.openDrawer(GravityCompat.START)
        }
    }
    /**
     * A function to get the current user details from firebase.
     */
    fun updateNavigationUserDetails(user: User,readBoardsList:Boolean) {
        hideProgressDialog()
        mUserName = user.name

        // The instance of the header view of the navigation view.
        val headerView = binding?.navView?.getHeaderView(0)
        // The instance of the user image of the navigation view.
        val navUserImage = headerView?.findViewById<ImageView>(R.id.iv_user_image)
        // Load the user image in the ImageView.
        Glide
            .with(this )
            .load(user.image)// URL of the image
            .centerCrop()// Scale type of the image.
            .placeholder(R.drawable.ic_user_place_holder)// A default place holder
            .into(navUserImage!!)// the view in which the image will be loaded.

        // The instance of the user name TextView of the navigation view.
        val navUsername = headerView.findViewById<TextView>(R.id.tv_username)
        // Set the user name
        navUsername.text = user.name

        if(readBoardsList){
            // Show the progress dialog.
            showProgressDialog()
            FireStoreClass().getBoardsList(this)
        }
    }
    /**
     * A function to populate the result of BOARDS list in the UI i.e in the recyclerView.
     */
    fun populateBoardListToUI(boardsList:ArrayList<Board>){
        hideProgressDialog()
        if(boardsList.size > 0){
            binding?.appBarMain?.mainContent?.rvBoardsList?.visibility = View.VISIBLE
            binding?.appBarMain?.mainContent?.tvNoBoardsAvailable?.visibility = View.GONE
            binding?.appBarMain?.mainContent?.rvBoardsList?.layoutManager = LinearLayoutManager(this)
            binding?.appBarMain?.mainContent?.rvBoardsList?.setHasFixedSize(true)
            // Create an instance of BoardItemsAdapter and pass the boardList to it.
            val adapter = BoardItemsAdapter(this,boardsList)
            // Attach the adapter to the recyclerView.
            binding?.appBarMain?.mainContent?.rvBoardsList?.adapter = adapter
            adapter.setOnClickListener(object :BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity,TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID,model.documentId)
                    startActivity(intent)
                }
            })
        }else{
            binding?.appBarMain?.mainContent?.rvBoardsList?.visibility = View.GONE
            binding?.appBarMain?.mainContent?.tvNoBoardsAvailable?.visibility = View.VISIBLE

        }
    }

    fun tokenUpdateSuccess() {
        hideProgressDialog()
        val editor:SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED,true)
        editor.apply()
        showProgressDialog()
        FireStoreClass().loadUserData(this,true)
    }
    private fun updateFCMToken(token:String){
        val userHashMap = HashMap<String,Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        showProgressDialog()
        FireStoreClass().updateUserProfileData(this,userHashMap)

    }

}