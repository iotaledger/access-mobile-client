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
package org.iota.access.ui.auth.login

import android.os.CountDownTimer
import android.util.Pair
import androidx.databinding.Bindable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.iota.access.BR
import org.iota.access.CommunicationViewModel
import org.iota.access.R
import org.iota.access.api.Communicator
import org.iota.access.models.User
import org.iota.access.models.UserUtils
import org.iota.access.user.UserManager
import org.iota.access.utils.ResourceProvider
import javax.inject.Inject

class LoginViewModel @Inject constructor(
        communicator: Communicator?,
        private val userManager: UserManager,
        resourceProvider: ResourceProvider?
) : CommunicationViewModel(communicator, resourceProvider) {

    private val _loginCompleted = PublishSubject.create<User?>()

    val loginCompleted: Observable<User?>
        get() = _loginCompleted

    fun logIn() {
        val loginUsername = this.loginUsername

        if (loginUsername == null || loginUsername.isEmpty()) {
            mShowDialogMessage.onNext(mResourceProvider.getString(R.string.msg_empty_username))
            return
        }

        object : CountDownTimer(500, 500) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                mShowLoading.onNext(Pair(false, null))

                val defaultUser = UserUtils.getDefaultUser(loginUsername)
                if (defaultUser != null) {
                    userManager.startSession(defaultUser)
                    _loginCompleted.onNext(defaultUser)
                } else {
                    mShowDialogMessage.onNext(mResourceProvider.getString(R.string.msg_unable_to_log_in))
                }
            }
        }.start()
    }

    @get:Bindable
    var loginUsername: String? = null
        set(username) {
            field = username
            notifyPropertyChanged(BR.loginUsername)
        }

    @get:Bindable
    var loginPassword: String? = null
        set(password) {
            field = password
            notifyPropertyChanged(BR.loginPassword)
        }

}
