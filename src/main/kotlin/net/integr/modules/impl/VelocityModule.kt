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
import net.integr.event.ExplosionVelocityEvent
import net.integr.event.PushOutOfBlocksEvent
import net.integr.event.ReceivePacketEvent
import net.integr.eventsystem.EventListen
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.BooleanSetting
import net.minecraft.entity.Entity
import net.minecraft.network.NetworkThreadUtils
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.world.explosion.Explosion

class VelocityModule : Module("Velocity", "Take no knockback", "velocity", mutableListOf(Filter.Move)) {
    init {
        settings.add(BooleanSetting("Disable Block Push", "Stops you from being pushed out of blocks", "blocks"))
    }

    @EventListen
    fun onPacket(event: ReceivePacketEvent) {
        if (event.packet is EntityVelocityUpdateS2CPacket) {
            event.cancel()
        }
    }

    @EventListen
    fun onExplosionVelocity(event: ExplosionVelocityEvent) {
        event.cancel()

        val explosion = Explosion(
            Onyx.MC.world,
            null as Entity?,
            event.packet.x,
            event.packet.y,
            event.packet.z,
            event.packet.radius,
            event.packet.affectedBlocks,
            event.packet.destructionType,
            event.packet.particle,
            event.packet.emitterParticle,
            event.packet.soundEvent
        )

        explosion.affectWorld(true)
    }

    @EventListen
    fun onPushOutOfBlock(event: PushOutOfBlocksEvent) {
        if (settings.getById<BooleanSetting>("blocks")!!.isEnabled()) {
            event.cancel()
        }
    }
}