package org.iota.access.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment


class InfoDialogFragment : DialogFragment() {

    interface InfoDialogListener {
        fun onInfoDialogConfirm(dialogTag: String)
    }

    private var listener: InfoDialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = if (arguments != null) Arguments(requireArguments()) else Arguments()

        val builder = AlertDialog.Builder(context)
        builder.setTitle(args.title)
        builder.setMessage(args.message)
        builder.setPositiveButton(args.buttonTitle) { dialog, _ ->
            listener?.onInfoDialogConfirm(args.dialogTag)
            dialog.dismiss()
        }

        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val parentFragment = parentFragment

        if (parentFragment is InfoDialogListener) {
            listener = parentFragment
        } else if (context is InfoDialogListener) {
            listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        private const val MESSAGE = "message"
        private const val DIALOG_TAG = "dialogTag"
        private const val TITLE = "title"
        private const val BUTTON_TITLE = "buttonTitle"

        fun newInstance(
                message: String,
                dialogTag: String,
                title: String? = null,
                buttonTitle: String = "OK"
        ): InfoDialogFragment =
                InfoDialogFragment().apply {
                    arguments = createArguments(message, dialogTag, title, buttonTitle)
                }

        @Suppress("MemberVisibilityCanBePrivate")
        fun createArguments(
                message: String,
                dialogTag: String,
                title: String? = null,
                buttonTile: String = "OK"
        ): Bundle = Bundle().apply {
            putString(MESSAGE, message)
            putString(DIALOG_TAG, dialogTag)
            putString(TITLE, title)
            putString(BUTTON_TITLE, buttonTile)
        }

        private class Arguments(
                val message: String = "message",
                val dialogTag: String = "tag",
                val title: String? = null,
                val buttonTitle: String = "OK"
        ) {
            constructor(bundle: Bundle) : this(
                    bundle.getString(MESSAGE, "message"),
                    bundle.getString(DIALOG_TAG, "tag"),
                    bundle.getString(TITLE),
                    bundle.getString(BUTTON_TITLE, "OK"))
        }

    }
}
