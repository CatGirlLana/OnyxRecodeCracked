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
import net.integr.modules.management.settings.impl.CyclerSetting
import net.integr.rendering.uisystem.Box
import net.integr.utilities.round
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d

class Speedometer : UiModule("Speedometer", "Measures your speed", "speedometer", 20, 110, mutableListOf(Filter.Render)) {
    init {
        settings.add(CyclerSetting("Unit: ", "The unit to use", "unit", mutableListOf("bps", "km/h", "mph")))
    }

    private var box = Box(0, 0, 110, 20, null, false)

    override fun render(context: DrawContext, originX: Int, originY: Int, delta: Float) {
        val playerPosVec: Vec3d = Onyx.MC.player!!.pos
        val travelledX: Double = playerPosVec.x - Onyx.MC.player!!.prevX
        val travelledZ: Double = playerPosVec.z - Onyx.MC.player!!.prevZ

        val currentSpeed = MathHelper.sqrt((travelledX * travelledX + travelledZ * travelledZ).toFloat()).toDouble()

        box.text = "Speed: " + when (settings.getById<CyclerSetting>("unit")!!.getElement()) {
            "bps" -> bps(currentSpeed).round(2).toString() + " bps"
            "km/h" -> kmh(currentSpeed).round(2).toString() + " km/h"
            "mph" -> mph(currentSpeed).round(2).toString() + " mph"
            else -> "None"
        }

        box.update(originX, originY).render(context, 10, 10, delta)
    }

    private fun bps(speed: Double) = speed / 0.05F;
    private fun kmh(speed: Double) = bps(speed) * 3.6;
    private fun mph(speed: Double) = bps(speed) * 2.237

}