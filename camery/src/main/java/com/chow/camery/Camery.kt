package com.chow.camery

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContract

fun ComponentActivity.registerImageLauncher(callback: (Uri?) -> Unit) =
    registerForActivityResult(object : ActivityResultContract<CameryConfig, Uri?>() {
        override fun createIntent(context: Context, input: CameryConfig) =
            Intent(context, CameryActivity::class.java).apply {
                putExtra(BundleKey.CONFIG, input)
            }

        override fun parseResult(resultCode: Int, intent: Intent?) =
            (intent?.extras?.getParcelable(BundleKey.IMAGE_URI) as? Uri)
    }) {
        callback.invoke(it)
    }