package com.unipi.torpiles.smartalert.ui.fragments

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.unipi.torpiles.smartalert.ui.activities.SignInActivity
import com.unipi.torpiles.smartalert.ui.activities.SignUpActivity

open class BaseFragment : Fragment() {

    fun goToSignInActivity(context: Context) {
        startActivity(Intent(context, SignInActivity::class.java))
    }

    fun goToSignUpActivity(context: Context) {
        startActivity(Intent(context, SignUpActivity::class.java))
    }
}
