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
import net.integr.utilities.game.interaction.ClickUtils

class FastplaceModule : Module("Fastplace", "Place faster", "fastplace", mutableListOf(Filter.Util)) {
    init {
        settings.add(SliderSetting("Delay: ", "The ticks to wait after each placement", "delay", 0.0, 5.0))
    }

    private var timer = 0
    @Suppress("UNUSED_PARAMETER")
    @EventListen
    fun onTick(event: PreTickEvent) {
        if (timer > 0) {
            timer--
            return
        }

        if (Onyx.MC.options.useKey.isPressed && !Onyx.MC.player!!.isUsingItem) ClickUtils.rightClickBlock()

        timer = settings.getById<SliderSetting>("delay")!!.getSetValue().toInt()
    }
}