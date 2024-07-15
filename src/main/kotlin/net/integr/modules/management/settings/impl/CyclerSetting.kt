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

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import net.integr.modules.management.settings.Setting
import net.integr.rendering.uisystem.Cycler
import net.integr.rendering.uisystem.base.HelixUiElement

class CyclerSetting(private val displayName: String, private val tooltip: String, id: String, private var items: MutableList<String>) : Setting(id) {
    @Expose var currentIndex = 0
    private var current = ""

    override fun getUiElement(): HelixUiElement {
        val uie = Cycler(0, 0, 200, 20, displayName, true, tooltip, items)
        uie.currentIndex = currentIndex
        uie.text2 = uie.text + uie.items[currentIndex]
        return uie
    }

    override fun load(obj: JsonObject): Setting {
        this.currentIndex = GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create().fromJson(obj, CyclerSetting::class.java).currentIndex
        this.current = items[currentIndex]
        return this
    }

    override fun onUpdate(el: HelixUiElement) {
        currentIndex = (el as Cycler).currentIndex
        current = el.items[currentIndex]
    }

    fun getElement(): String {
        return current
    }
}