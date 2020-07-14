/*
 *  This file is part of the IOTA Access distribution
 *  (https://github.com/iotaledger/access)
 *
 *  Copyright (c) 2020 IOTA Stiftung.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.iota.access

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.customview.widget.Openable
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.*
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.navigation.NavigationView
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import org.iota.access.di.AppSharedPreferences
import org.iota.access.ui.auth.login.LoginActivity
import org.iota.access.ui.dialogs.QuestionDialogFragment
import org.iota.access.user.UserManager
import org.iota.access.utils.ui.Theme
import org.iota.access.utils.ui.ThemeLab
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

class MainActivity : AppCompatActivity(),
        HasSupportFragmentInjector,
        QuestionDialogFragment.QuestionDialogListener {

    @Inject
    lateinit var supportFragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var themeLab: ThemeLab

    @Inject
    lateinit var preferences: AppSharedPreferences

    @Inject
    lateinit var userManager: UserManager

    private lateinit var appBarConfiguration: AppBarConfiguration

    private val toolbarTitle: TextView by lazy { findViewById<TextView>(R.id.toolbar_title) }
    private val drawerLayout: DrawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }
    private val navController: NavController by lazy { findNavController(R.id.nav_host_fragment) }

    private val topDestinations = setOf(
            R.id.commandListFragment,
            R.id.settingsFragment,
            R.id.logFragment,
            R.id.delegationFragment,
            R.id.walletFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)

        setContentView(R.layout.activity_main)

        setTheme(themeLab.getTheme(preferences.getInt(SettingsFragment.Keys.PREF_KEY_THEME)))

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        navController.setGraph(R.navigation.main_navigation)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(topDestinations, drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)

        val navView: NavigationView = findViewById(R.id.nav_view)
        setupNavigationView(navView)
    }

    private fun setupNavigationView(navView: NavigationView) {
        setupWithNavController(navView, navController) {
            when (it.itemId) {
                R.id.log_out_menu_item -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    onLogOutMenuItemSelected()
                    true
                }
                R.id.settingsFragment -> {
                    onSettingsMenuItemSelected()
                    true
                }
                else -> false
            }
        }

        // Set navigation header title to display user's full name
        navView.getHeaderView(0)?.let { headerView ->
            headerView.findViewById<TextView>(R.id.navHeaderTitle)?.let { navHeaderTitle ->
                navHeaderTitle.text = userManager.user?.fullName
            }
        }
    }

    private fun setTheme(theme: Theme) {
        setTheme(theme.themeId)

        // set title logo
        if (theme.logoId != null) {
            toolbarTitle.setCompoundDrawablesWithIntrinsicBounds(theme.logoId!!, 0, 0, 0)
        } else {
            toolbarTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = supportFragmentInjector

    override fun onBackPressed() {
        if (drawerLayout.isOpen) drawerLayout.closeDrawer(GravityCompat.START)
        else super.onBackPressed()
    }

    private fun onLogOutMenuItemSelected() {
        QuestionDialogFragment
                .newInstance(getString(R.string.msg_logout), LOGOUT_QUESTION_TAG)
                .show(supportFragmentManager, LOGOUT_QUESTION_TAG)
    }

    private fun onSettingsMenuItemSelected() {
        val navBuilder: NavOptions.Builder = NavOptions.Builder()
        navBuilder.setLaunchSingleTop(true)

        // Try to find start destination in navigation graph
        findStartDestination(navController.graph)?.let {
            navBuilder.setPopUpTo(it.id, false)
        }

        navController.navigate(R.id.settingsFragment, SettingsFragment.createArgs(true), navBuilder.build())
    }

    companion object {
        const val LOGOUT_QUESTION_TAG = "logOutQuestion"

        fun newIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    override fun onQuestionDialogAnswer(dialogTag: String, answer: QuestionDialogFragment.QuestionDialogAnswer) {
        if (dialogTag == LOGOUT_QUESTION_TAG && answer == QuestionDialogFragment.QuestionDialogAnswer.POSITIVE) {
            userManager.endSession()
            startActivity(Intent(this, LoginActivity::class.java))
            Timber.d("Successfully logged out")
            finish()
        }
    }
}

private fun setupWithNavController(navigationView: NavigationView, navController: NavController, navItemSelectedListener: (item: MenuItem) -> Boolean) {
    navigationView.setNavigationItemSelectedListener { item ->
        fun closeDrawer() {
            val parent = navigationView.parent
            if (parent is Openable) {
                (parent as Openable).close()
            }
        }

        var handled = navItemSelectedListener(item)
        if (handled) {
            closeDrawer()
        } else {
            handled = NavigationUI.onNavDestinationSelected(item, navController)
            if (handled)
                closeDrawer()
        }
        handled
    }
    val weakReference = WeakReference(navigationView)
    navController.addOnDestinationChangedListener(object : OnDestinationChangedListener {
        override fun onDestinationChanged(controller: NavController,
                                          destination: NavDestination,
                                          arguments: Bundle?) {
            val view = weakReference.get()
            if (view == null) {
                navController.removeOnDestinationChangedListener(this)
                return
            }
            val menu = view.menu
            var h = 0
            val size = menu.size()
            while (h < size) {
                val item = menu.getItem(h)
                item.isChecked = matchDestination(destination, item.itemId)
                h++
            }
        }
    })
}

private fun matchDestination(destination: NavDestination, @IdRes destId: Int): Boolean {
    var currentDestination: NavDestination? = destination
    while (currentDestination!!.id != destId && currentDestination.parent != null) {
        currentDestination = currentDestination.parent
    }
    return currentDestination.id == destId
}

fun findStartDestination(graph: NavGraph): NavDestination? {
    var startDestination: NavDestination? = graph
    while (startDestination is NavGraph) {
        val parent = startDestination
        startDestination = parent.findNode(parent.startDestination)
    }
    return startDestination
}
