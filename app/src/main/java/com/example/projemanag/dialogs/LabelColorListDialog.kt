package com.example.projemanag.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projemanag.R
import com.example.projemanag.adapters.LabelColorListItemsAdapter
import com.example.projemanag.databinding.DialogListBinding

abstract class LabelColorListDialog(
    context: Context,
    private var list: ArrayList<String>,
    private val title: String = "",
    private var mSelectedColor: String = ""
) : Dialog(context) {
    private var adapter: LabelColorListItemsAdapter? = null
    private var binding:DialogListBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogListBinding.inflate(layoutInflater)
            //LayoutInflater.from(context).inflate(R.layout.dialog_list, null)
        setContentView(binding?.root!!)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(binding!!)
    }
    private fun setUpRecyclerView(binding: DialogListBinding){
        binding.tvTitle.text = title
        binding.rvList.layoutManager = LinearLayoutManager(context)
        adapter = LabelColorListItemsAdapter(context,list, mSelectedColor)
        binding.rvList.adapter = adapter
        adapter!!.onItemClickListener = object : LabelColorListItemsAdapter.OnItemClickListener{
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }
        }
    }
    protected abstract fun onItemSelected(color:String)
}