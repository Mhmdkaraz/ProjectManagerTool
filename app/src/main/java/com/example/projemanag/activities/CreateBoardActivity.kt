package com.example.projemanag.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.databinding.ActivityCreateBoardBinding
import com.example.projemanag.firebase.FireStoreClass
import com.example.projemanag.models.Board
import com.example.projemanag.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

@Suppress("DEPRECATION")
class CreateBoardActivity : BaseActivity() {
    private var binding:ActivityCreateBoardBinding? = null
    // Add a global variable for URI of a selected image from phone storage.
    private var mSelectedImageFileUri:Uri? = null
    // A global variable for Username
    private lateinit var mUserName:String
    // A global variable for a board image URL
    private var mBoardImageURL:String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupActionBar()
        if(intent.hasExtra(Constants.NAME)){
            mUserName = intent.getStringExtra(Constants.NAME)!!
        }
        binding?.ivBoardImage?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this)
            } else {
                /*Requests permissions to be granted to this application. These permissions
              must be requested in your manifest, they should not be granted to your app,
              and they should have protection level*/
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }
        binding?.btnCreate?.setOnClickListener {
            // Here if the image is not selected then update the other details of user.
            if(mSelectedImageFileUri != null){
                uploadBoardImage()
            }else{
                showProgressDialog()
                // Call a function to update create a board.
                createBoard()
            }
        }
    }
    /**
     * This function will notify the user after tapping on allow or deny
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            //If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
            }
        } else {
            //Displaying another toast if permission is not granted
            Toast.makeText(
                this,
                "Oops, you just denied the permission for storage. You can allow it from settings.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null
        ) {
            mSelectedImageFileUri = data.data!!
            try {
                // Load the board image in the ImageView.
                Glide
                    .with(this)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(binding?.ivBoardImage!!)

            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }
    /**
     * A function to setup action bar
     */
    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarCreateBoardActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_android_back_white_24dp)
            actionBar.title = resources.getString(R.string.create_board_title)
        }
        binding?.toolbarCreateBoardActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    /**
     * A function to upload the Board Image to storage and getting the downloadable URL of the image.
     */
    private fun uploadBoardImage(){
        showProgressDialog()
        //getting the storage reference
        val sRef: StorageReference =
            FirebaseStorage.getInstance()
                .reference
                .child(
                    "BOARD_IMAGE" + System.currentTimeMillis()
                            + "." + Constants.getFileExtension(this,mSelectedImageFileUri)
                )
        //adding the file to reference
        sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener { taskSnapShot ->
            // The image upload is success
            Log.i(
                "Board Image URL",
                taskSnapShot.metadata!!.reference!!.downloadUrl.toString()
            )
            // Get the downloadable url from the task snapshot
            taskSnapShot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                Log.i("Downloadable Image URL", uri.toString())
                // assign the image url to the variable.
                mBoardImageURL = uri.toString()
                hideProgressDialog()
                // Call a function to create the board.
                createBoard()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(
                this@CreateBoardActivity,
                exception.message,
                Toast.LENGTH_LONG
            ).show()
            hideProgressDialog()
        }
    }
    /**
     * A function to make an entry of a board in the database.
     */
    private fun createBoard(){
        //  A list is created to add the assigned menu_members.
        //  This can be modified later on as of now the user itself will be the member of the board.
        val assignedUsersArrayList:ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserID())
        // Creating the instance of the Board and adding the values as per parameters.
        var board = Board(
            binding?.etBoardName?.text.toString(),
            mBoardImageURL,
            mUserName,
            assignedUsersArrayList
        )
        FireStoreClass().createBoard(this,board)
    }
    /**
     * A function for notifying the board is created successfully.
     */
    fun boardCreatedSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}