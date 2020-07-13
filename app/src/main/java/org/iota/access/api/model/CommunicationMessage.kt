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
package org.iota.access.api.model

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.iota.access.models.RegisterUserModel
import org.json.JSONException
import org.json.JSONObject

@Suppress("MemberVisibilityCanBePrivate")
object CommunicationMessage {
    const val CMD_ELEMENT = "cmd"
    const val USER_ID_ELEMENT = "user_id"
    const val RESOLVE = "resolve"
    const val GET_POLICY_LIST = "get_policy_list"
    const val SET_DATA_SET = "set_data_set"
    const val POLICY_ID_ELEMENT = "policy_id"
    const val ENABLE_POLICY = "enable_policy"
    const val GET_DATA_SET = "get_data_set"
    const val USERNAME = "username"
    const val GET_AUTH_USER_ID = "get_auth_user_id"
    const val GET_USER = "get_user"
    const val REGISTER_USER = "register_user"
    const val USER = "user"
    const val GET_ALL_USERS = "get_all_users"
    const val CLEAR_ALL_USERS = "clear_all_users"

    @JvmStatic
    fun makePolicyListRequest(userId: String): String {
        val json = JSONObject()
        try {
            json.put(CMD_ELEMENT, GET_POLICY_LIST)
            json.put(USER_ID_ELEMENT, userId)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return json.toString()
    }

    @JvmStatic
    fun makeResolvePolicyRequest(policyId: String, userId: String): String {
        val json = JSONObject()
        try {
            json.put(CMD_ELEMENT, RESOLVE)
            json.put(USER_ID_ELEMENT, userId)
            json.put(POLICY_ID_ELEMENT, policyId)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return json.toString()
    }

    @JvmStatic
    fun makeEnablePolicyRequest(policyId: String, userId: String): String {
        val json = JSONObject()
        try {
            json.put(CMD_ELEMENT, ENABLE_POLICY)
            json.put(USER_ID_ELEMENT, userId)
            json.put(POLICY_ID_ELEMENT, policyId)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return json.toString()
    }

    @JvmStatic
    fun makeGetAuthenteqUserIdRequest(username: String): String {
        val json = JSONObject()
        try {
            json.put(CMD_ELEMENT, GET_AUTH_USER_ID)
            json.put(USERNAME, username)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return json.toString()
    }

    @JvmStatic
    fun makeRegisterRequest(registerUserModel: RegisterUserModel, gson: Gson): String {
        val json = JsonObject()
        val obj = gson.toJsonTree(registerUserModel)
        json.addProperty(CMD_ELEMENT, REGISTER_USER)
        json.add(USER, obj)
        return json.toString()
    }

    @JvmStatic
    fun makeClearAllUsersRequest(): String {
        val json = JsonObject()
        json.addProperty(CMD_ELEMENT, CLEAR_ALL_USERS)
        return json.toString()
    }

    @JvmStatic
    fun getCmdFromMessage(message: String): String? {
        try {
            val jsonObject = JSONObject(message)
            return jsonObject.optString(CMD_ELEMENT) ?: null
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }
}
