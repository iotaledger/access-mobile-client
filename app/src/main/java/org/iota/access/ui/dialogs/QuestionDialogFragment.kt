package org.iota.access.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class QuestionDialogFragment : DialogFragment() {

    enum class QuestionDialogAnswer {
        POSITIVE,
        NEGATIVE
    }

    interface QuestionDialogListener {
        fun onQuestionDialogAnswer(dialogTag: String, answer: QuestionDialogAnswer)
    }

    private var listener: QuestionDialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = if (arguments != null) Arguments(requireArguments()) else Arguments()

        val builder = AlertDialog.Builder(context)
        builder.setTitle(args.title)
        builder.setMessage(args.question)
        builder.setPositiveButton(args.positiveButtonTitle) { dialog, _ ->
            listener?.onQuestionDialogAnswer(args.dialogTag, QuestionDialogAnswer.POSITIVE)
            dialog.dismiss()
        }
        builder.setNegativeButton(args.negativeButtonText) { dialog, _ ->
            listener?.onQuestionDialogAnswer(args.dialogTag, QuestionDialogAnswer.NEGATIVE)
            dialog.dismiss()
        }

        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val parentFragment = parentFragment

        if (parentFragment is QuestionDialogListener) {
            listener = parentFragment
        } else if (context is QuestionDialogListener) {
            listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        private const val QUESTION = "message"
        private const val DIALOG_TAG = "dialogTag"
        private const val TITLE = "title"
        private const val POSITIVE_BUTTON_TITLE = "positiveButtonTitle"
        private const val NEGATIVE_BUTTON_TITLE = "negativeButtonTitle"

        fun newInstance(
                question: String,
                dialogTag: String,
                title: String? = null,
                positiveButtonTitle: String = "Yes",
                negativeButtonTitle: String = "No"
        ): QuestionDialogFragment =
                QuestionDialogFragment().apply {
                    arguments = createArguments(question, dialogTag, title, positiveButtonTitle, negativeButtonTitle)
                }

        @Suppress("MemberVisibilityCanBePrivate")
        fun createArguments(
                question: String,
                dialogTag: String,
                title: String? = null,
                positiveButtonTitle: String = "Yes",
                negativeButtonTitle: String = "No"
        ): Bundle = Bundle().apply {
            putString(QUESTION, question)
            putString(DIALOG_TAG, dialogTag)
            putString(TITLE, title)
            putString(POSITIVE_BUTTON_TITLE, positiveButtonTitle)
            putString(NEGATIVE_BUTTON_TITLE, negativeButtonTitle)
        }

        private class Arguments(
                val question: String = "question",
                val dialogTag: String = "tag",
                val title: String? = null,
                val positiveButtonTitle: String = "Yes",
                val negativeButtonText: String = "No"
        ) {
            constructor(bundle: Bundle) : this(
                    bundle.getString(QUESTION, "question"),
                    bundle.getString(DIALOG_TAG, "tag"),
                    bundle.getString(TITLE),
                    bundle.getString(POSITIVE_BUTTON_TITLE, "Yes"),
                    bundle.getString(NEGATIVE_BUTTON_TITLE, "No"))
        }

    }
}
