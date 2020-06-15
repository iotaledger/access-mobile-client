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

package org.iota.access.ui.auth.register;

import androidx.databinding.Bindable;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import org.iota.access.CommunicationViewModel;
import org.iota.access.R;
import org.iota.access.api.Communicator;
import org.iota.access.api.model.CommunicationMessage;
import org.iota.access.api.model.TCPResponse;
import org.iota.access.models.GetUserIdResponse;
import org.iota.access.models.RegisterUserModel;
import org.iota.access.models.User;
import org.iota.access.utils.JsonUtils;
import org.iota.access.utils.ResourceProvider;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class RegisterViewModel extends CommunicationViewModel {

    private final Gson mGson;
    private final PublishSubject<Boolean> mStartOnboarding = PublishSubject.create();

    private final PublishSubject<User> mRegisterCompleted = PublishSubject.create();

    @Nullable
    private String mUsername;
    private String mFirstName;
    private String mLastName;
    private String mUserId;
    private Stage mStage = Stage.FIRST;

    @Inject
    public RegisterViewModel(Communicator communicator, ResourceProvider resourceProvider, Gson gson) {
        super(communicator, resourceProvider);
        mGson = gson;
    }

    public Observable<Boolean> getObservableStartOnboarding() {
        return mStartOnboarding;
    }

    public Observable<User> getRegisterCompleted() {
        return mRegisterCompleted;
    }

    @Bindable
    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
        notifyPropertyChanged(org.iota.access.BR.username);
    }

    @Bindable
    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        mFirstName = firstName;
        notifyPropertyChanged(org.iota.access.BR.firstName);
    }

    @Bindable
    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        mLastName = lastName;
        notifyPropertyChanged(org.iota.access.BR.lastName);
    }

    @Bindable
    public Stage getStage() {
        return mStage;
    }

    public void setStage(Stage stage) {
        mStage = stage;
        notifyPropertyChanged(org.iota.access.BR.stage);
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public void checkUsername() {
        if (TextUtils.isEmpty(mUsername))
            mShowDialogMessage.onNext(mResourceProvider.getString(R.string.msg_empty_username));
        else
            sendTCPMessage(CommunicationMessage.makeGetAuthenteqUserIdRequest(mUsername), mResourceProvider.getString(R.string.msg_registering));
    }

    public void registerUser() {
        String message = null;
        if (TextUtils.isEmpty(mUsername))
            message = mResourceProvider.getString(R.string.msg_empty_username);
        else if (TextUtils.isEmpty(mFirstName))
            message = mResourceProvider.getString(R.string.msg_empty_first_name);
        else if (TextUtils.isEmpty(mLastName))
            message = mResourceProvider.getString(R.string.msg_empty_last_name);

        if (message != null) {
            mShowDialogMessage.onNext(message);
            return;
        }

        RegisterUserModel registerUserModel = new RegisterUserModel(mFirstName, mLastName, mUsername, mUserId);
        sendTCPMessage(CommunicationMessage.makeRegisterRequest(registerUserModel, mGson), mResourceProvider.getString(R.string.msg_registering));
    }

    @Override
    protected void handleTCPResponse(String sentMessage, String response) {
        super.handleTCPResponse(sentMessage, response);

        JsonElement jsonElement = JsonUtils.extractJsonElement(response);
        if (jsonElement == null) return;

        String cmd = CommunicationMessage.getCmdFromMessage(sentMessage);
        if (cmd == null) return;

        switch (cmd) {
            case CommunicationMessage.REGISTER_USER: {
                try {
                    TCPResponse<User> tcpResponse = mGson.fromJson(jsonElement,
                            new TypeToken<TCPResponse<User>>() {
                            }.getType());
                    if (tcpResponse.isSuccessful()) {
                        User user = tcpResponse.getData();
                        if (user != null)
                            mRegisterCompleted.onNext(user);
                        else
                            mShowDialogMessage.onNext(mResourceProvider.getString(R.string.something_wrong_happened));
                    } else {
                        String message = tcpResponse.getMessage();
                        if (message == null)
                            message = mResourceProvider.getString(R.string.something_wrong_happened);
                        mShowDialogMessage.onNext(message);
                    }
                } catch (JsonSyntaxException ignored) {
                    mShowDialogMessage.onNext(mResourceProvider.getString(R.string.something_wrong_happened));
                }
            }
            break;
            case CommunicationMessage.GET_AUTH_USER_ID: {
                try {
                    TCPResponse<GetUserIdResponse> tcpResponse = mGson.fromJson(jsonElement,
                            new TypeToken<TCPResponse<GetUserIdResponse>>() {
                            }.getType());
                    if (tcpResponse.isSuccessful()) {
                        // username already exists
                        mShowDialogMessage.onNext(mResourceProvider.getString(R.string.msg_username_already_exists, mUsername));
                    } else {
                        // username does not exist
                        mStartOnboarding.onNext(true);
                    }
                } catch (JsonSyntaxException ignored) {
                }
            }
        }
    }

    public enum Stage {
        FIRST,
        SECOND
    }
}
