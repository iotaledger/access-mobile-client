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

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.iota.access.R;
import org.iota.access.api.model.policy.Policy;
import org.iota.access.databinding.FragmentDelegationPreviewJsonBinding;

import org.iota.access.utils.ui.ViewLifecycleFragment;
import com.google.gson.GsonBuilder;

public class DelegationPreviewJsonFragment extends ViewLifecycleFragment {

    private static String POLICY = "com.iota.access.delegation.policy";

    private FragmentDelegationPreviewJsonBinding mBinding;

    private Policy mPolicy;

    public static DelegationPreviewJsonFragment newInstance(Policy policy) {
        Bundle args = new Bundle();
        args.putSerializable(POLICY, policy);
        DelegationPreviewJsonFragment fragment = new DelegationPreviewJsonFragment();
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
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_delegation_preview_json, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        extractArguments();
        String text = new GsonBuilder().setPrettyPrinting().create().toJson(mPolicy);
        mBinding.textView.setText(text);
    }
}
