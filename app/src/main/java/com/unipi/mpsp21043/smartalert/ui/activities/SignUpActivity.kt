package com.unipi.mpsp21043.smartalert.ui.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.animation.AnimationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.unipi.mpsp21043.smartalert.R
import com.unipi.mpsp21043.smartalert.database.FirestoreHelper
import com.unipi.mpsp21043.smartalert.databinding.ActivitySignUpBinding
import com.unipi.mpsp21043.smartalert.models.User
import com.unipi.mpsp21043.smartalert.utils.Constants
import com.unipi.mpsp21043.smartalert.utils.SnackBarErrorClass


class SignUpActivity : BaseActivity() {
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        init()
    }

    private fun init() {
        setupUI()
        setupActionBar()
        setupClickListeners()
    }

    private fun setupUI() {
        binding.apply {
            inputTxtName.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    inputTxtLayoutName.isErrorEnabled = false
                }

                override fun afterTextChanged(s: Editable?) {}
            })

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
            txtViewSignIn.setOnClickListener { onBackPressed() }
            btnSignUp.setOnClickListener{ registerUser() }
        }
    }

    /**
     * A function to validate the entries of a new user.
     */
    private fun validateRegisterDetails(): Boolean {
        binding.apply {
            return when {
                TextUtils.isEmpty(inputTxtName.text.toString().trim { it <= ' ' }) -> {
                    SnackBarErrorClass
                        .make(root, getString(R.string.txt_error_empty_name))
                        .setBehavior(Constants.SNACKBAR_BEHAVIOR)
                        .show()
                    inputTxtLayoutName.requestFocus()
                    inputTxtLayoutName.error = getString(R.string.txt_error_empty_name)
                    false
                }

                TextUtils.isEmpty(inputTxtEmail.text.toString().trim { it <= ' ' }) -> {
                    SnackBarErrorClass
                        .make(root, getString(R.string.txt_error_empty_email))
                        .setBehavior(Constants.SNACKBAR_BEHAVIOR)
                        .show()
                    inputTxtLayoutEmail.requestFocus()
                    inputTxtLayoutEmail.error = getString(R.string.txt_error_empty_email)
                    false
                }

                TextUtils.isEmpty(inputTxtPassword.text.toString().trim { it <= ' ' }) -> {
                    SnackBarErrorClass
                        .make(root, getString(R.string.txt_error_empty_password))
                        .setBehavior(Constants.SNACKBAR_BEHAVIOR)
                        .show()
                    inputTxtLayoutPassword.requestFocus()
                    inputTxtLayoutPassword.error = getString(R.string.txt_error_empty_password)
                    false
                }

                else -> true
            }
        }
    }

    /**
     * A function to register the user with email and password using FirebaseAuth.
     */
    private fun registerUser() {

        // Check with validate function if the entries are valid or not.
        if (validateRegisterDetails()) {

            // Show the progress dialog.
            showProgressDialog()

            binding.apply  {
                val email: String = inputTxtEmail.text.toString().trim { it <= ' ' }
                val password: String = inputTxtPassword.text.toString().trim { it <= ' ' }

                // Create an instance and create a register a user with email and password.
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener{ task ->

                        // If the registration is successfully done
                        if (task.isSuccessful) {

                            // Firebase registered user
                            val firebaseUser: FirebaseUser = task.result!!.user!!

                            // Instance of User data model class.
                            val user = User(
                                firebaseUser.uid,
                                inputTxtName.text.toString().trim { it <= ' ' },
                                inputTxtEmail.text.toString().trim { it <= ' ' }
                            )


                            // Pass the required values in the constructor.
                            FirestoreHelper().registerUser(this@SignUpActivity, user)
                        } else {

                            // Hide the progress dialog
                            hideProgressDialog()

                            // If the registering is not successful then show the error message.
                            SnackBarErrorClass
                                .make(root, task.exception!!.message.toString())
                                .setBehavior(Constants.SNACKBAR_BEHAVIOR)
                                .show()
                        }
                    }
            }
        }
        else
            binding.btnSignUp.startAnimation(AnimationUtils.loadAnimation(this@SignUpActivity, R.anim.shake))
    }

    /**
     * A function to notify the success result of Firestore entry when the user is registered successfully.
     */
    fun userRegistrationSuccess() {

        // Hide the progress dialog
        hideProgressDialog()

        /**
         * Here the new user registered is automatically signed-in so we just sign-out the user from firebase
         * and send him to Intro Screen for Sign-In
         */
        FirebaseAuth.getInstance().signOut()

        goToSignInActivity(this@SignUpActivity,
            true,
            binding.inputTxtEmail.text.toString())

        // Finish the Register Screen
        finish()
    }

    private fun setupActionBar() {
        binding.toolbar.apply {
            setSupportActionBar(root)
            textViewActionBarLabel.text = getString(R.string.txt_register)
        }

        val actionBar = supportActionBar
        actionBar?.let {
            it.setDisplayShowCustomEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_chevron_left_24dp)
        }
    }
}
