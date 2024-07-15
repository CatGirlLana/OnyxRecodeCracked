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

package net.integr.utilities.game.interaction

import net.integr.Onyx
import net.integr.utilities.game.rotationfake.RotationFaker
import net.integr.utilities.game.rotationfake.RotationLocker
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.math.Vec3d
import kotlin.math.atan2
import kotlin.math.sqrt

class RotationUtils {
    companion object {
        fun lookAt(pos: Vec3d, setServerside: Boolean = true) {
            val neededRotation: Pair<Float, Float> = getNeededRotations(pos)

            if (RotationLocker.isLocked()) {
                RotationLocker.fakeYaw = neededRotation.first
                RotationLocker.fakePitch = neededRotation.second


                if (setServerside) {
                    RotationFaker.INSTANCE.look(neededRotation.first, neededRotation.second)
                } else RotationFaker.INSTANCE.look(Onyx.MC.player!!.yaw, Onyx.MC.player!!.pitch)

            } else {
                Onyx.MC.player!!.yaw = neededRotation.first
                Onyx.MC.player!!.pitch = neededRotation.second

                RotationLocker.fakeYaw = Onyx.MC.player!!.yaw
                RotationLocker.fakePitch = Onyx.MC.player!!.pitch
            }

        }

        fun rotate(yaw: Float, pitch: Float, setServerside: Boolean = true) {
            if (RotationLocker.isLocked()) {
                RotationLocker.fakeYaw = yaw
                RotationLocker.fakePitch = pitch

                if (setServerside) {
                    RotationFaker.INSTANCE.look(yaw, pitch)
                } else RotationFaker.INSTANCE.look(Onyx.MC.player!!.yaw, Onyx.MC.player!!.pitch)

            } else {
                Onyx.MC.player!!.yaw = yaw
                Onyx.MC.player!!.pitch = pitch

                RotationLocker.fakeYaw = Onyx.MC.player!!.yaw
                RotationLocker.fakePitch = Onyx.MC.player!!.pitch
            }

        }

        fun sendLookPacket(yaw: Float, pitch: Float) {
            Onyx.MC.networkHandler!!.sendPacket(PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, Onyx.MC.player!!.isOnGround))
        }

        fun sendFullLookPacket(yaw: Float, pitch: Float) {
            Onyx.MC.networkHandler!!.sendPacket(PlayerMoveC2SPacket.Full(Onyx.MC.player!!.x, Onyx.MC.player!!.y, Onyx.MC.player!!.z, yaw, pitch, Onyx.MC.player!!.isOnGround))
        }

        fun getNeededRotations(vec: Vec3d): Pair<Float, Float> {
            val eyesPos = Vec3d(
                Onyx.MC.player!!.x,
                Onyx.MC.player!!.y + Onyx.MC.player!!.getEyeHeight(Onyx.MC.player!!.pose),
                Onyx.MC.player!!.z
            )

            val diffX = vec.x - eyesPos.x
            val diffY = vec.y - eyesPos.y
            val diffZ = vec.z - eyesPos.z

            val diffXZ = sqrt(diffX * diffX + diffZ * diffZ)

            val yaw = Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f
            val pitch = -Math.toDegrees(atan2(diffY, diffXZ)).toFloat()

            return Pair(yaw, pitch)
        }
    }
}