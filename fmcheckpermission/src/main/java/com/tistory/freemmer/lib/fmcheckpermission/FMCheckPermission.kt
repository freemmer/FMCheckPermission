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
import kotlin.collections.ArrayList

/**
 * Created by freemmer on 11/01/2019.
 * History
 *    - 11/01/2019 Create file
 */

class FMCheckPermission (
    private val activity: Activity,
    private val inter: FMICheckPermission
) {
    private val REQUEST_PERMISSIONS_CHECK   = 3000
    private val OVERLAY_PERMISSION_REQ_CODE = 3001
    private val REQUEST_SETTING             = 3002

    private val list = ArrayList<String>()
    private var managerOverlayIntent: Intent? = null

    fun execute(permissions: Array<String>, packageName: String? = null) {
        list.clear()
        managerOverlayIntent = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (item in permissions) {
                if (item == Manifest.permission.SYSTEM_ALERT_WINDOW) {
                    if (Settings.canDrawOverlays(activity)) {
                        inter.onGrantedSystemAlertWindow()
                    } else  {
                        if (packageName != null) {
                            managerOverlayIntent =
                                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                            activity.startActivityForResult(managerOverlayIntent, OVERLAY_PERMISSION_REQ_CODE)
                        }
                    }
                } else {
                    list.add(item)
                }
            }

            if (managerOverlayIntent == null) {
                checkPermission(list.toTypedArray())
            }
        } else {
            inter.onGrantedRequestPermission()
        }
    }

    private fun checkPermission(permissions: Array<String>) {
        val needPermissionsList = ArrayList<String>()
        for (permission in permissions) { // 권한 체크
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                // 권한이 없을 경우
                needPermissionsList.add(permission)
            }
        }
        if (needPermissionsList.isNotEmpty()) {
            // 접근권한 요청: $needPermissionsList
            ActivityCompat.requestPermissions(activity, needPermissionsList.toTypedArray(), REQUEST_PERMISSIONS_CHECK)
        } else {
            // 접근권한 이미 허용되어 있음
            inter.onGrantedRequestPermission()
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != REQUEST_PERMISSIONS_CHECK) return

        val deniedPermissionsList = ArrayList<String>()
        val rationaleDeniedPermissions = ArrayList<String>()
        for (i in permissions.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i])) {
                    deniedPermissionsList.add(permissions[i])
                } else {
                    rationaleDeniedPermissions.add(permissions[i])
                }
            }
        }
        when {
            rationaleDeniedPermissions.isNotEmpty() -> {
                // 완전 거부한 퍼미션 있음(설정으로 이동해야 함)
                inter.onRequestPermissionRationale(rationaleDeniedPermissions)
            }
            deniedPermissionsList.isNotEmpty() -> {
                // 접근권한 요청 거부
                inter.onDeniedRequestPermission(deniedPermissionsList)
            }
            else -> {
                // 퍼미션 획득. 프로세스 진행
                inter.onGrantedRequestPermission()
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(activity)) {
                    inter.onGrantedSystemAlertWindow()
                } else {
                    inter.onDeniedSystemAlertWindow()
                }
                checkPermission(list.toTypedArray())
            }
        } else if (requestCode == REQUEST_SETTING) {
            checkPermission(list.toTypedArray())
        }
    }

    fun moveSetting(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:$packageName"))
            activity.startActivityForResult(intent, REQUEST_SETTING)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
            activity.startActivityForResult(intent, REQUEST_SETTING)
        }
    }

}

interface FMICheckPermission {
    fun checkPermission(permissions: Array<String>, packageName: String? = null)
    fun moveSetting(packageName: String)

    fun onRequestPermissionRationale(permissions: List<String>)
    fun onDeniedRequestPermission(permissions: List<String>)
    fun onGrantedRequestPermission()
    fun onDeniedSystemAlertWindow()
    fun onGrantedSystemAlertWindow()
}


abstract class FMCheckPermissionActivity: Activity(), FMICheckPermission {
    private var checker = FMCheckPermission(this, this)
    override fun checkPermission(permissions: Array<String>, packageName: String?) {
        checker.execute(permissions, packageName)
    }

    override fun moveSetting(packageName: String) {
        checker.moveSetting(packageName)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        checker.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        checker.onActivityResult(requestCode, resultCode, data)
    }
}

abstract class FMCheckPermissionAppCompatActivity: AppCompatActivity(), FMICheckPermission {
    private var checker = FMCheckPermission(this, this)
    override fun checkPermission(permissions: Array<String>, packageName: String?) {
        checker.execute(permissions, packageName)
    }

    override fun moveSetting(packageName: String) {
        checker.moveSetting(packageName)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        checker.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        checker.onActivityResult(requestCode, resultCode, data)
    }
}

abstract class FMCheckPermissionAppFragmentActivity: FragmentActivity(), FMICheckPermission {
    private var checker = FMCheckPermission(this, this)
    override fun checkPermission(permissions: Array<String>, packageName: String?) {
        checker.execute(permissions, packageName)
    }

    override fun moveSetting(packageName: String) {
        checker.moveSetting(packageName)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        checker.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        checker.onActivityResult(requestCode, resultCode, data)
    }
}

