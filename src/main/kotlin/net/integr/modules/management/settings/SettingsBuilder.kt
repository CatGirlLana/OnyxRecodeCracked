/*
 * Copyright © 2024 Integr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.integr.modules.management.settings

import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import net.integr.modules.management.settings.impl.KeyBindSetting

class SettingsBuilder(private val hasBind: Boolean = true) {
    @Expose val options: MutableList<Setting> = if (hasBind) mutableListOf(KeyBindSetting("Keybind: ", "The Key to toggle this Module", "bind")) else mutableListOf()

    fun add(setting: Setting): SettingsBuilder {
        if (hasBind) options.add(options.size-1, setting) else options.add(setting)
        return this
    }

    fun load(obj: JsonObject): SettingsBuilder {
        obj["settings"].asJsonObject["options"].asJsonArray.forEach {
            for (v in options) {
                if (v.id == it.asJsonObject["id"].asString) {
                    v.load(it.asJsonObject)
                }
            }
        }

        return this
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Setting> getById(id: String): T? {
        for (s in options) {
            if (s.id == id) return s as T
        }

        return null
    }
}