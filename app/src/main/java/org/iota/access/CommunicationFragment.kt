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

import android.os.Bundle
import android.util.Pair
import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.iota.access.di.Injectable
import timber.log.Timber
import javax.inject.Inject

abstract class CommunicationFragment<T : CommunicationViewModel> : BaseFragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    protected var disposable: CompositeDisposable? = null
    protected lateinit var viewModel: T

    private var isBinded = false

    protected abstract val viewModelClass: Class<T>

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory).get(viewModelClass)
        lifecycle.addObserver(viewModel)
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        if (!isBinded) {
            bindViewModel()
            isBinded = true
        }
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        unbindViewModel()
    }

    @CallSuper
    protected open fun bindViewModel() {
        disposable = CompositeDisposable()
        disposable?.apply {
            add(viewModel.observableShowLoadingMessage
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { pair: Pair<Boolean?, String?> -> showLoading(pair.first, pair.second) }) { t: Throwable? -> Timber.e(t) })
            add(viewModel
                    .observableShowDialogMessage
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ message: String -> showInfoDialog(message) }) { t: Throwable? -> Timber.e(t) })
            add(viewModel
                    .observableSnackbarMessage
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ message: String -> showInfoDialog(message) }) { t: Throwable? -> Timber.e(t) })
        }
    }

    @CallSuper
    protected fun unbindViewModel() = disposable?.dispose()

    protected fun showLoading(show: Boolean, message: String?) {
        if (show) {
            showProgress(message)
        } else {
            hideProgress()
        }
    }
}
