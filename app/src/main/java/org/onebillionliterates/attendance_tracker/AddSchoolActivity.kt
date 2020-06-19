package org.onebillionliterates.attendance_tracker

import android.app.Activity
import android.content.Intent
import android.location.Address
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.jakewharton.threetenabp.AndroidThreeTen
import com.schibstedspain.leku.*
import com.shasin.notificationbanner.Banner
import kotlinx.android.synthetic.main.school_create.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.onebillionliterates.attendance_tracker.data.AppCore
import org.onebillionliterates.attendance_tracker.data.AppCore.Companion.DEFAULT_LOC
import org.onebillionliterates.attendance_tracker.data.AppCoreException
import org.onebillionliterates.attendance_tracker.data.MESSAGES
import org.onebillionliterates.attendance_tracker.data.School
import org.onebillionliterates.attendance_tracker.drawables.MobileDrawable
import org.onebillionliterates.attendance_tracker.drawables.SchoolSolidDrawable


class AddSchoolActivity : AppCompatActivity() {
    companion object {
        private const val MAP_BUTTON_REQUEST_CODE = 1
    }

    private val TAG = "AddSchool_Activity"
    private lateinit var rootView: View
    private lateinit var embeddedMap: GoogleMap
    private lateinit var school: School
           private var bannerType = Banner.SUCCESS

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidThreeTen.init(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.school_create)
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
        rootView = window.decorView.rootView
        school = School()
        schoolNameIcon.setImageDrawable(SchoolSolidDrawable(this))
        phoneIcon.setImageDrawable(MobileDrawable(this))
        searchSchool.setOnClickListener {
            val locationPickerIntent = LocationPickerActivity.Builder()
                .withLocation(DEFAULT_LOC)
                .withSearchZone("en_IN")
                .build(applicationContext)

            startActivityForResult(locationPickerIntent, MAP_BUTTON_REQUEST_CODE)
        }
        addLocationToMap()
        addSchool.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                createSchoolProgress.visibility = View.VISIBLE

                var message = MESSAGES.SCHOOL_SAVED_MESSAGE.message
                val job = GlobalScope.launch(Dispatchers.IO) {
                    try {
                        school.name = schoolNameEditText.text.toString()
                        school.uniqueCode = uniqueCodeEditText.text.toString()

                        AppCore.instance.saveSchool(school)
                    } catch (exception: Exception) {
                        bannerType = Banner.ERROR
                        message = MESSAGES.DATA_VALIDATION_FAILURE.message
                        if (exception is AppCoreException)
                            message = exception.message

                        Log.e(TAG, "Error during saving school", exception)
                    }
                }
                job.join()
                createSchoolProgress.visibility = View.GONE
                Banner.make(rootView, this@AddSchoolActivity, bannerType, message, Banner.TOP).show()
                Banner.getInstance().bannerView.setOnClickListener {
                    Banner.getInstance().dismissBanner()
                    if (Banner.SUCCESS == bannerType) {
                        this@AddSchoolActivity.finish()
                    }
                }
            }
        }
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
            school.address = fullAddress!!.toString()
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
