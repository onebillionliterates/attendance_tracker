package org.onebillionliterates.attendance_tracker.data

import android.location.Location
import org.threeten.bp.LocalDateTime

class School(
    val id: String? = null,
    var adminRef: String? = null,
    var name: String? = null,
    var uniqueCode: String? = null,
    var address: String? = null,
    var postalCode: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var location: Location? = null
)
