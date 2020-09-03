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
package org.iota.access.ui.main.commandlist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.iota.access.BaseFragment
import org.iota.access.R
import org.iota.access.api.model.CommandAction
import org.iota.access.databinding.FragmentCommandListBinding
import org.iota.access.di.AppSharedPreferences
import org.iota.access.di.Injectable
import org.iota.access.ui.dialogs.QuestionDialogFragment
import org.iota.access.ui.main.commandlist.CommandActionAdapter.CommandActionAdapterListener
import org.iota.access.user.UserManager
import org.iota.access.utils.Constants
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

/**
 * Fragment representing the Main Screen.
 */
class CommandListFragment : BaseFragment(R.layout.fragment_command_list), Injectable, CommandActionAdapterListener {

    @Inject
    lateinit var preferences: AppSharedPreferences

    @Inject
    lateinit var userManager: UserManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentCommandListBinding
    private lateinit var viewModel: CommandListViewModel

    private val commands: MutableList<CommandAction> = mutableListOf()

    private var unpaidCommand: CommandAction? = null
    private var commandToDelete: CommandAction? = null
    private var disposable: CompositeDisposable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        binding.fab.setOnClickListener { onMicrophoneButtonClicked() }
        setHasOptionsMenu(true)
    }

    override fun onStart() {
        super.onStart()
        if (!viewModel.isPolicyRequested) viewModel.getPolicyList() else if (!viewModel.commandList.isEmpty) {
            binding.fab.show()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.recyclerView.adapter = CommandActionAdapter(commands, this)
        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.getPolicyList() }

        val storeOwner = navController.getViewModelStoreOwner(navController.graph.id)
        viewModel = ViewModelProvider(storeOwner, viewModelFactory).get(CommandListViewModel::class.java)

        activity?.onBackPressedDispatcher
                ?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        this@CommandListFragment.activity?.finish()
                    }
                })

        bindViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbindViewModel()
    }

    override fun showSnackbar(message: String) {
        super.showSnackbar(message)
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_clear_policy -> {
                clearPolicyList()
                true
            }
            R.id.action_refill_tokens -> {
                val question = getString(
                        R.string.msg_refill_tokens_question,
                        (CommandListViewModel.REFILL_AMOUNT * Constants.TOKEN_SCALE_FACTOR).toString())
                showQuestionDialog(question, REFILL_ACCOUNT_QUESTION)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onQuestionDialogAnswer(dialogTag: String, answer: QuestionDialogFragment.QuestionDialogAnswer) {
        when (dialogTag) {
            DELETE_COMMAND_QUESTION -> {
                if (answer == QuestionDialogFragment.QuestionDialogAnswer.POSITIVE) {
                    val commandToDelete = this.commandToDelete
                    if (commandToDelete != null) {
                        deleteCommand(commandToDelete)
                    }
                }
                commandToDelete = null
            }
            TAG_CLEAR_POLICY -> {
                if (answer == QuestionDialogFragment.QuestionDialogAnswer.POSITIVE) {
                    viewModel.clearPolicyList()
                }
            }
        }
    }


    @Suppress("UNUSED_PARAMETER")
    private fun deleteCommand(commandAction: CommandAction) {
        // TODO: 29.6.2020. Delete command
    }

    private fun clearPolicyList() = showQuestionDialog(getString(R.string.clear_policy), TAG_CLEAR_POLICY)

    @Suppress("UNUSED_PARAMETER")
    private fun payForCommand(command: CommandAction) {
//        val user = userManager.user ?: return
//        val senderWalletId = user.walletId
//        val receiverWalletId = resources.getStringArray(R.array.wallet_ids)[0]
//        val priority = 4
//        val requestBody = TSSendRequest(senderWalletId,
//                receiverWalletId, command.cost.toString(),
//                priority)
//        viewModel.payPolicy(requestBody, command.policyId)
    }

    override fun onCommandSelected(commandAction: CommandAction) {
        // check if command is NOT paid
        if (!commandAction.isPaid && commandAction.cost != null) {
            unpaidCommand = commandAction
            val cost = commandAction.cost
            val question = getString(R.string.msg_action_not_paid_question, (cost!! * Constants.TOKEN_SCALE_FACTOR).toString())
            showQuestionDialog(question, PAY_ACTION_QUESTION)
        } else {
            val inputStream = javaClass.getResourceAsStream("/assets/template.json")
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            viewModel.executeCommand(commandAction)
        }
    }

    override fun onCommandDeleteClick(commandAction: CommandAction) {
        val question = getString(R.string.delete_command_question)
        commandToDelete = commandAction
        showQuestionDialog(question, DELETE_COMMAND_QUESTION)
    }

    private fun onMicrophoneButtonClicked() =
            startActivityForResult(viewModel.asrIntent, ACTIVITY_RESULT_SPEECH)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTIVITY_RESULT_SPEECH) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                viewModel.onAsrResult(data)
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun showBackgroundMessage(@StringRes message: Int, @DrawableRes messageImage: Int) {
        binding.textMessage.visibility = View.VISIBLE
        binding.textMessage.setText(message)
        binding.textMessage.setCompoundDrawablesWithIntrinsicBounds(0, messageImage, 0, 0)
    }

    private fun hideBackgroundMessage() {
        binding.textMessage.visibility = View.GONE
    }

    private fun showRefresh(flag: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = flag
    }

    private fun handleNewCommandList(commandList: List<CommandAction>) {
        if (commandList.isEmpty()) {
            binding.fab.hide()
            showBackgroundMessage(R.string.msg_no_commands, R.drawable.ic_delegate)
        } else {
            binding.fab.show()
            hideBackgroundMessage()
        }
        commands.clear()
        commands.addAll(commandList)
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun bindViewModel() {
        lifecycle.addObserver(viewModel)
        disposable = CompositeDisposable()
        disposable?.apply {
            add(viewModel.observableCommandList
                    .observeOn(AndroidSchedulers.mainThread())
                    .filter { optList -> !optList.isEmpty }
                    .map { optList -> optList.get()!! }
                    .subscribe({ commandList: List<CommandAction> -> handleNewCommandList(commandList) }, Timber::e))
            add(viewModel.showRefresh
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ flag: Boolean -> showRefresh(flag) }, Timber::e))

            add(viewModel.observableShowLoadingMessage
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { pair: Pair<Boolean?, String?> -> showLoading(pair.first, pair.second) }, Timber::e))
            add(viewModel
                    .observableShowDialogMessage
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ message: String -> showInfoDialog(message) }, Timber::e))
            add(viewModel
                    .observableSnackbarMessage
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ message: String -> showInfoDialog(message) }, Timber::e))
        }
    }

    private fun unbindViewModel() {
        lifecycle.removeObserver(viewModel)
        disposable?.dispose()
    }

    companion object {
        private const val ACTIVITY_RESULT_SPEECH = 10
        private const val TAG_CLEAR_POLICY = "clear_policy_dialog"
        private const val REFILL_ACCOUNT_QUESTION: String = "refillAccountQuestion"
        private const val PAY_ACTION_QUESTION = "payActionQuestion"
        private const val DELETE_COMMAND_QUESTION = "deleteCommandQuestion"

        @JvmStatic
        fun newInstance(): CommandListFragment {
            return CommandListFragment()
        }

    }
}
