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
import net.integr.Variables
import net.integr.event.RenderWorldEvent
import net.integr.eventsystem.EventListen
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.rendering.RenderingEngine
import net.integr.utilities.game.CoordinateUtils
import net.minecraft.client.option.Perspective
import net.minecraft.util.math.Vec3d

class CosmeticsModule : Module("Cosmetics", "Gives you cosmetics", "cosmetics", listOf(Filter.Render)) {
    init {
        settings.add(BooleanSetting("China Hat", "Render a China hat", "chinaHat"))
    }

    @EventListen
    fun onRender(event: RenderWorldEvent) {
        if (settings.getById<BooleanSetting>("chinaHat")!!.isEnabled() && Onyx.MC.options.perspective != Perspective.FIRST_PERSON) {
            val pos: Vec3d = CoordinateUtils.getLerpedEntityPos(Onyx.MC.player!!, event.tickDelta)
            RenderingEngine.ThreeDimensional.circle(pos.add(0.0, Onyx.MC.player!!.boundingBox.lengthY+0.2, 0.0), 0.1, 1.0, 0.2f, true, event.matrices, Variables.guiColor)
        }
    }
}