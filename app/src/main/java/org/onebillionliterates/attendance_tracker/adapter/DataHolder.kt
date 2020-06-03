package org.onebillionliterates.attendance_tracker.adapter

import android.os.Parcel
import android.os.Parcelable

class DataHolder(
    val id: String?,
    val displayText: String?,
    var selected: Boolean = false
):Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(displayText)
        parcel.writeByte(if (selected) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataHolder> {
        override fun createFromParcel(parcel: Parcel): DataHolder {
            return DataHolder(parcel)
        }

        override fun newArray(size: Int): Array<DataHolder?> {
            return arrayOfNulls(size)
        }
    }
}
