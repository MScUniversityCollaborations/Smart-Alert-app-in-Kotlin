package com.unipi.torpiles.smartalert.database

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.unipi.torpiles.smartalert.models.Submission
import com.unipi.torpiles.smartalert.models.User
import com.unipi.torpiles.smartalert.ui.activities.*
import com.unipi.torpiles.smartalert.ui.fragments.AllSubmissionsFragment
import com.unipi.torpiles.smartalert.ui.fragments.HomeFragment
import com.unipi.torpiles.smartalert.ui.fragments.MyAccountFragment
import com.unipi.torpiles.smartalert.ui.fragments.MySubmissionsFragment
import com.unipi.torpiles.smartalert.utils.Constants


class FirestoreHelper {

    // Access a Cloud Firestore instance.
    private val dbFirestore = FirebaseFirestore.getInstance()


    // region -User Management-
    /**
     * A function to get the user id of current logged user.
     */
    fun getCurrentUserID(): String {
        // An Instance of currentUser using FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser

        // A variable to assign the currentUserId if it is not null or else it will be blank.
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID
    }

    fun getFCMRegistrationTokenDB(onComplete: (tokens: MutableList<String>) -> Unit) {
        dbFirestore.collection(Constants.COLLECTION_USERS)
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)!!
                onComplete(user.registrationTokens)
            }
            .addOnFailureListener { e ->
                Log.e(
                    Constants.TAG,
                    "Fetching FCM registration token failed.",
                    e
                )
            }
    }

    /**
     * A function that gets the registration fcm tokens from the registered user in the FireStore
     * database.
     *
     * @param registrationTokens todo
     */
    fun setFCMRegistrationToken(registrationTokens: MutableList<String>) {
        dbFirestore.collection(Constants.COLLECTION_USERS)
            .document(getCurrentUserID())
            .update(mapOf(Constants.FIELD_REGISTRATION_TOKENS to registrationTokens))
            .addOnFailureListener { e ->
                Log.e(
                    Constants.TAG,
                    "Update of FCM registration tokens failed.",
                    e
                )
            }
    }

    /**
     * A function to update the existing user details to the cloud firestore.
     *
     * @param activity Base class
     * @param userHashMap Which fields are to be updated.
     */
    fun updateProfile(activity: EditProfileActivity, userHashMap: HashMap<String, Any>) {

        dbFirestore.collection(Constants.COLLECTION_ADDRESSES)
            .document(getCurrentUserID())
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(userHashMap, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.successUpdateProfileToFirestore()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while updating the user's profile.",
                    e
                )
            }
    }

    /**
     * A function to make an entry of the registered user in the FireStore database.
     */
    fun registerUser(activity: SignUpActivity, userInfo: User) {

        // The "users" is collection name. If the collection is already created then it will not create the same one again.
        dbFirestore.collection(Constants.COLLECTION_USERS)
            // Document ID for users fields. Here the document it is the User ID.
            .document(userInfo.id)
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge later on instead of replacing the fields.
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.userRegistrationSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while registering the user.",
                    e
                )
            }
    }

    /**
     * A function to get the logged user details from from FireStore Database.
     */
    fun getUserDetails(activity: Activity) {

        // Here we pass the collection name from which we wants the data.
        dbFirestore.collection(Constants.COLLECTION_USERS)
            // The document id to get the Fields of user.
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->

                Log.d(activity.javaClass.simpleName, document.toString())

                // Here we have received the document snapshot which is converted into the User Data model object.
                val user = document.toObject(User::class.java)!!

                when (activity) {
                    is MainActivity -> {
                        activity.userDetailsSuccess(user)
                    }
                    is SignInActivity -> {
                        // Call a function of base activity for transferring the result to it.
                        activity.userLoggedInSuccess(user)
                    }
                    is AddSubmissionActivity -> {
                        // Call a function of base activity for transferring the result to it.
                        activity.userModelSuccess(user)
                    }
                    is SubmissionDetailsActivity -> {
                        // Call a function of base activity for transferring the result to it.
                        activity.userModelSuccess(user)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error. And print the error in log.
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is AddSubmissionActivity -> {
                        activity.hideProgressDialog()
                    }
                    is SubmissionDetailsActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting user details.",
                    e
                )
            }
    }

    /**
     * A function to get the logged user details from from FireStore Database.
     */
    fun getUserDetails(fragment: Fragment) {

        // Here we pass the collection name from which we wants the data.
        dbFirestore.collection(Constants.COLLECTION_USERS)
            // The document id to get the Fields of user.
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->

                Log.d(fragment.javaClass.simpleName, document.toString())

                // Here we have received the document snapshot which is converted into the User Data model object.
                val user = document.toObject(User::class.java)!!

                when (fragment) {
                    is MyAccountFragment -> {
                        fragment.userDetailsSuccess(user)
                    }
                }
            }
            .addOnFailureListener { e ->

                when (fragment) {
                    is MyAccountFragment -> {
                        fragment.unveilDetails()
                        // TODO Show error
                        /*fragment.hideProgressDialog()*/
                    }
                }

                Log.e(
                    fragment.javaClass.simpleName,
                    "Error while getting user details.",
                    e
                )
            }
    }

    /**
     * A function to get all the submissions list from cloud firestore.
     *
     * @param fragment The fragment is passed as parameter as the function is called from fragment and need to the success result.
     */
    fun getAllSubmissionsList(fragment: Fragment) {
        dbFirestore.collection(Constants.COLLECTION_SUBMISSIONS)
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->

                // Here we get the list of boards in the form of documents.
                Log.d("All Submissions List", document.documents.toString())

                // Here we have created a new instance for Submissions ArrayList.
                val allSubmissionsList: ArrayList<Submission> = ArrayList()

                // A for loop as per the list of documents to convert them into Submissions ArrayList.
                for (i in document.documents) {

                    val submission = i.toObject(Submission::class.java)
                    submission!!.id = i.id

                    allSubmissionsList.add(submission)
                }
                when (fragment) {
                    is HomeFragment -> {
                        fragment.successProblemsListFromFireStore(allSubmissionsList)
                    }
                    is AllSubmissionsFragment -> {
                        fragment.successAllSubmissionsListFromFireStore(allSubmissionsList)
                    }
                    else -> {}
                }
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error based on the base class instance.
                when (fragment) {
                    is HomeFragment -> {
                        // TODO: Show error state maybe
                    }
                    is AllSubmissionsFragment -> {
                        // TODO: Show error state maybe
                    }
                }

                Log.e("Get All Submissions List", "Error while getting submission list.", e)
            }
    }

    /**
     * A function to get the submissions list for a user from cloud firestore.
     *
     * @param fragmentSubmissions The fragment is passed as parameter as the function is called from fragment and need to the success result.
     */
    fun getUserSubmissionsList(fragmentSubmissions: MySubmissionsFragment) {
        dbFirestore.collection(Constants.COLLECTION_SUBMISSIONS)
            .whereEqualTo(Constants.FIELD_USER_ID, getCurrentUserID())
            .orderBy(Constants.FIELD_DATE_ADDED, Query.Direction.DESCENDING)
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->

                // Here we get the list of boards in the form of documents.
                Log.d("User Submissions List", document.documents.toString())

                // Here we have created a new instance for Submission ArrayList.
                val userSubmissionsList: ArrayList<Submission> = ArrayList()

                // A for loop as per the list of documents to convert them into Submission ArrayList.
                for (i in document.documents) {

                    val userSubmission = i.toObject(Submission::class.java)
                    userSubmission!!.id = i.id

                    userSubmissionsList.add(userSubmission)
                }
                fragmentSubmissions.successUserSubmissionsListFromFireStore(userSubmissionsList)
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error based on the base class instance.
                // TODO: Show error state maybe

                Log.e("Get User Submissions List", "Error while getting user submissions list.", e)
            }
    }

    // A function to upload the image to the cloud storage.
    fun uploadImageToCloudStorage(activity: Activity, imageFileURI: Uri?, imageType: String) {

        //getting the storage reference
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            imageType + System.currentTimeMillis() + "."
                    + Constants.getFileExtension(
                activity,
                imageFileURI
            )
        )

        //adding the file to reference
        sRef.putFile(imageFileURI!!)
            .addOnSuccessListener { taskSnapshot ->
                // The image upload is success
                Log.e(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                // Get the downloadable url from the task snapshot
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        Log.e("Downloadable Image URL", uri.toString())

                        // Here call a function of base activity for transferring the result to it.
                        when (activity) {
                            is AddSubmissionActivity -> {
                                activity.imageUploadSuccess(uri.toString())
                            }
                            is EditProfileActivity -> {
                                activity.imageUploadSuccess(uri.toString())
                            }
                        }
                    }
            }
            .addOnFailureListener { exception ->

                // Hide the progress dialog if there is any error. And print the error in log.
                when (activity) {
                    is AddSubmissionActivity -> {
                        activity.hideProgressDialog()
                    }

                    is EditProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(
                    activity.javaClass.simpleName,
                    exception.message,
                    exception
                )
            }
    }

    /**
     * A function to add a submission of the user in the cloud firestore.
     *
     * @param context
     * @param submission SSubmission Info
     */
    fun addSubmission(context: Context, submission: Submission) {

        dbFirestore.collection(Constants.COLLECTION_SUBMISSIONS)
            .document()
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(submission, SetOptions.merge())
            .addOnSuccessListener {
                when (context) {
                    is AddSubmissionActivity -> context.submitUserProblemSuccess()
                }
            }
            .addOnFailureListener { e ->

                // Hide the progress dialog if there is any error.
                when (context) {
                    is AddSubmissionActivity -> context.hideProgressDialog()
                }
                Log.e(
                    context.javaClass.simpleName,
                    "Error while adding a submission.",
                    e
                )
            }
    }

    fun changeSubmissionStatus(activity: Activity, submission: Submission, acceptOrDecline: Boolean) {
        submission.accepted = acceptOrDecline

        dbFirestore.collection(Constants.COLLECTION_SUBMISSIONS)
            .document(submission.id)
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(submission, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                when (activity) {
                    is SubmissionDetailsActivity -> activity.successUpdateSubmissionToFirestore()
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is SubmissionDetailsActivity -> activity.hideProgressDialog()
                }
                // And then print an error log.
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while updating Submission.",
                    e
                )
            }
    }
}
