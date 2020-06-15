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
@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package org.iota.access.models

import org.json.JSONArray
import org.json.JSONObject

sealed class PolicyAttribute : Mappable {
    interface Builder {
        fun build(): PolicyAttribute
    }
}

sealed class PolicyAttributeList : PolicyAttribute() {
    interface Builder {
        fun build(): PolicyAttributeList
    }
}

sealed class PolicyObligation : PolicyAttribute()

object PolicyAttributeEmpty : PolicyAttribute() {
    override fun toMap(): Map<String, Any> = mapOf()
}

object PolicyAttributeListEmpty : PolicyAttributeList() {
    override fun toMap(): Map<String, Any> = mapOf()
}

object PolicyObligationEmpty : PolicyObligation() {
    override fun toMap(): Map<String, Any> = mapOf()
}

data class PolicyAttributeSingle(
        val type: String,
        val value: String
) : PolicyAttribute() {
    interface Builder {
        fun build(): PolicyAttributeSingle
    }

    override fun toMap(): Map<String, Any> = mapOf(
            PolicyAttrKeys.TYPE to type,
            PolicyAttrKeys.VALUE to value)

    companion object {
        fun fromJSON(json: JSONObject): PolicyAttributeSingle? {
            val type = json.optString(PolicyAttrKeys.TYPE) ?: return null
            val value = json.optString(PolicyAttrKeys.VALUE) ?: return null
            return PolicyAttributeSingle(type, value)
        }
    }
}

class PolicyAttributeLogical(
        val attributeList: List<PolicyAttributeList>,
        val operation: LogicalOperator
) : PolicyAttributeList() {

    override fun toMap(): Map<String, Any> = mapOf(
            PolicyAttrKeys.ATTRIBUTE_LIST to attributeList.map { it.toMap() },
            PolicyAttrKeys.OPERATION to operation.toString())

    enum class LogicalOperator {
        AND,
        OR;

        override fun toString(): String = when (this) {
            AND -> "and"
            OR -> "or"
        }

        companion object {
            fun parseFrom(value: String): LogicalOperator? = values().find { it.toString() == value }
        }
    }

    companion object {
        fun fromJSON(json: JSONObject): PolicyAttributeLogical? {

            // Operation must be present
            val operationStr = json.optString(PolicyAttrKeys.OPERATION) ?: return null

            // Check if operation is in one of available logical operations
            val operation = LogicalOperator.parseFrom(operationStr) ?: return null

            // List of attributes must be present
            val attrList = json.optJSONArray(PolicyAttrKeys.ATTRIBUTE_LIST) ?: return null

            // There must be at least 2 attribute items in list
            if (attrList.length() < 2) return null

            val attributeList = mutableListOf<PolicyAttributeList>()

            for (index in 0 until attrList.length()) {
                val item = attrList.optJSONObject(index) ?: continue
                val attribute = (parsePolicyAttr(item) as? PolicyAttributeList) ?: continue
                attributeList.add(attribute)
            }

            return PolicyAttributeLogical(attributeList, operation)
        }
    }
}

data class PolicyAttributeComparable(
        val first: PolicyAttribute,
        val second: PolicyAttribute,
        val operation: Operation
) : PolicyAttributeList() {

    override fun toMap(): Map<String, Any> = mapOf(
            PolicyAttrKeys.ATTRIBUTE_LIST to attributeList.map { it.toMap() },
            PolicyAttrKeys.OPERATION to operation.toString())

    private val attributeList = listOf(first, second)

    enum class Operation {
        EQUAL,
        LESS_OR_EQUAL,
        GREATER_OR_EQUAL,
        LESS_THAN,
        GREATER_THAN;

        override fun toString(): String = when (this) {
            EQUAL -> "eq"
            LESS_OR_EQUAL -> "leq"
            GREATER_OR_EQUAL -> "geq"
            LESS_THAN -> "lt"
            GREATER_THAN -> "gt"
        }

        companion object {
            fun parseFrom(value: String): Operation? = values().find { it.toString() == value }
        }
    }

    companion object {
        fun fromJSON(json: JSONObject): PolicyAttributeComparable? {
            // Operation must be present
            val operationStr = json.optString(PolicyAttrKeys.OPERATION) ?: return null

            // Check if operation is in one of available comparator operations
            val operation = Operation.parseFrom(operationStr) ?: return null

            // List of attributes must be present
            val attrList = json.optJSONArray(PolicyAttrKeys.ATTRIBUTE_LIST) ?: return null

            // List of attributes must contain exactly 2 items
            if (attrList.length() != 2) return null

            // Both first and second items must be valid JSON objects
            val firstObject = attrList.optJSONObject(0) ?: return null
            val secondObject = attrList.optJSONObject(1) ?: return null

            // Both parse objects must be of type PolicyAttr
            val first = parsePolicyAttr(firstObject) ?: return null
            val second = parsePolicyAttr(secondObject) ?: return null

            return PolicyAttributeComparable(first, second, operation)
        }
    }
}

