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
import net.integr.rendering.uisystem.KeyBind
import net.integr.rendering.uisystem.base.HelixUiElement
import org.lwjgl.glfw.GLFW

class KeyBindSetting(private val displayName: String, private val tooltip: String, id: String) : Setting(id) {
    @Expose
    var bind: Int? = null

    override fun getUiElement(): HelixUiElement {
        val uie = KeyBind(0, 0, 200, 20, displayName, true, tooltip)
        uie.bind = bind
        uie.bindText = if (uie.bind != null) GLFW.glfwGetKeyName(uie.bind!!, 0)!!.uppercase() else "None"
        return uie
    }

    override fun load(obj: JsonObject): Setting {
        this.bind = GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create().fromJson(obj, KeyBindSetting::class.java).bind
        return this
    }

    override fun onUpdate(el: HelixUiElement) {
        bind = (el as KeyBind).bind
    }

    fun getSetBind(): Int? {
        return bind
    }

    fun isBound() = bind != null

    fun getKeyChar() = GLFW.glfwGetKeyName(bind!!, 0)
}