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
package org.iota.access.ui.auth.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import org.iota.access.CommunicationFragment
import org.iota.access.R
import org.iota.access.databinding.FragmentRegisterBinding
import org.iota.access.models.User
import org.iota.access.user.UserManager
import timber.log.Timber
import javax.inject.Inject

class RegisterFragment : CommunicationFragment<RegisterViewModel>(), View.OnClickListener {

    @Inject
    lateinit var userManager: UserManager

    private lateinit var binding: FragmentRegisterBinding

    override val viewModelClass: Class<RegisterViewModel>
        get() = RegisterViewModel::class.java

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel
        binding.buttonConnect.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v == binding.buttonConnect) {
            if (viewModel.stage == RegisterViewModel.Stage.FIRST) {
                viewModel.checkUsername()
            } else {
                viewModel.registerUser()
            }
        }
    }

    override fun bindViewModel() {
        super.bindViewModel()
        disposable?.add(viewModel.registerCompleted
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ user: User -> onRegisterComplete(user) }) { t: Throwable? -> Timber.e(t) })
    }

    private fun onRegisterComplete(user: User) {
        userManager.startSession(user)
        navController?.navigate(RegisterFragmentDirections.actionRegisterFragmentToActivityMain())
    }

    companion object {
        fun newInstance(): RegisterFragment {
            return RegisterFragment()
        }
    }
}
