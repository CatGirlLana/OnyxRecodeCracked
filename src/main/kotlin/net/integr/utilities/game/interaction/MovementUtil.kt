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
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.math.Vec3d

class MovementUtil {
    companion object {
        fun moveViaPacket(pos: Vec3d) {
            Onyx.MC.networkHandler!!.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(pos.x, pos.y, pos.z, Onyx.MC.player!!.isOnGround))
        }
    }
}