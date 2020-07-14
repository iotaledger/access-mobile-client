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
package org.iota.access.ui.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import org.iota.access.R

class ProgressDialogFragment : DialogFragment() {
    private var contentView: View? = null
    private var builder: AlertDialog.Builder? = null

    fun changeMessage(newMessage: String?) {
        if (contentView == null) return
        val messageTextView = contentView!!.findViewById<TextView>(R.id.dialog_progress_message)
        if (messageTextView != null) {
            messageTextView.text = newMessage
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity ?: return super.onCreateDialog(savedInstanceState)
        val args = arguments
        var message: String? = ""
        if (args != null) {
            message = args.getString(KEY_MESSAGE)
        }

        builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        contentView = inflater.inflate(R.layout.dialog_progress, null)
        val progressBar = contentView!!.findViewById<ProgressBar>(R.id.dialog_progress_progress_bar)
        progressBar.isIndeterminate = true
        val messageTextView = contentView!!.findViewById<TextView>(R.id.dialog_progress_message)
        messageTextView.text = message
        builder!!.setView(contentView)
        return builder!!.create()
    }

    companion object {
        private const val KEY_MESSAGE = "message"

        @JvmStatic
        fun newInstance(message: String?): ProgressDialogFragment =
                ProgressDialogFragment().apply {
                    arguments = Bundle().apply {
                        putString(KEY_MESSAGE, message)
                    }
                }
    }
}
