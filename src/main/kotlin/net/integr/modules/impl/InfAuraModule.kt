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

import net.integr.Variables
import net.integr.event.PreTickEvent
import net.integr.eventsystem.EventListen
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.CyclerSetting
import net.integr.modules.management.settings.impl.SliderSetting
import net.integr.utilities.game.entity.EntityFinder
import net.integr.utilities.game.interaction.AttackUtils
import net.integr.utilities.game.interaction.MovementUtil
import net.integr.utilities.game.pathfind.PathfindingManager
import net.minecraft.entity.LivingEntity
import java.util.concurrent.Executors

class InfAuraModule : Module("Infinite Aura", "Hit entities around you from very far", "infAura", mutableListOf(Filter.Combat)) {
    init {
        settings
            .add(CyclerSetting("Mode: ", "The base mode to use", "mode", mutableListOf("Normal", "1.8")))
            .add(SliderSetting("Range: ", "How far you can hit entities", "range", 1.0, 100.0))
            .add(CyclerSetting("Target: ", "The entities to target", "target", mutableListOf("All", "Players", "Mobs")))
    }

    private val service = Executors.newSingleThreadExecutor()

    @EventListen
    fun onTick(event: PreTickEvent) {
        val mode = settings.getById<CyclerSetting>("mode")!!.getElement()
        val range = settings.getById<SliderSetting>("range")!!.getSetValue()
        val target = settings.getById<CyclerSetting>("target")!!.getElement()

        val entity = EntityFinder.getClosestAuraEntity(range, target, false)

        if (entity != null && AttackUtils.canAttack()) {
            if (mode == "Normal") {
                service.execute {
                    runOn(entity, true)
                }
            } else {
                service.execute {
                    runOn(entity, false)
                }
            }
        }

        if (Variables.target != null && !Variables.target!!.isAlive) Variables.target = null
    }

    private fun runOn(entity: LivingEntity, reset: Boolean) {
        val path = PathfindingManager.getPathToBlock(entity.blockPos)

        path.iterateAndRender(time = 20) { _, current ->
            MovementUtil.moveViaPacket(current)
        }

        if (reset) AttackUtils.attack(entity, false) else AttackUtils.attackWithoutReset(entity, false)

        path.reversed().iterate { _, current ->
            MovementUtil.moveViaPacket(current)
        }
    }
}