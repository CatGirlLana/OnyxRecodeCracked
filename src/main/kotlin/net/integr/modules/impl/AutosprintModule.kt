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

@file:Suppress("unused", "DuplicatedCode")

package net.integr.modules.impl

import net.integr.Onyx
import net.integr.event.PreMoveEvent
import net.integr.event.PreTickEvent
import net.integr.eventsystem.EventListen
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.modules.management.settings.impl.SliderSetting
import net.minecraft.util.math.Vec3d

class AutosprintModule : Module("Autosprint", "Sprints for you", "autoSprint", listOf(Filter.Move)) {
    init {
        settings
            .add(BooleanSetting("Snappy", "Makes you move snappy", "snap"))
            .add(SliderSetting("Snap Speed: ", "The speed for the snappy movement", "snapSpeed", 1.0, 10.0))
            .add(BooleanSetting("All Directions", "Makes you sprint in all directions", "allDirs"))
    }

    @EventListen
    fun onPreMove(event: PreMoveEvent) {
        if (settings.getById<BooleanSetting>("snap")!!.isEnabled()) {
            doSnapMove()
        } else {
            if (settings.getById<BooleanSetting>("allDirs")!!.isEnabled()) {
                if (!Onyx.MC.player!!.isSprinting) Onyx.MC.player!!.isSprinting = true
            } else if (!Onyx.MC.options.sprintKey.isPressed) Onyx.MC.options.sprintKey.isPressed = true
        }

    }

    private fun doSnapMove() {
        val speed = settings.getById<SliderSetting>("snapSpeed")!!.getSetValue().toFloat()

        Onyx.MC.player!!.speed = 0.04f * speed

        val yaw = Onyx.MC.player!!.yaw

        var velocityX = 0.0
        val velocityY = Onyx.MC.player!!.velocity.y
        var velocityZ = 0.0

        val forward = Vec3d.fromPolar(0f, yaw)
        val side = Vec3d.fromPolar(0f, yaw + 90)

        if (Onyx.MC.options.forwardKey.isPressed) {
            velocityX += forward.x * 0.25 * speed
            velocityZ += forward.z * 0.25 * speed

            if (!Onyx.MC.player!!.isSprinting) Onyx.MC.player!!.isSprinting = true

        }

        if (Onyx.MC.options.backKey.isPressed) {
            velocityX -= forward.x * 0.25 * speed
            velocityZ -= forward.z * 0.25 * speed

            if (!Onyx.MC.player!!.isSprinting) Onyx.MC.player!!.isSprinting = true

        }

        if (Onyx.MC.options.leftKey.isPressed) {
            velocityZ -= side.z * 0.25 * speed
            velocityX -= side.x * 0.25 * speed

            if (!Onyx.MC.player!!.isSprinting) Onyx.MC.player!!.isSprinting = true

        }

        if (Onyx.MC.options.rightKey.isPressed) {
            velocityZ += side.z * 0.25 * speed
            velocityX += side.x * 0.25 * speed

            if (!Onyx.MC.player!!.isSprinting) Onyx.MC.player!!.isSprinting = true

        }

        Onyx.MC.player!!.setVelocity(velocityX, velocityY, velocityZ)
    }
}