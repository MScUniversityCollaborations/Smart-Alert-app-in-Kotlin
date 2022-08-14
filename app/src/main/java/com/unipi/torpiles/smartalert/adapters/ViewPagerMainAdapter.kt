package com.unipi.torpiles.smartalert.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.unipi.torpiles.smartalert.ui.fragments.HomeFragment
import com.unipi.torpiles.smartalert.ui.fragments.MyAccountFragment
import com.unipi.torpiles.smartalert.ui.fragments.SubmissionsFragment

class ViewPagerMainAdapter(fm: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fm, lifecycle) {

    private val fragmentsList:ArrayList<Fragment> = arrayListOf(
        HomeFragment(),
        SubmissionsFragment(),
        MyAccountFragment()
    )

    override fun getItemCount(): Int {
        return fragmentsList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentsList[position]
    }
}