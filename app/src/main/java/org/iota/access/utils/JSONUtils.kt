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
package org.iota.access.utils

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException

object JSONUtils {
    @JvmStatic
    fun extractJsonElement(json: String): JsonElement? {
        val chars = json.toCharArray()
        var balancedString: String? = null
        if (chars.isEmpty()) return null
        if (chars[0] == '{') {
            balancedString = balanceOpeningsAndClosings(json.toCharArray(), '{', '}')
        } else if (chars[0] == '[') {
            balancedString = balanceOpeningsAndClosings(json.toCharArray(), '[', ']')
        }
        return if (balancedString == null) null else try {
            JsonParser.parseString(balancedString)
        } catch (ignored: JsonSyntaxException) {
            null
        }
    }

    private fun balanceOpeningsAndClosings(input: CharArray, openChar: Char, closeChar: Char): String? {
        var balance = 0
        var start = -1
        var end = -1
        var isBalanced = false
        for (i in input.indices) {
            if (input[i] == openChar) {
                if (start == -1) start = i
                balance++
            } else if (input[i] == closeChar) {
                end = i
                balance--
            }
            if (balance == 0 && start != -1 && end != -1 && start < end) {
                isBalanced = true
                break
            }
        }
        return if (isBalanced) {
            String(input, start, end - start + 1)
        } else {
            null
        }
    }
}
