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
import net.integr.eventsystem.EventListen
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.modules.management.settings.impl.SliderSetting
import net.integr.utilities.game.interaction.BlockUtil
import net.integr.utilities.game.inventory.InvUtils
import net.integr.utilities.game.pausers.CombatPauser
import net.minecraft.item.Items
import net.minecraft.util.math.BlockPos
import net.minecraft.world.EmptyBlockView

class SurroundModule : Module("Surround", "Protects you by placing blocks on all sides", "surround", mutableListOf(Filter.Util)) {
    init {
        settings
            .add(SliderSetting("Delay: ", "Waits after each placement to prevent ghost blocks", "delay", 0.0, 5.0))
            .add(BooleanSetting("Floor", "Places on the floor", "floor"))
            .add(BooleanSetting("Security on Exit", "Places an extra layer on exiting the hole", "secure"))
            .add(BooleanSetting("Center Before", "Centers the player before placing", "center"))
            .add(BooleanSetting("Keep Protecting", "Keeps protecting you by not stopping placement while in the same spot", "keep"))
            .add(BooleanSetting("Pause Combat Modules", "Pauses modules such as Killaura and Crystal aura", "pause"))
            .add(SliderSetting("Amount: ", "The amount to attempt to place per block", "amount", 1.0, 10.0))
    }

    private var saveSpot: BlockPos? = null
    private var timer = 0

    override fun onEnable() {
        saveSpot = Onyx.MC.player!!.blockPos
    }

    @Suppress("UNUSED_PARAMETER")
    @EventListen
    fun onTick(event: PreTickEvent) {
        val pause = settings.getById<BooleanSetting>("pause")!!.isEnabled()

        if (timer > 0) {
            timer--
            return
        }

        val next = getNext()

        if (next != null) {
            if (pause) CombatPauser.pause()

            val block = findBlock()
            if (block != -1) {
                if (settings.getById<BooleanSetting>("center")!!.isEnabled()) Onyx.MC.player!!.setPosition(Onyx.MC.player!!.blockPos.toCenterPos().add(0.0, -0.5, 0.0))
                val pre = InvUtils.getSelectedSlot()
                InvUtils.selectSlot(block)
                for (i in (1..settings.getById<SliderSetting>("amount")!!.getSetValue().toInt())) BlockUtil.placeBlock(next, rotate = false, silent = true)
                InvUtils.selectSlot(pre)
            }

        } else {
            if (pause) CombatPauser.resume()
            if (!settings.getById<BooleanSetting>("keep")!!.isEnabled() || Onyx.MC.player!!.blockPos != saveSpot || if (!settings.getById<BooleanSetting>("secure")!!.isEnabled())  Onyx.MC.player!!.y != saveSpot!!.y.toDouble() else false) disable()
        }

        timer = settings.getById<SliderSetting>("delay")!!.getSetValue().toInt()
    }

    private fun findBlock(): Int {
        for (i in 0..8) {
            val stack = Onyx.MC.player!!.inventory.getStack(i)

            if (stack.item == Items.OBSIDIAN || stack.item == Items.CRYING_OBSIDIAN ||stack.item == Items.BEDROCK) return i
        }

        return -1
    }

    private fun getNext(): BlockPos? {
        val pairs = listOf(Pair(+1, 0), Pair(-1, 0), Pair(0, +1), Pair(0, -1))

        if (settings.getById<BooleanSetting>("floor")!!.isEnabled()) {
            if (!Onyx.MC.world!!.getBlockState(Onyx.MC.player!!.blockPos.add(0, -1, 0)).isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN)) return Onyx.MC.player!!.blockPos.add(0, -1, 0)
        }

        for ((i, j) in pairs) {
            if (!Onyx.MC.world!!.getBlockState(Onyx.MC.player!!.blockPos.add(i, 0, j)).isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN)) return Onyx.MC.player!!.blockPos.add(i, 0, j)
        }

        return null
    }
}