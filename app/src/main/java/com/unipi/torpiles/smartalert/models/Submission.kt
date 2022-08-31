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
data class Submission(
    var userId: String = "",

    val lat: String = "",
    val long: String = "",
    val category: String = "",
    val description: String = "",
    val imgUrl: String = "",
    val dangerRank: Int = -1,
    val isHighDanger: Boolean = false,
    val isAccepted: Boolean = false,
    @ServerTimestamp
    val dateAdded: Date = Date(),
    var id: String = "",
) : Parcelable
