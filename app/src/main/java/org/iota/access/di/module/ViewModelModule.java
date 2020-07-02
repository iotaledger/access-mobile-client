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

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.iota.access.CommunicationViewModel;
import org.iota.access.di.ViewModelKey;
import org.iota.access.ui.auth.login.LoginViewModel;
import org.iota.access.ui.main.commandlist.CommandListViewModel;
import org.iota.access.ui.main.delegation.DelegationRuleViewModel;
import org.iota.access.ui.main.delegation.DelegationSharedViewModel;
import org.iota.access.ui.main.delegation.DelegationViewModel;
import org.iota.access.viewmodel.CustomViewModelFactory;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

/**
 * Helper class for binding custom {@link ViewModelProvider.Factory} and ViewModels.
 */
@Module
public abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(CommandListViewModel.class)
    abstract ViewModel bindCommandListViewModel(CommandListViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel.class)
    abstract ViewModel bindLoginViewModel(LoginViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(DelegationViewModel.class)
    abstract ViewModel bindDelegationViewModel(DelegationViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(DelegationRuleViewModel.class)
    abstract ViewModel bindDelegationRuleViewModel(DelegationRuleViewModel viewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(CustomViewModelFactory factory);

    @Binds
    @IntoMap
    @ViewModelKey(CommunicationViewModel.class)
    abstract ViewModel bindCommunicationViewModel(CommunicationViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(DelegationSharedViewModel.class)
    abstract ViewModel bindDelegationSharedViewModel(DelegationSharedViewModel viewModel);
}
