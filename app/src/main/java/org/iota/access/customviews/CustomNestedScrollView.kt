package org.iota.access.customviews

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView

class CustomNestedScrollView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    public override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
    }

    public override fun onSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()
    }
}
