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
package org.iota.access.models

import android.util.Base64
import org.json.JSONObject
import java.io.Serializable

@Suppress("MemberVisibilityCanBePrivate", "unused")
class User(
        val publicId: String,
        val username: String,
        val firstName: String,
        val lastName: String,
        val walletId: String,
        /** Private key used for signing policies. */
        val signingKey: String
) : Serializable {

    val fullName: String
        get() = "$firstName $lastName"

    fun toMap(): Map<String, Any> = mapOf(
            KEY_PUBLIC_ID to publicId,
            KEY_USERNAME to username,
            KEY_FIRST_NAME to firstName,
            KEY_LAST_NAME to lastName,
            KEY_WALLET_ID to walletId,
            KEY_SIGNING_KEY to signingKey
    )

    fun toJSONObject(): JSONObject = JSONObject().apply {
        put(KEY_PUBLIC_ID, publicId)
        put(KEY_USERNAME, username)
        put(KEY_FIRST_NAME, firstName)
        put(KEY_LAST_NAME, lastName)
        put(KEY_WALLET_ID, walletId)
        put(KEY_SIGNING_KEY, signingKey)
    }

    val privateKey: ByteArray = Base64.decode(signingKey, Base64.NO_WRAP)

    companion object {
        private const val KEY_ID = "id"
        private const val KEY_PUBLIC_ID = "publicId"
        private const val KEY_USERNAME = "username"
        private const val KEY_FIRST_NAME = "firstName"
        private const val KEY_LAST_NAME = "lastName"
        private const val KEY_WALLET_ID = "walletId"
        private const val KEY_SIGNING_KEY = "signingKey"

        @JvmStatic
        fun fromMap(map: Map<String, Any>): User? {
            val publicId = map[KEY_PUBLIC_ID]?.toString() ?: return null
            val username = map[KEY_USERNAME]?.toString() ?: return null
            val firstName = map[KEY_FIRST_NAME]?.toString() ?: return null
            val lastName = map[KEY_LAST_NAME]?.toString() ?: return null
            val walletId = map[KEY_WALLET_ID]?.toString() ?: return null
            val signingKey = map[KEY_SIGNING_KEY]?.toString() ?: return null

            return User(
                    publicId = publicId,
                    username = username,
                    firstName = firstName,
                    lastName = lastName,
                    walletId = walletId,
                    signingKey = signingKey
            )
        }

        @JvmStatic
        fun fromJSONObject(json: JSONObject): User? {
            val publicId = json.optString(KEY_PUBLIC_ID) ?: return null
            val username = json.optString(KEY_USERNAME) ?: return null
            val firstName = json.optString(KEY_FIRST_NAME) ?: return null
            val lastName = json.optString(KEY_LAST_NAME) ?: return null
            val walletId = json.optString(KEY_WALLET_ID) ?: return null
            val signingKey = json.optString(KEY_SIGNING_KEY) ?: return null

            return User(
                    publicId = publicId,
                    username = username,
                    firstName = firstName,
                    lastName = lastName,
                    walletId = walletId,
                    signingKey = signingKey
            )
        }
    }
}
