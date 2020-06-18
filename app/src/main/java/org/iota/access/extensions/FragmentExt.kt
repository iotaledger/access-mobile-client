package org.iota.access.extensions

import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment


fun Fragment.getStyledText(@StringRes id: Int, vararg args: Any?): CharSequence {
    val text = String.format(getString(id), *args)
    return HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT)
}
