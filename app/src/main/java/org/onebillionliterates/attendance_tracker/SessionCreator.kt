package org.onebillionliterates.attendance_tracker

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jakewharton.threetenabp.AndroidThreeTen
import com.shasin.notificationbanner.Banner
import kotlinx.android.synthetic.main.session_create.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.onebillionliterates.attendance_tracker.adapter.DataHolder
import org.onebillionliterates.attendance_tracker.adapter.SessionBottomSheetAdapter
import org.onebillionliterates.attendance_tracker.data.AppCore
import org.onebillionliterates.attendance_tracker.data.AppCoreException
import org.onebillionliterates.attendance_tracker.data.MESSAGES
import org.onebillionliterates.attendance_tracker.data.Session
import org.onebillionliterates.attendance_tracker.drawables.*
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

class SessionCreator : AppCompatActivity(), View.OnClickListener {
    private var bannerType: Int = Banner.SUCCESS
    private val TAG = "SessionCreator_Activity"
    private val HOUR_12_TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a")
    private lateinit var allSchools: List<DataHolder>
    private lateinit var allTeachers: List<DataHolder>
    private lateinit var allWeekDays: List<DataHolder>
    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidThreeTen.init(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.session_create)
        initView()
    }

    private fun initView() {
        rootView = window.decorView.rootView
        schoolIcon.setImageDrawable(SchoolSolidDrawable(this))
        dayIcon.setImageDrawable(CalendarDaySolidDrawable(this))
        teacherIcon.setImageDrawable(UserDrawable(this))

        listOf(R.id.startDate, R.id.endDate)
            .forEach { mainViewId ->
                run {
                    val view = findViewById<View>(mainViewId).findViewById<View>(R.id.includedDateIcon)
                    val iconImageView = view.findViewById<ImageView>(R.id.drawableIcon)
                    iconImageView.setImageDrawable(DateDrawable(this))
                }
            }
        listOf(R.id.startTime, R.id.endTime)
            .forEach { mainViewId ->
                run {
                    val view = findViewById<View>(mainViewId).findViewById<View>(R.id.includedTimeIcon)
                    val iconImageView = view.findViewById<ImageView>(R.id.drawableIcon)
                    iconImageView.setImageDrawable(TimeDrawable(this))
                }
            }
        listOf(R.id.school, R.id.startDate, R.id.endDate, R.id.startTime, R.id.endTime, R.id.days, R.id.teachers)
            .forEach { mainViewId ->
                run {
                    val view = findViewById<View>(mainViewId).findViewById<View>(R.id.includedRightIcon)
                    val iconImageView = view.findViewById<ImageView>(R.id.drawableIcon)
                    iconImageView.setImageDrawable(RightIconSolidDrawable(this))
                }
            }

        days.setOnClickListener(this)
        teachers.setOnClickListener(this)
        school.setOnClickListener(this)
        startDate.setOnClickListener(this)
        endDate.setOnClickListener(this)
        startTime.setOnClickListener(this)
        endTime.setOnClickListener(this)
        addSession.setOnClickListener(this)

        /**
         * Loading all the data for the drawers
         */
        GlobalScope.launch(Dispatchers.Main) {
            createSessionProgress.visibility = View.VISIBLE

            val job = GlobalScope.launch(Dispatchers.IO) {
                allTeachers = AppCore.instance.fetchTeachersForAdmin()
                    .map { teacher -> DataHolder(id = teacher.id!!, displayText = teacher.name!!) }
                allSchools = AppCore.instance.fetchSchoolsForAdmin()
                    .map { school -> DataHolder(id = school.id!!, displayText = school.name!!) }
                allWeekDays = listOf(
                    DataHolder("1", "Monday"),
                    DataHolder("2", "Tuesday"),
                    DataHolder("3", "Wednesday"),
                    DataHolder("4", "Thursday"),
                    DataHolder("5", "Friday"),
                    DataHolder("6", "Saturday"),
                    DataHolder("7", "Sunday")
                )
            }
            job.join()
            createSessionProgress.visibility = View.GONE
        }

        Banner.getInstance().bannerView.findViewById<View>(R.id.rlCancel).setOnClickListener {
            Banner.getInstance().dismissBanner()
            if (Banner.SUCCESS == bannerType) {
                this@SessionCreator.finish()
            }
        }
    }

    override fun onClick(clickView: View?) {
        when (clickView) {
            days -> {
                openDaysBottomSheet()
            }

            teachers -> {
                openTeachersBottomSheet()
            }

            school -> {
                openSchoolBottomSheet()
            }

            startDate, endDate -> {
                val dialog = BottomSheetDialog(this)
                val datePicker = DatePicker(this)
                val today = LocalDate.now();
                datePicker.init(
                    today.year,
                    today.monthValue - 1,
                    today.dayOfMonth
                ) { _, year, monthOfYear, dayOfMonth ->
                    val dateString = LocalDate.of(year, monthOfYear + 1, dayOfMonth).toString()
                    when (clickView) {
                        startDate -> {
                            startDateSelectTextView.text = dateString
                        }
                        endDate -> {
                            endDateSelectTextView.text = dateString
                        }
                    }
                }
                dialog.setContentView(datePicker)
                dialog.show()
            }
            startTime, endTime -> {
                val dialog = BottomSheetDialog(this)
                val timePicker = TimePicker(this)
                timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
                    val formattedTime = LocalTime.of(hourOfDay, minute).format(HOUR_12_TIME_FORMAT)
                    when (clickView) {
                        startTime -> {
                            startTimeSelectTextView.text = formattedTime
                        }
                        endTime -> {
                            endTimeSelectTextView.text = formattedTime
                        }
                    }
                }
                dialog.setContentView(timePicker)
                dialog.show()
            }
            addSession -> {
                /**
                 * Saving Session
                 */
                GlobalScope.launch(Dispatchers.Main) {
                    createSessionProgress.visibility = View.VISIBLE

                    var message = MESSAGES.SESSION_SAVED_MESSAGE.message
                    val job = GlobalScope.launch(Dispatchers.IO) {
                        try {
                            val schoolData: DataHolder = allSchools.firstOrNull { holder -> holder.selected }!!
                            val teacherRefs: List<String> =
                                allTeachers.filter { holder -> holder.selected }.map { holder -> holder.id!! }
                            val daysSelected: List<Boolean> = allWeekDays.map { holder -> holder.selected }
                            val startTime = LocalTime.parse(startTimeSelectTextView.text, HOUR_12_TIME_FORMAT)!!
                            val endTime = LocalTime.parse(endTimeSelectTextView.text, HOUR_12_TIME_FORMAT)!!
                            val startDate = LocalDate.parse(startDateSelectTextView.text)!!
                            val endDate = LocalDate.parse(endDateSelectTextView.text)!!

                            val savedSession = AppCore.instance.saveSession(
                                Session(
                                    schoolRef = schoolData.id,
                                    teacherRefs = teacherRefs,
                                    weekDaysInfo = daysSelected,
                                    startDate = startDate,
                                    endDate = endDate,
                                    startTime = startTime,
                                    endTime = endTime,
                                    description = "${startTimeSelectTextView.text} @ ${schoolData.displayText}"
                                )
                            )
                            Log.v(TAG, "Session saved successfully - $savedSession")
                        } catch (exception: Exception) {
                            bannerType = Banner.ERROR
                            message = MESSAGES.DATA_VALIDATION_FAILURE.message
                            if (exception is AppCoreException)
                                message = exception.message

                            Log.e(TAG, "Error during adding session", exception)
                        }
                    }
                    job.join()
                    createSessionProgress.visibility = View.GONE

                    Banner.make(rootView, this@SessionCreator, bannerType, message, Banner.TOP).show()
                }
            }
        }
    }

    private fun openDaysBottomSheet() {
        val sheetView = layoutInflater.inflate(R.layout.session_creator_bottom_drawer, null)
        val headerText = sheetView.findViewById<TextView>(R.id.headerText)
        headerText.text = "Select Days"
        val recyclerView = sheetView.findViewById<RecyclerView>(R.id.recyclerview)
        //adding a layoutmanager
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        //creating our adapter
        val adapter = SessionBottomSheetAdapter(allWeekDays, singleSelect = false)

        //now adding the adapter to recyclerview
        recyclerView.adapter = adapter

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(sheetView)
        dialog.show()
        dialog.setOnDismissListener {
            val selectedDays = allWeekDays.filter { dataHolder -> dataHolder.selected }
            if (selectedDays.isNotEmpty()) daySelectTextView.text = "${selectedDays.size} days selected"
        }
    }


    private fun openTeachersBottomSheet() {
        val sheetView = layoutInflater.inflate(R.layout.session_creator_bottom_drawer, null)
        val headerText = sheetView.findViewById<TextView>(R.id.headerText)
        headerText.text = "Select Teachers"
        val recyclerView = sheetView.findViewById<RecyclerView>(R.id.recyclerview)
        //adding a layoutmanager
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        //creating our adapter
        val adapter = SessionBottomSheetAdapter(allTeachers, singleSelect = false)

        //now adding the adapter to recyclerview
        recyclerView.adapter = adapter

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(sheetView)
        dialog.show()
        dialog.setOnDismissListener {
            val selectedTeachers = allTeachers.filter { dataHolder -> dataHolder.selected }
            if (selectedTeachers.isNotEmpty()) teacherSelectTextView.text = "${selectedTeachers.size} teachers selected"
        }
    }

    private fun openSchoolBottomSheet() {
        val sheetView = layoutInflater.inflate(R.layout.session_creator_bottom_drawer, null)
        val headerText = sheetView.findViewById<TextView>(R.id.headerText)
        headerText.text = "Select School"
        val recyclerView = sheetView.findViewById<RecyclerView>(R.id.recyclerview)
        //adding a layoutmanager
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        //crating an arraylist to store users using the data class user
        val dialog = BottomSheetDialog(this)
        //creating our adapter
        val adapter = SessionBottomSheetAdapter(allSchools, singleSelect = true, bottomDialog = dialog)

        //now adding the adapter to recyclerview
        recyclerView.adapter = adapter

        dialog.setContentView(sheetView)
        dialog.show()
        dialog.setOnDismissListener {
            val selectedSchool = allSchools.filter { dataHolder -> dataHolder.selected }.firstOrNull()
            schoolSelectTextView.text = selectedSchool?.displayText ?: schoolSelectTextView.text
        }
    }

}
