package com.simplifiedkiosk.ui.itemlist

import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.simplifiedkiosk.R
import com.simplifiedkiosk.databinding.ActivityItemListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ItemListActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityItemListBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityItemListBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val toolbar = viewBinding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.custom_toolbar_title)
        val toolbarLayoutParams = supportActionBar?.customView?.layoutParams as Toolbar.LayoutParams
        toolbarLayoutParams.gravity = Gravity.CENTER
        supportActionBar?.customView?.layoutParams = toolbarLayoutParams
        val toolBarTitle = supportActionBar?.customView?.findViewById<TextView>(R.id.toolbar_title)
        toolBarTitle?.text = "REEF KIOSK"

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set up ActionBar to work with navController
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    // Handle Up navigation in the action bar
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
