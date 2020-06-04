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

package org.iota.access.di;

import android.content.SharedPreferences;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import org.iota.access.api.model.Command;
import org.iota.access.main.model.VehicleInfo;
import org.iota.access.models.User;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.iota.access.SettingsFragment;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by flaviu.lupu on 14-Nov-17.
 * Custom {@link SharedPreferences} class providing different methods for saving data
 */
@Singleton
public class  AppSharedPreferences {

    private final SharedPreferences mSharedPreferences;

    private final Gson mGson;

    @Inject
    AppSharedPreferences(SharedPreferences sharedPreferences, Gson gson) {
        this.mSharedPreferences = sharedPreferences;
        mGson = gson;
    }

    public void putBoolean(String key, boolean value) {
        mSharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return mSharedPreferences.getBoolean(key, defaultValue);
    }

    public void putString(String key, String value) {
        mSharedPreferences.edit().putString(key, value).apply();
    }

    public String getString(String key) {
        return mSharedPreferences.getString(key, "");
    }

    public int getInt(String key) {
        try {
            return mSharedPreferences.getInt(key, 0);
        } catch (Exception ignored) {
            try {
                //noinspection ConstantConditions
                return Integer.valueOf(mSharedPreferences.getString(key, "0"));
            } catch (Exception e) {
                return 0;
            }
        }
    }

    public void putInt(String key, int value) {
        mSharedPreferences.edit().putInt(key, value).apply();
    }

    @Nullable
    public List<VehicleInfo> getVehicleIfnormations() {
        ArrayList<VehicleInfo> arrayList = new ArrayList<>();
        String vehicleInfosString = getString(SettingsFragment.Keys.PREF_KEY_VEHICLE_INFORMATION);
        if (vehicleInfosString.equalsIgnoreCase("")) { return null; }

        Type type = new TypeToken<List<VehicleInfo>>(){}.getType();
        try {
            return mGson.fromJson(vehicleInfosString, type);
        } catch (JsonSyntaxException ignored) {
            return null;
        }
    }

    public void putVehicleInfromations(List<VehicleInfo> vehicleInfos) {
        putString(SettingsFragment.Keys.PREF_KEY_VEHICLE_INFORMATION, mGson.toJson(vehicleInfos));
    }

    public void putUser(@Nullable User user) {
        putString(SettingsFragment.Keys.PREF_KEY_USER, mGson.toJson(user));
    }

    @Nullable
    public User getUser() {
        String userJson = mSharedPreferences.getString(SettingsFragment.Keys.PREF_KEY_USER, null);
        if (TextUtils.isEmpty(userJson))
            return null;
        try {
            return mGson.fromJson(userJson, User.class);
        } catch (JsonSyntaxException ignored) {
            return null;
        }
    }

    public void putCommandList(List<Command> commandList) {
        putString(SettingsFragment.Keys.PREF_KEY_CUSTOM_COMMANDS, mGson.toJson(commandList));
    }

    @Nullable
    public List<Command> getCommandList() {
        ArrayList<Command> arrayList = new ArrayList<>();
        String commandListString = getString(SettingsFragment.Keys.PREF_KEY_CUSTOM_COMMANDS);
        if (commandListString.equalsIgnoreCase("")) { return null; }

        Type type = new TypeToken<List<Command>>(){}.getType();
        try {
            return mGson.fromJson(commandListString, type);
        } catch (JsonSyntaxException ignored) {
            return null;
        }
    }

    public void removeCommandFromList(Command command) {
        // TODO: 30.1.2019. Fix removing command
        List<Command> oldList = getCommandList();
        if (oldList == null) return;
        for (int i = 0, n = oldList.size(); i < n; ++i) {
            if (oldList.get(i).equals(command)) {
                oldList.remove(i);
                break;
            }
        }
        putCommandList(oldList);
    }

}
