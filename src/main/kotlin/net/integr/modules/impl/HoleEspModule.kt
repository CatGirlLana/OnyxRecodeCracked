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
import net.integr.event.PreTickEvent
import net.integr.event.RenderWorldEvent
import net.integr.eventsystem.EventListen
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.modules.management.settings.impl.SliderSetting
import net.integr.rendering.RenderingEngine
import net.integr.utilities.game.CoordinateUtils
import net.integr.utilities.game.pathfind.PathfindingManager
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.awt.Color


class HoleEspModule : Module("Hole Esp", "Shows all holes in close proximity", "holeEsp", mutableListOf(Filter.Render)) {
    init {
        settings
            .add(SliderSetting("Range: ", "The range to search in", "range", 2.0, 20.0))
            .add(SliderSetting("Update Delay: ", "The delay between updates", "delay", 2.0, 30.0))
            .add(BooleanSetting("Auto Pathfind", "Automatically pathfind to the hole", "autoPathfind"))
    }

    private val holes: HashMap<BlockPos, HoleType> = hashMapOf()
    private var timer = 0;

    @EventListen
    fun onRenderWorld(event: RenderWorldEvent) {
        if (holes.isNotEmpty()) {
            val next = getNextHole(holes) ?: return

            holes.forEach { (blockPos, holeType) ->
                val color = holeType.getColor()
                RenderingEngine.ThreeDimensional.floorQuad(blockPos.toCenterPos(), event.matrices, color.rgb)
            }

            if (settings.getById<BooleanSetting>("autoPathfind")!!.isEnabled()) {
                PathfindingManager.getPathToBlock(next).smoothened().render(holes[next]!!.getColor().rgb)
            }
        }
    }

    @EventListen
    fun onTick(event: PreTickEvent) {
        if (timer >= 0) {
            timer--
            return
        }

        holes.clear()
        getHoles(settings.getById<SliderSetting>("range")!!.getSetValue().toInt())

        timer = settings.getById<SliderSetting>("delay")!!.getSetValue().toInt()
    }

    private fun getNextHole(holes: HashMap<BlockPos, HoleType>): BlockPos? {
        val bestType = holes.minBy { it.value.ordinal }

        return holes.filter { it.value == bestType.value }.keys.minByOrNull { CoordinateUtils.distanceBetween(Onyx.MC.player!!.pos, it.toCenterPos()) }
    }

    private fun getHoles(range: Int) {
        for (i in (-range..range)) {
            for (j in (-range..range)) {
                for (k in (-range..range)) {
                    handleHole(BlockPos(Onyx.MC.player!!.blockX + i, Onyx.MC.player!!.blockY + j, Onyx.MC.player!!.blockZ + k))
                }
            }
        }
    }

    private fun handleHole(blockPos: BlockPos) {
        var verySafe = 0
        var safe = 0

        val checkStateCenter: BlockState = Onyx.MC.world!!.getBlockState(blockPos)
        val checkStateCenterOffset: BlockState = Onyx.MC.world!!.getBlockState(blockPos.add(0, 1, 0))

        if (!checkStateCenter.isAir || !checkStateCenterOffset.isAir) return

        for (direction in Direction.entries) {
            if (direction == Direction.UP) continue
            val offsetPos: BlockPos = blockPos.offset(direction)
            val state: BlockState = Onyx.MC.world!!.getBlockState(offsetPos)

            if (state.block == Blocks.BEDROCK) verySafe++
            else if (state.block == Blocks.REINFORCED_DEEPSLATE) verySafe++
            else if (state.block == Blocks.OBSIDIAN) safe++
            else if (direction == Direction.DOWN) return
        }

        if (safe + verySafe == 5) {
            holes[blockPos] = if (safe == 5) HoleType.Obsidian else (if (verySafe == 5) HoleType.Bedrock else HoleType.Mixed)
        }
    }

    private enum class HoleType {
        Bedrock, Mixed, Obsidian;

        fun getColor(): Color {
            return when (this) {
                Bedrock -> Color(5, 135, 5)
                Obsidian -> Color(207, 50, 6)
                Mixed -> Color(207, 200, 6)
            }
        }
    }
}