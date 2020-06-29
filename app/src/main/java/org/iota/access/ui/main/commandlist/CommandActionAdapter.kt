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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.iota.access.R
import org.iota.access.api.model.CommandAction
import org.iota.access.databinding.CommandListItemBinding
import java.lang.ref.WeakReference

/**
 * Adapter for the command action list.
 */
class CommandActionAdapter constructor(
        private val commands: MutableList<CommandAction>,
        listener: CommandActionAdapterListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class CommandViewHolder(binding: CommandListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val commandName: TextView = binding.textCommandName
        private val commandImage: ImageView = binding.imgStatus
        private val commandButton: Button = binding.buttonCommand
        private val paidTextView: TextView = binding.isPaidTextView
        private val deleteButton: ImageButton = binding.buttonDelete

        fun bind(commandAction: CommandAction, listener: WeakReference<CommandActionAdapterListener>) {
            commandButton.text = commandAction.actionName
            commandName.text = commandAction.headerName
            commandImage.setImageResource(commandAction.imageResId)
            commandButton.setOnClickListener { listener.get()?.onCommandSelected(commandAction) }
            deleteButton.setOnClickListener { listener.get()?.onCommandDeleteClick(commandAction) }
            deleteButton.visibility = if (commandAction.deletable) View.VISIBLE else View.GONE
            paidTextView.visibility = if (commandAction.isPaid) View.GONE else View.VISIBLE
        }
    }

    interface CommandActionAdapterListener {
        fun onCommandSelected(commandAction: CommandAction)
        fun onCommandDeleteClick(commandAction: CommandAction)
    }

    private val listener: WeakReference<CommandActionAdapterListener> = WeakReference(listener)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: CommandListItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.command_list_item,
                parent,
                false)
        return CommandViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val commandHolder = viewHolder as CommandViewHolder
        val command = commands[position]
        commandHolder.bind(command, listener)
    }

    override fun getItemCount(): Int {
        return commands.size
    }

}
