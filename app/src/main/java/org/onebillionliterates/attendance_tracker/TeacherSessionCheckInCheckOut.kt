package org.onebillionliterates.attendance_tracker

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.jakewharton.threetenabp.AndroidThreeTen
import com.shasin.notificationbanner.Banner
import kotlinx.android.synthetic.main.check_in_out.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.onebillionliterates.attendance_tracker.data.*

class TeacherSessionCheckInCheckOut : AppCompatActivity() {

    private val TAG = "AddSchool_Activity"
    private lateinit var rootView: View
    private lateinit var embeddedMap: GoogleMap
    private lateinit var school: School
    private lateinit var session: Session
    private lateinit var sessionRef: String
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidThreeTen.init(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.check_in_out)
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
        rootView = window.decorView.rootView
        adjustMap()

        uiHandler({
            val selectedSession = "qKVe8Q7qj6QHjW0RrvPx"//intent.extras!!.get("selectedSession") as String
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
                Banner.make(
                    rootView,
                    this@TeacherSessionCheckInCheckOut,
                    Banner.SUCCESS,
                    "Checked-In to session successfully",
                    Banner.TOP
                ).show()
                Banner.getInstance().bannerView.setOnClickListener {
                    Banner.getInstance().dismissBanner()
                }
            })
        }
        checkout.setOnClickListener() {
            uiHandler({
                AppCore.instance.checkoutFromSession(session)
            }, {
                Banner.make(
                    rootView,
                    this@TeacherSessionCheckInCheckOut,
                    Banner.SUCCESS,
                    "Checked-out from session successfully",
                    Banner.TOP
                ).show()
                Banner.getInstance().bannerView.setOnClickListener {
                    Banner.getInstance().dismissBanner()
                }
            })
        }
    }

    private fun uiHandler(beforeBlock: suspend () -> Unit, onUIBlock: suspend () -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            checkInOutProgress.visibility = View.VISIBLE

            var message: String? = null
            var bannerType: Int? = null
            val job = GlobalScope.launch(Dispatchers.IO) {
                try {
                    beforeBlock.invoke()
                } catch (exception: Exception) {
                    bannerType = Banner.ERROR
                    message = MESSAGES.DATA_OPERATION_FAILURE.message
                    if (exception is AppCoreWarnException)
                        bannerType = Banner.WARNING

                    if (exception is AppCoreException || exception is AppCoreWarnException)
                        message = exception.message!!

                    Log.e(TAG, "Error during saving school", exception)
                }
            }
            job.join()
            checkInOutProgress.visibility = View.GONE
            if (message != null) {
                Banner.make(rootView, this@TeacherSessionCheckInCheckOut, bannerType!!, message, Banner.TOP).show()
                Banner.getInstance().bannerView.setOnClickListener {
                    Banner.getInstance().dismissBanner()
                }
            }
            if (message == null) {
                onUIBlock.invoke()
            }
        }
    }

    private fun adjustMap() {
        val mapFragment = googlemap as SupportMapFragment
        with(mapFragment) {
            onCreate(null)
            getMapAsync() {
                MapsInitializer.initialize(applicationContext)
                if (!::school.isInitialized) {
                    setMapLocation(it, AppCore.DEFAULT_LOC, "Bangalore")
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
