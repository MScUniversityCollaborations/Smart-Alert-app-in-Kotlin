package com.unipi.torpiles.smartalert.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * A data model class with required fields.
 */
@Keep
@Parcelize
@IgnoreExtraProperties
data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val phoneCode: Int = 0,

    @ServerTimestamp
    val dateRegistered: Date = Date(),
    val role: String = "",
    val profImgUrl: String = "",
    val profileCompleted: Boolean = false,
) : Parcelable
