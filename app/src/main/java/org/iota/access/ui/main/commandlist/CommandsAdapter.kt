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

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.iota.access.R
import org.iota.access.api.model.Command
import org.iota.access.databinding.CommandListItemBinding
import org.iota.access.utils.ui.UiUtils
import java.lang.ref.WeakReference

/**
 * Adapter for the command list
 */
class CommandsAdapter constructor(
        private val commands: List<Command>,
        listener: CommandsAdapterListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class CommandViewHolder(binding: CommandListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private var commandName: TextView = binding.textCommandName
        private var imageHeader: ImageView = binding.imgHeader
        private var imageStatus: ImageView = binding.imgStatus
        private var commandButton: Button = binding.buttonCommand
        private var paidTextView: TextView = binding.isPaidTextView

        fun bind(command: Command, listener: WeakReference<CommandsAdapterListener>) {
            commandButton.text = command.actionName
            commandName.text = command.headerName
            UiUtils.setImageWithTint(imageHeader, command.headerImageResId, R.attr.command_list_item_header_image_tint, Color.WHITE)
            UiUtils.setImageWithTint(imageStatus, command.statusImageResId, R.attr.command_list_item_status_image_tint, Color.WHITE)
            commandButton.setOnClickListener { listener.get()?.onCommandSelected(command) }
            paidTextView.visibility = if (command.activeAction.isPaid) View.GONE else View.VISIBLE
        }
    }

    interface CommandsAdapterListener {
        fun onCommandSelected(command: Command)
    }

    private val listener: WeakReference<CommandsAdapterListener> = WeakReference(listener)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: CommandListItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.command_list_item,
                parent,
                false)
        return CommandViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val commandHolder = viewHolder as CommandViewHolder
        configureViewForCommand(commandHolder, position)
    }

    private fun configureViewForCommand(holder: CommandViewHolder, position: Int) {
        val command = commands[position]
        holder.bind(command, listener)
    }

    // Return the size of your data set (invoked by the layout manager)
    override fun getItemCount(): Int {
        return commands.size
    }

}
