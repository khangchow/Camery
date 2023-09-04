package com.chow.camery

import java.io.Serializable

abstract class CameryConfig : Serializable {
    abstract val numOfImage: Int
}

data class CameraConfig(
    override val numOfImage: Int = 1,
    val isSavingToGallery: Boolean = true,
    val galleryFolderName: String = Constants.LIBRARY_NAME
) : CameryConfig()

data class GalleryConfig(
    override val numOfImage: Int = 1
) : CameryConfig()