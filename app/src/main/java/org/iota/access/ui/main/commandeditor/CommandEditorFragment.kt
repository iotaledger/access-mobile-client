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
package org.iota.access.ui.main.commandeditor

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.iota.access.BaseFragment
import org.iota.access.R
import org.iota.access.databinding.FragmentCommandEditorBinding
import org.iota.access.di.Injectable
import timber.log.Timber
import javax.inject.Inject

/**
 * Fragment representing the Command editor screen
 */
class CommandEditorFragment : BaseFragment(R.layout.fragment_command_editor), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: CommandEditorViewModel
    private lateinit var binding: FragmentCommandEditorBinding

    private var disposable: CompositeDisposable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        binding.fab.setOnClickListener { viewModel.createNewCommand() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory).get(CommandEditorViewModel::class.java)
        binding.viewModel = viewModel
    }

    override fun onResume() {
        super.onResume()
        bindViewModel()
    }

    override fun onStop() {
        unbindViewModel()
        super.onStop()
    }

    private fun bindViewModel() {
        disposable = CompositeDisposable()
        disposable?.let {
            it.add(viewModel.showDialogMessage
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ message: String -> showInfoDialog(message) }) { t: Throwable? -> Timber.e(t) })
            it.add(viewModel.showLoading
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ flag: Boolean -> this.showLoading(flag) }) { t: Throwable? -> Timber.e(t) })
            it.add(viewModel.newCommand
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ onNewCommandCreate() }) { t: Throwable? -> Timber.e(t) })
            it.add(viewModel.snackbarTextMessage
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ message: String -> this.showSnackbar(message) }) { t -> Timber.e(t) })
        }
    }

    private fun unbindViewModel() = disposable?.dispose()

    private fun showLoading(flag: Boolean) =
            if (flag) showProgress(R.string.msg_creating_new_command) else hideProgress()

    private fun onNewCommandCreate() {
        navController.popBackStack()
    }
}
