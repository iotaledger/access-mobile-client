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

package org.iota.access.splash;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import org.iota.access.R;

import org.iota.access.login.LoginActivity;
import org.iota.access.navigation.NavigationDrawerActivity;
import org.iota.access.user.UserManager;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class SplashActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }

    @Inject
    UserManager mUserManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);
        setContentView(R.layout.activity_splash);

        // check if user has already logged in
        if (mUserManager.isUserLoggedIn()) {
            // user has logged in
            startActivity(NavigationDrawerActivity.newIntent(this));
        } else {
            // user has NOT logged in
            startActivity(LoginActivity.newIntent(this));
        }
        finish();
    }
}
