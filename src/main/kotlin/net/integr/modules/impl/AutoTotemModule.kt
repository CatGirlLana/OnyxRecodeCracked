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
import net.integr.modules.management.settings.impl.SliderSetting
import net.integr.utilities.game.inventory.InvUtils
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Items

class AutoTotemModule : Module("Autototem", "Automatically equips a totem", "autoTotem", mutableListOf(Filter.Util)) {
    init {
        settings
            .add(SliderSetting("Delay: ", "The time to wait between adding next totem to offhand", "delay", 0.0, 5.0))
            .add(SliderSetting("Min Health: ", "The minimum Health to totem at", "health", 2.0, 17.0))
    }

    private var wasTotemInOffhand = false
    private var timer: Int = 0
    private var nextTickSlot = 0

    @Suppress("UNUSED_PARAMETER")
    @EventListen
    fun onTick(event: PreTickEvent) {
        if (timer > 0) timer--
        doAutoTotem()
    }

    private fun doAutoTotem() {
        finishMovingTotem()

        val inventory: PlayerInventory = Onyx.MC.player!!.inventory
        val nextTotemSlot = searchForTotems(inventory)

        val offhandStack = inventory.getStack(40)
        if (offhandStack.item == Items.TOTEM_OF_UNDYING) {
            wasTotemInOffhand = true
            return
        }

        if (wasTotemInOffhand) {
            timer = settings.getById<SliderSetting>("delay")!!.getSetValue().toInt()
            wasTotemInOffhand = false
        }

        val healthF = settings.getById<SliderSetting>("health")!!.getSetValue()
        if (Onyx.MC.player!!.health > healthF) return

        if (Onyx.MC.currentScreen is HandledScreen<*> && Onyx.MC.currentScreen !is AbstractInventoryScreen<*>) return

        if (nextTotemSlot == -1) return

        if (timer > 0) {
            timer--
            return
        }

        val offhandEmpty = offhandStack.isEmpty

        InvUtils.moveItem(nextTotemSlot, 45)

        if (!offhandEmpty) nextTickSlot = nextTotemSlot
    }

    private fun finishMovingTotem() {
        if (nextTickSlot == -1) return

        InvUtils.clickSlot(nextTickSlot)
        nextTickSlot = -1
    }

    private fun searchForTotems(inventory: PlayerInventory): Int {
        var nextTotemSlot = -1

        for (slot in 0..36) {
            if (inventory.getStack(slot).item != Items.TOTEM_OF_UNDYING) continue
            if (nextTotemSlot == -1) nextTotemSlot = if (slot < 9) slot + 36 else slot
        }

        return nextTotemSlot
    }
}