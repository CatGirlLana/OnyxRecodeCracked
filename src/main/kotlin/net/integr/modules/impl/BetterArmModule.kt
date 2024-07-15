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

@file:Suppress("DuplicatedCode", "unused")

package net.integr.modules.impl

import net.integr.event.RenderArmEvent
import net.integr.event.RenderHeldItemEvent
import net.integr.eventsystem.EventListen
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.modules.management.settings.impl.SliderSetting
import net.minecraft.util.Hand
import net.minecraft.util.math.RotationAxis

class BetterArmModule : Module("Better Arm", "Changes rendering of your hand", "betterArm", listOf(Filter.Render)) {
    init {
        settings
            .add(BooleanSetting("Offhand", "Toggle rendering the offhand differently", "offhand"))
            .add(SliderSetting("X Offset: ", "The X Offset of the Hand", "xOffset", -2.0, 2.0))
            .add(SliderSetting("Y Offset: ", "The Y Offset of the Hand", "yOffset", -2.0, 2.0))
            .add(SliderSetting("Z Offset: ", "The Z Offset of the Hand", "zOffset", -2.0, 2.0))
            .add(SliderSetting("X Rotation: ", "The X Rotation of the Hand", "xRotation", -20.0, 20.0))
            .add(SliderSetting("Y Rotation: ", "The Y Rotation of the Hand", "yRotation", -20.0, 20.0))
            .add(SliderSetting("Z Rotation: ", "The Z Rotation of the Hand", "zRotation", -20.0, 20.0))
    }

    @EventListen
    fun onRenderItem(event: RenderHeldItemEvent) {
        val xO = settings.getById<SliderSetting>("xOffset")!!.getSetValue()
        val yO = settings.getById<SliderSetting>("yOffset")!!.getSetValue()
        val zO = settings.getById<SliderSetting>("zOffset")!!.getSetValue()

        val xR = settings.getById<SliderSetting>("xRotation")!!.getSetValue()
        val yR = settings.getById<SliderSetting>("yRotation")!!.getSetValue()
        val zR = settings.getById<SliderSetting>("zRotation")!!.getSetValue()

        if (event.hand == Hand.MAIN_HAND) {
            event.matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(xR.toFloat()))
            event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yR.toFloat()))
            event.matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(zR.toFloat()))
            event.matrices.scale(1f, 1f, 1f)
            event.matrices.translate(xO, yO, zO)
        } else if (settings.getById<BooleanSetting>("offhand")!!.isEnabled()){
            event.matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(xR.toFloat()))
            event.matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(yR.toFloat()))
            event.matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(zR.toFloat()))
            event.matrices.scale(1f, 1f, 1f)
            event.matrices.translate(-xO, yO, zO)
        }
    }

    @EventListen
    fun onRenderArm(event: RenderArmEvent) {
        val xO = settings.getById<SliderSetting>("xOffset")!!.getSetValue()
        val yO = settings.getById<SliderSetting>("yOffset")!!.getSetValue()
        val zO = settings.getById<SliderSetting>("zOffset")!!.getSetValue()

        val xR = settings.getById<SliderSetting>("xRotation")!!.getSetValue()
        val yR = settings.getById<SliderSetting>("yRotation")!!.getSetValue()
        val zR = settings.getById<SliderSetting>("zRotation")!!.getSetValue()

        event.matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(xR.toFloat()))
        event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yR.toFloat()))
        event.matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(zR.toFloat()))
        event.matrices.scale(1f, 1f, 1f)
        event.matrices.translate(xO, yO, zO)
    }
}