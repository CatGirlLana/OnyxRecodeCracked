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
import net.integr.event.GetHandSwingDurationEvent
import net.integr.event.RenderArmOrItemEvent
import net.integr.eventsystem.EventListen
import net.integr.eventsystem.Priority
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.modules.management.settings.impl.CyclerSetting
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Hand
import net.minecraft.util.math.RotationAxis
import kotlin.math.sin
import kotlin.math.sqrt

class AnimationsModule : Module("Animations", "Changes your swing animation", "animations", mutableListOf(Filter.Render)) {
    init {
        settings
            .add(BooleanSetting("Slow", "Slows down the animation", "slow"))
            .add(CyclerSetting("Mode: ", "The mode to use", "mode", mutableListOf(
                "Angle", "Move"
            )))
    }

    @EventListen(Priority.LAST)
    fun onGetHandSwingDuration(event: GetHandSwingDurationEvent) {
        if (settings.getById<BooleanSetting>("slow")!!.isEnabled()) {
            event.callback = 20
        }
    }

    @EventListen(Priority.LAST)
    fun onRenderArmOrItem(event: RenderArmOrItemEvent) {
        if (event.hand == Hand.MAIN_HAND) transformStack(event.matrices, event.tickDelta)
    }

    private fun transformStack(stack: MatrixStack, delta: Float) {
        val mode = settings.getById<CyclerSetting>("mode")!!.getElement()

        val swingProg = sin(sqrt(Onyx.MC.player!!.getHandSwingProgress(delta)) * Math.PI)*10

        when (mode) {
            "Angle" -> {
                stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(swingProg.toFloat() * 3))
            }

            "Move" -> {
                stack.translate(0.0, swingProg/8, 0.0)
            }
        }
    }
}