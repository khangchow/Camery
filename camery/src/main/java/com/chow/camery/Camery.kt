package com.chow.camery

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContract

object Camery {
    const val IMAGE_URI = "IMAGE_URI"
    const val CONFIG_KEY = "CONFIG_KEY"
}

fun ComponentActivity.registerImageLauncher(callback: (Uri?) -> Unit) =
    registerForActivityResult(object : ActivityResultContract<CameryConfig, Uri?>() {
        override fun createIntent(context: Context, input: CameryConfig) =
            Intent(context, CameryActivity::class.java).apply {
                putExtra(Camery.CONFIG_KEY, input)
            }

        override fun parseResult(resultCode: Int, intent: Intent?) =
            (intent?.extras?.getParcelable(Camery.IMAGE_URI) as? Uri)
    }) {
        callback.invoke(it)
    }