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

import androidx.databinding.Bindable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.iota.access.BR
import org.iota.access.BaseObservableViewModel
import org.iota.access.R
import org.iota.access.SettingsFragment
import org.iota.access.delegation.RuleLimitation
import org.iota.access.delegation.RuleSatisfyType
import org.iota.access.delegation.RuleType
import org.iota.access.di.AppSharedPreferences
import org.iota.access.models.rules.LocationRule
import org.iota.access.models.rules.MultipleRule
import org.iota.access.models.rules.Rule
import org.iota.access.models.rules.TimeRule
import org.iota.access.utils.ResourceProvider
import java.util.*
import javax.inject.Inject

class DelegationRuleViewModel @Inject constructor(
        preferences: AppSharedPreferences,
        private val mResourceProvider: ResourceProvider
) : BaseObservableViewModel() {

    var locationUnit: LocationRule.LocationUnit

    var ruleIds: MutableList<String> = mutableListOf()
        private set

    private val rulesBehavior = BehaviorSubject.createDefault<List<Rule>>(ArrayList())
    private val showMessagePublish = PublishSubject.create<String>()
    private val ruleTypeBehavior = BehaviorSubject.createDefault(RuleType.SINGLE)
    private val ruleLimitationBehavior = BehaviorSubject.createDefault(RuleLimitation.TIME)
    private val limitationTimeFromBehavior: BehaviorSubject<Date>
    private val limitationTimeUntilBehavior: BehaviorSubject<Date>

    private var rule: Rule? = null

    @get:Bindable
    var speedLimit: String? = null

    @get:Bindable
    var selectedRuleTypeIndex: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.selectedRuleTypeIndex)
            ruleTypeBehavior.onNext(RuleType.values()[value])
        }

    @get:Bindable
    var selectedRuleSatisfyTypeIndex: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.selectedRuleSatisfyTypeIndex)
        }

    @get:Bindable
    var selectedRuleLimitationIndex: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.selectedRuleLimitationIndex)
            ruleLimitationBehavior.onNext(RuleLimitation.values()[value])
        }

    @get:Bindable
    var isEditingEnabled: Boolean = false
        set(editingEnabled) {
            field = editingEnabled
            notifyPropertyChanged(BR.editingEnabled)
        }

    @get:Bindable
    var latitude: String? = null
        set(latitude) {
            field = latitude
            notifyPropertyChanged(BR.latitude)
        }

    @get:Bindable
    var longitude: String? = null
        set(longitude) {
            field = longitude
            notifyPropertyChanged(BR.longitude)
        }

    @get:Bindable
    var radius: String? = null
        set(radius) {
            field = radius
            notifyPropertyChanged(BR.radius)
        }

    val observableShowMessage: Observable<String>
        get() = showMessagePublish

    val observableRuleList: Observable<List<Rule>>
        get() = rulesBehavior

    fun setRules(rules: List<Rule>) {
        rulesBehavior.onNext(rules)
        ruleIds = rules.map { it.id }.toMutableList()
    }

    fun initializeWithRule(rule: Rule): Boolean {
        if (this.rule == null) {
            this.rule = rule
        } else {
            return false
        }

        when (rule) {
            is TimeRule -> {
                val timeRule = rule
                selectedRuleTypeIndex = RuleType.SINGLE.ordinal
                selectedRuleLimitationIndex = RuleLimitation.TIME.ordinal
                limitationTimeFromBehavior.onNext(timeRule.fromDate)
                limitationTimeUntilBehavior.onNext(timeRule.untilDate)
            }
            is LocationRule -> {
                val locationRule = rule
                selectedRuleTypeIndex = RuleType.SINGLE.ordinal
                selectedRuleLimitationIndex = RuleLimitation.LOCATION.ordinal
                latitude = locationRule.latitude.toString()
                longitude = locationRule.longitude.toString()
                radius = locationRule.radius.toString()
                locationUnit = locationRule.locationUnit
            }
            is MultipleRule -> {
                val multipleRule = rule
                ruleIds = multipleRule.ruleList.map { it.id }.toMutableList()
                selectedRuleTypeIndex = RuleType.MULTIPLE.ordinal
                selectedRuleSatisfyTypeIndex = multipleRule.ruleSatisfyType.ordinal
                rulesBehavior.onNext(multipleRule.ruleList)
            }
        }

        return true
    }

    fun addRuleId(ruleId: String) = ruleIds.add(ruleId)

    fun removeRuleId(ruleId: String) = ruleIds.remove(ruleId)

    val observableRuleType: Observable<RuleType>
        get() = ruleTypeBehavior

    val ruleType: RuleType?
        get() = ruleTypeBehavior.value

    val observableRuleLimitation: Observable<RuleLimitation>
        get() = ruleLimitationBehavior

    val ruleLimitation: RuleLimitation?
        get() = ruleLimitationBehavior.value

    val observableLimitationTimeFrom: Observable<Date>
        get() = limitationTimeFromBehavior

    val limitationTimeFrom: Date
        get() = limitationTimeFromBehavior.value ?: getInitialLimitationTimeFrom()

    val limitationTimeUntil: Date
        get() = limitationTimeUntilBehavior.value ?: getInitialLimitationTimeUntil()


    fun setLimitationTimeFrom(limitationTimeFrom: Date) {
        if (limitationTimeFrom > limitationTimeUntilBehavior.value) {
            val calendar = Calendar.getInstance()
            calendar.time = limitationTimeFrom
            calendar.add(Calendar.HOUR, 1)
            limitationTimeUntilBehavior.onNext(calendar.time)
        }
        limitationTimeFromBehavior.onNext(limitationTimeFrom)
    }

    val observableLimitationTimeUntil: Observable<Date>
        get() = limitationTimeUntilBehavior

    fun setLimitationTimeUntil(limitationTimeUntil: Date) {
        if (limitationTimeFromBehavior.value!! > limitationTimeUntil) {
            val calendar = Calendar.getInstance()
            calendar.time = limitationTimeUntil
            calendar.add(Calendar.HOUR, -1)
            limitationTimeFromBehavior.onNext(calendar.time)
        }
        limitationTimeUntilBehavior.onNext(limitationTimeUntil)
    }

    fun makeNewRule(ruleId: String, rules: List<Rule> = listOf()): Rule? {
        var rule: Rule? = null
        when (ruleTypeBehavior.value) {
            RuleType.SINGLE -> when (ruleLimitationBehavior.value) {
                RuleLimitation.TIME -> rule = createTimeRule(ruleId)
                RuleLimitation.LOCATION -> rule = validateAndCreateLocationRule(ruleId)
            }
            RuleType.MULTIPLE -> rule = createMultipleRule(ruleId, rules)
        }
        return rule
    }

    private val ruleSatisfyType
        get() =
            RuleSatisfyType.values()[selectedRuleSatisfyTypeIndex.coerceIn(RuleSatisfyType.values().indices)]

    private fun createTimeRule(ruleId: String): TimeRule = TimeRule(
            ruleId,
            limitationTimeFromBehavior.value!!,
            limitationTimeUntilBehavior.value!!)

    private fun validateAndCreateLocationRule(ruleId: String): LocationRule? {
        var message: String? = null

        val radiusFloat = radius?.toFloatOrNull()
        val longitudeFloat = longitude?.toFloatOrNull()
        val latitudeFloat = latitude?.toFloatOrNull()

        when {
            latitudeFloat == null -> message = mResourceProvider.getString(R.string.msg_empty_latitude)
            longitudeFloat == null -> message = mResourceProvider.getString(R.string.msg_empty_longitude)
            radiusFloat == null -> message = mResourceProvider.getString(R.string.msg_empty_radius)
        }

        if (message != null) {
            showMessagePublish.onNext(message)
            return null
        }

        return LocationRule(
                ruleId,
                latitudeFloat!!,
                longitudeFloat!!,
                radiusFloat!!,
                locationUnit)
    }

    private fun createMultipleRule(ruleId: String, rules: List<Rule>): MultipleRule? {
        var message: String? = null
        if (rules.isEmpty()) message = mResourceProvider.getString(R.string.error_msg_no_rules_added)
        if (message != null) {
            showMessagePublish.onNext(message)
            return null
        }
        return MultipleRule(
                ruleId,
                rules,
                ruleSatisfyType)
    }

    private fun getInitialLimitationTimeFrom(): Date = Calendar.getInstance().time

    private fun getInitialLimitationTimeUntil(): Date =
            Calendar.getInstance().apply {
                time = getInitialLimitationTimeFrom()
                add(Calendar.HOUR, 1)
            }.time

    init {
        limitationTimeFromBehavior = BehaviorSubject.createDefault(getInitialLimitationTimeFrom())
        limitationTimeUntilBehavior = BehaviorSubject.createDefault(getInitialLimitationTimeUntil())

        locationUnit = LocationRule.LocationUnit.KILOMETERS

        preferences.getString(SettingsFragment.Keys.PREF_KEY_DISTANCE_UNIT)
                ?.toIntOrNull()
                ?.let { locationUnit = LocationRule.LocationUnit.values()[it] }
    }
}
