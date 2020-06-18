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

package org.iota.access.ui.auth.login;

import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Pair;

import androidx.databinding.Bindable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.iota.access.CommunicationViewModel;
import org.iota.access.R;
import org.iota.access.api.Communicator;
import org.iota.access.api.model.CommunicationMessage;
import org.iota.access.api.model.TCPResponse;
import org.iota.access.api.tcp.TCPClient;
import org.iota.access.models.User;
import org.iota.access.models.UserUtils;
import org.iota.access.utils.JsonUtils;
import org.iota.access.utils.ResourceProvider;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;


public class LoginViewModel extends CommunicationViewModel {

    private final Gson mGson;
    private final PublishSubject<User> mLoginCompleted = PublishSubject.create();

    private String mLoginUsername;
    private String mLoginPassword;

    @Inject
    public LoginViewModel(Communicator communicator, Gson gson, ResourceProvider resourceProvider) {
        super(communicator, resourceProvider);
        mGson = gson;
    }


    public Observable<User> getLoginCompleted() {
        return mLoginCompleted;
    }

    public void logIn() {
        if (TextUtils.isEmpty(mLoginUsername)) {
            mShowDialogMessage.onNext(mResourceProvider.getString(R.string.msg_empty_username));
            return;
        }

        User defaultUser = UserUtils.getDefaultUser(mLoginUsername);
        if (defaultUser != null) {
            new CountDownTimer(500, 500) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    mShowLoading.onNext(new Pair<>(false, null));
                    mLoginCompleted.onNext(defaultUser);
                }
            }.start();
        } else {
            mShowDialogMessage.onNext(mResourceProvider.getString(R.string.msg_unable_to_log_in));
        }
    }

    @Bindable
    public String getLoginUsername() {
        return mLoginUsername;
    }

    public void setLoginUsername(String username) {
        mLoginUsername = username;
        notifyPropertyChanged(org.iota.access.BR.loginUsername);
    }

    @Bindable
    public String getLoginPassword() {
        return mLoginPassword;
    }

    public void setLoginPassword(String password) {
        mLoginPassword = password;
        notifyPropertyChanged(org.iota.access.BR.loginPassword);
    }

    @Override
    protected void handleTCPResponse(String sentMessage, String response) {
        super.handleTCPResponse(sentMessage, response);

        JsonElement jsonElement = JsonUtils.extractJsonElement(response);
        if (jsonElement == null) return;

        String cmd = CommunicationMessage.getCmdFromMessage(sentMessage);
        if (cmd == null) return;

        if (cmd.equals(CommunicationMessage.GET_USER)) {
            try {
                TCPResponse<User> tcpResponse = mGson.fromJson(jsonElement,
                        new TypeToken<TCPResponse<User>>() {
                        }.getType());
                if (tcpResponse.getData() == null)
                    handleTCPError(TCPClient.TCPError.UNKNOWN);
                else
                    mLoginCompleted.onNext(tcpResponse.getData());
            } catch (JsonSyntaxException ignored) {
            }
        }
    }

}
