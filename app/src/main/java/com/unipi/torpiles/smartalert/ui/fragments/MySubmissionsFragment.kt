package com.unipi.torpiles.smartalert.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.unipi.torpiles.smartalert.adapters.MySubmissionsListAdapter
import com.unipi.torpiles.smartalert.database.FirestoreHelper
import com.unipi.torpiles.smartalert.databinding.FragmentMySubmissionsBinding
import com.unipi.torpiles.smartalert.models.Submission
import com.unipi.torpiles.smartalert.utils.Constants
import com.unipi.torpiles.smartalert.utils.IntentUtils

class MySubmissionsFragment : BaseFragment() {
    // Scoped to the lifecycle of the fragment's view (between onCreateView and onDestroyView)
    private var _binding: FragmentMySubmissionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMySubmissionsBinding.inflate(inflater, container, false)

        init()

        return binding.root
    }

    private fun init() {
        // Check if the user is logged in, otherwise show the sign in state.
        if (FirestoreHelper().getCurrentUserID() == "") {
            binding.apply {
                fabAddNewSubmission.visibility = View.GONE
                // We make the sign in layout visible and add the button click listeners accordingly.
                layoutMustBeSignedIn.apply {
                    root.visibility = View.VISIBLE
                    btnSignIn.setOnClickListener{ goToSignInActivity(this@MySubmissionsFragment.requireContext()) }
                    txtViewSignUp.setOnClickListener{ goToSignUpActivity(this@MySubmissionsFragment.requireContext()) }
                }
            }
        }
        else {
            binding.fabAddNewSubmission.setOnClickListener { IntentUtils().goToAddSubmissionActivity(this@MySubmissionsFragment.requireActivity()) }

            veilRecycler()
            loadSubmissions()
        }
        
    }

    private fun loadSubmissions() {
        FirestoreHelper().getUserSubmissionsList(this@MySubmissionsFragment)
    }

    /**
     * A function to get the successful submissions list of the user from cloud firestore.
     *
     * @param submissionsList List with submissions.
     */
    fun successUserSubmissionsListFromFireStore(submissionsList: ArrayList<Submission>) {

        if (submissionsList.size > 0) {
            binding.veilRecyclerView.visibility = View.VISIBLE
            binding.layoutEmptyState.root.visibility = View.GONE

            // sets VeilRecyclerView's properties
            binding.veilRecyclerView.run {
                setAdapter(
                    MySubmissionsListAdapter(
                        requireActivity(),
                        submissionsList
                    )
                )
                setLayoutManager(LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false))
                getRecyclerView().setHasFixedSize(true)
                addVeiledItems(Constants.DEFAULT_VEILED_ITEMS_VERTICAL)
                // delay-auto-unveil
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        unVeil()
                    },
                    1000
                )
            }
        }
        else {
            binding.apply {
                veilRecyclerView.unVeil()
                veilRecyclerView.visibility = View.GONE
                layoutEmptyState.root.visibility = View.VISIBLE
            }
        }
    }

    private fun veilRecycler() {
        binding.apply {
            veilRecyclerView.veil()

            veilRecyclerView.addVeiledItems(Constants.DEFAULT_VEILED_ITEMS_VERTICAL)
        }
    }

    override fun onResume() {
        super.onResume()

        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}
