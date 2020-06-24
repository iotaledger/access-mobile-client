package org.iota.access.extensions

import android.util.Base64

fun ByteArray.toBase64(): String = Base64.encodeToString(this, Base64.NO_WRAP)
