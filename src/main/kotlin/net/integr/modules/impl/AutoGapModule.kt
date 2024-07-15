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

@file:Suppress("unused", "UNUSED_PARAMETER")

package net.integr.modules.impl

import net.integr.Onyx
import net.integr.event.PreTickEvent
import net.integr.eventsystem.EventListen
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.ModuleManager
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.modules.management.settings.impl.SliderSetting
import net.integr.utilities.game.inventory.InvUtils
import net.integr.utilities.game.pausers.CombatPauser
import net.minecraft.item.Items

class AutoGapModule : Module("Auto Gap", "Automatically eats an golden apple when you're low on health", "autoGap", mutableListOf(Filter.Combat)) {
    init {
        settings
            .add(SliderSetting("Health: ", "The health to eat a golden apple", "health", 0.0, 20.0))
            .add(BooleanSetting("Pause Combat", "Pause combat modules when active", "pauseCombat"))
    }

    private var pre = -1

    override fun onDisable() {
        CombatPauser.resume()
        Onyx.MC.options.useKey.isPressed = false
    }

    @EventListen
    fun onTick(event: PreTickEvent) {
        if (ModuleManager.getByClass<AutoMendModule>()!!.isEnabled()) return

        if (Onyx.MC.player!!.health < settings.getById<SliderSetting>("health")!!.getSetValue()) {
            var slot = InvUtils.findInHotbar(Items.ENCHANTED_GOLDEN_APPLE)

            if (slot == -1) slot = InvUtils.findInHotbar(Items.GOLDEN_APPLE)

            if (slot != -1) {
                if (settings.getById<BooleanSetting>("pauseCombat")!!.isEnabled()) {
                    CombatPauser.pause()
                }

                if (pre == -1) pre = InvUtils.getSelectedSlot()

                InvUtils.selectSlotPacket(slot)
                InvUtils.selectSlot(slot)

                Onyx.MC.options.useKey.isPressed = true
            } else {
                CombatPauser.resume()
                InvUtils.selectSlotPacket(pre)
                InvUtils.selectSlot(pre)

                Onyx.MC.options.useKey.isPressed = false
            }
        } else if (pre != -1) {
            CombatPauser.resume()
            InvUtils.selectSlotPacket(pre)
            InvUtils.selectSlot(pre)

            pre = -1

            Onyx.MC.options.useKey.isPressed = false
        }
    }
}