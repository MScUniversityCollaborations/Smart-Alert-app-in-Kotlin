package com.unipi.torpiles.smartalert.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.unipi.torpiles.smartalert.R
import com.unipi.torpiles.smartalert.adapters.ViewPagerMainAdapter
import com.unipi.torpiles.smartalert.database.FirestoreHelper
import com.unipi.torpiles.smartalert.databinding.ActivityMainBinding
import com.unipi.torpiles.smartalert.models.User
import com.unipi.torpiles.smartalert.utils.Constants
import com.unipi.torpiles.smartalert.utils.SnackBarSuccessClass


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setupUI()
    }

    private fun init() {
        if (intent.hasExtra(Constants.EXTRA_SHOW_SUBMISSION_CREATED_SNACKBAR)
            && intent.getBooleanExtra(Constants.EXTRA_SHOW_SUBMISSION_CREATED_SNACKBAR, false)) {
            SnackBarSuccessClass
                .make(binding.root, getString(R.string.submission_created_successfully))
                .show()
        }
        else if (intent.hasExtra(Constants.EXTRA_SUBMISSION_UPDATED_SNACKBAR)
            && intent.getBooleanExtra(Constants.EXTRA_SUBMISSION_UPDATED_SNACKBAR, false)) {
            SnackBarSuccessClass
                .make(binding.root, getString(R.string.submission_updated_successfully))
                .show()
        }
    }

    private fun setupUI() {
        setUpTabs()
        setupActionBar()
        setupNavDrawer()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar.root)
        binding.toolbar.textViewActionBarLabel.text = getString(R.string.txt_global_problems)
        val actionBar = supportActionBar
        actionBar?.let {
            it.setDisplayShowCustomEnabled(true)
            it.setCustomView(R.layout.toolbar_title_only)
        }
    }

    private fun setupNavDrawer() {
        binding.apply {
            val toggle = ActionBarDrawerToggle(
                this@MainActivity, drawerLayout, toolbar.root,
                R.string.nav_drawer_close, R.string.nav_drawer_close
            )
            drawerLayout.addDrawerListener(toggle)
            // Change drawer arrow icon
            toggle.drawerArrowDrawable.color =
                ContextCompat.getColor(this@MainActivity, R.color.colorTabSelected)
            // Set navigation arrow icon
            toggle.setHomeAsUpIndicator(R.drawable.ic_list)
            toggle.syncState()

            if (FirestoreHelper().getCurrentUserID().isEmpty()) {
                navView.inflateHeaderView(R.layout.nav_drawer_header_not_signed_in)
                navView.getHeaderView(0)
                    .findViewById<MaterialButton>(R.id.btn_NavView_Sign_In)
                    .setOnClickListener{ goToSignInActivity(this@MainActivity) }
            }
            else {
                navView.inflateHeaderView(R.layout.nav_drawer_header_signed_in)
                FirestoreHelper().getUserDetails(this@MainActivity)
            }
            navView.setNavigationItemSelectedListener(this@MainActivity)
            navView.setCheckedItem(R.id.nav_drawer_item_home)
        }
    }

    private fun setUpTabs() {
        val adapter = ViewPagerMainAdapter(supportFragmentManager, lifecycle)

        binding.apply {
            viewPagerHomeActivity.adapter = adapter

            TabLayoutMediator(tabs, viewPagerHomeActivity) { tab, position ->
                when (position) {
                    0 -> tab.setIcon(R.drawable.svg_location_pin)
                    1 -> tab.setIcon(R.drawable.ic_global)
                    2 -> tab.setIcon(R.drawable.ic_file_form)
                    3 -> tab.setIcon(R.drawable.ic_user_circle)
                }
            }.attach()

            tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    when (tab.position) {
                        0 -> {
                            toolbar.textViewActionBarLabel.text = getString(R.string.area_problems)
                            navView.setCheckedItem(R.id.nav_drawer_item_home)
                        }
                        1 -> {
                            toolbar.textViewActionBarLabel.text = getString(R.string.txt_global_problems)
                            navView.setCheckedItem(R.id.nav_drawer_item_all_submissions)
                        }
                        2 -> {
                            toolbar.textViewActionBarLabel.text = getString(R.string.txt_my_submissions)
                            navView.setCheckedItem(R.id.nav_drawer_item_my_submissions)
                        }
                        3 -> {
                            toolbar.textViewActionBarLabel.text = getString(R.string.txt_my_account)
                            navView.setCheckedItem(R.id.nav_drawer_item_profile)
                        }
                    }
                }
                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.

        when (item.itemId) {
            R.id.nav_drawer_item_home -> binding.tabs.getTabAt(0)?.select()
            R.id.nav_drawer_item_all_submissions -> binding.tabs.getTabAt(1)?.select()
            R.id.nav_drawer_item_my_submissions -> binding.tabs.getTabAt(2)?.select()
            R.id.nav_drawer_item_profile -> binding.tabs.getTabAt(3)?.select()
            R.id.nav_drawer_item_exit -> ActivityCompat.finishAffinity(this)
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_Layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    fun userDetailsSuccess(userInfo: User) {
        binding.apply {
            navView.getHeaderView(0).apply {
                findViewById<TextView>(R.id.navDrawer_SignedIn_Full_Name)
                    .text = userInfo.fullName
                findViewById<TextView>(R.id.navDrawer_SignedIn_Email)
                    .text = userInfo.email
            }

            // Check if user is ADMIN and then show the admin features in nav drawer bar.
            if (userInfo.role == Constants.ROLE_ADMIN) {
                // And also add the admin icon next to the name in nav drawer header.
                navView.getHeaderView(0).apply {
                    findViewById<TextView>(R.id.navDrawer_SignedIn_Full_Name)
                        .setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.svg_admin_icon, 0)
                }
            }
        }
    }

    override fun onBackPressed() {
        doubleBackToExit()
    }
}
