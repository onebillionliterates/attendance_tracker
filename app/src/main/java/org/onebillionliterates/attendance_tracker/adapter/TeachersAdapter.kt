package org.onebillionliterates.attendance_tracker.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.onebillionliterates.attendance_tracker.R
import org.onebillionliterates.attendance_tracker.data.Teacher
import org.onebillionliterates.attendance_tracker.util.TeachersViewUtils


class TeachersAdapter(act: Activity, var teachersList : MutableList<Teacher>) :
    RecyclerView.Adapter<TeachersAdapter.ViewHolder>() {

    var activity: Activity=act

    open class ViewHolder(act: Activity, con: Context, itemView: View) : RecyclerView.ViewHolder(itemView){

        val context: Context = con
        var activity: Activity=act

        open fun bindItems(teachersList : MutableList<Teacher>, index : Int) {
            val teacher= teachersList[index]

            val textViewName = itemView.findViewById(R.id.textViewUsername) as TextView
            textViewName.text = teacher.name

            val arrowImageView = itemView.findViewById(R.id.imageViewArrow) as ImageView
            var click=true
            arrowImageView.setOnClickListener {
                if (click) {
                    arrowImageView.setImageResource (R.drawable.arrow_gray);
                    click = false;
                } else {
                    arrowImageView.setImageResource (R.drawable.arrow_blue);
                    click = true;
                }
                TeachersViewUtils.populateBottomSheet(activity, context, R.layout.teachers_drawer, teachersList, index)
                println("clicked")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.teachers_list_layout, parent, false)
        return ViewHolder(activity, parent.context,  v)
    }

    override fun getItemCount(): Int {
        return teachersList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(teachersList, position)
    }
}
