package com.chow.camery

import java.io.Serializable

abstract class CameryConfig : Serializable {
    abstract val numOfImage: Int
}

data class CameraConfig(
    override val numOfImage: Int = 1
) : CameryConfig()

data class GalleryConfig(
    override val numOfImage: Int = 1
) : CameryConfig()