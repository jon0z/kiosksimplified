package com.simplifiedkiosk.ui.itemlist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
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

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    // Handle Up navigation in the action bar
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
