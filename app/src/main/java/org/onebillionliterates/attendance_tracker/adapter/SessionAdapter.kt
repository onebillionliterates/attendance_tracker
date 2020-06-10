package org.onebillionliterates.attendance_tracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.onebillionliterates.attendance_tracker.R
import org.onebillionliterates.attendance_tracker.SessionCheckinClick
import org.onebillionliterates.attendance_tracker.data.Session


class SessionAdapter(var sessionList: ArrayList<Session>, val sessionClickListener: SessionCheckinClick) :
    RecyclerView.Adapter<SessionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        fun bindItems(session: Session, sessionClickListener: SessionCheckinClick) {
            val textViewName = itemView.findViewById(R.id.textViewUsername) as TextView
            textViewName.text = session.description

            val arrowImageView = itemView.findViewById(R.id.imageViewArrow) as ImageView
            var click=true
            arrowImageView.setOnClickListener {
                if (click) {
                    arrowImageView.setImageResource (R.drawable.arrow_gray);
                    click = false;
                } else {
                    arrowImageView.setImageResource(R.drawable.arrow_blue);
                    click = true;
                }
            }

            itemView.setOnClickListener {
                sessionClickListener.onSessionClicked(session)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.teachers_list_layout, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return sessionList.size
    }

    override fun onBindViewHolder(holder: SessionAdapter.ViewHolder, position: Int) {
        holder.bindItems(sessionList[position], sessionClickListener = sessionClickListener)
    }
}
