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

package org.iota.access.login;

import androidx.databinding.Bindable;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.iota.access.BR;
import org.iota.access.CommunicationViewModel;
import org.iota.access.R;
import org.iota.access.api.Communicator;
import org.iota.access.api.model.CommunicationMessage;
import org.iota.access.api.model.TCPResponse;
import org.iota.access.api.tcp.TCPClient;
import org.iota.access.models.GetUserIdResponse;
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
    private final PublishSubject<String> mAuthenteqUserId = PublishSubject.create();

    private String mUsername;

    @Inject
    public LoginViewModel(Communicator communicator, Gson gson, ResourceProvider resourceProvider) {
        super(communicator, resourceProvider);
        mGson = gson;
    }


    public Observable<String> getObservableAuthenteqUserId() {
        return mAuthenteqUserId;
    }

    public Observable<User> getLoginCompleted() {
        return mLoginCompleted;
    }

    public void getAuthenteqUserId() {
        if (TextUtils.isEmpty(mUsername)) {
            mShowDialogMessage.onNext(mResourceProvider.getString(R.string.msg_empty_username));
            return;
        }

        User defaultUser = UserUtils.getDefaultUser(mUsername);
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
            sendTCPMessage(CommunicationMessage.makeGetAuthenteqUserIdRequest(mUsername), mResourceProvider.getString(R.string.msg_logging_in));
        }
    }

    public void getUser() {
        if (TextUtils.isEmpty(mUsername)) {
            mShowDialogMessage.onNext(mResourceProvider.getString(R.string.msg_empty_username));
            return;
        }
        sendTCPMessage(CommunicationMessage.makeGetUserRequest(mUsername), mResourceProvider.getString(R.string.msg_logging_in));
    }

    @Bindable
    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
        notifyPropertyChanged(BR.username);
    }

    @Override
    protected void handleTCPResponse(String sentMessage, String response) {
        super.handleTCPResponse(sentMessage, response);

        JsonElement jsonElement = JsonUtils.extractJsonElement(response);
        if (jsonElement == null) return;

        String cmd = CommunicationMessage.getCmdFromMessage(sentMessage);
        if (cmd == null) return;

        if (cmd.equals(CommunicationMessage.GET_AUTH_USER_ID)) {
            try {
                TCPResponse<GetUserIdResponse> tcpResponse = mGson.fromJson(jsonElement,
                        new TypeToken<TCPResponse<GetUserIdResponse>>() {
                        }.getType());
                if (tcpResponse.isSuccessful()) {
                    if (tcpResponse.getData() != null) {
                        mAuthenteqUserId.onNext(tcpResponse.getData().getUserId());
                    } else {
                        mShowDialogMessage.onNext(mResourceProvider.getString(R.string.something_wrong_happened));
                    }
                } else {
                    String message = tcpResponse.getMessage();
                    if (message == null)
                        message = mResourceProvider.getString(R.string.something_wrong_happened);
                    mShowDialogMessage.onNext(message);
                }
            } catch (JsonSyntaxException ignored) {
            }
        } else if (cmd.equals(CommunicationMessage.GET_USER)) {
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
