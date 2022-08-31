package com.unipi.torpiles.smartalert.utils

import android.app.Activity
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultLauncher
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.snackbar.BaseTransientBottomBar
import java.text.SimpleDateFormat
import java.util.*


// Create a custom object to declare all the constant values in a single file. The constant values declared here is can be used in whole application.
/**
 * A custom object to declare all the constant values in a single file. The constant values declared here is can be used in whole application.
 */
object Constants {

    // Default Constants
    const val TAG: String = "[SmartAlert]"
    const val DEFAULT_VEILED_ITEMS_VERTICAL: Int = 15
    val SNACKBAR_BEHAVIOR = BaseTransientBottomBar.Behavior().apply {
        setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY) }
    //A unique code for asking the Read Storage Permission using this we will be check and identify in the method onRequestPermissionsResult in the Base Activity.
    const val READ_STORAGE_PERMISSION_CODE = 2
    //A unique code for asking the Location Permissions using this we will be check and identify in the method onRequestPermissionsResult in the Base Activity.
    const val LOCATION_PERMISSION_CODE = 1
    // A unique code of image selection from Phone Storage.
    const val PICK_IMAGE_REQUEST_CODE = 2
    // A unique code of image selection from Phone Storage.
    const val GET_LOCATION_REQUEST_CODE = 1

    val standardSimpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH)

    // Firebase Constants
    // Collections
    const val COLLECTION_USERS: String = "users"
    const val COLLECTION_SUBMISSIONS: String = "submissions"
    const val COLLECTION_ADDRESSES: String = "addresses"

    // DB Fields
    // COMMON
    const val FIELD_DATE_ADDED: String = "dateAdded"
    // SUBMISSIONS
    const val FIELD_SUBMISSION_ID: String = "submissionId"
    const val SUBMISSION_IMAGE: String = "SUBMISSION_IMAGE"
    // USER
    const val FIELD_USER_ID: String = "userId"
    const val FIELD_FULL_NAME: String = "fullName"
    const val FIELD_NOTIFICATIONS: String = "notifications"
    const val FIELD_IMG_URL: String = "imgUrl"
    const val FIELD_COMPLETE_PROFILE: String = "profileCompleted"
    const val FIELD_PHONE_NUMBER: String = "phoneNumber"
    const val FIELD_PHONE_CODE: String = "phoneCode"
    const val FIELD_REGISTRATION_TOKENS: String = "registrationTokens"

    // Intent Extras
    const val EXTRA_SUBMISSION_ID: String = "extraSubmissionId"
    const val EXTRA_SUBMISSION_MODEL: String = "extraSubmissionModel"
    const val EXTRA_REG_USERS_SNACKBAR: String = "extraShowRegisteredUserSnackbar"
    const val EXTRA_PROFILE_NOT_COMPLETED_SNACKBAR: String = "extraShowProfileNotCompletedSnackbar"
    const val EXTRA_USER_EMAIL: String = "extraUserEmail"
    const val EXTRA_USER_DETAILS: String = "extraUserDetails"
    const val EXTRA_SHOW_SUBMISSION_CREATED_SNACKBAR= "extraSubmissionCreatedSnackbar"
    const val EXTRA_NOTIFICATION_ID: String = "extraNotificationId"

    // Notifications
    const val PAYLOAD_SUBMISSION_ID: String = "SUBMISSION_ID"
    const val PAYLOAD_SUBMISSION_IMG_URL: String = "SUBMISSION_IMG_URL"
    const val PAYLOAD_SUBMISSION_DESC: String = "SUBMISSION_DESC"
    const val GROUP_KEY_FAVORITES: String = "com.unipi.torpiles.smartalert.submissions"
    const val NOTIFICATION_CHANNEL_ID: String = "com.unipi.torpiles.smartalert"
    const val NOTIFICATION_ID : Int = 100

    // Other
    const val STORAGE_PATH_USERS: String = "Users/"
    const val STORAGE_PATH_SUBMISSIONS: String = "Submissions/"
    const val ROLE_MEMBER = "Member"
    const val ROLE_ADMIN = "Admin"

    /**
     * A function for user profile image selection from phone storage.
     */
    fun showImageChooser(activity: Activity) {
        // An intent for launching the image selection of phone storage.
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        // Launches the image selection of phone storage using the constant code.
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    /**
     * A function for user profile image selection from phone storage.
     */
    fun showImageChooserV2(activityResultLauncher: ActivityResultLauncher<Intent>) {
        // An intent for launching the image selection of phone storage.
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        // galleryIntent.type = "image/*"
        activityResultLauncher.launch(galleryIntent)
    }

    /**
     * A function to get the image file extension of the selected image.
     *
     * @param activity Activity reference.
     * @param uri Image file uri.
     */
    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        /*
         * MimeTypeMap: Two-way map that maps MIME-types to file extensions and vice versa.
         *
         * getSingleton(): Get the singleton instance of MimeTypeMap.
         *
         * getExtensionFromMimeType: Return the registered extension for the given MIME type.
         *
         * contentResolver.getType: Return the MIME type of the given content URL.
         */
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }

    /**
     * A function to check internet connection.
     *
     * @param context Context reference.
     */
    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val n: Network? = cm.activeNetwork
        if (n != null) {
            val nc = cm.getNetworkCapabilities(n)
            return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            )
        }
        return false
    }
}
// END
