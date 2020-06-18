package org.iota.access.ui.auth.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.customview.widget.Openable
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import dagger.android.AndroidInjection
import org.iota.access.BaseActivity
import org.iota.access.R

class LoginActivity : BaseActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)

        navController = findNavController(R.id.nav_host_fragment)
        navController.setGraph(R.navigation.login_navigation)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        NavigationUI.setupActionBarWithNavController(this, navController)
        NavigationUI.setupWithNavController(toolbar, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(null as Openable?) || super.onSupportNavigateUp()
    }

    companion object {
        fun newIntent(context: Context?): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }
}
