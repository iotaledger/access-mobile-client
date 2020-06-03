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

package org.iota.access.di.module;

import org.iota.access.SettingsFragment;
import org.iota.access.delegation.DelegationFragment;
import org.iota.access.delegation.DelegationListFragment;
import org.iota.access.delegation.preview.DelegationPreviewStructuredFragment;
import org.iota.access.delegation.rule.DelegationRuleFragment;
import org.iota.access.login.LoginFragment;
import org.iota.access.command_editor.CommandEditorFragment;
import org.iota.access.main.ui.CommandListFragment;
import org.iota.access.main.ui.VehicleInfoListFragment;
import org.iota.access.register.RegisterFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Module class for providing Fragments
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@Module
public abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract CommandListFragment contributeCommandListFragment();

    @ContributesAndroidInjector
    abstract CommandEditorFragment contributeCommandEditorFragment();

    @ContributesAndroidInjector
    abstract LoginFragment contributeLoginFragment();

    @ContributesAndroidInjector
    abstract VehicleInfoListFragment contributeVehicleInfoFragment();

    @ContributesAndroidInjector
    abstract SettingsFragment contributePreferenceFragment();

    @ContributesAndroidInjector
    abstract DelegationFragment contributeDelegationFragment();

    @ContributesAndroidInjector
    abstract DelegationListFragment contributeDelegationListFragment();

    @ContributesAndroidInjector
    abstract DelegationRuleFragment contributeDelegationRuleFragment();

    @ContributesAndroidInjector
    abstract DelegationPreviewStructuredFragment contributeDelegationPreviewFragment();

    @ContributesAndroidInjector
    abstract RegisterFragment contributeRegisterFragment();
}
