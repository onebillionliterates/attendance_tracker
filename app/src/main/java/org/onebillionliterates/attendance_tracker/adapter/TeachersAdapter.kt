package org.onebillionliterates.attendance_tracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.onebillionliterates.attendance_tracker.R
import org.onebillionliterates.attendance_tracker.model.Teacher


class TeachersAdapter(private var teachersList : ArrayList<Teacher>) :
    RecyclerView.Adapter<TeachersAdapter.ViewHolder>() {

    open class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        open fun bindItems(teacher: Teacher) {
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
                println("clicked")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.teachers_list_layout, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return teachersList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(teachersList[position])
    }
}
