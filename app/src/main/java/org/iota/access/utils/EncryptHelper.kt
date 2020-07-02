package org.iota.access.utils

import net.i2p.crypto.eddsa.EdDSAEngine
import net.i2p.crypto.eddsa.EdDSAPrivateKey
import net.i2p.crypto.eddsa.EdDSAPublicKey
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import java.security.KeyPair
import java.security.MessageDigest
import java.security.SecureRandom


@Suppress("unused")
object EncryptHelper {

    fun generateKeyPair(): KeyPair {
        val ed25519CurveSpec = EdDSANamedCurveTable.ED_25519_CURVE_SPEC
        val keygen = net.i2p.crypto.eddsa.KeyPairGenerator()

        keygen.initialize(ed25519CurveSpec, SecureRandom())

        return keygen.generateKeyPair()
    }

    fun signMessage(message: String, privateKey: EdDSAPrivateKey): ByteArray =
            EdDSAEngine(MessageDigest.getInstance("SHA-512"))
                    .apply {
                        initSign(privateKey)
                        update(message.toByteArray(Charsets.UTF_8))
                    }
                    .sign()

    fun verifyMessage(message: String, signature: ByteArray, publicKey: EdDSAPublicKey): Boolean =
            EdDSAEngine()
                    .apply {
                        initVerify(publicKey)
                        update(message.toByteArray(Charsets.UTF_8))
                    }
                    .verify(signature)

}




