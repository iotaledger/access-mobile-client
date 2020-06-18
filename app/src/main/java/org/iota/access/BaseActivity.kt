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

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import org.iota.access.databinding.ActivityBaseBinding
import org.iota.access.di.AppSharedPreferences
import org.iota.access.utils.ui.Theme
import org.iota.access.utils.ui.ThemeLab
import org.iota.access.utils.ui.UiUtils
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var preferences: AppSharedPreferences

    lateinit var binding: ActivityBaseBinding

    protected lateinit var toolbarTitle: TextView

    protected lateinit var toolbar: Toolbar

    @Inject
    lateinit var themeLab: ThemeLab

    val contentView: View
        get() {
            val decorView: View = this.window.decorView
            return decorView.findViewById<View>(android.R.id.content) as ViewGroup
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_base)
        setTheme(themeLab.getTheme(preferences.getInt(SettingsFragment.Keys.PREF_KEY_THEME)))


        toolbarTitle = binding.toolbarTitle
        toolbar = binding.toolbar

        // set theme from shared preferences
        setToolbar()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        var i = 0
        val n = menu.size()
        while (i < n) {
            val item = menu.getItem(i)
            var icon = item.icon
            if (icon == null) {
                i++
                continue
            }
            icon = DrawableCompat.wrap(icon)
            DrawableCompat.setTint(icon, UiUtils.getColorFromAttr(this@BaseActivity, R.attr.action_menu_icon_color, Color.GRAY))
            i++
        }
        return super.onCreateOptionsMenu(menu)
    }

//    override fun getTheme(): Resources.Theme {
//        val theme = super.getTheme()
//        theme.applyStyle(mThemeLab.getTheme(mPreferences.getInt(SettingsFragment.Keys.PREF_KEY_THEME)).themeId, true)
//        return theme
//    }

    protected open fun setTheme(theme: Theme) {
        setTheme(theme.themeId)

        // set title logo
        if (theme.logoId != null) {
            binding.toolbarTitle.setCompoundDrawablesWithIntrinsicBounds(theme.logoId!!, 0, 0, 0)
        } else {
            binding.toolbarTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }

        // set title
        if (theme.titleId != null) {
            binding.toolbarTitle.setText(theme.titleId!!)
        } else {
            binding.toolbarTitle.text = null
        }
        binding.toolbarTitle.setPadding(0, 0, 0, 0)

    }


    protected val currentFragment: Fragment?
        get() {
            val fragmentManager = supportFragmentManager
            return if (fragmentManager.backStackEntryCount > 0) {
                val fragmentTag = fragmentManager.getBackStackEntryAt(fragmentManager.backStackEntryCount - 1).name
                fragmentManager.findFragmentByTag(fragmentTag)
            } else {
                null
            }
        }

    protected open fun setToolbar() {
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
    }

    fun addFragmentToBackStack(fragment: Fragment, tag: String) {
        supportFragmentManager
                .beginTransaction()
                .addToBackStack(tag)
                .setCustomAnimations(android.R.animator.fade_in, 0)
                .replace(R.id.nav_host_fragment, fragment, tag)
                .commitAllowingStateLoss()
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return androidInjector
    }
}
