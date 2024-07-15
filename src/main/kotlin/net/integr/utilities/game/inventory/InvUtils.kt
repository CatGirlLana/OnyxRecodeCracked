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

package net.integr.utilities.game.inventory

import net.integr.Onyx
import net.minecraft.item.Item
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.screen.slot.SlotActionType

class InvUtils {
    companion object {
        fun selectSlotOptionalSilent(slot: Int, silent: Boolean) {
            if (slot in 0..9) {
                if (silent) selectSlotPacket(slot)
                else selectSlot(slot)
            }
        }
        fun selectSlot(slot: Int) {
            if (slot in 0..9) Onyx.MC.player!!.inventory.selectedSlot = slot
        }

        fun selectSlotPacket(slot: Int) {
            if (slot in 0..9) Onyx.MC.networkHandler!!.sendPacket(UpdateSelectedSlotC2SPacket(slot))
        }

        fun getSelectedSlot(): Int {
            return Onyx.MC.player!!.inventory.selectedSlot
        }

        fun moveItem(from: Int, to: Int) {
            Onyx.MC.interactionManager!!.clickSlot(0, from, 0, SlotActionType.PICKUP, Onyx.MC.player)
            Onyx.MC.interactionManager!!.clickSlot(0, to, 0, SlotActionType.PICKUP, Onyx.MC.player)
        }

        fun clickSlot(slot: Int) {
            Onyx.MC.interactionManager!!.clickSlot(0, slot, 0, SlotActionType.PICKUP, Onyx.MC.player)
        }

        fun quickMove(slot: Int) {
            Onyx.MC.interactionManager!!.clickSlot(0, slot, 0, SlotActionType.QUICK_MOVE, Onyx.MC.player)
        }

        fun countInHotbar(item: Item): Int {
            var count = 0
            for (i in (0..9)) {
                val stack = Onyx.MC.player!!.inventory.getStack(i)

                if (stack!!.item == item) count += stack.count
            }

            return count
        }

        fun findInHotbar(item: Item): Int {
            for (i in (0..9)) {
                val stack = Onyx.MC.player!!.inventory.getStack(i)

                if (stack!!.item == item) return i
            }

            return -1
        }
    }
}