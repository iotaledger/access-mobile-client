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

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.iota.access.BaseObservableViewModel
import org.iota.access.models.Policy
import org.iota.access.models.rules.Rule
import javax.inject.Inject

class DelegationSharedViewModel @Inject constructor() : BaseObservableViewModel() {
    private val rules: BehaviorSubject<MutableMap<String, Rule>> = BehaviorSubject.createDefault(HashMap())

    var previewingPolicy: Policy? = null

    val observableRules: Observable<Map<String, Rule>>
        get() = rules.map { it.toMap() }

    fun getRule(ruleId: String): Rule? = rules.value?.get(ruleId)

    fun putRule(ruleId: String, rule: Rule) =
            rules.value?.let {
                it[ruleId] = rule
                rules.onNext(it)
            }

    fun removeRule(ruleId: String) =
            rules.value?.let {
                it.remove(ruleId)
                rules.onNext(it)
            }

}
