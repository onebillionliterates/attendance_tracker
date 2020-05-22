package org.onebillionliterates.attendance_tracker.data

import org.junit.Test

class AppDataTest{

    val appData = AppData();

    @Test
    fun instance_test(){
        val appDataTest = AppData();
    }

    @Test
    fun get_admin_info() {
        val mobileNumber = "8884410287"
        val passcode = 330337

        appData.getAdminInfo(mobileNumber, passcode)
    }

    @Test
    fun get_all_admins() {
        appData.allAdmins();
    }
}
