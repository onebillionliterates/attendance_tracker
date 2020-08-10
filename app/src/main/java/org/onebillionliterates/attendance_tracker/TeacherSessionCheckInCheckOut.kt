package org.onebillionliterates.attendance_tracker

import android.os.Bundle
import android.view.MenuItem
import androidx.core.app.NavUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.check_in_out.*
import kotlinx.coroutines.tasks.await
import org.onebillionliterates.attendance_tracker.data.AppCore
import org.onebillionliterates.attendance_tracker.data.School
import org.onebillionliterates.attendance_tracker.data.Session

class TeacherSessionCheckInCheckOut : ActivityUIHandler() {

    private lateinit var embeddedMap: GoogleMap
    private lateinit var school: School
    private lateinit var session: Session
    private lateinit var selectedSession: String
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.check_in_out)
        super.TAG = "TeacherSessionCheckInCheckOut_Activity"
        super.progressTracker = checkInOutProgress

        actionBar?.setDisplayHomeAsUpEnabled(true)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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
        uiHandler({
            selectedSession = intent.extras!!.get("selectedSession") as String
            session = AppCore.instance.fetchSession(selectedSession)!!
            school = AppCore.instance.fetchSchool(session)!!
        }, {
            adjustMap()
        })

        checkin.setOnClickListener() {
            uiHandler({
                val currentTeacherLocation = fusedLocationClient.lastLocation.await()
                AppCore.instance.checkinToSession(currentTeacherLocation!!, school.location!!, session)
            }, {
                showSuccessBanner("Checked-In to session successfully")
            })
        }
        checkout.setOnClickListener() {
            uiHandler({
                AppCore.instance.checkoutFromSession(session)
            }, {
                showSuccessBanner("Checked-Out to session successfully")
            })
        }
    }

    private fun adjustMap() {
        val position = LatLng(school.location!!.latitude, school.location!!.longitude)
        
        val mapFragment = googlemap as SupportMapFragment
        with(mapFragment) {
            onCreate(null)
            getMapAsync() {
                MapsInitializer.initialize(applicationContext)
                if (!::school.isInitialized) {
                    setMapLocation(it, position, "Default location")
                    return@getMapAsync
                }

                val location = school.location!!
                setMapLocation(it, LatLng(location.latitude, location.longitude), school.address!!)
                return@getMapAsync
            }
        }
    }

    private fun setMapLocation(map: GoogleMap, position: LatLng, snippet: String) {
        with(map) {
            embeddedMap = map
            embeddedMap.isMyLocationEnabled = true

            moveCamera(CameraUpdateFactory.newLatLngZoom(position, 13f))
            addMarker(MarkerOptions().position(position).snippet(snippet))
            mapType = GoogleMap.MAP_TYPE_NORMAL
            setOnMapClickListener {
            }
        }
    }
}
