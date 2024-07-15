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
import net.integr.eventsystem.Priority
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.modules.management.settings.impl.SliderSetting
import net.integr.utilities.game.interaction.RotationUtils
import net.integr.utilities.game.rotationfake.RotationFaker
import net.integr.utilities.game.rotationfake.RotationLocker

class SpinbotModule : Module("Spinbot", "You spin me right round", "spinBot", mutableListOf(Filter.Render)) {
    init {
        settings
            .add(BooleanSetting("Yaw", "Allow yaw", "yaw"))
            .add(BooleanSetting("Pitch", "Allow pitch", "pitch"))
            .add(SliderSetting("Speed: ", "The speed to rotate at", "speed", 10.0, 50.0))
    }

    override fun onDisable() {
        RotationLocker.unLock()
    }

    @Suppress("UNUSED_PARAMETER")
    @EventListen(Priority.LAST)
    fun onTick(event: PreTickEvent) {
        val speed = settings.getById<SliderSetting>("speed")!!.getSetValue().toInt()
        val yaw = settings.getById<BooleanSetting>("yaw")!!.isEnabled()
        val pitch = settings.getById<BooleanSetting>("pitch")!!.isEnabled()

        val isFake = RotationFaker.INSTANCE.isFake

        RotationLocker.lock()

        RotationUtils.rotate(
            if (yaw) { if (isFake) RotationLocker.fakeYaw else RotationLocker.fakeYaw+speed } else { if (isFake) RotationLocker.fakeYaw else Onyx.MC.player!!.yaw},
            if (pitch) { if (isFake) RotationLocker.fakePitch else RotationLocker.fakePitch+speed } else { if (isFake) RotationLocker.fakePitch else Onyx.MC.player!!.pitch},
            false)
    }
}