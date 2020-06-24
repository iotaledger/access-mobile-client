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
@file:Suppress("unused")

package org.iota.access.utils

import android.util.Base64
import net.i2p.crypto.eddsa.EdDSAPrivateKey
import net.i2p.crypto.eddsa.EdDSAPublicKey
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec
import java.security.spec.PKCS8EncodedKeySpec

fun EdDSAPrivateKey.toBase64(): String = Base64.encodeToString(this.h, Base64.NO_WRAP)

fun EdDSAPublicKey.toBase64(): String = Base64.encodeToString(this.abyte, Base64.NO_WRAP)

object EdDSAPrivateKeyUtils {
    fun fromBase64(base64: String): EdDSAPrivateKey? {
        val ed25519CurveSpec = EdDSANamedCurveTable.ED_25519_CURVE_SPEC
        val privateKeyH = Base64.decode(base64, Base64.NO_WRAP)
        val keySpec = EdDSAPrivateKeySpec(ed25519CurveSpec, privateKeyH)
        return try {
            EdDSAPrivateKey(keySpec)
        } catch (e: IllegalArgumentException) {
            return null
        }
    }

    fun fromEncodedBase64(base64: String): EdDSAPrivateKey {
        val privateKey = Base64.decode(base64, Base64.NO_WRAP)
        return EdDSAPrivateKey(PKCS8EncodedKeySpec(privateKey))
    }
}

@Suppress("unused")
object EdDSAPublicKeyUtils {
    fun fromBase64(base64: String): EdDSAPublicKey? {
        val privateKey = Base64.decode(base64, Base64.NO_WRAP)
        val ed25519CurveSpec = EdDSANamedCurveTable.ED_25519_CURVE_SPEC
        return try {
            val keySpec = EdDSAPublicKeySpec(privateKey, ed25519CurveSpec)
            EdDSAPublicKey(keySpec)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun fromPrivateKey(privateKey: EdDSAPrivateKey): EdDSAPublicKey {
        val ed25519CurveSpec = EdDSANamedCurveTable.ED_25519_CURVE_SPEC
        val pubKeySpec = EdDSAPublicKeySpec(privateKey.a, ed25519CurveSpec)
        return EdDSAPublicKey(pubKeySpec)
    }
}
