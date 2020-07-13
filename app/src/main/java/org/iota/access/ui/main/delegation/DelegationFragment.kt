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
package org.iota.access.ui.main.delegation

import android.os.Bundle
import android.util.Pair
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.iota.access.BaseFragment
import org.iota.access.R
import org.iota.access.databinding.FragmentDelegationBinding
import org.iota.access.di.AppSharedPreferences
import org.iota.access.di.Injectable
import org.iota.access.extensions.getStyledText
import org.iota.access.models.DelegationAction
import org.iota.access.models.DelegationUser
import org.iota.access.models.rules.MultipleRule
import org.iota.access.models.rules.Rule
import org.iota.access.utils.Constants
import org.iota.access.utils.Optional
import org.iota.access.utils.ui.DialogFragmentUtil
import org.iota.access.utils.ui.SpinnerArrayAdapter
import org.iota.access.utils.ui.ThemeLab
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class DelegationFragment : BaseFragment(R.layout.fragment_delegation), Injectable {

    @Inject
    lateinit var themeLab: ThemeLab

    @Inject
    lateinit var preferences: AppSharedPreferences

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var delegationSharedViewModel: DelegationSharedViewModel
    private lateinit var binding: FragmentDelegationBinding
    private lateinit var viewModel: DelegationViewModel

    private var disposable: CompositeDisposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.nestedScrollView.onSaveInstanceState()?.let {
            viewModel.nestedScrollSavedInstance = it
        }
        unbindViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DataBindingUtil.bind(view)!!

        binding.buttonUsers.setOnClickListener { onUsersButtonClick() }
        binding.buttonActions.setOnClickListener { onActionsButtonClick() }
        binding.buttonOneTimeOnly.setOnClickListener { onOneTimeOnlyButtonClick() }
        binding.buttonUnlimited.setOnClickListener { onUnlimitedButtonClick() }
        binding.fab.setOnClickListener { onFabClick() }

        binding.buttonRulesGrant.setOnClickListener { onRulesGrantButtonClick() }
        binding.buttonRulesDeny.setOnClickListener { onRulesDenyButtonClick() }

        val values = arrayOfNulls<String>(DelegationViewModel.COST_VALUES.size)
        for (i in DelegationViewModel.COST_VALUES.indices) {
            val quantity = (DelegationViewModel.COST_VALUES[i] * Constants.TOKEN_SCALE_FACTOR).toInt()
            if (quantity == 0) values[i] = resources.getString(R.string.free) else values[i] = resources.getQuantityString(R.plurals.token_plural, quantity, (DelegationViewModel.COST_VALUES[i] * Constants.TOKEN_SCALE_FACTOR))
        }

        binding.spinnerCost.adapter = SpinnerArrayAdapter(activity, values)

        TooltipCompat.setTooltipText(binding.fab, getString(R.string.button_delegate))

        binding.lblObligateIfGranted.text = getStyledText(R.string.html_lbl_obligate_if_granted)
        binding.lblObligateIfDenied.text = getStyledText(R.string.html_lbl_obligate_if_denied)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory).get(DelegationViewModel::class.java)

        lifecycle.addObserver(viewModel)

        binding.viewModel = viewModel


        val storeOwner = navController.getViewModelStoreOwner(navController.graph.id)
        delegationSharedViewModel = ViewModelProvider(storeOwner, viewModelFactory).get(DelegationSharedViewModel::class.java)

        viewModel.nestedScrollSavedInstance?.let {
            binding.nestedScrollView.onRestoreInstanceState(it)
        }

        bindViewModel()

        // Obligation if policy is granted
        binding.spinnerObligateIfGranted.adapter = SpinnerArrayAdapter(
                context,
                viewModel.allObligations.map { it.displayName }.toMutableList().apply { add(0, "None") }.toTypedArray()
        )

        // Obligation if policy is denied
        binding.spinnerObligateIfDenied.adapter = SpinnerArrayAdapter(
                context,
                viewModel.allObligations.map { it.displayName }.toMutableList().apply { add(0, "None") }.toTypedArray()
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Add PREVIEW menu item
        val menuItem: MenuItem = menu.add(Menu.NONE, MENU_ITEM_PREVIEW, 0, R.string.action_preview)
        menuItem.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_preview)
        menuItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == MENU_ITEM_PREVIEW) {
            onPreviewOptionsItemSelected()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun onPreviewOptionsItemSelected() {
        val policy = viewModel.createPolicy(
                true,
                null,
                getRule(viewModel.rulesGrantId),
                getRule(viewModel.rulesDenyId))

        delegationSharedViewModel.previewingPolicy = policy
        val direction = DelegationFragmentDirections.actionDelegationFragmentToDelegationPreviewFragment()
        navController.navigate(direction)
    }

    private fun bindViewModel() {
        disposable = CompositeDisposable()
        disposable?.let {
            it.add(viewModel.observableSelectedUsers
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this@DelegationFragment::onUserListChange, Timber::e))

            it.add(viewModel.observableDelegationActionList
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this@DelegationFragment::onDelegationActionListChange, Timber::e))

            it.add(viewModel.observableMaxNumOfExecutions
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this@DelegationFragment::onMaximumExecutionNumberChange, Timber::e))

            it.add(viewModel.observableShowLoadingMessage
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { pair: Pair<Boolean?, String?> -> showLoading(pair.first, pair.second) }, Timber::e))

            it.add(viewModel.observableShowDialogMessage
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ message: String -> showInfoDialog(message) }, Timber::e))

            it.add(viewModel.observableSnackbarMessage
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ message: String -> showSnackbar(message) }, Timber::e))

            it.add(delegationSharedViewModel.observableRules
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this@DelegationFragment::onRulesChanged, Timber::e))
        }
    }

    private fun unbindViewModel() = disposable?.dispose()

    private fun onRulesChanged(rules: Map<String, Rule>) {
        for (key in rules.keys) {
            val rule = rules[key]
            val size = (if (rule is MultipleRule) rule.ruleList.size else if (rule != null) 1 else 1).toString()

            when (key) {
                viewModel.rulesGrantId -> {
                    binding.buttonRulesGrant.text = getStyledText(R.string.html_button_grant_count, size)
                }
                viewModel.rulesDenyId -> {
                    binding.buttonRulesDeny.text = getStyledText(R.string.html_button_deny_count, size)
                }
            }
        }
    }

    private fun onUsersButtonClick() {
        if (viewModel.allDelegationUsers.isEmpty()) return

        val selectedUsers = viewModel.selectedDelegationUsers
        val allUsers = viewModel.allDelegationUsers

        val selected = BooleanArray(allUsers.size)
        for (i in selected.indices) selected[i] = false
        for (selectedUser in selectedUsers) {
            val index = allUsers.indexOf(selectedUser)
            if (index >= 0) {
                selected[index] = true
            }
        }
        val userNames = arrayOfNulls<CharSequence>(allUsers.size)
        var i = 0
        val n = allUsers.size
        while (i < n) {
            userNames[i] = allUsers[i].username
            ++i
        }
        val dialog = DialogFragmentUtil.createListDialog(
                userNames,
                selected) { sel: BooleanArray -> onSelectionUsersFinish(sel) }

        DialogFragmentUtil.showDialog(dialog, childFragmentManager, USERS_LIST_FRAGMENT_TAG)
    }

    private fun onActionsButtonClick() {
        val delegationActionList = viewModel.delegationActionList
        val delegationActionNames: MutableList<CharSequence> = ArrayList()
        run {
            var i = 0
            val n = viewModel.allActions.size
            while (i < n) {
                delegationActionNames.add(viewModel.allActions[i].displayName)
                ++i
            }
        }
        val allActions = viewModel.allActions
        val selectedActions = BooleanArray(allActions.size)
        for (i in selectedActions.indices) selectedActions[i] = false
        run {
            var i = 0
            val n = delegationActionList.size
            while (i < n) {
                val index = allActions.indexOf(delegationActionList[i])
                if (index >= 0) selectedActions[index] = true
                ++i
            }
        }
        DialogFragmentUtil.showDialog(
                DialogFragmentUtil.createListDialog(
                        delegationActionNames.toTypedArray(),
                        selectedActions) { selected: BooleanArray -> onSelectionActionsFinish(selected) },
                childFragmentManager,
                ACTION_LIST_FRAGMENT_TAG)
    }

    private fun onOneTimeOnlyButtonClick() = viewModel.setMaxNumOfExecutions(1)

    private fun onUnlimitedButtonClick() = viewModel.setMaxNumOfExecutions(null)

    private fun onFabClick() = viewModel.delegate(
            getRule(viewModel.rulesGrantId),
            getRule(viewModel.rulesDenyId)
    )

    private fun onRulesGrantButtonClick() = navigateToDelegationRuleFragment(viewModel.rulesGrantId)

    private fun onRulesDenyButtonClick() = navigateToDelegationRuleFragment(viewModel.rulesDenyId)

    private fun navigateToDelegationRuleFragment(ruleId: String) {
        val rule = getRule(ruleId)
        val mode = if (rule != null) DelegationRuleFragment.Mode.EDITING else DelegationRuleFragment.Mode.CREATING
        val arguments = DelegationRuleFragment.createArguments(ruleId, mode)
        navController.navigate(R.id.action_delegationFragment_to_delegationRuleFragment, arguments)
    }

    private fun getRule(ruleId: String): Rule? = delegationSharedViewModel.getRule(ruleId)

    private fun onUserListChange(delegationUserList: Set<DelegationUser>) {
        val label = getString(R.string.label_delegate_to) + " (" + getString(R.string.label_selected, delegationUserList.size) + ")"
        binding.labelDelegateTo.text = label
    }

    private fun onDelegationActionListChange(delegationActionList: List<DelegationAction>) {
        val label = getString(R.string.label_actions) + " (" + getString(R.string.label_selected, delegationActionList.size) + ")"
        binding.labelActions.text = label
    }

    private fun onSelectionUsersFinish(selected: BooleanArray) {
        val delegationUsers: MutableSet<DelegationUser> = HashSet()
        for (i in selected.indices) if (selected[i]) delegationUsers.add(viewModel.allDelegationUsers[i])
        viewModel.selectedDelegationUsers = delegationUsers
    }

    private fun onSelectionActionsFinish(selected: BooleanArray) {
        val actions: MutableList<DelegationAction> = ArrayList()
        for (i in selected.indices) if (selected[i]) actions.add(viewModel.allActions[i])
        viewModel.setDelegationList(actions)
    }

    private fun onMaximumExecutionNumberChange(integerOptional: Optional<Int>) {
        if (integerOptional.isEmpty) {
            binding.buttonUnlimited.isChecked = true
        } else {
            binding.buttonOneTimeOnly.isChecked = true
        }
    }

    companion object {
        private val MENU_ITEM_PREVIEW = View.generateViewId()
        private const val USERS_LIST_FRAGMENT_TAG = "usersListFragmentTag"
        private const val ACTION_LIST_FRAGMENT_TAG = "actionListFragmentTag"

        fun newInstance(): DelegationFragment {
            return DelegationFragment()
        }
    }
}
