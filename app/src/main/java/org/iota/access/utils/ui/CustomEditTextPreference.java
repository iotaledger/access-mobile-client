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

package org.iota.access.utils.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.text.InputType;
import android.util.AttributeSet;

import org.iota.access.R;

public class CustomEditTextPreference extends EditTextPreference {

    public CustomEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomEditTextPreference, 0, 0);
        try {
            switch (a.getInt(R.styleable.CustomEditTextPreference_inputType, 0)) {
                case 0:
                    getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                    break;
                case 1:
                    getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
            }
        } finally {
            a.recycle();
        }
    }
}
