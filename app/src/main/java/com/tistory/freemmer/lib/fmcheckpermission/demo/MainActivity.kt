package com.tistory.freemmer.lib.fmcheckpermission.demo

import android.Manifest
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import com.tistory.freemmer.lib.fmcheckpermission.FMCheckPermissionActivity

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : FMCheckPermissionActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        btnCheckPermission.setOnClickListener {
            checkPermission(arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SYSTEM_ALERT_WINDOW
            ))
        }

        btnCheckPermissionSystemAlertWindow.setOnClickListener {
            checkPermission(arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SYSTEM_ALERT_WINDOW
            ), this.packageName)
        }

    }

    override fun onRequestPermissionRationale(permissions: List<String>) {
        Log.d(MainActivity::class.java.simpleName
            , "Request Permission Rationale(In case. checked 'Don't ask again') : $permissions")
        Snackbar
            .make(btnCheckPermission, "Request Permission Rationale(In case. checked 'Don't ask again') : $permissions"
                , Snackbar.LENGTH_LONG)
            .setAction("move setting") { moveSetting(this.packageName) }
            .show()
    }

    override fun onDeniedRequestPermission(permissions: List<String>) {
        Log.d(MainActivity::class.java.simpleName, "Denied Request Permission : $permissions")
        Snackbar
            .make(btnCheckPermission, "Denied Request Permission : $permissions", Snackbar.LENGTH_SHORT)
            .show()
    }

    override fun onGrantedRequestPermission() {
        Log.d(MainActivity::class.java.simpleName, "Granted Request Permission")
        Snackbar
            .make(btnCheckPermission, "Granted Request Permission", Snackbar.LENGTH_SHORT)
            .show()
    }

    override fun onDeniedSystemAlertWindow() {
        Log.d(MainActivity::class.java.simpleName, "Denied SystemAlertWindow")
        Snackbar
            .make(btnCheckPermission, "Denied SystemAlertWindow", Snackbar.LENGTH_SHORT)
            .show()
    }

    override fun onGrantedSystemAlertWindow() {
        Log.d(MainActivity::class.java.simpleName, "Granted SystemAlertWindow")
        Snackbar
            .make(btnCheckPermission, "Granted SystemAlertWindow", Snackbar.LENGTH_SHORT)
            .show()
    }

}

