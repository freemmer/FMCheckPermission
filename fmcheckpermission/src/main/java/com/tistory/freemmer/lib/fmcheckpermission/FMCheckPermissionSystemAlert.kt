package com.tistory.freemmer.lib.fmcheckpermission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.RequiresApi

/**
 * Created by freemmer on 11/01/2019.
 * History
 *    - 11/01/2019 Create file
 */

/**
 * SYSTEM_ALERT_WINDOW 퍼미션 체크를 지원한다.
 * Android 6.0 이상부터 SYSTEM_ALERT_WINDOW 퍼미션 관련 체크가 달라진다.
 * <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
 * @param pDeniedPermission     퍼미션 요청 거부되었을때 호출됨.
 * @param pGrantedPermission    퍼미션 획득 되었을때 호출됨
 */
class FMCheckPermissionSystemAlert (
    var pDeniedPermission: ((fmCheckPermissionSystemAlertActivity: FMCheckPermissionSystemAlertActivity) -> Unit)?,
    var pGrantedPermission: (() -> Unit)?
) {
    class Builder(init: Builder.() -> Unit) {
        /* 퍼미션 요청 거부됨 */
        var pDeniedPermission: ((fmCheckPermissionSystemAlertActivity: FMCheckPermissionSystemAlertActivity) -> Unit)? = null
        /* 퍼미션 획득됨 */
        var pGrantedPermission: (() -> Unit)? = null

        init {
            init()
        }

        fun build() : FMCheckPermissionSystemAlert {
            return FMCheckPermissionSystemAlert(pDeniedPermission, pGrantedPermission)
        }

        companion object {
            fun build(init: Builder.() -> Unit) = Builder(init).build()
        }
    }


    fun execute(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(activity)) {
                FMCheckPermissionSystemAlertActivity.serializable = this
                val intent = Intent(activity, FMCheckPermissionSystemAlertActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
            } else if (Settings.canDrawOverlays(activity)) {
                pGrantedPermission?.invoke()
            }
        } else {
            pGrantedPermission?.invoke()
        }
    }

    companion object {
        fun build(init: Builder.() -> Unit) = Builder(init)

        /**
         * SYSTEM_ALERT_WINDOW (Settings.canDrawOverlays()) 퍼미션이 허용되어 있는지 확인한다.
         */
        fun isGrantedPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }

    }

}

@RequiresApi(Build.VERSION_CODES.M)
class FMCheckPermissionSystemAlertActivity
    : Activity()
{
    companion object {
        lateinit var serializable: FMCheckPermissionSystemAlert
    }
    private val OVERLAY_PERMISSION_REQ_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_permission)
        //execute()
        checkPermission()
    }

    fun execute() {
        if (!Settings.canDrawOverlays(this)) {
            //checkPermission()
            serializable.pDeniedPermission?.invoke(this)
            finish()
        } else {
            serializable.pGrantedPermission?.invoke()
            finish()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.loadfadein, R.anim.loadfadeout)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            execute()
        }
    }

    private fun checkPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
    }
}
