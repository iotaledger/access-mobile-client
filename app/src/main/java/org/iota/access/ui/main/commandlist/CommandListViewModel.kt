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

import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Pair
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.iota.access.CommunicationViewModel
import org.iota.access.R
import org.iota.access.api.Communicator
import org.iota.access.api.PSService
import org.iota.access.api.asr.ASRClient
import org.iota.access.api.model.CommandAction
import org.iota.access.api.model.CommunicationMessage
import org.iota.access.api.model.TCPResponse
import org.iota.access.api.model.policy_server.PSClearPolicyListRequest
import org.iota.access.api.model.policy_server.PSEmptyResponse
import org.iota.access.api.model.token_server.TSEmptyResponse
import org.iota.access.api.model.token_server.TSFundRequest
import org.iota.access.api.model.token_server.TSSendRequest
import org.iota.access.api.tcp.TCPClient.TCPError
import org.iota.access.data.DataProvider
import org.iota.access.user.UserManager
import org.iota.access.utils.JSONUtils
import org.iota.access.utils.Optional
import org.iota.access.utils.ResourceProvider
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class CommandListViewModel
@Inject constructor(
        communicator: Communicator?,
        resourceProvider: ResourceProvider,
        private val dataProvider: DataProvider,
        private val asrClient: ASRClient,
        private val userManager: UserManager,
        private val psService: PSService,
        private val gson: Gson
) : CommunicationViewModel(communicator, resourceProvider) {

    private val _commandList = BehaviorSubject.createDefault(Optional<List<CommandAction>>(null))
    private val _showRefresh = PublishSubject.create<Boolean>()
    private var mAccountRefillCall: Call<TSEmptyResponse>? = null
    private var mSendTokenResponseBodyCall: Call<TSEmptyResponse>? = null
    private var invokedCommand: CommandAction? = null
    private var policyIdToEnable: String? = null

    var isPolicyRequested = false
        private set

    val observableCommandList: Observable<Optional<List<CommandAction>>>
        get() = _commandList

    val commandList: Optional<List<CommandAction>>
        get() = _commandList.value!! // always has a value because behavior subject is created with default value

    val showRefresh: Observable<Boolean>
        get() = _showRefresh

    /**
     * Asynchronously sends command to board to executeCommand it.
     *
     * @param command command which should be executed
     */
    fun executeCommand(command: CommandAction) {
        val user = userManager.user ?: return
        invokedCommand = command
        val policy = command.policyId
        sendTCPMessage(
                CommunicationMessage.makeResolvePolicyRequest(policy, user.publicId),
                resourceProvider.getString(R.string.msg_executing_command, command.actionName))
    }

    fun getPolicyList() {
        val user = userManager.user ?: return
        isPolicyRequested = true
        _showRefresh.onNext(true)
        val userId = user.publicId
        sendTCPMessage(CommunicationMessage.makePolicyListRequest(userId))
    }

    fun enablePolicy(policyId: String) {
        val user = userManager.user ?: return
        policyIdToEnable = policyId
        sendTCPMessage(CommunicationMessage.makeEnablePolicyRequest(policyId, user.publicId))
    }

    fun clearUserList() {
        sendTCPMessage(CommunicationMessage.makeClearAllUsersRequest(), resourceProvider.getString(R.string.msg_clearing_users))
    }

    /**
     * Send clear policy list command to server.
     */
    fun clearPolicyList() {
        mShowLoading.onNext(Pair(true, resourceProvider.getString(R.string.msg_policy_list_clearing)))
        psService.clearPolicyList(PSClearPolicyListRequest(dataProvider.deviceId))
                .enqueue(object : Callback<PSEmptyResponse?> {
                    override fun onResponse(call: Call<PSEmptyResponse?>, response: Response<PSEmptyResponse?>) {
                        // check if request was done
                        mShowLoading.onNext(Pair(false, null))
                        if (response.isSuccessful) {
                            mSnackbarMessage.onNext(resourceProvider.getString(R.string.msg_policy_list_cleared_successfully))
                        } else {
                            val message = """
                                Response code: ${response.code()}
                                Reason: ${response.message()}
                                """.trimIndent()
                            Timber.e(message)
                            mSnackbarMessage.onNext(resourceProvider.getString(R.string.msg_policy_list_cleared_error))
                        }
                    }

                    override fun onFailure(call: Call<PSEmptyResponse?>, t: Throwable) {
                        mSnackbarMessage.onNext(t.message ?: "Unknown error occurred")
                        mShowLoading.onNext(Pair(false, null))
                    }
                })
    }

    val asrIntent: Intent
        get() = asrClient.generateIntent()

    fun onAsrResult(data: Intent) {
        val text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (text != null && text.size > 0) {
            val matchingCommand = findCommandByCommandName(text[0])
            if (matchingCommand != null) {
                executeCommand(matchingCommand)
            } else {
                mSnackbarMessage.onNext(resourceProvider.getString(R.string.unknown_command))
            }
        }
    }

    /**
     * Tries to find command by it's action name.
     *
     * @param inputAction Name of action which is being searched in list of available actions.
     * @return If found, returns command which contains passed action, otherwise return null.
     */
    private fun findCommandByCommandName(inputAction: String): CommandAction? {
        if (_commandList.value!!.isEmpty) return null
        for (command in _commandList.value!!.get()!!) {
            if (command.action.equals(inputAction, ignoreCase = true)) {
                return command
            }
        }
        return null
    }

    override fun handleTCPError(error: TCPError) {
        super.handleTCPError(error)
        _showRefresh.onNext(false)
    }

    override fun handleTCPResponse(sentMessage: String, response: String) {
        super.handleTCPResponse(sentMessage, response)
        _showRefresh.onNext(false)
        val cmd = CommunicationMessage.getCmdFromMessage(sentMessage)
        val jsonElement = JSONUtils.extractJsonElement(response) ?: return
        if (cmd != null) {
            when (cmd) {
                CommunicationMessage.CLEAR_ALL_USERS -> try {
                    val tcpResponse = gson.fromJson<TCPResponse<Any>>(jsonElement,
                            object : TypeToken<TCPResponse<Any?>?>() {}.type)
                    if (tcpResponse.isSuccessful) {
                        mShowDialogMessage.onNext(resourceProvider.getString(R.string.msg_users_list_cleared_successfully))
                    } else {
                        var message = tcpResponse.message
                        if (message == null) message = resourceProvider.getString(R.string.something_wrong_happened)
                        mShowDialogMessage.onNext(message!!)
                    }
                } catch (ignored: JsonSyntaxException) {
                    mShowDialogMessage.onNext(resourceProvider.getString(R.string.msg_unable_to_clear_users))
                }
                CommunicationMessage.ENABLE_POLICY -> if (policyIdToEnable != null) {
                    enablePolicyAfterServerResponse(policyIdToEnable!!)
                    policyIdToEnable = null
                }
                CommunicationMessage.GET_POLICY_LIST -> try {
                    val jsonArray = jsonElement.asJsonArray
                    val actionList = CommandAction.parseFromJSONArray(JSONArray(jsonArray.toString()), resourceProvider)
                    _commandList.onNext(Optional(actionList))
                    return
                } catch (ignored: IllegalStateException) {
                }
                CommunicationMessage.RESOLVE -> if (invokedCommand != null) {
//                    val resolvedResponse = resolveResponse(response)
//                    if (resolvedResponse != null) {
//                        handleNewStatus(resolvedResponse, mInvokedCommand!!)
//                    }
                    invokedCommand = null
                }
            }
        }
    }

    private fun enablePolicyAfterServerResponse(policyId: String) {
        if (_commandList.value!!.isEmpty) return
        val commandList = _commandList.value!!.get()
        val index = commandList.indexOfFirst { it.policyId == policyId }
        if (index != -1) {
            commandList[index].isPaid = true
        }
        _commandList.onNext(Optional(commandList))
    }

    companion object {
        const val REFILL_AMOUNT = 1
    }

}
