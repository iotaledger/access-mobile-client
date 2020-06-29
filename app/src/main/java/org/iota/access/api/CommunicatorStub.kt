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
package org.iota.access.api

import android.os.CountDownTimer
import android.util.Pair
import com.google.gson.Gson
import com.google.gson.JsonArray
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.iota.access.api.model.CommunicationMessage
import org.iota.access.api.model.TCPResponse
import org.iota.access.api.tcp.TCPClient.TCPError
import org.iota.access.models.DelegationUser
import org.iota.access.models.GetUserIdResponse
import org.iota.access.models.User
import org.iota.access.models.UserUtils.getDefaultUser
import org.iota.access.utils.JSONUtils.extractJsonElement
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunicatorStub @Inject constructor(private val mTsService: TSService, private val mPsService: PSService) : Communicator {
    private var mDataset = JsonArray()
    private val mResponse = PublishSubject.create<Pair<String, String>>()
    private val mTCPError = PublishSubject.create<TCPError>()
    private val rand = Random()
    private val gson = Gson()
    override fun sendTCPMessage(message: String) {
        val millis = rand.nextInt(1000) + 500
        object : CountDownTimer(millis.toLong(), millis.toLong()) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                stubTCPMessage(message)
            }
        }.start()
    }

    private fun stubTCPMessage(message: String) {
        val cmd = CommunicationMessage.getCmdFromMessage(message)
        if (cmd != null) {
            when (cmd) {
                CommunicationMessage.GET_POLICY_LIST -> mResponse.onNext(Pair(message, "[{\"policy_id\":\"531565B8F40ECA5AAC55FEE6664A233E2D0312856B777D875EB9ACC4F85D1E99\",\"action\":\"action#1\"},{\"policy_id\":\"45C88599131156AED16B48FB6A57B8211F24B92877076164AC5D143A81730645\",\"action\":\"action#2\"}]"))
                CommunicationMessage.RESOLVE -> mResponse.onNext(Pair(message, "{\"response\":\"access granted\"}"))
                CommunicationMessage.SET_DATA_SET -> {
                    val jsonElement = extractJsonElement(message) ?: return
                    try {
                        mDataset = jsonElement.asJsonObject["dataset_list"].asJsonArray
                        mResponse.onNext(Pair(message, "{\"response\":\"access granted\"}"))
                    } catch (e: IllegalStateException) {
                        mResponse.onNext(Pair(message, "{\"response\":\"access denied\"}"))
                    } catch (e: UnsupportedOperationException) {
                        mResponse.onNext(Pair(message, "{\"response\":\"access denied\"}"))
                    }
                }
                CommunicationMessage.GET_DATA_SET -> mResponse.onNext(Pair(message, mDataset.toString()))
                CommunicationMessage.GET_AUTH_USER_ID -> {
                    val response = TCPResponse<GetUserIdResponse>()
                    response.setError(0)
                    response.message = "success"
                    val r = GetUserIdResponse()
                    r.userId = "123"
                    response.data = r
                    mResponse.onNext(Pair(message, gson.toJson(response)))
                }
                CommunicationMessage.GET_USER -> {
                    val response = TCPResponse<User>()
                    response.setError(0)
                    response.message = "success"
                    val user = getDefaultUser("jamie")
                    mResponse.onNext(Pair(message, gson.toJson(response)))
                }
                CommunicationMessage.GET_ALL_USERS -> {
                    val tcpResponse = TCPResponse<List<DelegationUser>>()
                    val users: MutableList<DelegationUser> = ArrayList()
                    users.add(DelegationUser("jamie", "3c9d985c5d630e6e02f676997c5e9f03b45c6b7529b2491e8de03c18af3c9d87f0a65ecb5dd8f390dee13835354b222df414104684ce9f1079a059f052ca6e51"))
                    users.add(DelegationUser("charlie", "3c9d985c5d630e6e02f676997c5e9f03b45c6b7529b2491e8de03c18af3c9d87f0a65ecb5dd8f390dee13835354b222df414104684ce9f1079a059f052ca6e51"))
                    users.add(DelegationUser("alex", "3c9d985c5d630e6e02f676997c5e9f03b45c6b7529b2491e8de03c18af3c9d87f0a65ecb5dd8f390dee13835354b222df414104684ce9f1079a059f052ca6e51"))
                    users.add(DelegationUser("richard", "3c9d985c5d630e6e02f676997c5e9f03b45c6b7529b2491e8de03c18af3c9d87f0a65ecb5dd8f390dee13835354b222df414104684ce9f1079a059f052ca6e51"))
                    tcpResponse.setError(0)
                    tcpResponse.message = "success"
                    tcpResponse.data = users
                    mResponse.onNext(Pair(message, gson.toJson(tcpResponse)))
                }
                else -> mResponse.onNext(Pair(message, ""))
            }
        }
    }

    override fun getObservableTCPResponse(): Observable<Pair<String, String>> {
        return mResponse
    }

    override fun getObservableTCPError(): Observable<TCPError> {
        return mTCPError
    }

    override fun disconnectTCP() {}

}
