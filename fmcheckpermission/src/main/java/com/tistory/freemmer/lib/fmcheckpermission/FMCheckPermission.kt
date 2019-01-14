package com.tistory.freemmer.lib.fmcheckpermission

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import java.util.ArrayList

/**
 * Created by freemmer on 11/01/2019.
 * History
 *    - 11/01/2019 Create file
 */

/**
 * Android 8.0 이상부터 사용되는 동적 퍼미션 체크를 지원한다
 * @param permissions   요청할 퍼미션
 * @param pRequestPermissionRationale   완전 거부한 퍼미션이 요청되었을때 호출됨
 *                                      (설정으로 이동 해야 함), 퍼미션 체크가 끝날경우 FMCheckPermissionActivity를 finish해야함
 * @param pDeniedRequestPermission      접근권한 요청 거부되었을때 호출됨
 *                                      퍼미션 체크가 끝날경우 FMCheckPermissionActivity를 finish해야함
 * @param pGrantedRequestPermission     퍼미션 획득 되었을때 호출됨
 */
class FMCheckPermission (
    var permissions: Array<String>,
    var pRequestPermissionRationale: ((FMCheckPermissionActivity: FMCheckPermissionActivity, permissions: List<String>) -> Unit)?,
    var pDeniedRequestPermission: ((FMCheckPermissionActivity: FMCheckPermissionActivity, permissions: List<String>) -> Unit)?,
    var pGrantedRequestPermission: (() -> Unit)?
) {
    class Builder(private var permissions: Array<String>, init: Builder.() -> Unit) {
        /* 완전 거부한 퍼미션이 요청됨 (설정으로 이동 해야 함), 퍼미션 체크가 끝날경우 checkPermissionActivity를 finish해야함 */
        var pRequestPermissionRationale: ((FMCheckPermissionActivity: FMCheckPermissionActivity, permissions: List<String>) -> Unit)? = null
        /* 접근권한 요청 거부됨, 퍼미션 체크가 끝날경우 checkPermissionActivity를 finish해야함 */
        var pDeniedRequestPermission: ((FMCheckPermissionActivity: FMCheckPermissionActivity, permissions: List<String>) -> Unit)? = null
        /* 퍼미션 획득 됨 */
        var pGrantedRequestPermission: (() -> Unit)? = null

        init {
            init()
        }

        fun build() : FMCheckPermission {
            return FMCheckPermission(permissions
                , pRequestPermissionRationale, pDeniedRequestPermission, pGrantedRequestPermission)
        }

        companion object {
            fun build(permissions: Array<String>, init: Builder.() -> Unit) = Builder(permissions, init).build()
        }
    }


    fun execute(activity: Activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            FMCheckPermissionActivity.serializable = this
            val intent = Intent(activity, FMCheckPermissionActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(intent)
        } else {
            pGrantedRequestPermission?.invoke()
        }
    }

    companion object {
        fun build(permissions: Array<String>, init: Builder.() -> Unit) = Builder(permissions, init)
    }

}



class FMCheckPermissionActivity
    : Activity()
{
    companion object {
        const val REQUEST_PERMISSIONS_CHECK = 100
        const val REQUEST_SETTING           = 101
        lateinit var serializable: FMCheckPermission
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_permission)
        execute()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.loadfadein, R.anim.loadfadeout)
    }


    fun execute() {
        if (serializable.permissions.isNotEmpty()) {
            checkPermission(serializable.permissions)
        } else {
            finish()
        }
    }


    private fun checkPermission(permissions: Array<String>) {
        val needPermissionsList = ArrayList<String>()

        for (permission in permissions) { // 권한 체크
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 권한이 없을 경우
                needPermissionsList.add(permission)
            }
        }
        if (needPermissionsList.isNotEmpty()) {
            Log.d(FMCheckPermission::class.java.simpleName, "Request permission: $needPermissionsList")
            ActivityCompat.requestPermissions(this, needPermissionsList.toTypedArray(), REQUEST_PERMISSIONS_CHECK)
        } else {
            serializable.pGrantedRequestPermission?.invoke()
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSIONS_CHECK) {
            val deniedPermissionsList = ArrayList<String>()
            val rationaleDeniedPermissions = ArrayList<String>()
            for (i in permissions.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                        deniedPermissionsList.add(permissions[i])
                    } else {
                        rationaleDeniedPermissions.add(permissions[i])
                    }
                }
            }
            if (rationaleDeniedPermissions.isNotEmpty()) {
                Log.d(FMCheckPermission::class.java.simpleName, "Permission Rejected $rationaleDeniedPermissions (must be move to the setting)")
                if (serializable.pRequestPermissionRationale != null) {
                    serializable.pRequestPermissionRationale!!.invoke(this, rationaleDeniedPermissions)
                } else {
                    defaultRequestPermissionRationale()
                }
            } else if (deniedPermissionsList.isNotEmpty()) {
                Log.d(FMCheckPermission::class.java.simpleName, "Permission Rejected")
                if (serializable.pDeniedRequestPermission != null) {
                    serializable.pDeniedRequestPermission!!.invoke(this, deniedPermissionsList)
                } else {
                    finish()
                }
            } else {
                Log.d(FMCheckPermission::class.java.simpleName, "Permission Accepted")
                serializable.pGrantedRequestPermission?.invoke()
                finish()
            }
        }
    }


    fun defaultRequestPermissionRationale() {
        val packageName = applicationContext.packageName
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:$packageName"))
            startActivityForResult(intent, REQUEST_SETTING)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
            startActivityForResult(intent, REQUEST_SETTING)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SETTING) {
            execute()
        }
    }

}