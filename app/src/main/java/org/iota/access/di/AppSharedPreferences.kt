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
package org.iota.access.di

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import org.iota.access.SettingsFragment
import org.iota.access.api.model.CommandAction
import org.iota.access.models.User
import org.iota.access.utils.ResourceProvider
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Custom [SharedPreferences] class providing different methods for saving data
 */
@Singleton
class AppSharedPreferences @Inject internal constructor(
        private val sharedPreferences: SharedPreferences,
        private val resourceProvider: ResourceProvider
) {

    private fun putString(key: String?, value: String?) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getString(key: String?): String? {
        return sharedPreferences.getString(key, "")
    }

    fun getInt(key: String?): Int {
        return try {
            sharedPreferences.getInt(key, 0)
        } catch (ignored: Exception) {
            try {
                Integer.valueOf(sharedPreferences.getString(key, "0")!!)
            } catch (e: Exception) {
                0
            }
        }
    }

    fun putUser(user: User?) {
        if (user != null) {
            putString(SettingsFragment.Keys.PREF_KEY_USER, user.toJSONObject().toString())
        } else {
            putString(SettingsFragment.Keys.PREF_KEY_USER, null)
        }
    }

    val user: User?
        get() {
            val userJson = sharedPreferences
                    .getString(SettingsFragment.Keys.PREF_KEY_USER, null) ?: return null
            val json = JSONObject(userJson)
            return User.fromJSONObject(json)
        }

}
