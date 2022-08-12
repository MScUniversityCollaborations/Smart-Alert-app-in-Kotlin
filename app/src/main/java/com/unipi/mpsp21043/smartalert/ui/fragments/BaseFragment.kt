package com.unipi.mpsp21043.smartalert.ui.fragments

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.unipi.mpsp21043.smartalert.ui.activities.SignInActivity

open class BaseFragment : Fragment() {

    fun goToSignInActivity(context: Context) {
        startActivity(Intent(context, SignInActivity::class.java))
    }

}
