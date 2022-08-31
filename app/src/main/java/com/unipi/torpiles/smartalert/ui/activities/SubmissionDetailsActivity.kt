package com.unipi.torpiles.smartalert.ui.activities

import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.unipi.torpiles.smartalert.R
import com.unipi.torpiles.smartalert.databinding.ActivitySubmissionDetailsBinding
import com.unipi.torpiles.smartalert.models.Submission
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
            textViewCategory.text = modelSubmission.category
            textViewDateAdded.text = modelSubmission.dateAdded.toString()
            textViewDescription.text = modelSubmission.description

            if (modelSubmission.isHighDanger)
                textViewCategory.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.svg_warning, 0, 0,0)

            // Move camera to first element and start from there.
            val marker = LatLng(modelSubmission.lat.toDouble(), modelSubmission.long.toDouble())
            mMap.moveCamera(CameraUpdateFactory.newLatLng(marker))
            mMap.addMarker(MarkerOptions().position(marker))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker,15f))
        }

        hideProgressDialog()
        unveilDetails()
    }

    private fun unveilDetails() {
        binding.apply {
            vLayoutHead.unVeil()
            vLayoutBody.unVeil()
        }
    }

    private fun setupUI() {
        // Veil the layout until all data is loaded
        binding.apply {
            vLayoutHead.veil()
            vLayoutBody.veil()
        }

        setupActionBar()
        setupClickListeners()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // mMap.setMyLocationEnabled(true);
        mMap.uiSettings.isZoomControlsEnabled = true

        loadSubmissionDetails()
    }

    private fun setupClickListeners() {
        // toolbar.actionBarImgBtnMyCart.setOnClickListener { IntentUtils().goToListCartItemsActivity(this@SubmissionDetailsActivity) }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar.root)

        val actionBar = supportActionBar
        actionBar?.let {
            it.setDisplayShowCustomEnabled(true)
            it.setCustomView(R.layout.toolbar_title_only)
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_chevron_left_24dp)
        }
    }

}
