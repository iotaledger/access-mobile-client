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
package org.iota.access.ui.main.delegation.preview

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.iota.access.R
import org.iota.access.databinding.ItemJsonElementBinding
import org.iota.access.utils.ui.DisplayUtil
import org.iota.access.utils.ui.recursiverecyclerview.RecursiveRecyclerAdapter

class JsonAdapter(items: List<JsonElement?>?) : RecursiveRecyclerAdapter<JsonAdapter.ViewHolder?>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val binding: ItemJsonElementBinding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.context), R.layout.item_json_element, viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val item = getItem(position)
        if (item is JsonElement) {
            holder.bind(item, getDepth(position), isExpended(position))
        }
    }

    inner class ViewHolder(binding: ItemJsonElementBinding) : RecyclerView.ViewHolder(binding.root) {
        private val mKeyTextView: TextView = binding.textKey
        private val mValueTextView: TextView = binding.textValue
        private val mArrowImageView: ImageView = binding.imageArrow

        @SuppressLint("SetTextI18n")
        fun bind(jsonElement: JsonElement, depth: Int, expended: Boolean) {
            mKeyTextView.text = jsonElement.key + ":"
            mValueTextView.text = jsonElement.value
            mKeyTextView.setPadding(DisplayUtil.convertDensityPixelToPixel(itemView.context, 5)
                    + depth * DisplayUtil.convertDensityPixelToPixel(itemView.context, 20),
                    0, 0, 0)
            if (jsonElement.children.size == 0) mArrowImageView.visibility = View.GONE else {
                if (expended) mArrowImageView.setImageResource(R.drawable.ic_arrow_up) else mArrowImageView.setImageResource(R.drawable.ic_arrow_down)
                mArrowImageView.visibility = View.VISIBLE
            }
        }

    }

    init {
        setItems(items)
    }
}
