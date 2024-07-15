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
import net.integr.event.EntityPopTotemEvent
import net.integr.event.ReceivePacketEvent
import net.integr.eventsystem.EventListen
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.SettingsActionButton
import net.integr.utilities.LogUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity


class CombatAssistant : Module("Combat Assistant", "Tracks stats of players in a fight", "combatAssistant") {
    init {
        settings
            .add(SettingsActionButton("Reset Session", "Resets the currently tracked data", "resetSession") {
                LogUtils.assistantMessage("Session Reset")

                pops.clear()
            })

            .add(SettingsActionButton("Show Stats", "Shows the current stats", "showStats") {
                printStats()
            })
    }

    private var pops: MutableMap<PlayerEntity, Int> = mutableMapOf()

    @EventListen
    fun onEntityPopTotem(event: EntityPopTotemEvent) {
        val entity: Entity = event.packet.getEntity(Onyx.MC.world!!)!!
        if (entity is PlayerEntity) {
            LogUtils.assistantMessage("${if (entity == Onyx.MC.player) "You" else entity.gameProfile.name + " has"} popped a Totem (${(pops[entity] ?: 0)+1} total)")

            if (pops.containsKey(entity)) {
                pops[entity] = pops[entity]!! + 1
            } else {
                pops[entity] = 1
            }
        }
    }

    private fun printStats() {
        LogUtils.assistantMessage("Stats:")
        LogUtils.assistantMessage("Pops:")
        pops.forEach { (entity, pops) ->
            LogUtils.assistantMessage("${if (entity == Onyx.MC.player) "You" else entity.gameProfile.name + " has"} popped $pops Totem(s)")
        }
    }
}