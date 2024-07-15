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

@file:Suppress("DuplicatedCode")

package net.integr.modules.management

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import net.integr.Onyx
import net.integr.event.KeyEvent
import net.integr.eventsystem.EventSystem
import net.integr.modules.filters.Filter
import net.integr.modules.management.settings.SettingsBuilder
import net.integr.utilities.LogUtils
import net.integr.utilities.game.notification.NotificationHandler
import net.minecraft.util.Formatting
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

open class Module(open var displayName: String, open var toolTip: String, open var id: String, open var filters: List<Filter> = listOf(), var isExecuted: Boolean = false) {
    @Expose var enabled = false
    @Expose var settings: SettingsBuilder = SettingsBuilder()

    open fun onEnable() {

    }

    open fun onDisable() {

    }

    fun enable() {
        onEnable()
        if (!isExecuted) {
            enabled = true
            NotificationHandler.notify("$displayName ${Formatting.GREEN}on")
            EventSystem.register(this)
        } else {
            NotificationHandler.notify("$displayName triggered")
            disable()
        }
    }

    fun setState(en: Boolean) {
        if (en) {
            onEnable()
            if (!isExecuted) {
                enabled = true
                NotificationHandler.notify("$displayName ${Formatting.GREEN}on")
                EventSystem.register(this)
            } else {
                NotificationHandler.notify("$displayName triggered")
                disable()
            }
        } else {
            enabled = false
            EventSystem.unRegister(this)
            onDisable()
            if (!isExecuted) NotificationHandler.notify("$displayName ${Formatting.RED}off")
        }
    }

    fun disable() {
        enabled = false
        onDisable()
        EventSystem.unRegister(this)
        if (!isExecuted) NotificationHandler.notify("$displayName ${Formatting.RED}off")
    }

    fun toggle() {
        enabled = !enabled

        if (enabled) {
            onEnable()
            if (!isExecuted) {
                NotificationHandler.notify("$displayName ${Formatting.GREEN}on")
                EventSystem.register(this)
            } else {
                NotificationHandler.notify("$displayName triggered")
                disable()
            }
        } else {
            EventSystem.unRegister(this)
            onDisable()
            if (!isExecuted) NotificationHandler.notify("$displayName ${Formatting.RED}off")
        }
    }

    fun isEnabled(): Boolean {
        return enabled
    }

    open fun onKeyEvent(event: KeyEvent) {

    }

    fun save() {
        val json = GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create().toJson(this)

        try {
            Files.createDirectories(Path.of(Onyx.CONFIG.toString()))
            Files.write(Path.of(Onyx.CONFIG.toString() + "\\$id.json"), json.toByteArray())
        } catch (e: IOException) {
            LogUtils.sendLog("Could not save config [$id]!")
            e.printStackTrace()
        }
    }

    fun exists(): Boolean {
        return Path.of(Onyx.CONFIG.toString() + "\\$id.json").exists()
    }

    open fun load() {
        val json = Files.readAllLines(Path.of(Onyx.CONFIG.toString() + "\\$id.json")).joinToString("")
        val jsonObj = GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create().fromJson(json, JsonObject::class.java)

        this.enabled = jsonObj["enabled"].asBoolean
        this.settings = settings.load(jsonObj)
    }

    fun load(jsonObj: JsonObject) {

        this.enabled = jsonObj["enabled"].asBoolean
        this.settings = settings.load(jsonObj)
    }

    fun getSearchingTags(): List<String> {
        val fList: MutableList<String> = mutableListOf()
        for (filter in filters) {fList += filter.name.lowercase().filter { it != ' ' }}

        return listOf(displayName.lowercase().filter { it != ' ' }, toolTip.lowercase().filter { it != ' ' }, id.lowercase().filter { it != ' ' }, fList.joinToString(""))
    }
}