package com.unipi.torpiles.smartalert.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.animation.AnimationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.unipi.torpiles.smartalert.R
import com.unipi.torpiles.smartalert.database.FirestoreHelper
import com.unipi.torpiles.smartalert.databinding.ActivitySignInBinding
import com.unipi.torpiles.smartalert.models.User
import com.unipi.torpiles.smartalert.utils.Constants
import com.unipi.torpiles.smartalert.utils.SnackBarErrorClass
import com.unipi.torpiles.smartalert.utils.SnackBarSuccessClass


class SignInActivity : BaseActivity() {
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        init()
    }

    private fun init() {
        if (intent.hasExtra(Constants.EXTRA_REG_USERS_SNACKBAR)) {
            if (intent.extras?.getBoolean(Constants.EXTRA_REG_USERS_SNACKBAR) == true) {
                SnackBarSuccessClass
                    .make(binding.root, getString(R.string.txt_congratulations_2))
                    .show()
            }
            binding.inputTxtEmail.setText(intent.getStringExtra(Constants.EXTRA_USER_EMAIL))
        }

        setupUI()
        setupActionBar()
        setupClickListeners()
    }

    private fun setupUI() {
        binding.apply {
            inputTxtEmail.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    inputTxtLayoutEmail.isErrorEnabled = false
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            inputTxtPassword.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    inputTxtLayoutPassword.isErrorEnabled = false
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            txtViewSignUp.setOnClickListener {
                // Launch the sign up screen when the user clicks on the sign up text.
                val intent = Intent(this@SignInActivity, SignUpActivity::class.java)
                startActivity(intent)
            }
            txtViewForgotPassword.setOnClickListener {
                // Launch the forgot password screen when the user clicks on the forgot password text.
                val intent = Intent(this@SignInActivity, ForgotPasswordActivity::class.java)
                startActivity(intent)
            }
            btnSignIn.setOnClickListener { signInUser() }
        }
    }

    private fun signInUser() {
        if (Constants.isNetworkConnected(this@SignInActivity)) {
            if (validateFields()) {
                // Show the progress dialog.
                showProgressDialog()

                binding.apply {
                    // Get the text from editText and trim the space
                    val email = inputTxtEmail.text.toString().trim { it <= ' ' }
                    val password = inputTxtPassword.text.toString().trim { it <= ' ' }

                    // Log-In using FirebaseAuth
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->

                            if (task.isSuccessful) {
                                // Save token.

                                FirestoreHelper().saveFCMToken(this@SignInActivity)
                            } else {
                                // Hide the progress dialog
                                hideProgressDialog()
                                SnackBarErrorClass
                                    .make(root, task.exception!!.message.toString())
                                    .show()
                            }
                        }
                }
            }
            else
                binding.btnSignIn.startAnimation(AnimationUtils.loadAnimation(this@SignInActivity, R.anim.shake))
        }
        else {
            SnackBarErrorClass
                .make(binding.root, getString(R.string.txt_error_no_internet_connection))
                .show()
        }

    }

    fun fcmTokenSavedSuccess() {
        FirestoreHelper().getUserDetails(this@SignInActivity)
    }

    /**
     * A function to notify user that logged in success and get the user details from the FireStore database after authentication.
     */
    fun userLoggedInSuccess(user: User) {

        // Hide the progress dialog.
        hideProgressDialog()

        if (!user.profileCompleted) {
            // todo
            // If the user profile is incomplete then launch the UserProfileActivity.

            /*val intent = Intent(this@SignInActivity, UserProfileActivity::class.java)
            intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
            startActivity(intent)*/

            goToMainActivity(this@SignInActivity, Constants.SHOW_PROFILE_NOT_COMPLETED_SNACK_BAR)
            finish()
        } else {
            // Redirect the user to Dashboard Screen after log in.
            goToMainActivity(this@SignInActivity)
            finish()
        }
        finish()
    }

    private fun retrieveAndStoreToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val token = task.result


                }
            }
    }

    private fun validateFields(): Boolean {
        binding.apply {
            return when {
                TextUtils.isEmpty(inputTxtEmail.text.toString().trim { it <= ' ' }) -> {
                    SnackBarErrorClass
                        .make(root, getString(R.string.txt_error_empty_email))
                        .show()
                    inputTxtLayoutEmail.requestFocus()
                    inputTxtLayoutEmail.error = getString(R.string.txt_error_empty_email)
                    false
                }

                TextUtils.isEmpty(inputTxtPassword.text.toString().trim { it <= ' ' }) -> {
                    SnackBarErrorClass
                        .make(root, getString(R.string.txt_error_empty_password))
                        .show()
                    inputTxtLayoutPassword.requestFocus()
                    inputTxtLayoutPassword.error = getString(R.string.txt_error_empty_password)
                    false
                }

                else -> true
            }
        }
    }

    private fun setupActionBar() {
        binding.toolbar.apply {
            setSupportActionBar(root)
            textViewActionBarLabel.text = getString(R.string.txt_login)
        }

        val actionBar = supportActionBar
        actionBar?.let {
            it.setDisplayShowCustomEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_chevron_left_24dp)
        }
    }

    override fun onBackPressed() {
        goToMainActivity(this@SignInActivity)
    }
}
