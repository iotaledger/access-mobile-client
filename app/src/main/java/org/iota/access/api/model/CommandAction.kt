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

import androidx.annotation.DrawableRes
import org.iota.access.R
import org.iota.access.utils.ResourceProvider
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class CommandAction(
        val policyId: String,
        /**
         *  Action name as obtained form server. For user friendly action name,
         * use getActionNameResId() instead.
         */
        val action: String,
        @DrawableRes val imageResId: Int,
        val actionName: String,
        val headerName: String,
        var cost: Float? = null,
        val deletable: Boolean = false
) {

    var isPaid: Boolean
        get() = if (cost != null) {
            cost != 0f
        } else {
            true
        }
        set(paid) {
            if (paid) {
                cost = null
            }
        }

    fun toMap(): Map<String, Any?> = mapOf(
            POLICY_ID to policyId,
            ACTION to action,
            COST to cost
    )

    object KnownCommandAction {
        const val ACTION_1 = "action#1"
        const val ACTION_2 = "action#2"
        const val ACTION_3 = "action#3"
        const val ACTION_4 = "action#4"
    }

    companion object {
        private const val ACTION = "action"
        private const val POLICY_ID = "policy_id"
        private const val COST = "cost"

        @Suppress("unused")
        @JvmStatic
        fun parseFromJSONArray(jsonArray: JSONArray, resourceProvider: ResourceProvider): List<CommandAction> {
            val actions: MutableList<CommandAction> = mutableListOf()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.optJSONObject(i) ?: continue
                val action = fromJSON(jsonObject, resourceProvider) ?: continue
                actions.add(action)
            }
            return actions
        }

        @JvmStatic
        fun fromJSON(json: JSONObject, resourceProvider: ResourceProvider): CommandAction? {
            val action = json.optString(ACTION) ?: return null
            val policyId = json.optString(POLICY_ID) ?: return null

            @Suppress("UNNECESSARY_SAFE_CALL")
            val cost = json.optString(COST)?.toFloatOrNull()

            val imageResId = R.drawable.ic_key

            val actionNameResId: Int = when (action.toLowerCase(Locale.ROOT)) {
                KnownCommandAction.ACTION_1 -> R.string.action_1
                KnownCommandAction.ACTION_2 -> R.string.action_2
                KnownCommandAction.ACTION_3 -> R.string.action_3
                KnownCommandAction.ACTION_4 -> R.string.action_4
                else -> 0
            }
            val actionName = if (actionNameResId == 0) "Unknown" else resourceProvider.getString(actionNameResId)

            val headerNameResId: Int = when (action.toLowerCase(Locale.ROOT)) {
                KnownCommandAction.ACTION_1 -> R.string.action_1
                KnownCommandAction.ACTION_2 -> R.string.action_2
                KnownCommandAction.ACTION_3 -> R.string.action_3
                KnownCommandAction.ACTION_4 -> R.string.action_4
                else -> 0
            }
            val headerName = if (headerNameResId == 0) "Unknown" else resourceProvider.getString(headerNameResId)

            return CommandAction(
                    policyId = policyId,
                    action = action,
                    imageResId = imageResId,
                    actionName = actionName,
                    headerName = headerName,
                    cost = cost)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandAction

        if (policyId != other.policyId) return false
        if (action != other.action) return false
        if (cost != other.cost) return false

        return true
    }

    override fun hashCode(): Int {
        var result = policyId.hashCode()
        result = 31 * result + action.hashCode()
        result = 31 * result + (cost?.hashCode() ?: 0)
        return result
    }


}
