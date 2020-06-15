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
package org.iota.access.ui.main.delegation.preview

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import org.iota.access.BaseFragment
import org.iota.access.R
import org.iota.access.databinding.FragmentDelegationPreviewBinding
import org.iota.access.utils.ui.UiUtils

class DelegationPreviewFragment : BaseFragment(R.layout.fragment_delegation_preview) {

    private lateinit var binding: FragmentDelegationPreviewBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.structured))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.json))
        binding.tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.pager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        binding.pager.addOnPageChangeListener(TabLayoutOnPageChangeListener(binding.tabLayout))

        val adapter = DelegationPreviewPagerAdapter(childFragmentManager)
        binding.pager.adapter = adapter
        binding.tabLayout.setTabTextColors(
                UiUtils.getColorFromAttr(binding.tabLayout.context, R.attr.nav_drawer_color_item_text_unchecked, Color.GRAY),
                UiUtils.getColorFromAttr(binding.tabLayout.context, R.attr.nav_drawer_color_item_text_unchecked, Color.GRAY)
        )
    }
}
