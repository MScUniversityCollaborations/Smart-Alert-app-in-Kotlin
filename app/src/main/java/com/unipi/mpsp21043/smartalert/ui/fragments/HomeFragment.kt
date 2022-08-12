package com.unipi.mpsp21043.smartalert.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.unipi.mpsp21043.smartalert.database.FirestoreHelper
import com.unipi.mpsp21043.smartalert.databinding.FragmentHomeBinding
import com.unipi.mpsp21043.smartalert.models.Submission

class HomeFragment : BaseFragment(), OnMapReadyCallback {

    // ~~~~~~~VARIABLES~~~~~~~
    private var _binding: FragmentHomeBinding? = null  // Scoped to the lifecycle of the fragment's view (between onCreateView and onDestroyView)
    private val binding get() = _binding!!
    private lateinit var mMap: GoogleMap
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // init()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
        supportMapFragment!!.getMapAsync(this)
    }

    private fun loadProblems() {
        FirestoreHelper().getProblemsList(this@HomeFragment)
    }

    fun successProblemsListFromFireStore(problemsList: ArrayList<Submission>) {
        if (problemsList.size > 0) {
            binding.apply {
                layoutEmptyState.root.visibility = View.GONE
            }

            mMap.clear()

            // Move camera to first element and start from there.
            val firstElement = LatLng(problemsList[0].lat.toDouble(), problemsList[0].long.toDouble())
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(firstElement))

            // val myMarker = LatLng(problemsList[0].lat.toDouble(), problemsList[0].long.toDouble())
            // mMap.addMarker(MarkerOptions().position(myMarker).title(problemsList[0].description))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstElement,15f))

            for (problem in problemsList) {
                val marker = LatLng(problem.lat.toDouble(), problem.long.toDouble())
                mMap.addMarker(MarkerOptions().position(marker).title(problem.description))
                // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker,15f))
            }
        }
        else {
            binding.apply {
                layoutEmptyState.root.visibility = View.VISIBLE
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // mMap.setMyLocationEnabled(true);
        mMap.uiSettings.isZoomControlsEnabled = true

        loadProblems()
    }

   /* override fun onResume() {
        super.onResume()

        init()
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
