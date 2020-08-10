package org.onebillionliterates.attendance_tracker

import android.app.Activity
import android.content.Intent
import android.location.Address
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.core.app.NavUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.schibstedspain.leku.*
import kotlinx.android.synthetic.main.school_create.*
import org.onebillionliterates.attendance_tracker.ActivityRequestCodes.SCHOOL_MAP_ACTIVITY
import org.onebillionliterates.attendance_tracker.data.AppCore
import org.onebillionliterates.attendance_tracker.data.MESSAGES.SCHOOL_SAVED_MESSAGE
import org.onebillionliterates.attendance_tracker.data.School
import org.onebillionliterates.attendance_tracker.drawables.MobileDrawable
import org.onebillionliterates.attendance_tracker.drawables.SchoolSolidDrawable


class AddSchoolActivity : ActivityUIHandler() {
    private lateinit var embeddedMap: GoogleMap
    private lateinit var school: School

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.school_create)
        this.TAG = "AddSchool_Activity"
        this.progressTracker = createSchoolProgress

        actionBar?.setDisplayHomeAsUpEnabled(true)
        initView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun initView() {
        schoolNameIcon.setImageDrawable(SchoolSolidDrawable(this))
        phoneIcon.setImageDrawable(MobileDrawable(this))
        uiHandler(
            {
                val schoolId = intent.extras?.getString(ActivityRequestCodes.SCHOOL_UPDATE_ACTIVITY.name)
                if (schoolId.isNullOrEmpty()) {
                    // Setting Bangalore as Default location
                    val defaultLocation = Location("DEFAULT")
                    defaultLocation.latitude = 12.9542946 
                    defaultLocation.longitude = 77.4908551
                    
                    school = School(
                        location = defaultLocation
                    )
                    return@uiHandler
                }
                
                school = AppCore.instance.loadSchool(schoolId)
            },
            {
                if(!school.name.isNullOrEmpty() && !school.uniqueCode.isNullOrEmpty()){
                    schoolNameEditText.setText(school.name)
                    uniqueCodeEditText.setText(school.uniqueCode)
                }
                
                addLocationToMap()

                addSchool.setOnClickListener {
                    uiHandler(
                        {
                            school.name = schoolNameEditText.text.toString()
                            school.uniqueCode = uniqueCodeEditText.text.toString()
                            AppCore.instance.saveOrUpdate(school)
                        },
                        {
                            showSuccessBanner(SCHOOL_SAVED_MESSAGE.message)
                        }
                    )
                }
            }
        )
    }

    private fun addLocationToMap() {
        val position = LatLng(school.location!!.latitude, school.location!!.longitude)
        
        val mapFragment = schoolAddressDescription as SupportMapFragment
        with(mapFragment) {
            onCreate(null)
            getMapAsync() {
                MapsInitializer.initialize(applicationContext)
                setMapLocation(it, position, "Default location")
            }
        }

        searchSchool.setOnClickListener {
            val locationPickerIntent = LocationPickerActivity.Builder()
                .withLocation(position)
                .withSearchZone("en_IN")
                .build(applicationContext)

            startActivityForResult(locationPickerIntent, SCHOOL_MAP_ACTIVITY.requestCode)
        }
    }

    private fun setMapLocation(map: GoogleMap, position: LatLng, snippet: String) {
        with(map) {
            embeddedMap = map
            moveCamera(CameraUpdateFactory.newLatLngZoom(position, 13f))
            addMarker(MarkerOptions().position(position).snippet(snippet))
            mapType = GoogleMap.MAP_TYPE_NORMAL
            setOnMapClickListener {
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            Log.d("RESULT****", "OK")
            val latitude = data.getDoubleExtra(LATITUDE, school.location!!.latitude)
            Log.d("LATITUDE****", latitude.toString())
            val longitude = data.getDoubleExtra(LONGITUDE, school.location!!.longitude)
            Log.d("LONGITUDE****", longitude.toString())
            val address = data.getStringExtra(LOCATION_ADDRESS)
            Log.d("ADDRESS****", address.toString())
            val postalcode = data.getStringExtra(ZIPCODE)
            Log.d("POSTALCODE****", postalcode.toString())
            val fullAddress = data.getParcelableExtra<Address>(ADDRESS)
            if (fullAddress != null) {
                Log.d("FULL ADDRESS****", fullAddress.toString())
            }

            setMapLocation(embeddedMap, position = LatLng(latitude, longitude), snippet = address)
            school.address = address.toString()
            school.location = Location("SELECTED_LOCATION")
            school.location!!.longitude = longitude
            school.location!!.latitude = latitude
            school.postalCode = postalcode!!
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            Log.d("RESULT****", "CANCELLED")
        }
    }
}
