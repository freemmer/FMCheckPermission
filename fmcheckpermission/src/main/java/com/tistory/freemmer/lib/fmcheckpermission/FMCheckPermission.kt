package com.tistory.freemmer.lib.fmcheckpermission

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import java.lang.ref.WeakReference
import kotlin.collections.ArrayList

/**
 * Created by freemmer on 11/01/2019.
 * History
 *    - 11/01/2019 Create file
 */
class FMCheckPermission private constructor(
    private val activity: Activity
) {

    companion object {
        private var weakReference: WeakReference<FMCheckPermission>? = null

        fun instance(activity: Activity): FMCheckPermission {
            if (weakReference?.get() == null) {
                weakReference = WeakReference(FMCheckPermission(activity))
            }
            return weakReference?.get()!!
        }
    }

    private val REQUEST_PERMISSIONS_CHECK   = 3000
    private val OVERLAY_PERMISSION_REQ_CODE = 3001
    private val REQUEST_SETTING             = 3002

    private lateinit var pAllowedFunc: (() -> Unit)
    private lateinit var pDeniedFunc: ((checkedDoNotAskPermissions: Array<String>, permissions: Array<String>) -> Unit)
    private val list = ArrayList<String>()
    private var managerOverlayIntent: Intent? = null

    fun check(permissions: Array<String>
              , pAllowedFunc:() -> Unit
              , pDeniedFunc:(checkedDoNotAskPermissions: Array<String>, permissions: Array<String>) -> Unit) {
        this.pAllowedFunc = pAllowedFunc
        this.pDeniedFunc = pDeniedFunc
        list.clear()
        managerOverlayIntent = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (item in permissions) {
                if (item == Manifest.permission.SYSTEM_ALERT_WINDOW) {
                    if (Settings.canDrawOverlays(activity)) {
                        // SYSTEM_ALERT_WINDOW 허용됨
                    } else  {
                        managerOverlayIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                            , Uri.parse("package:${activity.applicationContext.packageName}"))
                        activity.startActivityForResult(managerOverlayIntent, OVERLAY_PERMISSION_REQ_CODE)
                        return
                    }
                } else {
                    list.add(item)
                }
            }
            checkNormalPermission(list.toTypedArray())
        } else {
            pAllowedFunc.invoke()
        }
    }

    private fun checkNormalPermission(permissions: Array<String>) {
        val needPermissionsList = ArrayList<String>() // 권한이 없는 Permission list
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                needPermissionsList.add(permission)
            }
        }
        if (needPermissionsList.isNotEmpty()) { // 접근권한 요청: $needPermissionsList
            ActivityCompat.requestPermissions(activity, needPermissionsList.toTypedArray(), REQUEST_PERMISSIONS_CHECK)
        } else {
           pAllowedFunc.invoke()
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != REQUEST_PERMISSIONS_CHECK) return

        val deniedPermissionsList = ArrayList<String>()      // 완전 거부한 퍼미션 (Checked 'Don't ask')
        val rationaleDeniedPermissions = ArrayList<String>() // 거부한 퍼미션 (일반)
        for (i in permissions.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i])) {
                    deniedPermissionsList.add(permissions[i])
                } else {
                    rationaleDeniedPermissions.add(permissions[i])
                }
            }
        }
        if (rationaleDeniedPermissions.isEmpty() && deniedPermissionsList.isEmpty()) {
            pAllowedFunc.invoke()
        } else {
            pDeniedFunc.invoke(rationaleDeniedPermissions.toTypedArray(), deniedPermissionsList.toTypedArray())
        }
    }

    fun onActivityResult(requestCode: Int) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(activity)) {
                    checkNormalPermission(list.toTypedArray())
                } else {
                    list.add(Manifest.permission.SYSTEM_ALERT_WINDOW)
                    pDeniedFunc.invoke(arrayOf(), list.toTypedArray())
                }
            }
        } else if (requestCode == REQUEST_SETTING) {
            checkNormalPermission(list.toTypedArray())
        }
    }

    fun moveSetting() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:${activity.applicationContext.packageName}"))
            activity.startActivityForResult(intent, REQUEST_SETTING)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
            activity.startActivityForResult(intent, REQUEST_SETTING)
        }
    }

}




abstract class FMCheckPermissionActivity: Activity() {
    private lateinit var checker: FMCheckPermission

    fun checkPermission(permissions: Array<String>
                        , pAllowedFunc:() -> Unit
                        , pDeniedFunc:(checkedDoNotAskPermissions: Array<String>, permissions: Array<String>) -> Unit)
    {
        checker = FMCheckPermission.instance(this)
        checker.check(permissions, pAllowedFunc, pDeniedFunc)
    }

    fun movePermissionSetting() {
        checker.moveSetting()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        checker.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        checker.onActivityResult(requestCode)
    }
}

abstract class FMCheckPermissionAppCompatActivity: AppCompatActivity() {
    private lateinit var checker: FMCheckPermission

    fun checkPermission(permissions: Array<String>
                        , pAllowedFunc:() -> Unit
                        , pDeniedFunc:(checkedDoNotAskPermissions: Array<String>, permissions: Array<String>) -> Unit)
    {
        checker = FMCheckPermission.instance(this)
        checker.check(permissions, pAllowedFunc, pDeniedFunc)
    }

    fun movePermissionSetting() {
        checker.moveSetting()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        checker.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        checker.onActivityResult(requestCode)
    }
}

abstract class FMCheckPermissionAppFragmentActivity: FragmentActivity() {
    private lateinit var checker: FMCheckPermission

    fun checkPermission(permissions: Array<String>
                        , pAllowedFunc:() -> Unit
                        , pDeniedFunc:(checkedDoNotAskPermissions: Array<String>, permissions: Array<String>) -> Unit)
    {
        checker = FMCheckPermission.instance(this)
        checker.check(permissions, pAllowedFunc, pDeniedFunc)
    }

    fun movePermissionSetting() {
        checker.moveSetting()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        checker.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        checker.onActivityResult(requestCode)
    }
}

