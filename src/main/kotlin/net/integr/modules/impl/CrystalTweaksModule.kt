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

package net.integr.modules.impl

import net.integr.Onyx
import net.integr.event.PlaySoundEvent
import net.integr.event.PreTickEvent
import net.integr.eventsystem.EventListen
import net.integr.eventsystem.Priority
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.modules.management.settings.impl.SliderSetting
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents

class CrystalTweaksModule : Module("Crystal Tweaks", "Tweak various aspects of end crystals", "crystalTweaks", mutableListOf(Filter.Render)) {
    init {
        settings
            .add(SliderSetting("Speed: ", "The speed to rotate at", "speed", 0.0, 5.0))
            .add(SliderSetting("Bounce: ", "The height to bounce to", "bounce", 0.0, 5.0))
            .add(SliderSetting("Scale: ", "The scale multiplier", "scale", 0.2, 2.0))
            .add(BooleanSetting("Sound", "Plays a sound when the crystal is broken", "sound"))
    }

    /**
     * See more: EndCrystalEntityRendererMixin.java
     */

    private var timeout = 0

    @EventListen(Priority.FIRST)
    fun onPlaySound(event: PlaySoundEvent) {
        if (event.event == SoundEvents.ENTITY_GENERIC_EXPLODE) {
            if (settings.getById<BooleanSetting>("sound")!!.isEnabled()) {
                Onyx.MC.soundManager.play(PositionedSoundInstance.master(SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1.3F, 0.1F))
                Onyx.MC.soundManager.play(PositionedSoundInstance.master(SoundEvents.ITEM_FLINTANDSTEEL_USE, 1.0F, 0.5F))

                timeout = 9
            }
        }
    }

    @EventListen
    fun onTick(event: PreTickEvent) {
        if (timeout > 0) {
            timeout--
            return
        }
    }
}