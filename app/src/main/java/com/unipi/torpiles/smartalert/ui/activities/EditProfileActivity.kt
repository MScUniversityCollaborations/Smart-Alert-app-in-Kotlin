package com.unipi.torpiles.smartalert.ui.activities

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.unipi.torpiles.smartalert.R
import com.unipi.torpiles.smartalert.database.FirestoreHelper
import com.unipi.torpiles.smartalert.databinding.ActivityEditProfileBinding
import com.unipi.torpiles.smartalert.models.User
import com.unipi.torpiles.smartalert.utils.Constants
import com.unipi.torpiles.smartalert.utils.Constants.STORAGE_PATH_USERS
import com.unipi.torpiles.smartalert.utils.GlideLoader
import com.unipi.torpiles.smartalert.utils.SnackBarErrorClass
import java.io.IOException


class EditProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var mUserDetails: User

    // Add a global variable for URI of a selected image from phone storage.
    private var mSelectedImageFileUri: Uri? = null

    private var mUserProfileImageURL: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        setupUI()

        if (intent.hasExtra(Constants.EXTRA_USER_DETAILS)) {
            // Get the user details from intent as a ParcelableExtra.
            mUserDetails = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!

            binding.apply {
                inputTxtFullName.setText(mUserDetails.fullName)
                countryCodePicker.setCountryForPhoneCode(mUserDetails.phoneCode)
                inputTxtPhoneNumber.setText(mUserDetails.phoneNumber)

                // Set notifications value in radio group.
                if (mUserDetails.notifications)
                    radioButtonNotificationsYes.isChecked = true
                else
                    radioButtonNotificationsNo.isChecked = true

                if (mUserDetails.profImgUrl.isNotEmpty()) {
                    GlideLoader(this@EditProfileActivity).loadUserPicture(
                        mUserDetails.profImgUrl,
                        binding.circleImageViewUserPicture
                    )
                }
            }
        }
    }

    private fun uploadPicture() {
        if (validateFields()) {
            showProgressDialog()

            if (mSelectedImageFileUri != null) {

                FirestoreHelper().uploadImageToCloudStorage(
                    this@EditProfileActivity,
                    mSelectedImageFileUri,
                    STORAGE_PATH_USERS + Constants.FIELD_IMG_URL
                )
            } else {
                updateProfileToFirestore()
            }
        }
    }

    private fun updateProfileToFirestore() {
        val userHashMap = HashMap<String, Any>()

        // Here we get the text from editText and trim the space
        val inputFullName = binding.inputTxtFullName.text.toString().trim { it <= ' ' }
        val inputPhoneCode = binding.countryCodePicker.selectedCountryCodeAsInt
        val inputPhoneNumber = binding.inputTxtPhoneNumber.text.toString().trim { it <= ' ' }

        binding.apply {
            if (inputFullName != mUserDetails.fullName) {
                userHashMap[Constants.FIELD_FULL_NAME] = inputFullName
            }

            var notifications = true
            if (!radioButtonNotificationsYes.isChecked)
                notifications = false

            if (mUserProfileImageURL.isNotEmpty()) {
                userHashMap[Constants.FIELD_IMG_URL] = mUserProfileImageURL
            }

            if (inputPhoneNumber.isNotEmpty() && inputPhoneNumber != mUserDetails.phoneNumber) {
                userHashMap[Constants.FIELD_PHONE_NUMBER] = inputPhoneNumber
            }

            if (notifications != mUserDetails.notifications) {
                userHashMap[Constants.FIELD_NOTIFICATIONS] = notifications
            }

            if (inputPhoneCode != 0) {
                userHashMap[Constants.FIELD_PHONE_CODE] = inputPhoneCode
            }

            // Here if user is about to complete the profile then update the field or else no need.
            // false: User profile is incomplete.
            // true: User profile is completed.
            if (!mUserDetails.profileCompleted && (inputPhoneNumber != "" && inputPhoneCode != 0 && mUserProfileImageURL != "")) {
                userHashMap[Constants.FIELD_COMPLETE_PROFILE] = true
            }

            FirestoreHelper().updateProfile(this@EditProfileActivity, userHashMap)
        }
    }

    fun successUpdateProfileToFirestore() {
        // Hide progress dialog
        hideProgressDialog()

        Toast.makeText(
            this,
            resources.getString(R.string.txt_profile_updated),
            Toast.LENGTH_SHORT
        ).show()

        setResult(RESULT_OK)
        finish()
    }

    private fun validateFields(): Boolean {
        binding.apply {
            return when {
                TextUtils.isEmpty(inputTxtFullName.text.toString().trim { it <= ' ' }) -> {
                    SnackBarErrorClass
                        .make(root, getString(R.string.txt_error_empty_name))
                        .setBehavior(Constants.SNACKBAR_BEHAVIOR)
                        .show()
                    inputTxtLayoutFullName.requestFocus()
                    inputTxtLayoutFullName.error = getString(R.string.txt_error_empty_name)
                    false
                }

                TextUtils.isEmpty(inputTxtPhoneNumber.text.toString().trim { it <= ' ' }) -> {
                    SnackBarErrorClass
                        .make(root, getString(R.string.txt_error_empty_phone))
                        .setBehavior(Constants.SNACKBAR_BEHAVIOR)
                        .show()
                    inputTxtLayoutPhoneNumber.requestFocus()
                    inputTxtLayoutPhoneNumber.error = getString(R.string.txt_error_empty_phone)
                    false
                }

                else -> true
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            imgUserPictureFrame.setOnClickListener {
                if (ContextCompat.checkSelfPermission(
                        this@EditProfileActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    Constants.showImageChooserV2(activityResultLauncherGetImage)
                    //Constants.showImageChooser(this@EditProfileActivity)
                }
                else {
                    /*Requests permissions to be granted to this application. These permissions
                     must be requested in your manifest, they should not be granted to your app,
                     and they should have protection level*/
                    ActivityCompat.requestPermissions(
                        this@EditProfileActivity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        Constants.READ_STORAGE_PERMISSION_CODE
                    )
                }
            }
        }
    }

    private val activityResultLauncherGetImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            if (it.data != null) {
                try {
                    // The uri of selected image from phone storage.
                    mSelectedImageFileUri = it.data!!.data!!

                    GlideLoader(this@EditProfileActivity).loadUserPicture(
                        mSelectedImageFileUri!!,
                        binding.circleImageViewUserPicture
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@EditProfileActivity,
                        resources.getString(R.string.image_selection_failed),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
        else if (it.resultCode == Activity.RESULT_CANCELED) {
            // A log is printed when user close or cancel the image selection.
            Log.e("Request Cancelled", "Image selection cancelled")
        }
    }

    /**
     * This function will identify the result of runtime permission after the user allows or deny permission based on the unique code.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            // If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooserV2(activityResultLauncherGetImage)
            } else {
                // Displaying another toast if permission is not granted
                Toast.makeText(
                    this,
                    resources.getString(R.string.read_storage_permission_denied),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupUI() {
        setupClickListeners()
        setupActionBar()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar.root)

        val actionBar = supportActionBar
        binding.apply {
            toolbar.imgBtnSave.setOnClickListener {
                uploadPicture()
            }
        }
        actionBar?.let {
            it.setDisplayShowCustomEnabled(true)
            it.setCustomView(R.layout.toolbar_title_only)
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_chevron_left_24dp)
        }
    }

    /**
     * A function to notify the success result of image upload to the Cloud Storage.
     *
     * @param imageURL After successful upload the Firebase Cloud returns the URL.
     */
    fun imageUploadSuccess(imageURL: String) {

        mUserProfileImageURL = imageURL

        updateProfileToFirestore()
    }
}
