package com.example.projemanag.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projemanag.R
import com.example.projemanag.adapters.TaskListItemsAdapter
import com.example.projemanag.databinding.ActivityTaskListBinding
import com.example.projemanag.firebase.FireStoreClass
import com.example.projemanag.models.Board
import com.example.projemanag.models.Card
import com.example.projemanag.models.Task
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants

@Suppress("DEPRECATION")
class TaskListActivity : BaseActivity() {
    private var binding: ActivityTaskListBinding? = null
    // A global variable for Board Details.
    private lateinit var mBoardDetails:Board
    private lateinit var mBoardDocumentId:String
    lateinit var mAssignedMembersDetailList:ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            mBoardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }
        // Show the progress dialog.
        showProgressDialog()
        FireStoreClass().getBoardDetails(this, mBoardDocumentId)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_members -> {
                val intent = Intent(this,MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)//works : parcelable
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    /**
     * A function to setup action bar
     */
    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarTaskListActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_android_back_white_24dp)
            actionBar.title = mBoardDetails.name
        }
        binding?.toolbarTaskListActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    /**
     * A function to get the result of Board Detail.
     */
    fun boardDetails(board: Board) {
        mBoardDetails = board
        hideProgressDialog()
        setupActionBar()


        showProgressDialog()
        FireStoreClass().getAssignedMembersListDetails(this,mBoardDetails.assignedTo)
    }
    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        showProgressDialog()
        FireStoreClass().getBoardDetails(this,mBoardDetails.documentId)
    }
    fun createTaskList(taskListName:String){
        val task = Task(taskListName,FireStoreClass().getCurrentUserId())
        mBoardDetails.taskList.add(0,task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog()

        FireStoreClass().addUpdateTaskList(this, mBoardDetails)
    }
    fun updateTaskList(position:Int,listName:String,model:Task){
        val task = Task(listName,model.createdBy)
        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        showProgressDialog()
        FireStoreClass().addUpdateTaskList(this,mBoardDetails)
    }
    fun deleteTaskList(position: Int){
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog()

        FireStoreClass().addUpdateTaskList(this, mBoardDetails)
    }
    fun addCardToTaskList(position: Int,cardName:String){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        var cardAssignedUserList:ArrayList<String> = ArrayList()

        cardAssignedUserList.add(FireStoreClass().getCurrentUserId())

        val card = Card(cardName,FireStoreClass().getCurrentUserId(),cardAssignedUserList)

        val cardsList = mBoardDetails.taskList[position].cards
        cardsList.add(card)

        val task = Task(
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardsList
        )

        mBoardDetails.taskList[position] = task

        showProgressDialog()
        FireStoreClass().addUpdateTaskList(this,mBoardDetails)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK
            && requestCode == MEMBERS_REQUEST_CODE
            || requestCode == CARD_DETAILS_REQUEST_CODE){
            showProgressDialog()
            FireStoreClass().getBoardDetails(this, mBoardDocumentId)
        }else{
            Log.e("Cancelled", "Cancelled", )
        }
    }
    fun cardDetails(taskListPosition:Int,cardPosition:Int){
        val intent = Intent(this,CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION,taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION,cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST,mAssignedMembersDetailList)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }
    fun boardMembersDetailsList(list:ArrayList<User>){
        mAssignedMembersDetailList = list
        hideProgressDialog()
        val addTaskList = Task(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)

        binding?.rvTaskList?.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        binding?.rvTaskList?.setHasFixedSize(true)
        val adapter = TaskListItemsAdapter(this,mBoardDetails.taskList)
        binding?.rvTaskList?.adapter = adapter
    }
    fun updateCardsInTaskList(taskListPosition:Int,cards:ArrayList<Card>){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        mBoardDetails.taskList[taskListPosition].cards = cards
        showProgressDialog()
        FireStoreClass().addUpdateTaskList(this,mBoardDetails)
    }
    companion object{
        const val MEMBERS_REQUEST_CODE:Int = 13
        const val CARD_DETAILS_REQUEST_CODE:Int = 14
    }
}