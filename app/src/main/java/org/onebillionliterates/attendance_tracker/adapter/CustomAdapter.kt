package org.onebillionliterates.attendance_tracker.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.onebillionliterates.attendance_tracker.R
import org.onebillionliterates.attendance_tracker.model.Teacher
import java.io.IOException


class CustomAdapter(var teacherList : ArrayList<Teacher>) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        fun bindItems(teacher: Teacher) {
            val textViewName = itemView.findViewById(R.id.textViewUsername) as TextView
            val textViewAddress  = itemView.findViewById(R.id.textViewAddress) as TextView
            textViewName.text = teacher.name
            textViewAddress.text = teacher.address

            val imageViewAddress  = itemView.findViewById(R.id.image_view_arrow) as ImageView
            val assetsBitmap:Bitmap? = getBitmapFromAssets("arrow_background.jpg")
            imageViewAddress.setImageBitmap(assetsBitmap)
        }

        fun getBitmapFromAssets(fileName: String): Bitmap? {
            return try {
                BitmapFactory.decodeFile(fileName)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.teachers_list_layout, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return teacherList.size
    }

    override fun onBindViewHolder(holder: CustomAdapter.ViewHolder, position: Int) {
        holder.bindItems(teacherList[position])
    }



}
