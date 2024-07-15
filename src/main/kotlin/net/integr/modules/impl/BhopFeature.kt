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

@file:Suppress("DuplicatedCode")

package net.integr.modules.impl

import net.integr.Onyx
import net.integr.event.PreMoveEvent
import net.integr.eventsystem.EventListen
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.CyclerSetting
import net.integr.modules.management.settings.impl.SliderSetting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class BhopFeature : Module("Bhop", "Makes you jump around", "bhop", mutableListOf(Filter.Move)) {
    init {
        settings
            .add(CyclerSetting("Mode: ", "The mode to use", "mode", mutableListOf("Normal", "Low")))
            .add(SliderSetting("Speed: ", "The speed of the bhop", "speed", 1.0, 10.0))
            .add(SliderSetting("Low Height: ", "The height of the Low mode jumps", "lowHeight", 15.0, 40.0))
    }

    var startHeight = 0.0

    override fun onEnable() {
        startHeight = Onyx.MC.player!!.y
    }

    @EventListen
    fun onPreMove(event: PreMoveEvent) {
        val mode = settings.getById<CyclerSetting>("mode")!!.getElement()
        when (mode) {
            "Normal" -> doMovement()
            "Low" -> doLowMovement()
        }
    }

    private fun doLowMovement() {
        val speed = settings.getById<SliderSetting>("speed")!!.getSetValue().toFloat()

        Onyx.MC.player!!.speed = 0.04f * speed

        val yaw = Onyx.MC.player!!.yaw

        var velocityX = 0.0
        val velocityY = Onyx.MC.player!!.velocity.y
        var velocityZ = 0.0

        val forward = Vec3d.fromPolar(0f, yaw)
        val side = Vec3d.fromPolar(0f, yaw + 90)

        var shouldJump = false

        if (Onyx.MC.options.forwardKey.isPressed) {
            velocityX += forward.x * 0.25 * speed
            velocityZ += forward.z * 0.25 * speed

            if (Onyx.MC.player!!.isOnGround) shouldJump = true
        }

        if (Onyx.MC.options.backKey.isPressed) {
            velocityX -= forward.x * 0.25 * speed
            velocityZ -= forward.z * 0.25 * speed

            if (Onyx.MC.player!!.isOnGround) shouldJump = true
        }

        if (Onyx.MC.options.leftKey.isPressed) {
            velocityZ -= side.z * 0.25 * speed
            velocityX -= side.x * 0.25 * speed

            if (Onyx.MC.player!!.isOnGround) shouldJump = true
        }

        if (Onyx.MC.options.rightKey.isPressed) {
            velocityZ += side.z * 0.25 * speed
            velocityX += side.x * 0.25 * speed

            if (Onyx.MC.player!!.isOnGround) shouldJump = true
        }

        Onyx.MC.player!!.setVelocity(velocityX, velocityY, velocityZ)

        if (shouldJump) {
            Onyx.MC.player!!.setVelocity(velocityX, settings.getById<SliderSetting>("lowHeight")!!.getSetValue()/100, velocityZ)
            if (shouldJump()) Onyx.MC.player!!.jump()
        } else Onyx.MC.player!!.setVelocity(velocityX, velocityY, velocityZ)
    }

    private fun doMovement() {
        val speed = settings.getById<SliderSetting>("speed")!!.getSetValue().toFloat()

        Onyx.MC.player!!.speed = 0.04f * speed

        val yaw = Onyx.MC.player!!.yaw

        var velocityX = 0.0
        val velocityY = Onyx.MC.player!!.velocity.y
        var velocityZ = 0.0

        val forward = Vec3d.fromPolar(0f, yaw)
        val side = Vec3d.fromPolar(0f, yaw + 90)

        var shouldJump = false

        if (Onyx.MC.options.forwardKey.isPressed) {
            velocityX += forward.x * 0.25 * speed
            velocityZ += forward.z * 0.25 * speed

            if (Onyx.MC.player!!.isOnGround) shouldJump = true
        }

        if (Onyx.MC.options.backKey.isPressed) {
            velocityX -= forward.x * 0.25 * speed
            velocityZ -= forward.z * 0.25 * speed

            if (Onyx.MC.player!!.isOnGround) shouldJump = true
        }

        if (Onyx.MC.options.leftKey.isPressed) {
            velocityZ -= side.z * 0.25 * speed
            velocityX -= side.x * 0.25 * speed

            if (Onyx.MC.player!!.isOnGround) shouldJump = true
        }

        if (Onyx.MC.options.rightKey.isPressed) {
            velocityZ += side.z * 0.25 * speed
            velocityX += side.x * 0.25 * speed

            if (Onyx.MC.player!!.isOnGround) shouldJump = true
        }

        Onyx.MC.player!!.setVelocity(velocityX, velocityY, velocityZ)

        if (shouldJump) Onyx.MC.player!!.jump()
    }

    private fun shouldJump(): Boolean {
        for (i in (-1..1)) {
            for (j in (-1..1)) {
                if (shouldJumpAt(Onyx.MC.player!!.blockPos.add(i, 0, j))) return true
            }
        }

        return false
    }

    private fun shouldJumpAt(pos: BlockPos) = Onyx.MC.world!!.getBlockState(pos).isSolidBlock(Onyx.MC.world!!.getChunkAsView(Onyx.MC.player!!.chunkPos.x, Onyx.MC.player!!.chunkPos.z), pos)
}