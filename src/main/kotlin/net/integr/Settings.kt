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

package net.integr

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import net.integr.modules.management.settings.SettingsBuilder
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.modules.management.settings.impl.CyclerSetting
import net.integr.modules.management.settings.impl.SliderSetting
import net.integr.utilities.LogUtils
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

class Settings {
    companion object {
        val INSTANCE = Settings()
    }

    @Expose var settings: SettingsBuilder = SettingsBuilder(hasBind = false)
        .add(CyclerSetting("Accent: ", "The accent for the Client", "accent", mutableListOf("Orange", "Rgb", "Red", "Green", "Yellow", "Pink", "Purple", "Blue")))
        .add(CyclerSetting("Theme: ", "The theme for the Client", "theme", mutableListOf("Dark", "Light")))
        .add(BooleanSetting("Glow", "Toggle the glowing effects", "glow"))
        .add(BooleanSetting("DiscordRPC", "Toggle the discord Presence", "discordRpc"))
        .add(BooleanSetting("Watermark", "Toggle the watermark", "watermark"))

    fun save() {
        val json = GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create().toJson(this)

        try {
            Files.createDirectories(Path.of(Onyx.CONFIG.toString() + "\\settings"))
            Files.write(Path.of(Onyx.CONFIG.toString() + "\\settings\\settings.json"), json.toByteArray())
        } catch (e: IOException) {
            LogUtils.sendLog("Could not save config [Settings]!")
            e.printStackTrace()
        }
    }

    fun exist(): Boolean {
        return Path.of(Onyx.CONFIG.toString() + "\\settings\\settings.json").exists()
    }

    fun load() {
        val json = Files.readAllLines(Path.of(Onyx.CONFIG.toString() + "\\settings\\settings.json")).joinToString("")
        val jsonObj = GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create().fromJson(json, JsonObject::class.java)

        this.settings = settings.load(jsonObj)
    }
}