package com.chow.camery

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Display
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.chow.camery.databinding.ActivityCameryBinding
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import kotlin.math.abs


class CameryActivity : AppCompatActivity() {
    private val binding by lazy { ActivityCameryBinding.inflate(layoutInflater) }
    private var cameraFacing = CameraSelector.LENS_FACING_BACK
    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.actionBar)

        (intent.getSerializableExtra(Camery.CONFIG_KEY) as? CameryConfig?)?.let {
            when (it) {
                is CameraConfig -> {
                    Log.d("CHOTAOTEST", "is camera config")
                }
                is GalleryConfig -> {

                }
                else -> {}
            }
        }

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (areAllPermissionsGranted(permissions)) {
                    showCamera(cameraFacing)
                } else {
                    // Permissions not granted, handle accordingly
                }
            }

        if (arePermissionsGranted()) {
            showCamera(cameraFacing)
        } else {
            requestPermissionLauncher.launch(permissions)
        }

        binding.apply {
            ivFlip.setOnClickListener {
                cameraFacing = if (cameraFacing == CameraSelector.LENS_FACING_BACK) {
                    CameraSelector.LENS_FACING_FRONT
                } else {
                    CameraSelector.LENS_FACING_BACK
                }
                showCamera(cameraFacing)
            }
        }
    }

    private fun showCamera(cameraFacing: Int) {
        binding.cameraPreview.apply {
            post {
                val aspectRatio = aspectRatio(width, height)
                ProcessCameraProvider.getInstance(this@CameryActivity).apply {
                    addListener({
                        try {
                            val cameraProvider = get()
                            val preview =
                                Preview.Builder().setTargetAspectRatio(aspectRatio).build()
                            val imageCapture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .setTargetRotation(
                                    (getSystemService(Context.DISPLAY_SERVICE) as DisplayManager).getDisplay(
                                        Display.DEFAULT_DISPLAY
                                    ).rotation
                                ).build()
                            val cameraSelector =
                                CameraSelector.Builder().requireLensFacing(cameraFacing).build()
                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(
                                this@CameryActivity,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                            binding.ivCapture.setOnClickListener {
                                takePicture(imageCapture)
                            }
                            binding.ivFlash.setOnClickListener {
                                setFlashIcon(camera)
                            }
                            preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
                        } catch (e: Exception) {

                        }
                    }, ContextCompat.getMainExecutor(this@CameryActivity))
                }
            }
        }
    }

    private fun setFlashIcon(camera: Camera) {
        if (camera.cameraInfo.hasFlashUnit()) {
            if (camera.cameraInfo.torchState.value == 0) {
                camera.cameraControl.enableTorch(true)
                binding.ivFlash.setImageResource(R.drawable.ic_flash)
            } else {
                camera.cameraControl.enableTorch(false)
                binding.ivFlash.setImageResource(R.drawable.ic_flash_off)
            }
        }
    }

    private fun takePicture(imageCapture: ImageCapture) {
        binding.rlLoading.visible()
        File(cacheDir, "${System.currentTimeMillis()}.jpg").let { file ->
            imageCapture.takePicture(
                ImageCapture.OutputFileOptions.Builder(file).build(),
                Executors.newCachedThreadPool(),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        if (outputFileResults.savedUri == null) return
                        saveImageToGallery(file, outputFileResults.savedUri!!)
//                        showCamera(cameraFacing)
                        binding.rlLoading.invisible()
                    }

                    override fun onError(exception: ImageCaptureException) {
                        binding.rlLoading.invisible()
                    }

                })
        }
    }

    private fun saveImageToGallery(file: File, uri: Uri) {
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "CustomCamera"
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
                    returnUriAndFinishActivity(savedUri)
                }
            }
        } catch (e: Exception) {

        }
        file.delete()
    }

    private fun returnUriAndFinishActivity(uri: Uri) {
        setResult(RESULT_OK, Intent().apply {
            putExtra(Camery.IMAGE_URI, uri)
        })
        finish()
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = width.coerceAtLeast(height) / width.coerceAtMost(height)
        if (abs(previewRatio - 4.0 / 3.0) <= abs(previewRatio - 16.0 / 9.0)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun arePermissionsGranted(): Boolean {
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

    private fun areAllPermissionsGranted(permissions: Map<String, Boolean>?): Boolean {
        permissions?.forEach { (_, granted) ->
            if (!granted) {
                return false
            }
        }
        return true
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