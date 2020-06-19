package org.onebillionliterates.attendance_tracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import info.androidhive.fontawesome.FontTextView
import org.onebillionliterates.attendance_tracker.R


class ListingAdapter(
    private val dataList: List<DataHolder>,
    private val singleSelect: Boolean,
    private val bottomDialog: BottomSheetDialog? = null
) :
    RecyclerView.Adapter<ListingAdapter.ViewHolder>() {

    class ViewHolder(
        itemView: View,
        private val showSingleSelect: Boolean,
        private val bottomDialogDismiss: BottomSheetDialog? = null
    ) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(info: DataHolder) {
            val displayTextView = itemView.findViewById(R.id.displayText) as TextView
            val selectedIcon = itemView.findViewById(R.id.cellSelected) as FontTextView
            val arrowRight = itemView.findViewById(R.id.arrowRight) as FontTextView
            val displayCheckboxView = itemView.findViewById(R.id.checkedText) as AppCompatCheckBox

            if (showSingleSelect) {
                displayTextView.visibility = View.VISIBLE
                displayTextView.text = info.displayText
                if (bottomDialogDismiss == null)
                    arrowRight.visibility = View.VISIBLE

                if (info.selected && bottomDialogDismiss != null) selectedIcon.visibility = View.VISIBLE

                itemView.setOnClickListener { view ->
                    info.selected = true
                    bottomDialogDismiss?.dismiss()
                }
                return
            }

            displayCheckboxView.visibility = View.VISIBLE
            displayCheckboxView.text = info.displayText
            displayCheckboxView.isChecked = info.selected
            displayCheckboxView.setOnCheckedChangeListener { _, isChecked ->
                info.selected = isChecked
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.listing_cell, parent, false)
        return ViewHolder(v, singleSelect, bottomDialog)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ListingAdapter.ViewHolder, position: Int) {
        holder.bindItems(dataList[position])
    }


}