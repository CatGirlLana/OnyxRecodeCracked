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

import net.integr.event.DamageTiltEvent
import net.integr.event.PlaySoundEvent
import net.integr.event.RenderEntityEvent
import net.integr.event.SpawnBreakingParticlesEvent
import net.integr.event.SpawnParticleEvent
import net.integr.eventsystem.EventListen
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.BooleanSetting
import net.minecraft.entity.EntityType
import net.minecraft.particle.ParticleTypes
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents

class NoRenderModule : Module("No Render", "Disables rendering of some things", "noRender", listOf(Filter.Render)) {
    init {
        settings
            .add(BooleanSetting("No Items", "Disable Items", "items"))
            .add(BooleanSetting("No Entities", "Disable Entities", "entities"))
            .add(BooleanSetting("No Break Particles", "Disable Break Particles", "breakParticles"))
            .add(BooleanSetting("No Explosions", "Disable Explosions", "explosions"))
            .add(BooleanSetting("No Damage Tilt", "Disable Damage Tilt", "damageTilt"))
    }

    @EventListen
    fun onRenderEntity(event: RenderEntityEvent) {
        if (settings.getById<BooleanSetting>("entities")!!.isEnabled()) {
            event.cancel()
        } else {
            if (settings.getById<BooleanSetting>("items")!!.isEnabled()) {
                if (event.entity.type == EntityType.ITEM) {
                    event.cancel()
                }
            }
        }
    }

    @EventListen
    fun onSpawnParticle(event: SpawnParticleEvent) {
        if (event.parameters.type == ParticleTypes.EXPLOSION || event.parameters.type == ParticleTypes.EXPLOSION_EMITTER) {
            if (settings.getById<BooleanSetting>("explosions")!!.isEnabled()) {
                event.cancel()
            }
        }
    }

    @EventListen
    fun onPlaySound(event: PlaySoundEvent) {
        if (event.event == SoundEvents.ENTITY_GENERIC_EXPLODE) {
            if (settings.getById<BooleanSetting>("explosions")!!.isEnabled()) {
                event.cancel()
            }
        }
    }

    @EventListen
    fun onSpawnBreakingParticles(event: SpawnBreakingParticlesEvent) {
        if (settings.getById<BooleanSetting>("breakParticles")!!.isEnabled()) {
            event.cancel()
        }
    }

    @EventListen
    fun onDamageTilt(event: DamageTiltEvent) {
        if (settings.getById<BooleanSetting>("damageTilt")!!.isEnabled()) {
            event.cancel()
        }
    }
}