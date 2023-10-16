package com.chow.camery

import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import com.chow.camery.databinding.ActivityCameryBinding
import com.chow.camery.extension.*
import java.io.File
import java.io.FileOutputStream


class CameryActivity : AppCompatActivity() {
    private val binding by lazy { ActivityCameryBinding.inflate(layoutInflater) }
    private var cameraFacing = CameraSelector.LENS_FACING_BACK
    private lateinit var galleryFolderName: String
    private var isSavingToGallery = true
    private val registerRequestCameraPermissionsLauncher = registerRequestCameraPermissionsLauncher(
        permissionsGrantedCallback = {
            showCamera()
        }, permissionsNotGrantedCallback = {

        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.actionBar)
        ((intent.getSerializableExtra(BundleKey.CONFIG) as? CameraConfig?) ?: CameraConfig()).let {
            galleryFolderName = it.galleryFolderName
            isSavingToGallery = it.isSavingToGallery
        }
        if (areCameraPermissionsGranted()) {
            showCamera()
        } else {
            registerRequestCameraPermissionsLauncher.launchToRequestCameraPermissions()
        }
        binding.apply {
            ivFlip.setOnClickListener {
                cameraFacing = if (cameraFacing == CameraSelector.LENS_FACING_BACK) {
                    CameraSelector.LENS_FACING_FRONT
                } else {
                    CameraSelector.LENS_FACING_BACK
                }
                showCamera()
            }
        }
    }

    private fun showCamera() {
        binding.apply {
            cameraPreview.showCamera(
                this@CameryActivity,
                cameraFacing,
                ivCapture,
                ivFlash,
                takePhotoCallback = { uri, file ->
                    if (isSavingToGallery) saveImageToGallery(file, uri)
                    else returnUriAndFinishActivity(file, uri)
                },
                toggleFlashCallback = {
                    ivFlash.setImageResource(if (it) R.drawable.ic_flash else R.drawable.ic_flash_off)
                },
                showLoadingCallback = {
                    rlLoading.showOrInvisibleWithCondition(it)
                })
        }
    }

    private fun saveImageToGallery(file: File, uri: Uri) {
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            galleryFolderName
        ).also {
            if (it.exists().not()) it.mkdirs()
        }
        val imageFile = File(storageDir, "IMG_${System.currentTimeMillis()}.jpg")
        try {
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(imageFile).use { output ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }
            MediaScannerConnection.scanFile(
                this, arrayOf(imageFile.absolutePath),
                null
            ) { _: String, savedUri: Uri ->
                runOnUiThread {
                    returnUriAndFinishActivity(file, savedUri)
                }
            }
        } catch (e: Exception) {

        }
    }

    private fun returnUriAndFinishActivity(file: File, uri: Uri) {
        setResult(RESULT_OK, Intent().apply {
            putExtra(BundleKey.IMAGE_URI, uri)
        })
        file.delete()
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_gallery -> {
                Toast.makeText(this, "open gallery", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}