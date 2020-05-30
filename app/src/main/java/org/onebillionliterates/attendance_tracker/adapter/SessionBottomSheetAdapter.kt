package org.onebillionliterates.attendance_tracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.RecyclerView
import org.onebillionliterates.attendance_tracker.R


class SessionBottomSheetAdapter(private val dataList : List<DataHolder>, private val singleSelect: Boolean) :
    RecyclerView.Adapter<SessionBottomSheetAdapter.ViewHolder>() {

    class ViewHolder(
        itemView: View,
        private val showSingleSelect: Boolean
    ) : RecyclerView.ViewHolder(itemView){

        fun bindItems(info: DataHolder) {
            val displayTextView = itemView.findViewById(R.id.displayText) as TextView
            val displayCheckboxView = itemView.findViewById(R.id.checkedText) as AppCompatCheckBox

            if(showSingleSelect){
                displayTextView.visibility = View.VISIBLE
                displayTextView.text = info.displayText
                return
            }

            displayCheckboxView.visibility = View.VISIBLE
            displayCheckboxView.text = info.displayText
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionBottomSheetAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.session_create_bottom_drawer_cell, parent, false)
        return ViewHolder(v, singleSelect)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: SessionBottomSheetAdapter.ViewHolder, position: Int) {
        holder.bindItems(dataList[position])
    }



}
