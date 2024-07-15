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

package net.integr.utilities.game.highlight

import net.integr.Variables
import net.integr.event.RenderWorldEvent
import net.integr.eventsystem.EventListen
import net.integr.rendering.RenderingEngine
import net.integr.utilities.LogUtils
import net.integr.utilities.game.CoordinateUtils
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d


class HighlightHandler {
    companion object {
        val INSTANCE = HighlightHandler()
    }

    private var oldCrystal: Vec3d? = null

    @EventListen
    fun onRender(event: RenderWorldEvent) {
        for (block in Highlighter.blocks.distinctBy { it.first }) {
            RenderingEngine.ThreeDimensional.box(block.first, event.matrices, Variables.guiColor)
        }

        Highlighter.blocks.forEach {it.second.set(it.second.get()-1)}

        for (block in Highlighter.blocksOutlined.distinctBy { it.first }) {
            RenderingEngine.ThreeDimensional.outlinedBox(block.first, event.matrices, Variables.guiColor)
        }

        Highlighter.blocksOutlined.forEach {it.second.set(it.second.get()-1)}

        for (line in Highlighter.lines.distinctBy { it.first }) {
            RenderingEngine.ThreeDimensional.line(line.first.first.first, line.first.first.second, event.matrices, line.first.second)
        }

        Highlighter.lines.forEach {it.second.set(it.second.get()-1)}

        renderCrystal(event)

        Highlighter.blocks = Highlighter.blocks.filter { it.second.get() > 0 }
        Highlighter.blocksOutlined = Highlighter.blocksOutlined.filter { it.second.get() > 0 }
        Highlighter.lines = Highlighter.lines.filter { it.second.get() > 0 }
    }

    private fun renderCrystal(event: RenderWorldEvent) {
        val crystal = Highlighter.crystal
        if (crystal != null) {
            if (oldCrystal != null) {
                val newCrystal = CoordinateUtils.lerpPositionBetween(oldCrystal!!, crystal, 0.5f)
                oldCrystal = newCrystal

                RenderingEngine.ThreeDimensional.box(newCrystal, event.matrices, Variables.guiColor)
            } else oldCrystal = crystal
        }
    }

}