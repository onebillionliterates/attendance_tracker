package org.onebillionliterates.attendance_tracker

import android.app.Activity
import android.app.Activity.*
import android.content.Context
import android.content.Intent
import android.location.Address
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.schibstedspain.leku.*
import com.schibstedspain.leku.locale.SearchZoneRect
import com.schibstedspain.leku.tracker.LocationPickerTracker
import com.schibstedspain.leku.tracker.TrackEvents
import org.onebillionliterates.attendance_tracker.adapter.SchoolsAdapter
import org.onebillionliterates.attendance_tracker.model.School

class SchoolListingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.school_listing)

        val listContainer = findViewById<RecyclerView>(R.id.school_list_container)
        initListContainer(listContainer)

        val buttonAddNew = findViewById<Button>(R.id.add_school_button)
        buttonAddNew.setOnClickListener {
            val locationPickerIntent = LocationPickerActivity.Builder()
                .withLocation(12.9542946, 77.4908551)
                .withSearchZone("en_IN")
                .build(applicationContext)

            startActivityForResult(locationPickerIntent, MAP_BUTTON_REQUEST_CODE)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            Log.d("RESULT****", "OK")
            val latitude = data.getDoubleExtra(LATITUDE, 0.0)
            Log.d("LATITUDE****", latitude.toString())
            val longitude = data.getDoubleExtra(LONGITUDE, 0.0)
            Log.d("LONGITUDE****", longitude.toString())
            val address = data.getStringExtra(LOCATION_ADDRESS)
            Log.d("ADDRESS****", address.toString())
            val postalcode = data.getStringExtra(ZIPCODE)
            Log.d("POSTALCODE****", postalcode.toString())
            val fullAddress = data.getParcelableExtra<Address>(ADDRESS)
            if (fullAddress != null) {
                Log.d("FULL ADDRESS****", fullAddress.toString())
            }
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            Log.d("RESULT****", "CANCELLED")
        }
    }


    private fun initListContainer(listContainer: RecyclerView) {
        listContainer.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        val dummySchools = ArrayList<School>()
        //adding some dummy data to the list
        dummySchools.add(School(name = "Public School", phoneNumber = "987654321"))
        dummySchools.add(School(name = "DPS School", phoneNumber = "7865945534"))
        dummySchools.add(School(name = "Private School", phoneNumber = "123455657"))
        dummySchools.add(School(name = "International School", phoneNumber = "456723456"))
        dummySchools.add(School(name = "Government School", phoneNumber = "7895834567"))

        val adapter = SchoolsAdapter(dummySchools)
        listContainer.adapter = adapter
    }

    companion object {
        private const val MAP_BUTTON_REQUEST_CODE = 1
    }
}
