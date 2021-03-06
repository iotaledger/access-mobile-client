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
package org.iota.access.delegation

import android.content.res.Resources
import androidx.annotation.StringRes
import org.iota.access.R

enum class RuleLimitation {
    TIME, LOCATION;

    @get:StringRes
    private val nameResId: Int
        get() = when (this) {
            TIME -> R.string.rule_limitation_time
            LOCATION -> R.string.rule_limitation_location
        }

    enum class TimeType {
        FROM, UNTIL
    }

    companion object {
        fun getAllStringValues(resources: Resources): Array<String> =
                values().map { resources.getString(it.nameResId) }.toTypedArray()
    }
}
