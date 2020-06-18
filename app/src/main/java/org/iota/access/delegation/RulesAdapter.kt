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
package org.iota.access.delegation

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.iota.access.R
import org.iota.access.databinding.ViewMultipleRuleBinding
import org.iota.access.databinding.ViewRuleLocationBinding
import org.iota.access.databinding.ViewRuleTimeBinding
import org.iota.access.models.rules.LocationRule
import org.iota.access.models.rules.MultipleRule
import org.iota.access.models.rules.Rule
import org.iota.access.models.rules.TimeRule
import org.iota.access.utils.Constants
import org.iota.access.utils.ui.UiUtils
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*


class RulesAdapter(listener: RulesAdapterListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val listener: WeakReference<RulesAdapterListener> = WeakReference(listener)

    var rules: List<Rule> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var addingEnabled: Boolean = true
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int = when (position) {
        rules.size -> ADD_RULE_VIEW_TYPE
        else -> when (rules[position]) {
            is LocationRule -> RULE_LOCATION_VIEW_TYPE
            is TimeRule -> RULE_TIME_VIEW_TYPE
            is MultipleRule -> RULE_MULTIPLE_VIEW_TYPE
            else -> ADD_RULE_VIEW_TYPE
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                RULE_LOCATION_VIEW_TYPE -> LocationRuleViewHolder(
                        DataBindingUtil.inflate(
                                LayoutInflater.from(viewGroup.context),
                                R.layout.view_rule_location,
                                viewGroup,
                                false))
                RULE_TIME_VIEW_TYPE -> TimeRuleViewHolder(
                        DataBindingUtil.inflate(
                                LayoutInflater.from(viewGroup.context),
                                R.layout.view_rule_time,
                                viewGroup,
                                false))
                RULE_MULTIPLE_VIEW_TYPE -> MultipleRuleViewHolder(
                        DataBindingUtil.inflate(
                                LayoutInflater.from(viewGroup.context),
                                R.layout.view_multiple_rule,
                                viewGroup,
                                false))
                ADD_RULE_VIEW_TYPE -> AddRuleViewHolder(
                        LayoutInflater.from(viewGroup.context).inflate(
                                R.layout.add_rule_list_item,
                                viewGroup,
                                false))
                else -> AddRuleViewHolder(
                        LayoutInflater.from(viewGroup.context).inflate(
                                R.layout.add_rule_list_item,
                                viewGroup,
                                false))
            }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            RULE_LOCATION_VIEW_TYPE -> (rules[position] as? LocationRule)?.let {
                (viewHolder as LocationRuleViewHolder).bind(it, listener)
            }
            RULE_TIME_VIEW_TYPE -> (rules[position] as? TimeRule)?.let {
                (viewHolder as TimeRuleViewHolder).bind(it, listener)
            }
            RULE_MULTIPLE_VIEW_TYPE -> (rules[position] as? MultipleRule)?.let {
                (viewHolder as MultipleRuleViewHolder).bind(it, listener)
            }
            ADD_RULE_VIEW_TYPE -> {
                (viewHolder as AddRuleViewHolder).button.setOnClickListener { listener.get()?.onAddRuleClicked() }
            }
        }
    }

    override fun getItemCount(): Int = rules.size + if (addingEnabled) 1 else 0

    interface RulesAdapterListener {
        fun onAddRuleClicked()
        fun onRuleSelected(rule: Rule, type: RuleType)
    }

    private abstract class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        protected var imageButton: ImageButton? = null

        protected fun setImage(@DrawableRes resId: Int) {
            if (imageButton == null) return
            var mapImage = UiUtils.getCompatDrawable(imageButton!!.context, resId)
            if (mapImage != null) {
                mapImage = mapImage.mutate()
                mapImage.setTint(UiUtils.getColorFromAttr(imageButton!!.context, R.attr.button_1_image_color, Color.GRAY))
                imageButton!!.background = mapImage
            }
        }
    }

    private class TimeRuleViewHolder(binding: ViewRuleTimeBinding) : CustomViewHolder(binding.root) {
        val fromTextView: TextView
        val untilTextView: TextView

        fun bind(rule: TimeRule, listener: WeakReference<RulesAdapterListener>) {
            val format = SimpleDateFormat(Constants.DATE_AND_TIME_FORMAT, Locale.getDefault())
            fromTextView.text = format.format(rule.fromDate)
            untilTextView.text = format.format(rule.untilDate)
            itemView.setOnClickListener { listener.get()?.onRuleSelected(rule, RuleType.SINGLE) }
        }

        init {
            imageButton = binding.imageArrow
            fromTextView = binding.labelFrom
            untilTextView = binding.labelUntil
            setImage(R.drawable.ic_clock)
        }
    }

    private class LocationRuleViewHolder(binding: ViewRuleLocationBinding) : CustomViewHolder(binding.root) {
        val latitudeTextView: TextView
        val longitudeTextView: TextView
        val radiusTextView: TextView
        val unitTextView: TextView

        fun bind(rule: LocationRule, listener: WeakReference<RulesAdapterListener>) {
            latitudeTextView.text = rule.latitude.toString()
            longitudeTextView.text = rule.longitude.toString()
            radiusTextView.text = rule.radius.toString()
            unitTextView.text = rule.locationUnit.shortStringValue
            itemView.setOnClickListener { listener.get()?.onRuleSelected(rule, RuleType.SINGLE) }
        }

        init {
            imageButton = binding.imageArrow
            latitudeTextView = binding.labelLatitude
            longitudeTextView = binding.labelLongitude
            radiusTextView = binding.labelRadius
            unitTextView = binding.labelUnits
            setImage(R.drawable.ic_map)
        }
    }

    private class AddRuleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val button: Button = view.findViewById(R.id.button)

        init {
            button.text = view.context.getString(R.string.button_add_rule)
            var buttonImage = UiUtils.getCompatDrawable(view.context, R.drawable.ic_add_circle)
            if (buttonImage != null) {
                buttonImage = buttonImage.mutate()
                buttonImage.setTint(UiUtils.getColorFromAttr(view.context, R.attr.button_1_image_color, Color.GRAY))
                button.setCompoundDrawablesWithIntrinsicBounds(buttonImage, null, null, null)
            }
        }
    }

    private class MultipleRuleViewHolder(binding: ViewMultipleRuleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(rule: Rule, listener: WeakReference<RulesAdapterListener>) {
            itemView.setOnClickListener { listener.get()?.onRuleSelected(rule, RuleType.MULTIPLE) }
        }
    }

    companion object {
        private const val ADD_RULE_VIEW_TYPE = 0
        private const val RULE_LOCATION_VIEW_TYPE = 1
        private const val RULE_TIME_VIEW_TYPE = 2
        private const val RULE_MULTIPLE_VIEW_TYPE = 3
    }

}
