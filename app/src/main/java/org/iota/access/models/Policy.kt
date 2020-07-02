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

import org.json.JSONObject
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class Policy(
        val hashFunction: String,
        val policyObject: PolicyObject,
        val cost: String = "",
        policyId: String? = null
) : Mappable {

    val policyId: String = policyId ?: calculatePolicyId(policyObject, hashFunction)

    override fun toMap(): Map<String, Any> = mapOf(
            PolicyKeys.HASH_FUNCTION to hashFunction,
            PolicyKeys.POLICY_ID to policyId,
            PolicyKeys.POLICY_OBJECT to policyObject.toMap(),
            PolicyKeys.COST to cost)

    companion object {
        fun fromJSON(json: JSONObject): Policy? {
            val hashFunction = json.optString(PolicyKeys.HASH_FUNCTION) ?: return null
            val cost = json.optString(PolicyKeys.COST) ?: return null
            val policyId = json.optString(PolicyKeys.POLICY_ID) ?: return null
            val policyObjectObj = json.optJSONObject(PolicyKeys.POLICY_OBJECT) ?: return null
            val policyObject = PolicyObject.fromJSON(policyObjectObj) ?: return null
            return Policy(hashFunction, policyObject, cost, policyId)
        }

        private fun calculatePolicyId(policyObject: PolicyObject, hashFunction: String): String {
            val policyObjectHash = policyObject.calculateHash(hashFunction)
            return String.format("%0" + policyObjectHash.size * 2 + "X", BigInteger(1, policyObjectHash))
        }
    }
}

data class PolicyObject(
        val obligationDeny: PolicyObligation,
        val obligationGrant: PolicyObligation,
        val policyDoc: PolicyAttribute,
        val policyGoc: PolicyAttribute
) : Mappable {

    override fun toMap(): Map<String, Any> = mapOf(
            PolicyKeys.OBLIGATION_DENY to obligationDeny.toMap(),
            PolicyKeys.OBLIGATION_GRANT to obligationGrant.toMap(),
            PolicyKeys.POLICY_DOC to policyDoc.toMap(),
            PolicyKeys.POLICY_GOC to policyGoc.toMap())

    @Throws(NoSuchAlgorithmException::class)
    fun calculateHash(hashFunction: String): ByteArray = MessageDigest.getInstance(hashFunction)
            .apply { reset() }
            .digest(JSONObject(toMap()).toString().toByteArray(Charsets.UTF_8))

    companion object {
        fun fromJSON(json: JSONObject): PolicyObject? {
            val obligationGrantObj = json.optJSONObject(PolicyKeys.OBLIGATION_GRANT)
            val obligationDenyObj = json.optJSONObject(PolicyKeys.OBLIGATION_DENY)
            val policyDocObj = json.optJSONObject(PolicyKeys.POLICY_DOC)
            val policyGocObj = json.optJSONObject(PolicyKeys.POLICY_GOC)

            val obligationGrant = (parsePolicyAttr(obligationGrantObj) as? PolicyObligation)
                    ?: PolicyObligationEmpty
            val obligationDeny = (parsePolicyAttr(obligationDenyObj) as? PolicyObligation)
                    ?: PolicyObligationEmpty
            val policyDoc = parsePolicyAttr(policyDocObj) ?: PolicyObligationEmpty
            val policyGoc = parsePolicyAttr(policyGocObj) ?: PolicyAttributeEmpty

            return PolicyObject(obligationDeny, obligationGrant, policyDoc, policyGoc)
        }
    }
}

private object PolicyKeys {
    const val HASH_FUNCTION = "hash_function"
    const val POLICY_ID = "policy_id"
    const val COST = "cost"
    const val POLICY_OBJECT = "policy_object"
    const val OBLIGATION_GRANT = "obligation_grant"
    const val OBLIGATION_DENY = "obligation_deny"
    const val POLICY_GOC = "policy_goc"
    const val POLICY_DOC = "policy_doc"
}

interface Mappable {
    fun toMap(): Map<String, Any>
}
