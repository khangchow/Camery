package com.chow.camery.extension

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

private fun getPermissionsForTakingPhotoWithCamera() = arrayOf(
    Manifest.permission.CAMERA
).run {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
        plus(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    else
        this
}

fun ComponentActivity.registerRequestCameraPermissionsLauncher(
    permissionsGrantedCallback: () -> Unit,
    permissionsNotGrantedCallback: () -> Unit
) = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
) { permissions ->
    if (areRequestedPermissionsGranted(permissions)) {
        permissionsGrantedCallback.invoke()
    } else {
        permissionsNotGrantedCallback.invoke()
    }
}

fun ActivityResultLauncher<Array<String>>.launchToRequestCameraPermissions() {
    launch(getPermissionsForTakingPhotoWithCamera())
}

private fun areRequestedPermissionsGranted(permissions: Map<String, Boolean>?): Boolean {
    permissions?.forEach { (_, granted) ->
        if (!granted) {
            return false
        }
    }
    return true
}

private fun Context.arePermissionsGranted(permissions: Array<String>): Boolean {
    for (permission in permissions) {
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
    }
    return true
}

fun Context.areCameraPermissionsGranted() =
    arePermissionsGranted(getPermissionsForTakingPhotoWithCamera())