package com.unipi.torpiles.smartalert.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.unipi.torpiles.smartalert.R
import com.unipi.torpiles.smartalert.database.FirestoreHelper
import com.unipi.torpiles.smartalert.databinding.ActivityAddSubmissionBinding
import com.unipi.torpiles.smartalert.models.Submission
import com.unipi.torpiles.smartalert.utils.*
import com.unipi.torpiles.smartalert.utils.Constants.STORAGE_PATH_SUBMISSIONS
import java.io.IOException


class AddSubmissionActivity : BaseActivity() {
    private lateinit var binding: ActivityAddSubmissionBinding

    // A global variable for user submission.
    private lateinit var mSubmission: Submission

    // Add a global variable for URI of a selected image from phone storage.
    private var mSelectedImageFileUri: Uri? = null

    private var mUserProfileImageURL: String = ""

    private lateinit var locationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSubmissionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        init()
    }

    private fun init() {
        // Preparing location requests.
        locationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_HIGH_ACCURACY
            interval = 5000
            fastestInterval = 2000
            maxWaitTime = 100
        }

        setupUI()
        setupActionBar()
        setupClickListeners()
    }

    private fun setupUI() {
        binding.apply {
            inputTxtCategory.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    inputTxtLayoutCategory.isErrorEnabled = false
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            inputTxtCategory.setOnClickListener {
                val dialog = DialogUtils().showDialogSelectCategory(this@AddSubmissionActivity)
                dialog.show()

                val dialogSelectButton = dialog.findViewById<MaterialButton>(R.id.btn_Dialog_Select)
                dialogSelectButton.setOnClickListener {
                    if (dialog.findViewById<RadioButton>(R.id.radioButtonNaturalDisasters).isChecked) {
                        inputTxtCategory.setText(getString(R.string.natural_disaster))
                    }
                    else if (dialog.findViewById<RadioButton>(R.id.radioButtonWaterFlood).isChecked) {
                        inputTxtCategory.setText(getString(R.string.water_flood))
                    }
                    else if (dialog.findViewById<RadioButton>(R.id.radioButtonTsunami).isChecked) {
                        inputTxtCategory.setText(getString(R.string.tsunami))
                    }
                    else if (dialog.findViewById<RadioButton>(R.id.radioButtonFire).isChecked) {
                        inputTxtCategory.setText(getString(R.string.fire))
                    }
                    else if (dialog.findViewById<RadioButton>(R.id.radioButtonRobbery).isChecked) {
                        inputTxtCategory.setText(getString(R.string.robbery))
                    }
                    else if (dialog.findViewById<RadioButton>(R.id.radioButtonFog).isChecked) {
                        inputTxtCategory.setText(getString(R.string.fog))
                    }
                    else if (dialog.findViewById<RadioButton>(R.id.radioButtonDamages).isChecked) {
                        inputTxtCategory.setText(getString(R.string.damages))
                    }
                    else if (dialog.findViewById<RadioButton>(R.id.radioButtonOther).isChecked) {
                        inputTxtCategory.setText(getString(R.string.other))
                    }
                    else {
                        inputTxtCategory.setText("")
                    }
                    dialog.dismiss()
                }
            }

            inputTxtLocation.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    inputTxtLayoutLocation.isErrorEnabled = false
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            inputTxtLocation.setOnClickListener {
                getCurrentLocation()
            }

            inputTxtDescription.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    inputTxtLayoutDescription.isErrorEnabled = false
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnSubmit.setOnClickListener{ checkIfImageWasUploaded() }
            frameLayoutPicture.setOnClickListener {
                if (ContextCompat.checkSelfPermission(
                        this@AddSubmissionActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    Constants.showImageChooser(this@AddSubmissionActivity)
                } else {
                    /*Requests permissions to be granted to this application. These permissions
                     must be requested in your manifest, they should not be granted to your app,
                     and they should have protection level*/
                    ActivityCompat.requestPermissions(
                        this@AddSubmissionActivity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        Constants.READ_STORAGE_PERMISSION_CODE
                    )
                }
            }
        }
    }

    /**
     * A function to get the current location of the user.
     */
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this@AddSubmissionActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (isGPSEnabled()) {
                LocationServices.getFusedLocationProviderClient(this@AddSubmissionActivity)
                    .requestLocationUpdates(locationRequest, object : LocationCallback() {
                        override fun onLocationResult(@NonNull locationResult: LocationResult) {
                            super.onLocationResult(locationResult)
                            LocationServices.getFusedLocationProviderClient(this@AddSubmissionActivity)
                                .removeLocationUpdates(this)
                            if (locationResult.locations
                                    .size > 0
                            ) {
                                val index: Int = locationResult.locations.size - 1
                                val latitude: Double =
                                    locationResult.locations[index].latitude
                                val longitude: Double =
                                    locationResult.locations[index].longitude
                                binding.inputTxtLocation.setText(String.format(getString(R.string.format_location), latitude, longitude))
                            }
                        }
                    }, Looper.getMainLooper())
            } else {
                turnOnGPS()
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    private fun turnOnGPS() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result: Task<LocationSettingsResponse> = LocationServices.getSettingsClient(
            applicationContext
        )
            .checkLocationSettings(builder.build())
        result.addOnCompleteListener {
            try {
                // val response = task.getResult(ApiException::class.java)
                Toast.makeText(
                    this@AddSubmissionActivity,
                    getString(R.string.gps_already_on),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: ApiException) {
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvableApiException = e as ResolvableApiException
                        resolvableApiException.startResolutionForResult(
                            this@AddSubmissionActivity,
                            2
                        )
                    } catch (ex: SendIntentException) {
                        ex.printStackTrace()
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                }
            }
        }
    }

    private fun isGPSEnabled(): Boolean {
        var locationManager: LocationManager? = null
        if (locationManager == null) {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
    /**
     * A function to validate the entries of a new user.
     */
    private fun validateDetails(): Boolean {
        binding.apply {
            return when {
                TextUtils.isEmpty(inputTxtCategory.text.toString().trim { it <= ' ' }) -> {
                    SnackBarErrorClass
                        .make(root, getString(R.string.txt_error_empty_category))
                        .setBehavior(Constants.SNACKBAR_BEHAVIOR)
                        .show()
                    inputTxtLayoutLocation.requestFocus()
                    inputTxtLayoutLocation.error = getString(R.string.txt_error_empty_category)
                    false
                }

                TextUtils.isEmpty(inputTxtLocation.text.toString().trim { it <= ' ' }) -> {
                    SnackBarErrorClass
                        .make(root, getString(R.string.txt_error_empty_location))
                        .setBehavior(Constants.SNACKBAR_BEHAVIOR)
                        .show()
                    inputTxtLayoutLocation.requestFocus()
                    inputTxtLayoutLocation.error = getString(R.string.txt_error_empty_location)
                    false
                }

                TextUtils.isEmpty(inputTxtDescription.text.toString().trim { it <= ' ' }) -> {
                    SnackBarErrorClass
                        .make(root, getString(R.string.txt_error_empty_description))
                        .setBehavior(Constants.SNACKBAR_BEHAVIOR)
                        .show()
                    inputTxtLayoutDescription.requestFocus()
                    inputTxtLayoutDescription.error = getString(R.string.txt_error_empty_password)
                    false
                }

                else -> true
            }
        }
    }

    /**
     * A function to submit user's problem to Firestore
     */
    private fun submitUserProblem() {

        // Check with validate function if the entries are valid or not.
        if (validateDetails()) {

            binding.apply  {
                val location: String = inputTxtLocation.text.toString().trim { it <= ' ' }
                val description: String = inputTxtDescription.text.toString().trim { it <= ' ' }
                val category: String = inputTxtCategory.text.toString().trim { it <= ' ' }

                mSubmission = Submission(
                    FirestoreHelper().getCurrentUserID(),
                    location.split(", ")[0],
                    location.split(", ")[1],
                    category,
                    description,
                    mUserProfileImageURL
                )

                FirestoreHelper().addSubmission(this@AddSubmissionActivity, mSubmission)
            }
        }
        else
            binding.btnSubmit.startAnimation(AnimationUtils.loadAnimation(this@AddSubmissionActivity, R.anim.shake))
    }

    /**
     * A function to notify the success result after completing all required steps.
     */
    fun submitUserProblemSuccess() {
        // Hide the progress dialog.
        hideProgressDialog()

        IntentUtils().goToMainActivity(this, true)
        finish()
    }

    private fun checkIfImageWasUploaded() {
        // Show the progress dialog.
        showProgressDialog()

        if (mSelectedImageFileUri != null) {
            FirestoreHelper().uploadImageToCloudStorage(
                this@AddSubmissionActivity,
                mSelectedImageFileUri,
                STORAGE_PATH_SUBMISSIONS + Constants.SUBMISSION_IMAGE
            )
        }
        else {
            SnackBarErrorClass
                .make(binding.root, getString(R.string.txt_error_empty_image))
                .setBehavior(Constants.SNACKBAR_BEHAVIOR)
                .show()
            binding.btnSubmit.startAnimation(AnimationUtils.loadAnimation(this@AddSubmissionActivity, R.anim.shake))

            // Hide dialog since there is an error.
            hideProgressDialog()
        }
    }

    /**
     * A function to notify the success result of image upload to the Cloud Storage.
     *
     * @param imageURL After successful upload the Firebase Cloud returns the URL.
     */
    fun imageUploadSuccess(imageURL: String) {
        mUserProfileImageURL = imageURL

        submitUserProblem()
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
            //If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this@AddSubmissionActivity)
            }
            else {
                //Displaying another toast if permission is not granted
                Toast.makeText(
                    this,
                    resources.getString(R.string.read_storage_permission_denied),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        else if (requestCode == Constants.LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    getCurrentLocation()
                }
                else {
                    turnOnGPS()
                }
            }
        }
    }

    /**
     * Receive the result from a previous call to
     * {@link #startActivityForResult(Intent, int)}.  This follows the
     * related Activity API as described there in
     * {@link Activity#onActivityResult(int, int, Intent)}.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.PICK_IMAGE_REQUEST_CODE) {
                if (data != null) {
                    try {

                        // The uri of selected image from phone storage.
                        mSelectedImageFileUri = data.data!!

                        GlideLoader(this@AddSubmissionActivity).loadUserPicture(
                            mSelectedImageFileUri!!,
                            binding.imageViewPicture
                        )
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@AddSubmissionActivity,
                            resources.getString(R.string.image_selection_failed),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
            else if (requestCode == Constants.GET_LOCATION_REQUEST_CODE) {
                if (resultCode == Activity.RESULT_OK) {
                    getCurrentLocation()
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // A log is printed when user close or cancel the image selection.
            Log.e("Request Cancelled", "Image selection cancelled")
        }
    }

    private fun setupActionBar() {
        binding.toolbar.apply {
            setSupportActionBar(root)
            textViewActionBarLabel.text = getString(R.string.txt_add_a_submission)
        }

        val actionBar = supportActionBar
        actionBar?.let {
            it.setDisplayShowCustomEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_chevron_left_24dp)
        }
    }
}
