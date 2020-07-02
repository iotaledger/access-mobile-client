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

import android.os.Parcelable
import android.util.Pair
import androidx.databinding.Bindable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.iota.access.BR
import org.iota.access.CommunicationViewModel
import org.iota.access.R
import org.iota.access.api.APILibDacAuthNative
import org.iota.access.api.Communicator
import org.iota.access.api.PSService
import org.iota.access.api.model.policy_server.PSDelegatePolicyRequest
import org.iota.access.api.model.policy_server.PSEmptyResponse
import org.iota.access.data.DataProvider
import org.iota.access.extensions.toBase64
import org.iota.access.models.*
import org.iota.access.models.rules.ExecuteNumberRule
import org.iota.access.models.rules.Rule
import org.iota.access.user.UserManager
import org.iota.access.utils.Constants
import org.iota.access.utils.Optional
import org.iota.access.utils.ResourceProvider
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class DelegationViewModel @Inject constructor(
        communicator: Communicator,
        resourceProvider: ResourceProvider,
        dataProvider: DataProvider,
        private val authNative: APILibDacAuthNative,
        private val userManager: UserManager,
        private val psService: PSService
) : CommunicationViewModel(communicator, resourceProvider) {

    val allActions: List<DelegationAction> = dataProvider.availableActions
    val allDelegationUsers: List<DelegationUser> = dataProvider.availableUsers
    val allObligations: List<DelegationObligation> = dataProvider.availableObligations

    var nestedScrollSavedInstance: Parcelable? = null

    val rulesGrantId = Rule.generateId()
    val rulesDenyId = Rule.generateId()

    private val deviceId: String = dataProvider.deviceId

    private val selectedUsers = BehaviorSubject.createDefault<Set<DelegationUser>>(HashSet())
    private val _delegationActionList = BehaviorSubject.createDefault<List<DelegationAction>>(ArrayList())
    private val maxNumOfExecutions = BehaviorSubject.createDefault(Optional<Int>(null))

    private var requestsDisposable: CompositeDisposable? = null
    private var requests: MutableList<Observable<PSEmptyResponse>>? = null

    private val obligationGrant: DelegationObligation?
        get() = if (selectedObligationGrantIndex == 0) null else allObligations[selectedObligationGrantIndex - 1]

    private val obligationDeny: DelegationObligation?
        get() = if (selectedObligationDenyIndex == 0) null else allObligations[selectedObligationDenyIndex - 1]

    val observableDelegationActionList: Observable<List<DelegationAction>>
        get() = _delegationActionList

    val delegationActionList: List<DelegationAction>
        get() = _delegationActionList.value!!

    fun setDelegationList(delegationActionList: List<DelegationAction>) =
            _delegationActionList.onNext(delegationActionList)

    val observableMaxNumOfExecutions: Observable<Optional<Int>>
        get() = maxNumOfExecutions

    fun setMaxNumOfExecutions(value: Int?) = maxNumOfExecutions.onNext(Optional(value))

    @get:Bindable
    var selectedObligationGrantIndex: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.selectedObligationGrantIndex)
        }

    @get:Bindable
    var selectedObligationDenyIndex: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.selectedObligationDenyIndex)
        }

    @get:Bindable
    var selectedCostIndex: Int = 0
        set(selectedCostIndex) {
            field = selectedCostIndex
            notifyPropertyChanged(BR.selectedCostIndex)
        }

    val observableSelectedUsers: Observable<Set<DelegationUser>>
        get() = selectedUsers

    var selectedDelegationUsers: Set<DelegationUser>
        get() = selectedUsers.value!!
        set(selectedUsers) {
            this.selectedUsers.onNext(selectedUsers)
        }

    fun delegate(gocRule: Rule?, docRule: Rule?) {
        val user = userManager.user
        if (user == null) {
            mShowDialogMessage.onNext(resourceProvider.getString(R.string.error_msg_user_must_be_logged_in))
            return
        }
        if (selectedUsers.value?.size ?: 0 == 0) {
            mShowDialogMessage.onNext(resourceProvider.getString(R.string.error_msg_no_users_selected))
            return
        }
        if (_delegationActionList.value?.size ?: 0 == 0) {
            mShowDialogMessage.onNext(resourceProvider.getString(R.string.error_msg_no_actions_selected))
            return
        }

        mShowLoading.onNext(Pair(true, resourceProvider.getString(R.string.msg_delegating)))

        requests = ArrayList()

        val ownerId = user.publicId
        val privateKey = user.privateKey

        for (action in delegationActionList) {
            val policy = createPolicy(false, action, gocRule, docRule)

            val request = createDelegatePolicyRequest(policy, ownerId, privateKey)

            requests?.add(psService
                    .delegatePolicy(request)
                    .subscribeOn(Schedulers.io()))
        }
        if (requestsDisposable != null) {
            requestsDisposable!!.dispose()
        } else {
            requestsDisposable = CompositeDisposable()
        }

        val combined = Observable.zip(requests) { responses: Array<Any?> ->
            var isSuccess = true
            for (obj in responses) {
                if (obj !is PSEmptyResponse) return@zip false
                isSuccess = !obj.isError && isSuccess
                Timber.d(obj.toString())
            }
            isSuccess
        }

        requestsDisposable?.add(combined
                .subscribeOn(Schedulers.io())
                .subscribe( // on next
                        { isSuccessful: Boolean ->
                            if (requestsDisposable != null) requestsDisposable!!.dispose()
                            requestsDisposable = null
                            mShowLoading.onNext(Pair(false, null))
                            if (isSuccessful) {
                                mShowDialogMessage.onNext(resourceProvider.getString(R.string.msg_delegating_success))
                            } else {
                                mShowDialogMessage.onNext(resourceProvider.getString(R.string.error_msg_delegation_failed))
                            }
                        }  // on error
                ) { throwable: Throwable ->
                    if (requestsDisposable != null) requestsDisposable!!.dispose()
                    requestsDisposable = null
                    mShowLoading.onNext(Pair(false, null))
                    mShowDialogMessage.onNext(throwable.message ?: "Unknown error")
                })
    }

    /**
     * Creates policy.
     *
     * @param obfuscate If `true`, policyId and publicId will be obfuscated.
     * If `false`, policyId and publicId will not be obfuscated.
     * @param action Action to be delegated.
     * @param gocRule Rule for policy GoC.
     * @param docRule Rule for policy DoC.
     */
    fun createPolicy(
            obfuscate: Boolean,
            action: DelegationAction? = null,
            gocRule: Rule? = null,
            docRule: Rule? = null
    ): Policy {
        val gocAttrList: MutableList<PolicyAttributeList> = mutableListOf()

        // Add GoC rule to GoC
        gocRule?.let {
            gocAttrList.add(it.build())
        }

        // Add users to GoC
        createUsersPolicyAttribute(obfuscate)?.let { usersAttr ->
            gocAttrList.add(usersAttr)
        }

        // Add action to GoC
        action?.build()?.let { actionAttr ->
            gocAttrList.add(actionAttr)
        }

        // Add number of executions to GoC
        val optNumOfExecutions = maxNumOfExecutions.value
        if (optNumOfExecutions != null && !optNumOfExecutions.isEmpty) {
            val numOfExecutionsAttr = ExecuteNumberRule(optNumOfExecutions.get()).build()
            gocAttrList.add(numOfExecutionsAttr)
        }

        val policyGoc = PolicyAttributeLogical(gocAttrList, PolicyAttributeLogical.LogicalOperator.AND)

        val policyDoc = docRule?.build()

        val obligationDeny = this.obligationDeny
        val polOblDenyList =
                if (obligationDeny != null) PolicyObligationList(listOf(obligationDeny.build()))
                else PolicyObligationEmpty

        val obligationGrant = this.obligationGrant
        val polOblGrantList =
                if (obligationGrant != null) PolicyObligationList(listOf(obligationGrant.build()))
                else PolicyObligationEmpty

        val policyObject = PolicyObject(
                obligationDeny = polOblDenyList,
                obligationGrant = polOblGrantList,
                policyDoc = policyDoc ?: PolicyAttributeEmpty,
                policyGoc = policyGoc
        )

        val cost = COST_VALUES[selectedCostIndex]
        return Policy("sha-256", policyObject, cost.toString(), if (obfuscate) "**********" else null)
    }

    /**
     * Created [PSDelegatePolicyRequest] object to be send as a request to policy store server.
     *
     * @param policy Policy.
     * @param ownerId ID of policy owner.
     * @param privateKey Key to be used for signing policy.
     */
    private fun createDelegatePolicyRequest(
            policy: Policy,
            ownerId: String,
            privateKey: ByteArray
    ): PSDelegatePolicyRequest {
        val message = policy.policyId //JSONObject(policy.toMap()).toString()
        val messageByteArray = message.toByteArray(Charsets.UTF_8)

        val signature = authNative.cryptoSign(messageByteArray, messageByteArray.size, privateKey)
        val signatureBase64 = signature.toBase64()

        return PSDelegatePolicyRequest(ownerId, deviceId, policy.toMap(), signatureBase64)
    }

    /**
     * Creates policy attributes for every user.
     *
     * @param obfuscate if true, public_id of every user will be replaced with **********
     * @return policy attribute
     */
    private fun createUsersPolicyAttribute(obfuscate: Boolean): PolicyAttributeList? {
        val selectedUsers = selectedUsers.value ?: return null
        if (selectedUsers.isEmpty()) return null

        return if (selectedUsers.size == 1) {
            val user = selectedUsers.iterator().next()
            user.policyAttrList(obfuscate)
        } else {
            val list: MutableList<PolicyAttributeList> = mutableListOf()
            for (user in selectedUsers) {
                list.add(user.policyAttrList(obfuscate))
            }
            PolicyAttributeLogical(list, PolicyAttributeLogical.LogicalOperator.OR)
        }
    }

    companion object {
        val COST_VALUES = floatArrayOf(0f, 50f / Constants.TOKEN_SCALE_FACTOR, 100f / Constants.TOKEN_SCALE_FACTOR, 150f / Constants.TOKEN_SCALE_FACTOR)
    }
}
