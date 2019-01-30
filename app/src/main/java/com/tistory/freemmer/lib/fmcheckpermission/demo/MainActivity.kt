package com.tistory.freemmer.lib.fmcheckpermission.demo

import android.Manifest
import android.os.Bundle
import android.support.design.widget.Snackbar
import com.tistory.freemmer.lib.fmcheckpermission.FMCheckPermissionActivity

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : FMCheckPermissionActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnCheckPermission.setOnClickListener {
            checkPermission(
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.SYSTEM_ALERT_WINDOW)
                , {
                    // All Permissions requested are allowed
                    Snackbar.make(btnCheckPermission
                        , "OK!!", Snackbar.LENGTH_SHORT).show()
                }, {checkedDoNotAskPermissions, permissions ->
                    // Requested Permission denied
                    if (checkedDoNotAskPermissions.isNotEmpty()) {
                        Snackbar.make(btnCheckPermission
                            , "Requested Permissions denied with 'Don't ask again' : $checkedDoNotAskPermissions"
                            , Snackbar.LENGTH_LONG)
                            .setAction("move setting") { movePermissionSetting() }.show()
                    } else {
                        Snackbar.make(btnCheckPermission
                            , "Requested Permission denied : $permissions", Snackbar.LENGTH_SHORT).show()
                    }
                })
        }

    }

}

