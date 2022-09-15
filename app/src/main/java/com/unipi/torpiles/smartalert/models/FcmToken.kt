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
data class FcmToken(
    var id: String = "",
    val token: String = "",
    @ServerTimestamp
    val dateAdded: Date = Date(),
    val userId: String = "",
) : Parcelable
