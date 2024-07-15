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
import net.integr.modules.filters.Filter
import net.integr.rendering.uisystem.MovableBox
import net.minecraft.client.gui.DrawContext
import java.nio.file.Files
import java.nio.file.Path

open class UiModule(override var displayName: String, override var toolTip: String, override var id: String, var height: Int, var width: Int, override var filters: List<Filter> = listOf()) : Module(displayName, toolTip, id, filters) {
    @Expose var originX: Int = 20
    @Expose var originY: Int = 20

    fun runRender(context: DrawContext, delta: Float) {
        render(context, originX, originY, delta)
    }

    fun updatePosition(x: Int, y: Int) {
        originX = x
        originY = y
    }

    open fun render(context: DrawContext, originX: Int, originY: Int, delta: Float) {

    }

    override fun load() {
        val json = Files.readAllLines(Path.of(Onyx.CONFIG.toString() + "\\$id.json")).joinToString("")
        val jsonObj = GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create().fromJson(json, JsonObject::class.java)

        this.enabled = jsonObj["enabled"].asBoolean
        this.originX = jsonObj["originX"].asInt
        this.originY = jsonObj["originY"].asInt

        this.settings = settings.load(jsonObj)
    }

    fun getPlaceholderBox(): MovableBox {
        return MovableBox(originX, originY, width, height, displayName)
    }
}