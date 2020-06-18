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

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.TooltipCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.iota.access.BaseFragment
import org.iota.access.R
import org.iota.access.databinding.FragmentDelegationRuleBinding
import org.iota.access.databinding.ViewMakeRuleLocationBinding
import org.iota.access.databinding.ViewMakeRuleTimeBinding
import org.iota.access.delegation.RuleLimitation
import org.iota.access.delegation.RuleSatisfyType
import org.iota.access.delegation.RuleType
import org.iota.access.delegation.RulesAdapter
import org.iota.access.di.Injectable
import org.iota.access.models.rules.LocationRule
import org.iota.access.models.rules.MultipleRule
import org.iota.access.models.rules.Rule
import org.iota.access.ui.dialogs.QuestionDialogFragment
import org.iota.access.utils.Constants
import org.iota.access.utils.ui.SpinnerArrayAdapter
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class DelegationRuleFragment : BaseFragment(R.layout.fragment_delegation_rule),
        Injectable, RulesAdapter.RulesAdapterListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentDelegationRuleBinding
    private lateinit var delegationSharedViewModel: DelegationSharedViewModel
    private lateinit var viewModel: DelegationRuleViewModel

    private var disposable: CompositeDisposable? = null
    private var rulesAdapter: RulesAdapter? = RulesAdapter(this)

    private val args: Arguments by lazy { Arguments(arguments ?: Bundle()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!

        // Type of rule
        binding.spinnerTypeOfRule.adapter = SpinnerArrayAdapter(
                context,
                RuleType.getAllStringValues(resources)
        )

        // Rule satisfy type
        binding.spinnerSatisfyType.adapter = SpinnerArrayAdapter(
                context,
                RuleSatisfyType.getAllStringValues(resources)
        )

        // Rule limitation
        binding.spinnerLimitBy.adapter = SpinnerArrayAdapter(
                context,
                RuleLimitation.getAllStringValues(resources)
        )
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                clearTmpRules()
                navController.popBackStack()
            }
        })

        // Get shared view model for communicating with other fragments
        val storeOwner = navController.getViewModelStoreOwner(navController.graph.id)
        delegationSharedViewModel = ViewModelProvider(storeOwner, viewModelFactory).get(DelegationSharedViewModel::class.java)

        // Get view model charged for this fragment's view
        viewModel = ViewModelProvider(this, viewModelFactory).get(DelegationRuleViewModel::class.java)
        binding.viewModel = viewModel

        // Setup UI for editing rule if it exists
        if (args.mode == Mode.EDITING) {
            setHasOptionsMenu(true)
            binding.fab.setImageResource(R.drawable.ic_save)
            TooltipCompat.setTooltipText(binding.fab, getString(R.string.button_save))
        } else {
            TooltipCompat.setTooltipText(binding.fab, getString(R.string.button_add))
        }

        val initialRule = delegationSharedViewModel.getRule(args.ruleId)
        if (initialRule != null && viewModel.initializeWithRule(initialRule)) {
            (initialRule as? MultipleRule)?.let {
                setTmpRules(it.ruleList)
            }
        } else {
            val rules = getTmpRules()
            viewModel.setRules(rules)
        }

        viewModel.isEditingEnabled = true

        binding.fab.setOnClickListener { onFabClick() }

        bindViewModel()
    }

    override fun onDestroyView() {
        unbindViewModel()
        rulesAdapter = null
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_delegation_rule, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_delete -> {
            onDeleteOptionsItemSelected()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun onDeleteOptionsItemSelected() {
        showQuestionDialog(getString(R.string.msg_delete_rule_question), DELETE_RULE_QUESTION)
    }

    override fun onQuestionDialogAnswer(dialogTag: String, answer: QuestionDialogFragment.QuestionDialogAnswer) {
        if (dialogTag == DELETE_RULE_QUESTION) {
            onDeleteRuleConfirmed()
        }
    }

    private fun bindViewModel() {
        disposable = CompositeDisposable()

        disposable?.let {

            // rules list
            it.add(viewModel.observableRuleList
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this@DelegationRuleFragment::onRuleListChange, ::logThrowable))

            // dialog messages
            it.add(viewModel.observableShowMessage
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ message -> showInfoDialog(message) }, ::logThrowable))

            // mRule type
            it.add(viewModel.observableRuleType
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this@DelegationRuleFragment::onRuleTypeChange, ::logThrowable))

            // mRule limitation
            it.add(viewModel.observableRuleLimitation
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this@DelegationRuleFragment::onRuleLimitationChange, ::logThrowable))

            // mRule limitation time from
            it.add(viewModel.observableLimitationTimeFrom
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this@DelegationRuleFragment::onRuleLimitationTimeFromChange, ::logThrowable))

            // mRule limitation time until
            it.add(viewModel.observableLimitationTimeUntil
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this@DelegationRuleFragment::onRuleLimitationTimeUntilChange, ::logThrowable))
        }

    }

    /**
     * Logs throwable.
     */
    private fun logThrowable(t: Throwable?) = Timber.e(t)

    private fun unbindViewModel() = disposable?.dispose()

    private fun onRuleListChange(ruleList: List<Rule>) {
        rulesAdapter?.rules = ruleList
    }

    private fun onRuleTypeChange(ruleType: RuleType) {
        when (ruleType) {
            RuleType.SINGLE -> {
                binding.spinnerSatisfyType.visibility = View.GONE
                binding.labelSatisfyType.visibility = View.GONE
                binding.labelLimitBy.visibility = View.VISIBLE
                binding.spinnerLimitBy.visibility = View.VISIBLE
                viewModel.ruleLimitation?.let {
                    onRuleLimitationChange(it)
                }
            }
            RuleType.MULTIPLE -> {
                binding.spinnerSatisfyType.visibility = View.VISIBLE
                binding.labelSatisfyType.visibility = View.VISIBLE
                binding.labelLimitBy.visibility = View.GONE
                binding.spinnerLimitBy.visibility = View.GONE
                addRulesRecyclerView()
            }
        }
    }

    private fun onRuleLimitationChange(ruleLimitation: RuleLimitation) {
        if (viewModel.ruleType != RuleType.SINGLE) return
        binding.frameLayoutDefineRule.removeAllViews()
        when (ruleLimitation) {
            RuleLimitation.TIME -> {
                val timeBinding: ViewMakeRuleTimeBinding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.view_make_rule_time, null, false)
                binding.frameLayoutDefineRule.addView(timeBinding.root)
                timeBinding.buttonFrom.setOnClickListener { openDatePicker(RuleLimitation.TimeType.FROM) }
                timeBinding.buttonUntil.setOnClickListener { openDatePicker(RuleLimitation.TimeType.UNTIL) }
                onRuleLimitationTimeFromChange(viewModel.limitationTimeFrom)
                onRuleLimitationTimeUntilChange(viewModel.limitationTimeUntil)
            }
            RuleLimitation.LOCATION -> {
                val locationBinding: ViewMakeRuleLocationBinding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.view_make_rule_location, null, false)
                binding.frameLayoutDefineRule.addView(locationBinding.root)
                locationBinding.viewModel = viewModel
                val adapter: ArrayAdapter<LocationRule.LocationUnit> = SpinnerArrayAdapter(activity, LocationRule.LocationUnit.allValues())
                locationBinding.spinnerUnit.adapter = adapter
                locationBinding.spinnerUnit.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        viewModel.locationUnit = parent.getItemAtPosition(position) as LocationRule.LocationUnit
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
                locationBinding.spinnerUnit.setSelection(viewModel.locationUnit.ordinal)
            }
        }
    }

    private fun openDatePicker(timeType: RuleLimitation.TimeType) {
        if (activity == null) return
        val calendar = Calendar.getInstance()
        when (timeType) {
            RuleLimitation.TimeType.FROM -> calendar.time = viewModel.limitationTimeFrom
            RuleLimitation.TimeType.UNTIL -> calendar.time = viewModel.limitationTimeUntil
        }
        val dialog = DatePickerDialog(
                requireActivity(),
                OnDateSetListener { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int -> openTimePicker(timeType, calendar, year, month, dayOfMonth) },
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DATE])
        dialog.setOnShowListener { dialog1: DialogInterface? ->
            if (dialog1 is DatePickerDialog) {
                val positiveButton = dialog1.getButton(DatePickerDialog.BUTTON_POSITIVE)
                positiveButton?.setTextColor(Color.BLACK)
                val negativeButton = dialog1.getButton(DatePickerDialog.BUTTON_NEGATIVE)
                negativeButton?.setTextColor(Color.BLACK)
            }
        }
        dialog.show()
    }

    private fun openTimePicker(timeType: RuleLimitation.TimeType, calendar: Calendar, year: Int, month: Int, dayOfMonth: Int) {
        val dialog = TimePickerDialog(
                activity,
                OnTimeSetListener { _: TimePicker?, hourOfDay: Int, minute: Int ->
                    val date = Calendar.getInstance()
                    date[year, month, dayOfMonth, hourOfDay] = minute
                    when (timeType) {
                        RuleLimitation.TimeType.FROM -> viewModel.setLimitationTimeFrom(date.time)
                        RuleLimitation.TimeType.UNTIL -> viewModel.setLimitationTimeUntil(date.time)
                    }
                },
                calendar[Calendar.HOUR_OF_DAY],
                calendar[Calendar.MINUTE],
                true)
        dialog.setOnShowListener { dialog1: DialogInterface? ->
            if (dialog1 is TimePickerDialog) {
                val positiveButton = dialog1.getButton(TimePickerDialog.BUTTON_POSITIVE)
                positiveButton?.setTextColor(Color.BLACK)
                val negativeButton = dialog1.getButton(TimePickerDialog.BUTTON_NEGATIVE)
                negativeButton?.setTextColor(Color.BLACK)
            }
        }
        dialog.show()
    }

    private fun onRuleLimitationTimeFromChange(date: Date) {
        val timeBinding: ViewMakeRuleTimeBinding = ruleTimeBinding ?: return
        val format = SimpleDateFormat(Constants.DATE_AND_TIME_FORMAT, Locale.getDefault())
        timeBinding.buttonFrom.text = format.format(date)
    }

    private fun onRuleLimitationTimeUntilChange(date: Date) {
        val timeBinding: ViewMakeRuleTimeBinding = ruleTimeBinding ?: return
        val format = SimpleDateFormat(Constants.DATE_AND_TIME_FORMAT, Locale.getDefault())
        timeBinding.buttonUntil.text = format.format(date)
    }

    private val ruleTimeBinding: ViewMakeRuleTimeBinding?
        get() {
            if (binding.frameLayoutDefineRule.childCount < 1) return null
            val binding: ViewDataBinding? = DataBindingUtil.getBinding(binding.frameLayoutDefineRule.getChildAt(0))
            return if (binding is ViewMakeRuleTimeBinding) binding else null
        }

    private fun addRulesRecyclerView() {
        binding.frameLayoutDefineRule.removeAllViews()
        val recyclerView: RecyclerView = LayoutInflater.from(requireContext())
                .inflate(R.layout.recycler_view, binding.frameLayoutDefineRule, false)
                .findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = rulesAdapter
        rulesAdapter?.addingEnabled = viewModel.isEditingEnabled
        binding.frameLayoutDefineRule.addView(recyclerView)
    }

    override fun onAddRuleClicked() {
        val ruleId = Rule.generateId()
        viewModel.addRuleId(ruleId)
        val arguments = createArguments(ruleId, Mode.CREATING)
        navController.navigate(R.id.action_delegationRuleFragment_self, arguments)
    }

    override fun onRuleSelected(rule: Rule, type: RuleType) {
        val arguments = createArguments(rule.id, Mode.EDITING)
        navController.navigate(R.id.action_delegationRuleFragment_self, arguments)
    }

    /**
     * Called when user confirms deletion of rule.
     */
    private fun onDeleteRuleConfirmed() {
        delegationSharedViewModel.removeRule(args.ruleId)
        clearTmpRules()
        navController.popBackStack()
    }

    /**
     * Called when user taps fab "Add"/"Save" button.
     */
    private fun onFabClick() {
        val rule = viewModel.makeNewRule(args.ruleId, getTmpRules()) ?: return

        delegationSharedViewModel.putRule(args.ruleId, rule)

        clearTmpRules()
        navController.popBackStack()
    }

    private fun clearTmpRules() =
            viewModel.ruleIds.forEach { delegationSharedViewModel.removeRule(it) }

    private fun getTmpRules(): List<Rule> {
        val rules: MutableList<Rule> = mutableListOf()
        for (ruleId in viewModel.ruleIds) {
            val rule = delegationSharedViewModel.getRule(ruleId)
            if (rule != null) rules.add(rule)
            else viewModel.removeRuleId(ruleId)
        }
        return rules.toList()
    }

    private fun setTmpRules(rules: List<Rule>) =
            rules.forEach { delegationSharedViewModel.putRule(it.id, it) }

    companion object {
        private const val RULE_ID = "org.iota.access.ui.main.delegation.DelegationRuleFragment.ruleId"
        private const val MODE = "org.iota.access.ui.main.delegation.DelegationRuleFragment.mode"

        /**
         * Creates arguments necessary for this fragment.
         *
         * @param ruleId ID of "to be created" or editing rule.
         * @param mode Set fragment in [Mode.EDITING] or [Mode.CREATING] mode.
         * @return Arguments as [Bundle]
         */
        fun createArguments(
                ruleId: String = Rule.generateId(),
                mode: Mode
        ): Bundle = Bundle().apply {
            putString(RULE_ID, ruleId)
            putInt(MODE, mode.ordinal)
        }

        private const val DELETE_RULE_QUESTION: String = "deleteRuleQuestion"
    }

    enum class Mode {
        CREATING,
        EDITING
    }

    /**
     * Data class representing parsed arguments to be used in [DelegationRuleFragment].
     */
    private data class Arguments(val ruleId: String, val mode: Mode) {
        constructor(bundle: Bundle) : this(
                bundle.getString(RULE_ID, Rule.generateId()),
                Mode.values()[bundle.getInt(MODE)])
    }
}
