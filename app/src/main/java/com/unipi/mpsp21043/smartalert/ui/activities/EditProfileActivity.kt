package com.unipi.mpsp21043.smartalert.ui.activities

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.unipi.mpsp21043.smartalert.R
import com.unipi.mpsp21043.smartalert.database.FirestoreHelper
import com.unipi.mpsp21043.smartalert.databinding.ActivityEditProfileBinding
import com.unipi.mpsp21043.smartalert.models.User
import com.unipi.mpsp21043.smartalert.utils.Constants
import com.unipi.mpsp21043.smartalert.utils.SnackBarErrorClass


class EditProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var mUserDetails: User

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
            }
        }
    }

    private fun updateProfileToFirestore() {

        var fullName = ""
        var phoneCode = 0
        var phoneNumber = ""

        // Here we get the text from editText and trim the space
        binding.run {
            fullName = inputTxtFullName.text.toString().trim { it <= ' ' }
            phoneCode = countryCodePicker.selectedCountryCodeAsInt
            phoneNumber = inputTxtPhoneNumber.text.toString().trim { it <= ' ' }
        }

        if (validateFields()) {
            showProgressDialog()

            val userModel = User(
                id = FirestoreHelper().getCurrentUserID(),
                fullName = fullName,
                phoneNumber = phoneNumber,
                phoneCode = phoneCode
            )

            FirestoreHelper().updateProfile(this, userModel)
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

    private fun setupUI() {
        setupActionBar()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar.root)

        val actionBar = supportActionBar
        binding.apply {
            toolbar.imgBtnSave.setOnClickListener {
                updateProfileToFirestore()
            }
        }
        actionBar?.let {
            it.setDisplayShowCustomEnabled(true)
            it.setCustomView(R.layout.toolbar_title_only)
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_chevron_left_24dp)
        }
    }
}
