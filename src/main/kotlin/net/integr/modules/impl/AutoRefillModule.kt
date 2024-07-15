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
import net.integr.utilities.game.inventory.InvUtils
import net.minecraft.item.Item

class AutoRefillModule : Module("Auto Refill", "Automatically gets the next stack of blocks", "autoRefill", mutableListOf(Filter.Util)) {

    private var lastItem: Item? = null

    @Suppress("UNUSED_PARAMETER")
    @EventListen
    fun onTick(event: PreTickEvent) {
        refillHand()
    }

    private fun refillHand() {
        if (Onyx.MC.player!!.inventory.mainHandStack.isEmpty) {
            val slot = searchForItem()
            if (slot != -1) {
                InvUtils.quickMove(slot)
                if (searchInHotbar() != -1) {
                    InvUtils.selectSlot(searchInHotbar())
                }
            }
        } else {
            lastItem = Onyx.MC.player!!.inventory.mainHandStack.item
        }
    }

    private fun searchForItem(): Int {
        var nextSlot = -1

        for (slot in 0..36) {
            if (Onyx.MC.player!!.inventory.getStack(slot).item != lastItem) continue

            if (nextSlot == -1) nextSlot = if (slot < 9) slot + 36 else slot
        }

        return nextSlot
    }

    private fun searchInHotbar(): Int {
        val nextSlot = -1

        for (slot in 0..8) {
            if (Onyx.MC.player!!.inventory.getStack(slot).item == lastItem) {
                return slot
            }
        }

        return nextSlot
    }
}