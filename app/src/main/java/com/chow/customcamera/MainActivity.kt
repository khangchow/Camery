package com.chow.customcamera

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.chow.camery.CameraConfig
import com.chow.camery.registerImageLauncher
import com.chow.customcamera.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val imageLauncher = registerImageLauncher {
        if (it == null) return@registerImageLauncher
        Glide.with(this).load(it).into(binding.ivAvatar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.btnOpenCamera.setOnClickListener {
            imageLauncher.launch(CameraConfig())
        }
    }
}