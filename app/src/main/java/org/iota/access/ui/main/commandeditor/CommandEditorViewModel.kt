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
package org.iota.access.ui.main.commandeditor

import android.os.CountDownTimer
import android.text.TextUtils
import androidx.databinding.Bindable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.iota.access.BR
import org.iota.access.BaseObservableViewModel
import org.iota.access.R
import org.iota.access.api.model.Command
import org.iota.access.api.model.Command.CommandException
import org.iota.access.api.model.CommandAction
import org.iota.access.di.AppSharedPreferences
import org.iota.access.utils.ResourceProvider
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

class CommandEditorViewModel @Inject constructor(
        private val preferences: AppSharedPreferences,
        private val resourceProvider: ResourceProvider
) : BaseObservableViewModel() {
    private val _showDialogMessage = PublishSubject.create<String>()
    private val _showLoading = PublishSubject.create<Boolean>()
    private val _newCommand = PublishSubject.create<Command>()
    private val _snackbarMessage = PublishSubject.create<String>()

    val showDialogMessage: Observable<String>
        get() = _showDialogMessage

    val showLoading: Observable<Boolean>
        get() = _showLoading

    val newCommand: Observable<Command>
        get() = _newCommand

    val snackbarTextMessage: Observable<String>
        get() = _snackbarMessage

    @get:Bindable
    var commandName: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.commandName)
        }

    @get:Bindable
    var commandJson: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.commandJson)
        }

    fun createNewCommand() {
        if (!checkIsValid()) return
        _showLoading.onNext(true)
        object : CountDownTimer(1500, 1500) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                _showLoading.onNext(false)
                val action = CommandAction()
                action.action = commandName
                action.actionName = commandName
                action.headerName = commandName
                val newCommand: Command
                newCommand = try {
                    Command.Builder()
                            .setActionList(arrayOf(action))
                            .setActiveAction(action)
                            .setState(Command.KnownState.COMMAND_STATE_LOCKED)
                            .setDeletable(true)
                            .create()
                } catch (e: CommandException) {
                    e.printStackTrace()
                    return
                }
                saveNewCommand(newCommand)
                _newCommand.onNext(newCommand)
                _snackbarMessage.onNext(resourceProvider.getString(R.string.msg_command_successfully_created))
                commandName = null
                commandJson = null
            }
        }.start()
    }

    private fun saveNewCommand(newCommand: Command) {
        val oldList = preferences.commandList?.toMutableList() ?: mutableListOf()
        oldList.add(newCommand)
        preferences.putCommandList(oldList)
    }

    private fun checkIsValid(): Boolean {
        if (TextUtils.isEmpty(commandName)) {
            _showDialogMessage.onNext(resourceProvider.getString(R.string.msg_command_name_empty))
            return false
        }
        if (!isJSONValid(commandJson)) {
            _showDialogMessage.onNext(resourceProvider.getString(R.string.msg_command_json_invalid))
            return false
        }
        return true
    }

    /**
     * Checks if the JSON input has the correct format
     *
     * @param input the JSON input which was entered
     * @return true if JSON format is correct, false otherwise
     */
    private fun isJSONValid(input: String?): Boolean {
        if (input == null || input.isEmpty()) return false
        try {
            JSONObject(input)
        } catch (ex: JSONException) {
            try {
                JSONArray(input)
            } catch (ex1: JSONException) {
                return false
            }
        }
        return true
    }

}
