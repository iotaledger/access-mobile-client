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
import org.iota.access.api.TSService
import org.iota.access.api.asr.ASRClient
import org.iota.access.api.model.*
import org.iota.access.api.model.policy_server.PSClearPolicyListRequest
import org.iota.access.api.model.policy_server.PSEmptyResponse
import org.iota.access.api.model.token_server.TSEmptyResponse
import org.iota.access.api.model.token_server.TSFundRequest
import org.iota.access.api.model.token_server.TSSendRequest
import org.iota.access.api.tcp.TCPClient.TCPError
import org.iota.access.data.DataProvider
import org.iota.access.user.UserManager
import org.iota.access.utils.Constants
import org.iota.access.utils.JsonUtils
import org.iota.access.utils.Optional
import org.iota.access.utils.ResourceProvider
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class CommandListViewModel @Inject constructor(communicator: Communicator?,
                                               private val dataProvider: DataProvider,
                                               resourceProvider: ResourceProvider?,
                                               private val asrClient: ASRClient,
                                               private val userManager: UserManager,
                                               private val tsService: TSService,
                                               private val psService: PSService,
                                               private val gson: Gson
) : CommunicationViewModel(communicator, resourceProvider) {

    private val _commandList = BehaviorSubject.createDefault(Optional<List<Command>?>(null))
    private val _showRefresh = PublishSubject.create<Boolean>()
    private var mAccountRefillCall: Call<TSEmptyResponse>? = null
    private var mSendTokenResponseBodyCall: Call<TSEmptyResponse>? = null
    private var mInvokedCommand: Command? = null
    private var policyIdToEnable: String? = null

    var isPolicyRequested = false
        private set

    val observableCommandList: Observable<Optional<List<Command>?>>
        get() = _commandList

    val commandList: Optional<List<Command>?>
        get() = _commandList.value!! // always has a value because behavior subject is created with default value

    val showRefresh: Observable<Boolean>
        get() = _showRefresh

    /**
     * Asynchronously sends command to board to executeCommand it.
     *
     * @param command command which should be executed
     */
    fun executeCommand(command: Command) {
        mInvokedCommand = command
        val policy = command.activeAction.policyId
        sendTCPMessage(CommunicationMessage.makeResolvePolicyRequest(policy), mResourceProvider.getString(R.string.msg_executing_command, command.actionName))
    }

    val policyList: Unit
        get() {
            if (userManager.user == null) return
            isPolicyRequested = true
            _showRefresh.onNext(true)
            val userId = userManager.user!!.publicId
            sendTCPMessage(CommunicationMessage.makePolicyListRequest(userId))
        }

    fun enablePolicy(policyId: String?) {
        policyIdToEnable = policyId
        sendTCPMessage(CommunicationMessage.makeEnablePolicyRequest(policyId))
    }

    fun payPolicy(requestBody: TSSendRequest?, policyId: String?) {
        mSendTokenResponseBodyCall = tsService.sendTokens(requestBody)
        mSendTokenResponseBodyCall?.enqueue(object : Callback<TSEmptyResponse?> {
            override fun onResponse(call: Call<TSEmptyResponse?>, response: Response<TSEmptyResponse?>) {
                mShowLoading.onNext(Pair(false, null))
                when {
                    response.body() == null -> {
                        mShowDialogMessage.onNext(mResourceProvider.getString(R.string.msg_unable_to_pay))
                    }
                    response.body()!!.isError -> {
                        mShowDialogMessage.onNext(response.body()!!.message)
                    }
                    else -> {
                        enablePolicy(policyId)
                    }
                }
            }

            override fun onFailure(call: Call<TSEmptyResponse?>, t: Throwable) {
                mShowLoading.onNext(Pair(false, null))
                mShowDialogMessage.onNext(t.message ?: "Unknown error occurred")
            }
        })
        mShowLoading.onNext(Pair(true, mResourceProvider.getString(R.string.msg_paying)))
    }

    fun refillAccount(walletId: String?) {
        mAccountRefillCall = tsService.fundAccount(TSFundRequest(walletId, REFILL_AMOUNT))
        mAccountRefillCall?.enqueue(object : Callback<TSEmptyResponse?> {
            override fun onResponse(call: Call<TSEmptyResponse?>, response: Response<TSEmptyResponse?>) {
                mShowLoading.onNext(Pair(false, null))
                when {
                    response.body() == null -> {
                        mShowDialogMessage.onNext(mResourceProvider.getString(R.string.msg_refilling_failed))
                    }
                    response.body()!!.isError -> {
                        mShowDialogMessage.onNext(response.body()!!.message)
                    }
                    else -> {
                        mShowDialogMessage.onNext(mResourceProvider.getString(R.string.msg_refilling_successful))
                    }
                }
            }

            override fun onFailure(call: Call<TSEmptyResponse?>, t: Throwable) {
                mShowLoading.onNext(Pair(false, null))
                mShowDialogMessage.onNext(t.message ?: "Unknown error occurred")
            }
        })
        mShowLoading.onNext(Pair(true, mResourceProvider.getString(R.string.msg_refilling_account)))
    }

    fun clearUserList() {
        sendTCPMessage(CommunicationMessage.makeClearAllUsersRequest(), mResourceProvider.getString(R.string.msg_clearing_users))
    }

    /**
     * Send clear policy list command to server.
     */
    fun clearPolicyList() {
        mShowLoading.onNext(Pair(true, mResourceProvider.getString(R.string.msg_policy_list_clearing)))
        psService.clearPolicyList(PSClearPolicyListRequest(dataProvider.deviceId))
                .enqueue(object : Callback<PSEmptyResponse?> {
                    override fun onResponse(call: Call<PSEmptyResponse?>, response: Response<PSEmptyResponse?>) {
                        // check if request was done
                        mShowLoading.onNext(Pair(false, null))
                        if (response.isSuccessful) {
                            mSnackbarMessage.onNext(mResourceProvider.getString(R.string.msg_policy_list_cleared_successfully))
                        } else {
                            val message = """
                                Response code: ${response.code()}
                                Reason: ${response.message()}
                                """.trimIndent()
                            Timber.e(message)
                            mSnackbarMessage.onNext(mResourceProvider.getString(R.string.msg_policy_list_cleared_error))
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
                mSnackbarMessage.onNext(mResourceProvider.getString(R.string.unknown_command))
            }
        }
    }

    /**
     * Tries to find command by it's action name.
     *
     * @param inputAction Name of action which is being searched in list of available actions.
     * @return If found, returns command which contains passed action, otherwise return null.
     */
    private fun findCommandByCommandName(inputAction: String): Command? {
        if (_commandList.value!!.isEmpty) return null
        for (command in _commandList.value!!.get()!!) {
            for (action in command.actionList) {
                if (inputAction.equals(action.actionName, ignoreCase = true)) {
                    command.activeAction = action
                    return command
                }
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
        val jsonElement = JsonUtils.extractJsonElement(response) ?: return
        if (cmd != null) {
            when (cmd) {
                CommunicationMessage.CLEAR_ALL_USERS -> try {
                    val tcpResponse = gson.fromJson<TCPResponse<Any>>(jsonElement,
                            object : TypeToken<TCPResponse<Any?>?>() {}.type)
                    if (tcpResponse.isSuccessful) {
                        mShowDialogMessage.onNext(mResourceProvider.getString(R.string.msg_users_list_cleared_successfully))
                    } else {
                        var message = tcpResponse.message
                        if (message == null) message = mResourceProvider.getString(R.string.something_wrong_happened)
                        mShowDialogMessage.onNext(message!!)
                    }
                } catch (ignored: JsonSyntaxException) {
                    mShowDialogMessage.onNext(mResourceProvider.getString(R.string.msg_unable_to_clear_users))
                }
                CommunicationMessage.ENABLE_POLICY -> if (policyIdToEnable != null) {
                    enablePolicyAfterServerResponse(policyIdToEnable!!)
                    policyIdToEnable = null
                }
                CommunicationMessage.GET_POLICY_LIST -> try {
                    val jsonArray = jsonElement.asJsonArray
                    val actionList = CommandAction.getListOfActions(jsonArray, mResourceProvider)
                    val commandList = Command.makeListOfCommands(actionList, mResourceProvider)
                    _commandList.onNext(Optional(commandList))
                    return
                } catch (ignored: IllegalStateException) {
                }
                CommunicationMessage.RESOLVE -> if (mInvokedCommand != null) {
                    val resolvedResponse = resolveResponse(response)
                    if (resolvedResponse != null) {
                        handleNewStatus(resolvedResponse, mInvokedCommand!!)
                    }
                    mInvokedCommand = null
                }
            }
        }
    }

    private fun enablePolicyAfterServerResponse(policyId: String) {
        if (_commandList.value!!.isEmpty) return
        val commandList = _commandList.value!!.get()
        var i = 0
        val n = commandList!!.size
        while (i < n) {
            var j = 0
            val m = commandList[i].actionList.size
            while (j < m) {
                if (commandList[i].actionList[j].policyId.equals(policyId, ignoreCase = true)) {
                    commandList[i].actionList[j].isPaid = true
                }
                j++
            }
            i++
        }
        _commandList.onNext(Optional(commandList))
    }

    private fun resolveResponse(message: String): String? {
        try {
            val jsonObject = JSONObject(message)
            if (jsonObject.has(JSON_RESPONSE_TAG)) {
                return jsonObject.getString(JSON_RESPONSE_TAG)
            }
        } catch (ignored: JSONException) {
        }
        return null
    }

    /**
     * Parses UDP message and changes status of sensors.
     *
     * @param message message received through UDP.
     */
    private fun handleNewStatus(message: String) {
        if (_commandList.value!!.isEmpty) return
        try {
            val udpResponse = gson.fromJson(message, UDPResponse::class.java)
            val commandList = _commandList.value!!.get()
            if (commandList != null && commandList.isNotEmpty()) {
                for (i in commandList.indices) {
                    val action = commandList[i].activeAction.action
                    if ((Constants.ACTION_CLOSE_DOOR.equals(action, ignoreCase = true)
                                    || Constants.ACTION_OPEN_DOOR.equals(action, ignoreCase = true))
                            && DOOR_COMPONENT.equals(udpResponse.component, ignoreCase = true)) {
                        commandList[i].state = udpResponse.state
                    } else if (Constants.ACTION_OPEN_TRUNK.equals(action, ignoreCase = true)
                            && TRUNK_COMPONENT.equals(udpResponse.component, ignoreCase = true)) {
                        commandList[i].state = udpResponse.state
                    }
                }
                _commandList.onNext(Optional(commandList))
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * Changes status of sensors based on board's command response.
     *
     * @param message response received from device.
     * @param command command that was sent to device.
     */
    private fun handleNewStatus(message: String, command: Command) {
        if (!ACCESS_GRANTED.equals(message, ignoreCase = true)) return
        if (_commandList.value!!.isEmpty) return
        val commandList = _commandList.value!!.get()
        if (commandList!!.isNotEmpty()) {
            for (i in commandList.indices) {
                if (command == commandList[i]) {
                    commandList[i].didExecute(command.activeAction)
                }
            }
            _commandList.onNext(Optional(commandList))
        }
    }

    companion object {
        const val REFILL_AMOUNT = 1
        private const val DOOR_COMPONENT = "door"
        private const val TRUNK_COMPONENT = "trunk"
        private const val JSON_RESPONSE_TAG = "response"
        private const val ACCESS_GRANTED = "access granted"
    }

}