class PolicyObligationList(val obligations: List<PolicyAttributeSingle>) : PolicyObligation() {

    override fun toMap(): Map<String, Any> =
            mapOf(PolicyAttrKeys.OBLIGATIONS to obligations.map { it.toMap() })

    companion object {
        fun emptyList(): PolicyObligationList = PolicyObligationList(listOf())

        fun fromJSON(json: JSONObject): PolicyObligationList? {
            val jsonArray: JSONArray = json.optJSONArray(PolicyAttrKeys.OBLIGATIONS) ?: return null

            val obligations = mutableListOf<PolicyAttributeSingle>()

            for (index in 0 until jsonArray.length()) {
                val item = jsonArray.optJSONObject(index) ?: continue
                val obligation = PolicyAttributeSingle.fromJSON(item) ?: continue
                obligations.add(obligation)
            }

            return PolicyObligationList(obligations)
        }
    }
}

data class PolicyAttributeCondition(
        val condition: PolicyAttributeList,
        val ifTrue: PolicyObligationList,
        val ifFalse: PolicyObligationList = PolicyObligationList.emptyList()
) : PolicyAttributeList() {

    override fun toMap(): Map<String, Any> = mapOf(
            PolicyAttrKeys.OPERATION to operation,
            PolicyAttrKeys.ATTRIBUTE_LIST to attributeList.map { it.toMap() })

    val operation: String
        get() = "if"

    private val attributeList: List<PolicyAttribute>
        get() = listOf(condition, ifTrue, ifFalse)

    companion object {
        fun fromJSON(json: JSONObject): PolicyAttributeCondition? {

            // Operation must be present
            val operation = json.optString(PolicyAttrKeys.OPERATION) ?: return null

            // Operation must be "if"
            if (operation != "if") return null

            // List of attributes must be present
            val attrList = json.optJSONArray(PolicyAttrKeys.ATTRIBUTE_LIST) ?: return null

            // Array must be of length 2 or 3 which logically means it contains
            // condition and true objects at least (false block may me omitted)
            if (attrList.length() !in 2..3) return null

            val conditionObject = attrList.optJSONObject(0) ?: return null
            val condition = parsePolicyAttr(conditionObject) as? PolicyAttributeList ?: return null
            val ifTrueObject = attrList.optJSONObject(1) ?: return null
            val ifTrue = PolicyObligationList.fromJSON(ifTrueObject) ?: return null

            var ifFalse: PolicyObligationList? = null
            if (attrList.length() == 3) {
                val ifFalseObject = attrList.optJSONObject(2) ?: return null
                ifFalse = PolicyObligationList.fromJSON(ifFalseObject) ?: return null
            }
            return PolicyAttributeCondition(condition, ifTrue, ifFalse
                    ?: PolicyObligationList.emptyList())
        }
    }
}

fun parsePolicyAttr(json: JSONObject?): PolicyAttribute? {
    if (json == null) return null

    PolicyAttributeCondition.fromJSON(json)?.let {
        return@parsePolicyAttr it
    }

    PolicyAttributeLogical.fromJSON(json)?.let {
        return@parsePolicyAttr it
    }

    PolicyAttributeComparable.fromJSON(json)?.let {
        return@parsePolicyAttr it
    }

    PolicyObligationList.fromJSON(json)?.let {
        return@parsePolicyAttr it
    }

    PolicyAttributeSingle.fromJSON(json)?.let {
        return@parsePolicyAttr it
    }

    return null
}

private object PolicyAttrKeys {
    const val OPERATION = "operation"
    const val ATTRIBUTE_LIST = "attribute_list"
    const val OBLIGATIONS = "obligations"
    const val TYPE = "type"
    const val VALUE = "value"
}
