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
import net.integr.rendering.uisystem.Slider
import net.integr.rendering.uisystem.base.HelixUiElement
import net.integr.utilities.round

class SliderSetting(private val displayName: String, private val tooltip: String, id: String, private var min: Double, private var max: Double, private val default: Double = 0.0) : Setting(id) {
    @Expose var value = default

    override fun getUiElement(): HelixUiElement {
        val uie = Slider(0, 0, 200, 20, displayName, true, tooltip, min, max)
        uie.fillX = uie.xFillFromValue(value)
        uie.text2 = uie.text + uie.valueFromFillX().round(1)
        return uie
    }

    override fun load(obj: JsonObject): Setting {
        this.value = GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create().fromJson(obj, SliderSetting::class.java).value
        return this
    }

    override fun onUpdate(el: HelixUiElement) {
        value = (el as Slider).valueFromFillX()
    }

    fun getSetValue(): Double {
        return value.round(1)
    }

    fun getSetValueAsFloat(): Float {
        return value.round(1).toFloat()
    }
}