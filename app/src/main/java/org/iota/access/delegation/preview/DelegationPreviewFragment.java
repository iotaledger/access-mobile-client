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

package org.iota.access.delegation.preview;

import androidx.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.iota.access.R;
import org.iota.access.api.model.policy.Policy;
import org.iota.access.databinding.FragmentDelegationPreviewBinding;
import org.iota.access.utils.ui.UiUtils;
import org.iota.access.utils.ui.ViewLifecycleFragment;

public class DelegationPreviewFragment extends ViewLifecycleFragment {

    private static String POLICY = "com.iota.access.delegation.policy";

    private FragmentDelegationPreviewBinding mBinding;
    private Policy mPolicy;

    public static DelegationPreviewFragment newInstance(Policy policy) {
        DelegationPreviewFragment fragment = new DelegationPreviewFragment();
        Bundle args = new Bundle();
        args.putSerializable(POLICY, policy);
        fragment.setArguments(args);
        return fragment;
    }

    private void extractArguments() {
        Bundle args = getArguments();
        if (args == null) return;
        mPolicy = (Policy) args.getSerializable(POLICY);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_delegation_preview, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        extractArguments();

        mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText(R.string.structured));
        mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText(R.string.json));
        mBinding.tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        mBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mBinding.pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        mBinding.pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mBinding.tabLayout));

        DelegationPreviewPagerAdapter adapter = new DelegationPreviewPagerAdapter(getChildFragmentManager(), mPolicy);
        mBinding.pager.setAdapter(adapter);

        mBinding.tabLayout.setTabTextColors(
                UiUtils.getColorFromAttr(mBinding.tabLayout.getContext(), R.attr.nav_drawer_color_item_text_unchecked, Color.GRAY),
                UiUtils.getColorFromAttr(mBinding.tabLayout.getContext(), R.attr.nav_drawer_color_item_text_unchecked, Color.GRAY)
        );
    }
}
