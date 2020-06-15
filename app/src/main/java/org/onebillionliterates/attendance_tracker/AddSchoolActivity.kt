package org.onebillionliterates.attendance_tracker

import android.app.Activity
import android.content.Intent
import android.location.Address
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.schibstedspain.leku.*
import kotlinx.android.synthetic.main.school_create.*
import org.onebillionliterates.attendance_tracker.drawables.EmailDrawable
import org.onebillionliterates.attendance_tracker.drawables.MobileDrawable
import org.onebillionliterates.attendance_tracker.drawables.SchoolSolidDrawable

class AddSchoolActivity : AppCompatActivity() {
    companion object {
        private const val MAP_BUTTON_REQUEST_CODE = 1
        private val DEFAULT_LOC = LatLng(12.9542946, 77.4908551)
    }

    private lateinit var embeddedMap:GoogleMap;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.school_create)

        initView()
    }


    private fun initView() {
        schoolNameIcon.setImageDrawable(SchoolSolidDrawable(this))
        phoneIcon.setImageDrawable(MobileDrawable(this))
        emailIcon.setImageDrawable(EmailDrawable(this))
        searchSchool.setOnClickListener {
            val locationPickerIntent = LocationPickerActivity.Builder()
                .withLocation(DEFAULT_LOC)
                .withSearchZone("en_IN")
                .build(applicationContext)

            startActivityForResult(locationPickerIntent, MAP_BUTTON_REQUEST_CODE)
        }

        addLocationToMap()
    }

    private fun addLocationToMap(position: LatLng = DEFAULT_LOC) {
        val mapFragment = schoolAddressDescription as SupportMapFragment
        with(mapFragment) {
            onCreate(null)
            getMapAsync() {
                MapsInitializer.initialize(applicationContext)
                setMapLocation(it, position, "Bangalore")
            }
        }
    }

    private fun setMapLocation(map: GoogleMap, position: LatLng, snippet:String) {
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
            val latitude = data.getDoubleExtra(LATITUDE, DEFAULT_LOC.latitude)
            Log.d("LATITUDE****", latitude.toString())
            val longitude = data.getDoubleExtra(LONGITUDE, DEFAULT_LOC.longitude)
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
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            Log.d("RESULT****", "CANCELLED")
        }
    }
}
