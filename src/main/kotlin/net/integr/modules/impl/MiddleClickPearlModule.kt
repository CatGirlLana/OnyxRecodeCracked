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

import net.integr.event.DoItemPickEvent
import net.integr.eventsystem.EventListen
import net.integr.eventsystem.Priority
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.utilities.game.command.CommandUtils
import net.integr.utilities.game.interaction.ClickUtils
import net.integr.utilities.game.inventory.InvUtils
import net.minecraft.item.Items

class MiddleClickPearlModule: Module("Middle click pearl", "Middle click to throw an ender pearl", "middleClickPearl", mutableListOf(Filter.Util)) {
    init {
        settings.add(BooleanSetting("Redupe", "Automatically redupe the pearl on DupeAnarchy", "redupe"))
    }

    @EventListen(Priority.LAST)
    fun onPickBlockEvent(event: DoItemPickEvent) {
        val epSlot = InvUtils.findInHotbar(Items.ENDER_PEARL)

        if (epSlot == -1) return

        val pre = InvUtils.getSelectedSlot()
        InvUtils.selectSlot(epSlot)
        if (settings.getById<BooleanSetting>("redupe")!!.isEnabled()) CommandUtils.sendCommand("dupe 2")
        ClickUtils.rightClick()
        InvUtils.selectSlot(pre)

        event.cancel()
    }
}