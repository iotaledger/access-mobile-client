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
package org.iota.access.ui.main.delegation.preview

import org.iota.access.utils.ui.recursiverecyclerview.RecursiveItem
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class JsonElement(
        val key: String,
        val value: String?,
        val json: JSONObject?,
        private val jsonArray: JSONArray?
) : RecursiveItem {

    //    public
    override fun getChildren(): List<RecursiveItem> {
        var list: List<RecursiveItem> = ArrayList()
        if (jsonArray != null) {
            list = listOf(*makeList(jsonArray).toTypedArray())
        }
        if (value != null) return list
        if (json == null) return list
        list = listOf(*makeList(json).toTypedArray())
        return list
    }

    companion object {
        fun makeList(json: JSONObject): List<JsonElement> {
            val keys = json.keys()
            val items: MutableList<JsonElement> = ArrayList()
            while (keys.hasNext()) {
                val key = keys.next()
                try {
                    when (val obj = json[key]) {
                        is String -> {
                            items.add(JsonElement(key, obj, null, null))
                        }
                        is Int -> {
                            items.add(JsonElement(key, obj.toString(), null, null))
                        }
                        is JSONObject -> {
                            items.add(JsonElement(key, null, obj, null))
                        }
                        is JSONArray -> {
                            items.add(JsonElement(key, null, null, obj))
                        }
                    }
                } catch (ignored: JSONException) {
                }
            }
            return items
        }

        fun makeList(json: JSONArray): List<JsonElement> {
            val items: MutableList<JsonElement> = ArrayList()
            var i = 0
            val n = json.length()
            while (i < n) {
                try {
                    val `object` = json.getJSONObject(i)
                    items.addAll(makeList(`object`))
                } catch (ignored: JSONException) {
                    try {
                        val `object` = json.getJSONArray(i)
                        items.addAll(makeList(`object`))
                    } catch (e1: JSONException) {
                        e1.printStackTrace()
                    }
                }
                ++i
            }
            return items
        }
    }

}
