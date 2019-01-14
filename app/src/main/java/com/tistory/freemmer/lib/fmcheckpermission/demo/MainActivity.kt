package com.tistory.freemmer.lib.fmcheckpermission.demo

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import com.tistory.freemmer.lib.fmcheckpermission.FMCheckPermission
import com.tistory.freemmer.lib.fmcheckpermission.FMCheckPermissionSystemAlert

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        btnCheckPermission.setOnClickListener {
            checkPermission(it)
        }

        btnCheckPermissionSystemAlertWindow.setOnClickListener {
            checkPermissionSystemAlert(it)
        }

    }

    /**
     * Check permissions to except 'SYSTEM_ALERT_WINDOW'
     */
    private fun checkPermission(view: View) {
        FMCheckPermission.Builder.build(arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ))
        {
            pGrantedRequestPermission = {
                Snackbar.make(view, "Granted Permission (퍼미션 획득 됨)", Snackbar.LENGTH_SHORT)
                    .show()
            }
            pDeniedRequestPermission = { checkPermissionActivity, permissions ->
                val alert = AlertDialog.Builder(checkPermissionActivity).create()
                alert.setTitle("Info")
                alert.setMessage("Denied Permission (거부됨) : $permissions")
                alert.setButton(AlertDialog.BUTTON_POSITIVE, "Try again") { dialog, id ->
                    checkPermissionActivity.execute()
                    dialog.dismiss()
                }
                alert.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { dialog, id ->
                    checkPermissionActivity.finish()
                    dialog.dismiss()
                }
                alert.show()
            }
            pRequestPermissionRationale = { checkPermissionActivity, permissions ->
                val alert = AlertDialog.Builder(checkPermissionActivity).create()
                alert.setTitle("Info")
                alert.setMessage("Request permission that checked 'Don't ask again'(다시보지 않음을 선택한 퍼미션이 요청됨) : $permissions")
                alert.setButton(AlertDialog.BUTTON_POSITIVE, "Move Setting") { dialog, id ->
                    checkPermissionActivity.defaultRequestPermissionRationale()
                    dialog.dismiss()
                }
                alert.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { dialog, id ->
                    checkPermissionActivity.finish()
                    dialog.dismiss()
                }
                alert.show()
            }
        }.execute(this)
    }

    /**
     * Check permission accept to 'SYSTEM_ALERT_WINDOW'
     */
    private fun checkPermissionSystemAlert(view: View) {
        FMCheckPermissionSystemAlert.Builder.build {
            pDeniedPermission = {
                Snackbar.make(view, "System Alert Window : Denied Permission (거부됨)", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
            }
            pGrantedPermission = {
                Snackbar.make(view, "System Alert Window : Granted Permission (권한획득)", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
            }
        }.execute(this)
    }


}
