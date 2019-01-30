![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)
[![](https://jitpack.io/v/freemmer/FMCheckPermission.svg)](https://jitpack.io/#freemmer/FMCheckPermission)

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
    implementation 'com.github.freemmer:FMCheckPermission:1.2.2'
}
```


## How to use
1. Do inherit one of the classes below.
+ FMCheckPermissionActivity            : extends Activity
+ FMCheckPermissionAppCompatActivity   : extends AppCompatActivity
+ FMCheckPermissionAppFragmentActivity : extends FragmentActivity

2. Use to the code below.
```Kotlin
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
```

## Another how to use
```kotlin
class MainActivity: Activity() {
    private lateinit var checker: FMCheckPermission

    private fun checkPermission(permissions: Array<String>
                        , pAllowedFunc:() -> Unit
                        , pDeniedFunc:(checkedDoNotAskPermissions: Array<String>, permissions: Array<String>) -> Unit)
    {
        checker = FMCheckPermission.instance(this)
        checker.check(permissions, pAllowedFunc, pDeniedFunc)
    }

    private fun movePermissionSetting() {
        checker.moveSetting()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        checker.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        checker.onActivityResult(requestCode)
    }
}
````

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
