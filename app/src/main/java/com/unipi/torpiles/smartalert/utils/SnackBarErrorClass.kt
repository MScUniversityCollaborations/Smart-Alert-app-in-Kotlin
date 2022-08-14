package com.unipi.torpiles.smartalert.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.unipi.torpiles.smartalert.R
import com.unipi.torpiles.smartalert.extension.findSuitableParent

class SnackBarErrorClass(
    parent: ViewGroup,
    content: SnackBarErrorView
) : BaseTransientBottomBar<SnackBarErrorClass>(parent, content, content) {

    init {
        getView().setBackgroundColor(ContextCompat.getColor(view.context, android.R.color.transparent))
        getView().setPadding(0, 0, 0, 0)
        getView().findViewById<MaterialButton>(R.id.btn_Dismiss).setOnClickListener { dismiss() }
        animationMode = ANIMATION_MODE_SLIDE
        behavior = Constants.SNACKBAR_BEHAVIOR
    }

    companion object {

        fun make(view: View, contentTxt: String): SnackBarErrorClass {

            // First we find a suitable parent for our custom view
            val parent = view.findSuitableParent() ?: throw IllegalArgumentException(
                "No suitable parent found from the given view. Please provide a valid view."
            )

            // We inflate our custom view
            val customView = LayoutInflater.from(view.context).inflate(
                R.layout.layout_snackbar_error,
                parent,
                false
            ) as SnackBarErrorView

            customView.apply {
                findViewById<TextView>(R.id.txtView_Content).text = contentTxt
            }

            // We create and return our Snack-bar
            return SnackBarErrorClass(
                parent,
                customView
            )
        }

    }

}
