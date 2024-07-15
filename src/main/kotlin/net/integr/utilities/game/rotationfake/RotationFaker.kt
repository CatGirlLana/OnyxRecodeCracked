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

package net.integr.utilities.game.rotationfake

import net.integr.Onyx
import net.integr.event.PostTickEvent
import net.integr.event.SendPacketEvent
import net.integr.eventsystem.EventListen
import net.integr.eventsystem.Priority
import net.integr.utilities.game.interaction.RotationUtils
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.math.Vec3d

class RotationFaker {
    companion object {
        val INSTANCE = RotationFaker()
    }

    private var serverYaw = 0f
    private var serverPitch = 0f

    var isFake = false

    @EventListen
    fun onPacket(e: SendPacketEvent) {
        if (!RotationLocker.isLocked()) return

        if (Onyx.MC.player != null) {
            if (e.packet is PlayerMoveC2SPacket) {
                if ((e.packet as PlayerMoveC2SPacket).getPitch(serverPitch) != serverPitch || (e.packet as PlayerMoveC2SPacket).getYaw(serverYaw) != serverYaw) {
                    e.cancel()
                    RotationUtils.sendFullLookPacket(serverYaw, serverPitch)
                }
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @EventListen(Priority.LAST)
    fun onTick(event: PostTickEvent) {
        isFake = false
    }

    fun face(vec: Vec3d) {
        val needed: Pair<Float, Float> = RotationUtils.getNeededRotations(vec)

        serverYaw = needed.first
        serverPitch = needed.second
        isFake = true
    }

    fun look(yaw: Float, pitch: Float) {
        serverYaw = yaw
        serverPitch = pitch

        isFake = true
    }
}