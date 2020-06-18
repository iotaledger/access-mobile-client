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

package org.iota.access;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.core.view.ViewCompat;
import androidx.preference.PreferenceFragmentCompat;

public abstract class BasePreferenceFragmentCompat extends PreferenceFragmentCompat {

    @Override
    public Animation onCreateAnimation(int transit, final boolean enter, int nextAnim) {
        if (nextAnim == 0) return super.onCreateAnimation(transit, enter, nextAnim);

        Animation nextAnimation = AnimationUtils.loadAnimation(getContext(), nextAnim);
        nextAnimation.setAnimationListener(new Animation.AnimationListener() {

            private float mOldTranslationZ;

            @Override
            public void onAnimationStart(Animation animation) {
                if (getView() != null && enter && nextAnim == R.anim.enter_anim) {
                    mOldTranslationZ = ViewCompat.getTranslationZ(getView());
                    ViewCompat.setTranslationZ(getView(), 100.f);
                }

                if (getView() != null) {
                    mOldTranslationZ = ViewCompat.getTranslationZ(getView());
                    if (nextAnim == R.anim.enter_anim) {
                        ViewCompat.setTranslationZ(getView(), 100.f);
                    } else if (nextAnim == R.anim.pop_enter_anim) {
                        ViewCompat.setTranslationZ(getView(), -100f);
                    }
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (getView() != null) {
                    if (nextAnim == R.anim.enter_anim) {
                        ViewCompat.setTranslationZ(getView(), mOldTranslationZ);
                    } else if (nextAnim == R.anim.pop_enter_anim) {
                        ViewCompat.setTranslationZ(getView(), mOldTranslationZ);
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        return nextAnimation;
    }

}