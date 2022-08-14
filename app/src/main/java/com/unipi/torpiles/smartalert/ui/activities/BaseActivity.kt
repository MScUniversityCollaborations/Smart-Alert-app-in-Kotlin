package com.unipi.torpiles.smartalert.ui.activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.unipi.torpiles.smartalert.R
import com.unipi.torpiles.smartalert.utils.Constants
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * A base activity class is used to define the functions and members which we will use in all the activities.
 * It inherits the AppCompatActivity class so in other activity class we will replace the AppCompatActivity with BaseActivity.
 */
open class BaseActivity : AppCompatActivity() {

    // Create an executor that executes tasks in a background thread.
    private val backgroundExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    // A global variable for double back press feature.
    private var doubleBackToExitPressedOnce = false

    /**
     * This is a progress dialog instance which we will initialize later on.
     */
    private lateinit var mProgressDialog: Dialog

    /**
     * This function is used to show the progress dialog with the title and message to user.
     */
    fun showProgressDialog() {
        mProgressDialog = Dialog(this)

        /*Set the screen content from a layout resource.
        The resource will be inflated, adding all top-level views to the screen.*/
        mProgressDialog.setContentView(R.layout.dialog_progress)

        mProgressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        mProgressDialog.setCancelable(false)
        mProgressDialog.setCanceledOnTouchOutside(false)

        //Start the dialog and display it on screen.
        mProgressDialog.show()
    }

    /**
     * This function is used to dismiss the progress dialog if it is visible to user.
     */
    fun hideProgressDialog() {
        mProgressDialog.dismiss()
    }

    fun goToMainActivity(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    fun goToMainActivity(context: Context, showProfileNotCompletedSnackBar: Boolean) {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(Constants.EXTRA_PROFILE_NOT_COMPLETED_SNACKBAR, showProfileNotCompletedSnackBar)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }


    fun goToSignInActivity(context: Context) {
        startActivity(Intent(context, SignInActivity::class.java))
    }

    fun goToSignInActivity(context: Context, showRegisteredSnackBar: Boolean, userEmail: String) {
        val intent = Intent(context, SignInActivity::class.java)
        intent.putExtra(Constants.EXTRA_REG_USERS_SNACKBAR, showRegisteredSnackBar)
        intent.putExtra(Constants.EXTRA_USER_EMAIL, userEmail)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        finish()
        startActivity(intent)
    }

    /**
     * A function to implement the double back press feature to exit the app.
     */
    fun doubleBackToExit() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true

        Toast.makeText(
            this,
            resources.getString(R.string.txt_please_click_back_again_to_exit),
            Toast.LENGTH_SHORT
        ).show()

        backgroundExecutor.schedule({
            doubleBackToExitPressedOnce = false
        }, 2000, TimeUnit.MILLISECONDS)
    }

}
