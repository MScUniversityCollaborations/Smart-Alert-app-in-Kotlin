package com.unipi.torpiles.smartalert.ui.activities

import android.os.Bundle
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.unipi.torpiles.smartalert.R
import com.unipi.torpiles.smartalert.database.FirestoreHelper
import com.unipi.torpiles.smartalert.databinding.ActivitySubmissionDetailsBinding
import com.unipi.torpiles.smartalert.models.Submission
import com.unipi.torpiles.smartalert.models.User
import com.unipi.torpiles.smartalert.utils.Constants
import com.unipi.torpiles.smartalert.utils.GlideLoader

class SubmissionDetailsActivity : BaseActivity(), OnMapReadyCallback {

    /**
     * Class variables
     *
     * @see binding
     * @see modelSubmission
     * */
    private lateinit var binding: ActivitySubmissionDetailsBinding
    private lateinit var modelSubmission: Submission
    private lateinit var modelCurrentUser: User
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // This calls the parent constructor
        binding = ActivitySubmissionDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view) // This is used to align the xml view to this class

        init()
        setupUI()
    }

    private fun init() {
        if (intent.hasExtra(Constants.EXTRA_SUBMISSION_MODEL)) {
            modelSubmission =
                intent.getParcelableExtra(Constants.EXTRA_SUBMISSION_MODEL)!!
        }

        showProgressDialog()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragmentSubmissionDetails) as SupportMapFragment?

        mapFragment?.getMapAsync(this)
    }

    private fun loadSubmissionDetails() {
        binding.apply {
            // Populate the submission details in the UI.
            GlideLoader(this@SubmissionDetailsActivity).loadPictureWide(
                modelSubmission.imgUrl,
                imgViewPicture
            )
            toolbar.textViewActionBarLabel.text = modelSubmission.category
            textViewCategory.text = String.format(getString(R.string.format_category), modelSubmission.category)
            textViewDateAdded.text = modelSubmission.dateAdded.toString()
            textViewDescription.text = String.format(getString(R.string.format_description), modelSubmission.description)
            textViewAddedByUser.text = String.format(getString(R.string.format_added_by), modelSubmission.user.fullName)

            if (modelSubmission.dangerRank == -1)
                textViewDangerRank.text = String.format(getString(R.string.format_danger_rank), getString(R.string.unranked))
            else
                textViewDangerRank.text = String.format(getString(R.string.format_danger_rank), modelSubmission.dangerRank)

            // If submission is marked as high danger!
            if (modelSubmission.highDanger)
                textViewCategory.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.svg_warning, 0, 0,0)

            // If submission is verified by an admin!
            if (modelSubmission.accepted) {
                textViewVerification.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.svg_tick_rounded_icon, 0, 0, 0)
                textViewVerification.text = getString(R.string.verified)
            }
            // If submission is NOT verified by an admin!
            else {
                textViewVerification.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.svg_cross_rounded_icon, 0, 0, 0)
                textViewVerification.text = getString(R.string.not_verified)
            }

            // Move camera to first element and start from there.
            val marker = LatLng(modelSubmission.lat.toDouble(), modelSubmission.long.toDouble())
            mMap.moveCamera(CameraUpdateFactory.newLatLng(marker))
            mMap.addMarker(MarkerOptions().position(marker))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker,15f))
        }

        loadUserModel()
    }

    private fun unveilDetails() {
        binding.apply {
            vLayoutHead.unVeil()
            vLayoutBody.unVeil()
        }
    }

    private fun loadUserModel() {
        FirestoreHelper().getUserDetails(this@SubmissionDetailsActivity)
    }

    fun userModelSuccess(userModel: User) {
        modelCurrentUser = userModel

        hideProgressDialog()
        unveilDetails()

        if (modelCurrentUser.role == Constants.ROLE_ADMIN)
            binding.toolbar.imgBtnSave.visibility = View.VISIBLE
    }

    private fun verifySubmission() {
        showProgressDialog()

        FirestoreHelper().changeSubmissionStatus(this@SubmissionDetailsActivity, modelSubmission, true)
    }

    fun successUpdateSubmissionToFirestore() {
        hideProgressDialog()

        goToMainActivity(this@SubmissionDetailsActivity, Constants.SHOW_SUBMISSION_UPDATED_SNACK_BAR)
    }

    private fun setupUI() {
        // Veil the layout until all data is loaded
        binding.apply {
            vLayoutHead.veil()
            vLayoutBody.veil()
        }

        setupActionBar()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // mMap.setMyLocationEnabled(true);
        mMap.uiSettings.isZoomControlsEnabled = true

        loadSubmissionDetails()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar.root)

        val actionBar = supportActionBar
        binding.apply {
            toolbar.textViewActionBarLabel.text = modelSubmission.category

            toolbar.imgBtnSave.apply {
                setOnClickListener {
                    verifySubmission()
                }
                visibility = View.INVISIBLE
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
