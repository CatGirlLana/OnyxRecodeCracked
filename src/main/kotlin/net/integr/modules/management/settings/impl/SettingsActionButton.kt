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

package net.integr.modules.management.settings.impl

import com.google.gson.JsonObject
import net.integr.modules.management.settings.Setting
import net.integr.rendering.uisystem.Button
import net.integr.rendering.uisystem.base.HelixUiElement

class SettingsActionButton(private val displayName: String, private val tooltip: String, id: String, private var action: () -> Unit) : Setting(id) {
    override fun getUiElement(): HelixUiElement {
        val uie = Button(0, 0, 200, 20, displayName, true, tooltip, false, action = action)
        return uie
    }

    override fun load(obj: JsonObject): Setting {
        return this
    }
}