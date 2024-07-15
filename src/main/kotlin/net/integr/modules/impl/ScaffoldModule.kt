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
import net.integr.event.PreMoveEvent
import net.integr.event.PreTickEvent
import net.integr.eventsystem.EventListen
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.ModuleManager
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.utilities.game.interaction.BlockUtil
import net.integr.utilities.game.inventory.InvUtils
import net.integr.utilities.game.rotationfake.RotationLocker
import net.minecraft.block.Block
import net.minecraft.block.FallingBlock
import net.minecraft.item.BlockItem
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.EmptyBlockView

class ScaffoldModule : Module("Scaffold", "Automatically bridges for you", "scaffold", mutableListOf(Filter.Move)) {
    init {
        settings
            .add(BooleanSetting("Rotate", "Makes the player rotate", "rotate"))
            .add(BooleanSetting("Silent", "Makes the player not swing", "silent"))
    }
    override fun onEnable() {
        val bhop = ModuleManager.getByClass<BhopFeature>()!!
        if (bhop.isEnabled()) bhop.startHeight = Onyx.MC.player!!.y
    }

    override fun onDisable() {
        RotationLocker.unLock()
    }

    @Suppress("UNUSED_PARAMETER")
    @EventListen
    fun onPreMove(event: PreMoveEvent) {
        val rotate = settings.getById<BooleanSetting>("rotate")!!.isEnabled()
        val silent = settings.getById<BooleanSetting>("silent")!!.isEnabled()

        val next = findBlock()

        if (next != -1) {
            val pre = InvUtils.getSelectedSlot()
            InvUtils.selectSlot(next)

            if (rotate) RotationLocker.lock() else RotationLocker.unLock()

            if (ModuleManager.getByClass<BhopFeature>()!!.isEnabled()) {
                attemptPlacement(BlockPos(Onyx.MC.player!!.blockPos.x, (ModuleManager.getByClass<BhopFeature>()!!.startHeight - 1).toInt(), Onyx.MC.player!!.blockPos.z), rotate, silent)
            } else attemptPlacement(Onyx.MC.player!!.blockPos.add(0, -1, 0), rotate, silent)

            InvUtils.selectSlot(pre)
        } else RotationLocker.unLock()
    }

    private fun findBlock(): Int {
        for (i in 0..8) {
            val stack = Onyx.MC.player!!.inventory.getStack(i)

            if (stack!!.isEmpty || stack.item !is BlockItem) continue

            val block = Block.getBlockFromItem(stack.item)
            if (!block.defaultState.isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN)) continue

            if (block is FallingBlock && FallingBlock.canFallThrough(Onyx.MC.world!!.getBlockState(Onyx.MC.player!!.blockPos.subtract(Vec3i(0, 1, 0))))) continue

            return i
        }

        return -1
    }

    private fun canPlace(pos: BlockPos): Boolean {
        return !Onyx.MC.world!!.getBlockState(pos).isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN)
    }

    private fun attemptPlacement(pos: BlockPos, rotate: Boolean, silent: Boolean) {
        if (canPlace(pos)) {
            BlockUtil.placeBlock(pos, rotate, silent)
        }
    }
}