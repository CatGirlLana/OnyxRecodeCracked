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
import net.integr.event.KeyEvent
import net.integr.event.PreMoveEvent
import net.integr.eventsystem.EventListen
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.KeyBindSetting
import net.integr.utilities.game.notification.NotificationHandler
import net.minecraft.util.math.Vec3d
import org.lwjgl.glfw.GLFW

class FlightModule : Module("Flight", "Makes you fly", "flight") {
    init {
        settings.add(KeyBindSetting("Speed Bind: ", "Cycle the speed trough various modes", "speedBind"))
    }

    private var flightSpeedMode = 1
    private var floatingTime = 0

    override fun onKeyEvent(event: KeyEvent) {
        if (event.action == GLFW.GLFW_PRESS && event.key == settings.getById<KeyBindSetting>("speedBind")!!.bind) {
            if (flightSpeedMode == 10) flightSpeedMode = 1
            else flightSpeedMode++

            NotificationHandler.notify("Flight speed $flightSpeedMode")
        }
    }

    override fun onEnable() {
        Onyx.MC.player!!.abilities.flying = true
    }

    override fun onDisable() {
        Onyx.MC.player!!.abilities.flying = false
    }

    @EventListen
    fun onPreMove(event: PreMoveEvent) {
        doFly(0.40*flightSpeedMode, 0.40*flightSpeedMode)

        when (floatingTime) {
            20 -> {
                Onyx.MC.player!!.addVelocity(0.0, -0.04, 0.0)
                floatingTime = 0
            }
            2 -> {
                Onyx.MC.player!!.addVelocity(0.0, 0.04, 0.0)
                floatingTime++
            }
            else -> floatingTime++
        }
    }

    private fun doFly(speed: Double, hSpeed: Double) {
        Onyx.MC.player!!.speed = 0.06f
        Onyx.MC.player!!.setVelocity(0.0, Onyx.MC.player!!.velocity.getY(), 0.0)

        var velocityX = 0.0
        var velocityY = 0.0
        var velocityZ = 0.0

        val yaw = Onyx.MC.player!!.yaw

        val forward = Vec3d.fromPolar(0f, yaw)
        val side = Vec3d.fromPolar(0f, yaw + 90)

        if (Onyx.MC.options.forwardKey.isPressed) {
            velocityX += forward.x * speed
            velocityZ += forward.z * speed
        }

        if (Onyx.MC.options.backKey.isPressed) {
            velocityX -= forward.x * speed
            velocityZ -= forward.z * speed
        }

        if (Onyx.MC.options.leftKey.isPressed) {
            velocityZ -= side.z * speed
            velocityX -= side.x * speed
        }

        if (Onyx.MC.options.rightKey.isPressed) {
            velocityZ += side.z * speed
            velocityX += side.x * speed
        }

        if (Onyx.MC.options.jumpKey.isPressed) {
            velocityY += hSpeed
        }

        if (Onyx.MC.options.sneakKey.isPressed) {
            velocityY -= hSpeed
        }

        Onyx.MC.player!!.setVelocity(velocityX, velocityY, velocityZ)
    }
}