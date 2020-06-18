package org.iota.access.extensions

import android.os.Build

fun doIfNougat(block: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) block.invoke()
}