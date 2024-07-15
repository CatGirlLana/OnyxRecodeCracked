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

package net.integr.utilities.game.interaction

import net.integr.Onyx
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround
import net.minecraft.util.Hand

class AttackUtils {
    companion object {
        fun attackViaManager(entity: Entity) {
            Onyx.MC.interactionManager!!.attackEntity(Onyx.MC.player, entity)
        }

        fun attack(entity: LivingEntity, critical: Boolean) {
            sendAttack(entity, critical)
            Onyx.MC.player!!.resetLastAttackedTicks()

            Onyx.MC.player!!.swingHand(Hand.MAIN_HAND)
        }

        fun attackWithoutReset(entity: Entity, critical: Boolean, swing: Boolean = true) {
            sendAttack(entity, critical)

            if (swing) Onyx.MC.player!!.swingHand(Hand.MAIN_HAND)
        }

        fun canAttack() = Onyx.MC.player!!.getAttackCooldownProgress(0.5f) >= 1

        private fun sendAttack(entity: Entity?, critical: Boolean) {
            val conn = Onyx.MC.player!!.networkHandler.connection

            val posX: Double = Onyx.MC.player!!.x
            val posY: Double = Onyx.MC.player!!.y
            val posZ: Double = Onyx.MC.player!!.z

            val attackPacket: Packet<*> = PlayerInteractEntityC2SPacket.attack(entity, Onyx.MC.player!!.isSneaking)

            if (critical) {
                conn!!.send(PositionAndOnGround(posX, posY + 0.0625, posZ, true))
                conn.send(PositionAndOnGround(posX, posY, posZ, false))
                conn.send(PositionAndOnGround(posX, posY + 1.1E-5, posZ, false))
                conn.send(PositionAndOnGround(posX, posY, posZ, false))
                Onyx.MC.player!!.fallDistance = 0.1f
            }

            conn!!.send(attackPacket, null) //sending packet
        }
    }
}