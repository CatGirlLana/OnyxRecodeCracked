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

import net.integr.Variables
import net.integr.event.RenderWorldEvent
import net.integr.eventsystem.EventListen
import net.integr.modules.filters.Filter
import net.integr.modules.management.UiModule
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.rendering.RenderingEngine
import net.integr.rendering.uisystem.Box
import net.integr.rendering.uisystem.Slider
import net.integr.utilities.game.CoordinateUtils
import net.minecraft.client.gui.DrawContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Vec3d

class TargetHudModule : UiModule("Target Hud", "Shows info on the current target", "targetHud", 78, 102, mutableListOf(Filter.Render)) {
    init {
        settings
            .add(BooleanSetting("Ui Box", "Renders a box in the game Ui", "box"))
            .add(BooleanSetting("Circle Indicator", "Renders a circle in 3d space to indicate", "circle"))
    }

    private var currentYOffset: Double = 0.0
    private var travelsUp: Boolean = true

    private val backgroundBox = Box(0, 0, 102, 78, null, false, outlined = true, innerIsDisabled = false)
    private val nameBox = Box(0, 0, 90, 20, "", false, outlined = false, innerIsDisabled = false)

    private val healthSlider = Slider(0, 100, 90, 20, "Health: ", false, "", 0.0, 20.0)
    private val defenseSlider = Slider(0, 100, 90, 20, "Armor: ", false, "", 0.0, 20.0)

    override fun render(context: DrawContext, originX: Int, originY: Int, delta: Float) {
        val target = Variables.target

        if (target != null && settings.getById<BooleanSetting>("box")!!.isEnabled()) {
            backgroundBox.update(originX, originY).render(context, 0, 0, delta)

            if (target is PlayerEntity) nameBox.text = target.gameProfile.name else nameBox.text = target.type.name.string
            nameBox.update(originX+5, originY+4).render(context, 0, 0, delta)

            defenseSlider.max = 20.0
            defenseSlider.setValue(target.armor.toDouble())
            defenseSlider.update(originX+5, originY+26).render(context, 0, 0, delta)

            healthSlider.max = target.maxHealth.toDouble() + target.maxAbsorption.toDouble()
            healthSlider.setValue(target.health.toDouble() + target.absorptionAmount.toDouble())
            healthSlider.update(originX+5, originY+51).render(context, 0, 0, delta)

        }
    }

    @EventListen
    fun onRender(event: RenderWorldEvent) {
        if (settings.getById<BooleanSetting>("circle")!!.isEnabled() && Variables.target != null) {
            val pos: Vec3d = CoordinateUtils.getLerpedEntityPos(Variables.target!!, event.tickDelta)

            RenderingEngine.ThreeDimensional.circle(pos.add(0.0, currentYOffset, 0.0), 0.1, 0.7, 0f, true, event.matrices, Variables.guiColor)
            RenderingEngine.ThreeDimensional.circle(pos, 0.1, 0.7, 0f, true, event.matrices, Variables.guiColor)

            updateOffset()
        }
    }

    private fun updateOffset() {
        if (travelsUp) {
            currentYOffset += 0.01
            if (currentYOffset >= Variables.target!!.boundingBox.lengthY) {
                travelsUp = false
            }
        } else {
            currentYOffset -= 0.01
            if (currentYOffset <= 0) {
                travelsUp = true
            }
        }
    }
}