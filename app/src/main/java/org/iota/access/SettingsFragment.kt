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
package org.iota.access

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import org.iota.access.SettingsFragment.Keys.PREF_KEY_POLICY_IP_ADDRESS
import org.iota.access.di.Injectable
import org.iota.access.utils.ui.DisplayUtil
import org.iota.access.utils.ui.ThemeLab
import org.iota.access.utils.ui.UiUtils
import java.lang.Exception
import javax.inject.Inject

class SettingsFragment : BasePreferenceFragmentCompat(), Injectable, Preference.OnPreferenceChangeListener {
    @Inject
    lateinit var themeLab: ThemeLab

    private var showAdditionalSettings = false

    private var initialTheme: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            initialTheme = savedInstanceState.getString(INITIAL_THEME)
        }

        // extract arguments
        arguments?.let {
            if (it.containsKey(SHOW_ADDITIONAL_SETTINGS)) showAdditionalSettings = it.getBoolean(SHOW_ADDITIONAL_SETTINGS)
        }
    }

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(Color.WHITE)

        // Unit category
        (preferenceScreen.findPreference<Preference>(PREF_CATEGORY_UNITS) as? PreferenceCategory)?.let {
            it.isVisible = showAdditionalSettings
        }

        // ACCESS IP address
        (preferenceScreen.findPreference<Preference>(Keys.PREF_KEY_ACCESS_IP_ADDRESS) as? EditTextPreference)?.let {

            it.summary = it.text
            it.onPreferenceChangeListener = this
        }

        // ACCESS port number
        (preferenceScreen.findPreference<Preference>(Keys.PREF_KEY_ACCESS_PORT_NUMBER) as? EditTextPreference)?.let {
            it.summary = it.text
            it.onPreferenceChangeListener = this
        }

        // POLICY IP address
        (preferenceScreen.findPreference<Preference>(Keys.PREF_KEY_POLICY_IP_ADDRESS) as? EditTextPreference)?.let {
            it.summary = it.text
        }
        preferenceScreen.findPreference<Preference>(Keys.PREF_KEY_POLICY_IP_ADDRESS)?.setOnPreferenceChangeListener { preference, newValue ->
            try {
                val policyPort: String? = (preferenceScreen.findPreference<Preference>(Keys.PREF_KEY_POLICY_PORT_NUMBER) as? EditTextPreference)?.text
                updatePolicyUrl(newValue.toString(), policyPort)
                preference.summary = newValue.toString()
                true
            } catch (e: Exception) {
                Toast.makeText(context, getString(R.string.invalid_policy_url),Toast.LENGTH_SHORT).show()
                false
            }
        }


        // POLICY port number
        (preferenceScreen.findPreference<Preference>(Keys.PREF_KEY_POLICY_PORT_NUMBER) as? EditTextPreference)?.let {
            it.summary = it.text
        }
        preferenceScreen.findPreference<Preference>(Keys.PREF_KEY_POLICY_PORT_NUMBER)?.setOnPreferenceChangeListener { preference, newValue ->
            try {
                val policyServer: String? = (preferenceScreen.findPreference<Preference>(Keys.PREF_KEY_POLICY_IP_ADDRESS) as? EditTextPreference)?.text
                updatePolicyUrl(policyServer, newValue.toString())
                preference.summary = newValue.toString()
                true
            } catch (e: Exception) {
                Toast.makeText(context, getString(R.string.invalid_policy_url),Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    private fun updatePolicyUrl(server: String?, port: String?) {
        val policyUrl = "http://$server:$port"
        RetrofitUrlManager.getInstance().putDomain("policy", policyUrl);
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INITIAL_THEME, initialTheme)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val activity = activity ?: return

        // Set divider
        UiUtils.getCompatDrawable(activity, R.drawable.separation_line)?.let {
            it.setTint(UiUtils.getColorFromAttr(activity, R.attr.separator_line_color, Color.GRAY))
            setDivider(it)
            setDividerHeight(DisplayUtil.convertDensityPixelToPixel(activity, 2))
        }

        // Themes
        (preferenceScreen.findPreference<Preference>(Keys.PREF_KEY_THEME) as? ListPreference)?.let {
            configListPreference(it, themeLab.getThemeNames(getActivity()))
            if (initialTheme == null) initialTheme = it.value
        }

        // Temperature units
        (preferenceScreen.findPreference<Preference>(Keys.PREF_KEY_TEMPERATURE_UNIT) as? ListPreference)?.let {
            configListPreference(it, resources.getStringArray(R.array.temperature_units))
        }

        // Distance units
        (preferenceScreen.findPreference<Preference>(Keys.PREF_KEY_DISTANCE_UNIT) as? ListPreference)?.let {
            configListPreference(it, resources.getStringArray(R.array.distance_units))
        }
    }

    private fun configListPreference(preference: ListPreference, entryList: Array<String>) {
        preference.entries = entryList
        val entryValues = arrayOfNulls<String>(entryList.size)
        for (i in entryValues.indices) entryValues[i] = i.toString()
        preference.entryValues = entryValues
        if (entryValues.isNotEmpty() && preference.entry == null) preference.value = entryValues[0]
        preference.summary = preference.entry
        preference.onPreferenceChangeListener = this
    }

    override fun onPreferenceChange(preference: Preference, o: Any): Boolean {
        if (preference is EditTextPreference) {
            preference.setSummary(o as CharSequence)
            return true
        }
        if (preference is ListPreference) {
            val newValue = o as String
            val oldValue = preference.value
            // check if preference is theme related
            if (preference.getKey() == Keys.PREF_KEY_THEME) {
                // check if new value is different from old value
                // this means theme has changed so activity has to be recreated
                if (newValue != oldValue) {
                    if (activity != null) {
                        activity?.recreate()
                    }
                }
            }
            preference.value = o
            preference.setSummary(preference.entry)
            return false
        }
        return true
    }

    object Keys {
        const val PREF_KEY_USER = "pref_user"
        const val PREF_KEY_THEME = "pref_theme"
        const val PREF_KEY_ACCESS_IP_ADDRESS = "pref_access_ip_address"
        const val PREF_KEY_ACCESS_PORT_NUMBER = "pref_access_port_number"
        const val PREF_KEY_POLICY_IP_ADDRESS = "pref_policy_ip_address"
        const val PREF_KEY_POLICY_PORT_NUMBER = "pref_policy_port_number"
        const val PREF_DEVICE_ID = "pref_device_id"
        const val PREF_KEY_IP_ADDRESS_EMBEDDED = "pref_ip_address_embedded"
        const val PREF_KEY_PORT_NUMBER_EMBEDDED = "pref_port_number_embedded"
        const val PREF_KEY_PROTOCOL = "pref_protocol"
        const val PREF_KEY_TEMPERATURE_UNIT = "pref_temperature_unit"
        const val PREF_KEY_DISTANCE_UNIT = "pref_distance_unit"
        const val PREF_KEY_CUSTOM_COMMANDS = "pref_custom_commands"
    }

    companion object {
        private const val INITIAL_THEME = "com.iota.access.initial_theme"
        private const val SHOW_ADDITIONAL_SETTINGS = "com.iota.access.show_additional_settings"
        private const val PREF_CATEGORY_UNITS = "pref_category_units"

        fun createArgs(showAdditionalSettings: Boolean): Bundle {
            val args = Bundle()
            args.putBoolean(SHOW_ADDITIONAL_SETTINGS, showAdditionalSettings)
            return args
        }

        fun newInstance(showAdditionalSettings: Boolean): SettingsFragment {
            val fragment = SettingsFragment()
            val args = createArgs(showAdditionalSettings)
            fragment.arguments = args
            return fragment
        }
    }
}
