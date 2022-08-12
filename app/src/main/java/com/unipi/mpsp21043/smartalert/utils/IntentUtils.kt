package com.unipi.mpsp21043.smartalert.utils

import android.content.Context
import android.content.Intent
import com.unipi.mpsp21043.smartalert.models.Submission
import com.unipi.mpsp21043.smartalert.models.User
import com.unipi.mpsp21043.smartalert.ui.activities.AddSubmissionActivity
import com.unipi.mpsp21043.smartalert.ui.activities.EditProfileActivity
import com.unipi.mpsp21043.smartalert.ui.activities.MainActivity
import com.unipi.mpsp21043.smartalert.ui.activities.SubmissionDetailsActivity

class IntentUtils {

    fun goToUpdateUserDetailsActivity(context: Context, user: User) {
        val intent = Intent(context, EditProfileActivity::class.java)
        intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.applicationContext.startActivity(intent)
    }

    fun goToAddSubmissionActivity(context: Context) {
        val intent = Intent(context, AddSubmissionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.applicationContext.startActivity(intent)
    }

    fun goToSubmissionDetailsActivity(context: Context, model: Submission) {
        val intent = Intent(context, SubmissionDetailsActivity::class.java)
        intent.putExtra(Constants.EXTRA_SUBMISSION_MODEL, model)
        context.startActivity(intent)
    }

    fun goToMainActivity(context: Context, showSubmissionCreatedSnackbar: Boolean) {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(Constants.EXTRA_SHOW_SUBMISSION_CREATED_SNACKBAR, showSubmissionCreatedSnackbar)
        context.startActivity(intent)
    }

}
