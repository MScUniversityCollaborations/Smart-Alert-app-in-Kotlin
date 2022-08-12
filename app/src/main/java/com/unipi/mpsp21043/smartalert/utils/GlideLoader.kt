package com.unipi.mpsp21043.smartalert.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.unipi.mpsp21043.smartalert.R
import java.io.IOException


/**
 * A custom object to create a common functions for Glide which can be used in whole application.
 */
@GlideModule
class GlideLoader(val context: Context) : AppGlideModule() {

    /**
     * A function to load image from Uri or URL for the user profile picture.
     */
    fun loadUserPicture(image: Any, imageView: ImageView) {
        try {
            // Load the image in the ImageView.
            Glide
                .with(context)
                .load(image) // Uri or URL of the image
                .centerCrop() // Scale type of the image.
                .placeholder(R.drawable.ic_user_sample) // A default place holder if image is failed to load.
                .into(imageView) // the view in which the image will be loaded.
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * A function to load image from Uri or URL for the image view.
     */
    fun loadPicture(image: Any, imageView: ImageView) {
        try {
            // Load the image in the ImageView.
            Glide
                .with(context)
                .load(image) // Uri or URL of the image
                .placeholder(R.drawable.ic_picture) // A default place holder if image is failed to load.
                .into(imageView) // the view in which the image will be loaded.
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * A function to load image from Uri or URL for the image view.
     */
    fun loadPictureWide(image: Any, imageView: ImageView) {
        try {
            // Load the image in the ImageView.
            Glide
                .with(context)
                .load(image) // Uri or URL of the image
                .placeholder(R.drawable.ic_picture_wide) // A default place holder if image is failed to load.
                .into(imageView) // the view in which the image will be loaded.
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
