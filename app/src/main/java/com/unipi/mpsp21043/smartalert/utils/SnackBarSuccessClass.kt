package com.unipi.mpsp21043.smartalert.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.unipi.mpsp21043.smartalert.R
import com.unipi.mpsp21043.smartalert.extension.findSuitableParent

class SnackBarSuccessClass(
    parent: ViewGroup,
    content: SnackBarSuccessView
) : BaseTransientBottomBar<SnackBarSuccessClass>(parent, content, content) {

    init {
        getView().setBackgroundColor(ContextCompat.getColor(view.context, android.R.color.transparent))
        getView().setPadding(0, 0, 0, 0)

        getView().findViewById<TextView>(R.id.btn_SnackBar_Success_Dismiss).setOnClickListener { dismiss() }
        animationMode = ANIMATION_MODE_SLIDE
    }

    companion object {

        fun make(view: View, contentTxt: String): SnackBarSuccessClass {

            // First we find a suitable parent for our custom view
            val parent = view.findSuitableParent() ?: throw IllegalArgumentException(
                "No suitable parent found from the given view. Please provide a valid view."
            )

            // We inflate our custom view
            val customView = LayoutInflater.from(view.context).inflate(
                R.layout.layout_snackbar_success,
                parent,
                false
            ) as SnackBarSuccessView

            customView.apply {
                findViewById<TextView>(R.id.txt_SnackBar_Success_Header_2).text = contentTxt
            }

            // We create and return our Snack-bar
            return SnackBarSuccessClass(
                parent,
                customView
            )
        }

    }

}
