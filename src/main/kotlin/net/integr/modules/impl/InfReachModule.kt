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
import net.integr.utilities.game.entity.EntityFinder
import net.integr.utilities.game.interaction.AttackUtils
import net.integr.utilities.game.interaction.MovementUtil
import net.integr.utilities.game.pathfind.PathfindingManager
import net.minecraft.entity.LivingEntity

class InfReachModule : Module("Infinite Reach", "Reach further than normal", "infReach", mutableListOf(Filter.Combat)) {
    init {
        settings.add(SliderSetting("Range: ", "How far you can reach", "range", 1.0, 100.0))
    }

    private var lastTarget: LivingEntity? = null

    override fun onDisable() {
        lastTarget = null
    }

    @EventListen
    fun onTick(event: PreTickEvent) {
        if (Onyx.MC.options.attackKey.isPressed) {
            val range = settings.getById<SliderSetting>("range")!!.getSetValue()

            Onyx.MC.options.attackKey.isPressed = false

            val entity = EntityFinder.getStaredAtEntity(range)

            if (entity != null) {
                lastTarget = entity

                runOn(entity)
            } else if (lastTarget != null) {
                runOn(lastTarget!!)
            }
        }

        if (lastTarget != null && !lastTarget!!.isAlive) {
            lastTarget = null
        }
    }

    private fun runOn(entity: LivingEntity) {
        val path = PathfindingManager.getPathToBlock(entity.blockPos)

        lastTarget = entity

        path.iterateAndRender(time = 20) { _, current ->
            MovementUtil.moveViaPacket(current)
        }

        AttackUtils.attack(entity, false)

        path.reversed().iterate { _, current ->
            MovementUtil.moveViaPacket(current)
        }
    }
}