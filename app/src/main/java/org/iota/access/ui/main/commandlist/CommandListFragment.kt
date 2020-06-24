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
import android.view.*
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import org.iota.access.CommunicationFragment
import org.iota.access.R
import org.iota.access.api.model.Command
import org.iota.access.api.model.token_server.TSSendRequest
import org.iota.access.databinding.FragmentCommandListBinding
import org.iota.access.di.AppSharedPreferences
import org.iota.access.ui.dialogs.QuestionDialogFragment
import org.iota.access.ui.main.commandlist.CommandsAdapter.CommandsAdapterListener
import org.iota.access.user.UserManager
import org.iota.access.utils.Constants
import org.iota.access.utils.ui.DialogFragmentUtil
import timber.log.Timber
import java.io.IOException
import java.util.*
import javax.inject.Inject

/**
 * Fragment representing the Main Screen
 */
class CommandListFragment : CommunicationFragment<CommandListViewModel>(), CommandsAdapterListener {

    @Inject
    lateinit var preferences: AppSharedPreferences

    @Inject
    lateinit var userManager: UserManager

    private lateinit var binding: FragmentCommandListBinding

    private val commands: MutableList<Command> = ArrayList()

    override val viewModelClass: Class<CommandListViewModel>
        get() = CommandListViewModel::class.java

    private var unpaidCommand: Command? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onStart() {
        super.onStart()
        if (!viewModel.isPolicyRequested) viewModel.policyList else if (!viewModel.commandList.isEmpty) {
            binding.fab.show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_command_list, container, false)
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.fab.setOnClickListener { onMicrophoneButtonClicked() }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.recyclerView.adapter = CommandsAdapter(commands, this)
        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.policyList }
        combineCommandsFromServerAndFromPreferences()
    }

    private fun combineCommandsFromServerAndFromPreferences() {
        var fromServer: List<Command>? = null
        if (!viewModel.commandList.isEmpty) fromServer = viewModel.commandList.get()
        val fromPreferences = preferences.commandList
        val combinedList: MutableList<Command> = ArrayList()
        if (fromServer != null) combinedList.addAll(fromServer)
        if (fromPreferences != null) combinedList.addAll(fromPreferences)
        commands.clear()
        commands.addAll(combinedList)
        binding.recyclerView.adapter?.notifyDataSetChanged()
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
            R.id.action_add_new_command -> {
                navController.navigate(CommandListFragmentDirections.actionCommandListFragmentToCommandEditorFragment())
                true
            }
            R.id.action_refill_tokens -> {
                val question = getString(
                        R.string.msg_refill_tokens_question,
                        (CommandListViewModel.REFILL_AMOUNT * Constants.TOKEN_SCALE_FACTOR).toString())
                showQuestionDialog(question, REFILL_ACCOUNT_QUESTION)
                true
            }
            R.id.action_clear_users -> {
                clearUserList()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onQuestionDialogAnswer(dialogTag: String, answer: QuestionDialogFragment.QuestionDialogAnswer) {
        when (dialogTag) {
            REFILL_ACCOUNT_QUESTION -> refillAccount()
            PAY_ACTION_QUESTION -> {
                val command = unpaidCommand ?: return
                payForCommand(command)
                unpaidCommand = null
            }
        }
    }

    private fun clearPolicyList() {
        val dialog = DialogFragmentUtil.createAlertDialog(
                getString(R.string.clear_policy),
                android.R.string.yes,
                android.R.string.no
        ) { viewModel.clearPolicyList() }
        DialogFragmentUtil.showDialog(dialog, childFragmentManager, TAG_CLEAR_POLICY)
    }

    private fun clearUserList() {
        val dialog = DialogFragmentUtil.createAlertDialog(
                getString(R.string.clear_users),
                android.R.string.yes,
                android.R.string.no
        ) { viewModel.clearUserList() }
        DialogFragmentUtil.showDialog(dialog, childFragmentManager, TAG_CLEAR_POLICY)
    }

    private fun payForCommand(command: Command) {
        val user = userManager.user ?: return
        val senderWalletId = user.walletId
        val receiverWalletId = resources.getStringArray(R.array.wallet_ids)[0]
        val priority = 4
        val requestBody = TSSendRequest(senderWalletId,
                receiverWalletId, command.activeAction.cost.toString(),
                priority)
        viewModel.payPolicy(requestBody, command.activeAction.policyId)
    }

    override fun onCommandSelected(command: Command) {
        // check if command is NOT paid
        if (!command.activeAction.isPaid && command.activeAction.cost != null) {
            unpaidCommand = command
            val cost = command.activeAction.cost
            val question = getString(R.string.msg_action_not_paid_question, (cost!! * Constants.TOKEN_SCALE_FACTOR).toString())
            showQuestionDialog(question, PAY_ACTION_QUESTION)
        } else {
            val inputStream = javaClass.getResourceAsStream("/assets/template.json")
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            viewModel.executeCommand(command)
        }
    }

    private fun onMicrophoneButtonClicked() {
        startActivityForResult(viewModel.asrIntent, ACTIVITY_RESULT_SPEECH)
    }

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

    private fun refillAccount() {
        if (userManager.user == null) {
            return  // should newer occur
        }
        val walletId = userManager.user!!.walletId
        viewModel.refillAccount(walletId)
    }

    private fun showRefresh(flag: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = flag
    }

    private fun handleNewCommandList(commandList: List<Command>) {
        combineCommandsFromServerAndFromPreferences()
        if (commandList.isEmpty()) {
            binding.fab.hide()
            showBackgroundMessage(R.string.msg_no_commands, R.drawable.ic_delegate)
        } else {
            binding.fab.show()
            hideBackgroundMessage()
        }
    }

    override fun bindViewModel() {
        super.bindViewModel()
        disposable?.let {
            it.add(viewModel.observableCommandList
                    .observeOn(AndroidSchedulers.mainThread())
                    .filter { optList -> !optList.isEmpty }
                    .map { optList -> optList.get()!! }
                    .subscribe({ commandList: List<Command> -> handleNewCommandList(commandList) }, Timber::e))
            it.add(viewModel.showRefresh
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ flag: Boolean -> showRefresh(flag) }, Timber::e))
        }
    }

    companion object {
        private const val ACTIVITY_RESULT_SPEECH = 10
        private const val TAG_CLEAR_POLICY = "clear_policy_dialog"
        private const val REFILL_ACCOUNT_QUESTION: String = "refillAccountQuestion"
        private const val PAY_ACTION_QUESTION = "payActionQuestion"

        @JvmStatic
        fun newInstance(): CommandListFragment {
            return CommandListFragment()
        }

    }
}
