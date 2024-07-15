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

@file:Suppress("unused")

package net.integr.modules.impl

import net.integr.Onyx
import net.integr.modules.filters.Filter
import net.integr.modules.management.UiModule
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.rendering.uisystem.Box
import net.minecraft.client.gui.DrawContext

class CoordinatesModule : UiModule("Coordinates", "Renders your Coordinates", "coordinates", 60,100, listOf(Filter.Render)) {
    init {
        settings.add(BooleanSetting("Swap X and Y", "Swap Coordinates to protect yourself", "swapXY"))
        settings.add(BooleanSetting("Swap Y and Z", "Swap Coordinates to protect yourself", "swapYZ"))
        settings.add(BooleanSetting("Swap Z and X", "Swap Coordinates to protect yourself", "swapZX"))
    }

    private var background: Box = Box(0, 0, 100, 60, null, false)

    private var coordinatesXBox: Box = Box(0, 0, 100, 20, null, false, outlined = false)
    private var coordinatesYBox: Box = Box(0, 0, 100, 20, null, false, outlined = false)
    private var coordinatesZBox: Box = Box(0, 0, 100, 20, null, false, outlined = false)

    override fun render(context: DrawContext, originX: Int, originY: Int, delta: Float) {
        var x = Onyx.MC.player!!.blockX
        var y = Onyx.MC.player!!.blockY
        var z = Onyx.MC.player!!.blockZ

        if (settings.getById<BooleanSetting>("swapXY")!!.isEnabled()) x = y.also { y = x }
        if (settings.getById<BooleanSetting>("swapYZ")!!.isEnabled()) y = z.also { z = y }
        if (settings.getById<BooleanSetting>("swapZX")!!.isEnabled()) z = x.also { x = z }

        coordinatesXBox.text = " X: $x"
        coordinatesYBox.text = " Y: $y"
        coordinatesZBox.text = " Z: $z"

        background.update(originX, originY).render(context, 10, 10, delta)
        coordinatesXBox.update(originX, originY).render(context, 10, 10, delta)
        coordinatesYBox.update(originX, originY+20).render(context, 10, 10, delta)
        coordinatesZBox.update(originX, originY+40).render(context, 10, 10, delta)
    }
}