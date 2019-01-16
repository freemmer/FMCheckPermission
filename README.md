# FMCheckPermission
For check permission on Android (Kotlin)



## Demo ScreenShot

![Screenshot](https://github.com/freemmer/FMCheckPermission/blob/master/Screenshots/screenshots.gif) 


## Setup

Project build.gradle
```Groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

App build.gradle
```Groovy
dependencies {
    implementation 'com.github.freemmer:FMCheckPermission:1.2.0'
}
```


## How to use

```Kotlin
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
```
## Activity Type
+ FMCheckPermissionActivity            : extends Activity
+ FMCheckPermissionAppCompatActivity   : extends AppCompatActivity
+ FMCheckPermissionAppFragmentActivity : extends FragmentActivity

## Another how to use
FMICheckPermission is inherited and implemented as below.
```kotlin
abstract class FMCheckPermissionActivity: Activity(), FMICheckPermission {
    private lateinit var checker: FMCheckPermission
    private fun checkLateInit() {
        if (!this::checker.isInitialized) {
            checker = FMCheckPermission(this, this)
        }
    }
    fun checkPermission(permissions: Array<String>, packageName: String?) {
        checkLateInit()
        checker.execute(permissions, packageName)
    }

    fun moveSetting(packageName: String) {
        checkLateInit()
        checker.moveSetting(packageName)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        checker.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        checker.onActivityResult(requestCode, resultCode, data)
    }
}
```

## License 
```code
This software is licensed under the [Apache 2 license](LICENSE), quoted below.

Copyright 2019 freemmer. <http://freemmer.tistory.com>

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this project except in compliance with the License. You may obtain a copy
of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
```

