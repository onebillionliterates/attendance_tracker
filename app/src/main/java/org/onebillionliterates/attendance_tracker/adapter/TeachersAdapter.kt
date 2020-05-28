package org.onebillionliterates.attendance_tracker.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.onebillionliterates.attendance_tracker.R
import org.onebillionliterates.attendance_tracker.model.Teacher

class TeachersAdapter(val userList: ArrayList<Teacher>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.teachers_list_layout, parent, false)
        return CustomAdapter.ViewHolder(v)
    }

    override fun onBindViewHolder(holder: CustomAdapter.ViewHolder, position: Int) {
        holder.bindItems(userList[position])
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(teacher: Teacher) {
            val textViewName = itemView.findViewById(R.id.textViewUsername) as TextView
            textViewName.text = teacher.name
        }
    }
}