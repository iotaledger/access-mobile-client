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
package org.iota.access

import android.graphics.Color
import android.view.Menu
import android.view.MenuInflater
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.CallSuper
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import org.iota.access.ui.dialogs.InfoDialogFragment
import org.iota.access.ui.dialogs.ProgressDialogFragment
import org.iota.access.ui.dialogs.QuestionDialogFragment
import org.iota.access.utils.ui.UiUtils
import timber.log.Timber

abstract class BaseFragment : Fragment,
        InfoDialogFragment.InfoDialogListener,
        QuestionDialogFragment.QuestionDialogListener {

    private var snackbar: Snackbar? = null

    constructor()

    constructor(contentLayoutId: Int) : super(contentLayoutId)

    protected open fun showSnackbar(message: String) {
        val fragmentView = view ?: return
        try {
            snackbar = Snackbar.make(fragmentView, message, Snackbar.LENGTH_LONG)
            snackbar?.apply {
                setAction(android.R.string.ok) { snackbar?.dismiss() }
                setActionTextColor(Color.GREEN)
                show()
            }
            Timber.d("Shown snackbar with message: %s", message)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    protected val navController: NavController
        get() = NavHostFragment.findNavController(this)


    @CallSuper
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        var i = 0
        val n = menu.size()
        while (i < n) {
            val item = menu.getItem(i)
            var icon = item.icon
            if (icon == null) {
                i++
                continue
            }
            icon = DrawableCompat.wrap(icon)
            DrawableCompat.setTint(icon, UiUtils.getColorFromAttr(context, R.attr.action_menu_icon_color, Color.GRAY))
            i++
        }
    }

    protected fun showProgress(message: String?) =
            ProgressDialogFragment.newInstance(message).show(childFragmentManager, FRAGMENT_TAG_PROGRESS_DIALOG)

    protected fun hideProgress() {
        childFragmentManager.executePendingTransactions()
        val progressFragment = childFragmentManager.findFragmentByTag(FRAGMENT_TAG_PROGRESS_DIALOG) as ProgressDialogFragment?
        progressFragment?.dismiss()
    }

    protected fun showLoading(show: Boolean?, message: String?) {
        if (show == true) {
            showProgress(message)
        } else {
            hideProgress()
        }
    }

    protected fun showInfoDialog(
            message: String,
            dialogTag: String = INFO_DIALOG_TAG,
            title: String? = null,
            buttonTitle: String = context?.getString(android.R.string.ok) ?: "OK"
    ) {
        if (childFragmentManager.findFragmentByTag(dialogTag) != null) {
            Timber.d("Trying to present another InfoDialogFragment with same tag: $dialogTag")
        } else {
            InfoDialogFragment.newInstance(message, dialogTag, title, buttonTitle).show(childFragmentManager, dialogTag)
        }
    }

    override fun onInfoDialogConfirm(dialogTag: String) {}

    protected fun showQuestionDialog(
            question: String,
            dialogTag: String,
            title: String? = null,
            positiveButtonTitle: String = context?.getString(android.R.string.ok) ?: "OK",
            negativeButtonTitle: String = context?.getString(android.R.string.cancel) ?: "Cancel"
    ) {
        if (childFragmentManager.findFragmentByTag(dialogTag) != null) {
            Timber.d("Trying to present another QuestionDialogFragment with same tag: $dialogTag")
        } else {
            QuestionDialogFragment.newInstance(question, dialogTag, title, positiveButtonTitle, negativeButtonTitle).show(childFragmentManager, dialogTag)
        }
    }

    override fun onQuestionDialogAnswer(dialogTag: String, answer: QuestionDialogFragment.QuestionDialogAnswer) {}

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (nextAnim == 0) return super.onCreateAnimation(transit, enter, nextAnim)
        val nextAnimation = AnimationUtils.loadAnimation(context, nextAnim)
        nextAnimation.setAnimationListener(object : Animation.AnimationListener {
            private var mOldTranslationZ = 0f
            override fun onAnimationStart(animation: Animation) {
                if (view != null && enter && nextAnim == R.anim.enter_anim) {
                    mOldTranslationZ = ViewCompat.getTranslationZ(view!!)
                    ViewCompat.setTranslationZ(view!!, 100f)
                    return
                }
                if (view != null) {
                    mOldTranslationZ = ViewCompat.getTranslationZ(view!!)
                    if (nextAnim == R.anim.enter_anim) {
                        ViewCompat.setTranslationZ(view!!, 100f)
                    } else if (nextAnim == R.anim.pop_enter_anim) {
                        ViewCompat.setTranslationZ(view!!, -100f)
                    }
                }
            }

            override fun onAnimationEnd(animation: Animation) {
                if (view != null) {
                    if (nextAnim == R.anim.enter_anim) {
                        ViewCompat.setTranslationZ(view!!, mOldTranslationZ)
                    } else if (nextAnim == R.anim.pop_enter_anim) {
                        ViewCompat.setTranslationZ(view!!, mOldTranslationZ)
                    }
                }
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        return nextAnimation
    }

    override fun onPause() {
        super.onPause()
        if (snackbar != null) {
            snackbar!!.dismiss()
            snackbar = null
        }
    }

    companion object {
        private const val FRAGMENT_TAG_PROGRESS_DIALOG = "com.iota.access.progress_dialog_tag"

        private const val INFO_DIALOG_TAG = "infoDialogTag"
    }
}
