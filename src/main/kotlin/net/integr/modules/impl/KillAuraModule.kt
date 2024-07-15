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

@file:Suppress("DuplicatedCode", "unused")

package net.integr.modules.impl

import net.integr.Onyx
import net.integr.Variables
import net.integr.event.PreTickEvent
import net.integr.eventsystem.EventListen
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.modules.management.settings.impl.CyclerSetting
import net.integr.modules.management.settings.impl.SliderSetting
import net.integr.utilities.game.*
import net.integr.utilities.game.entity.EntityFinder
import net.integr.utilities.game.interaction.AttackUtils
import net.integr.utilities.game.interaction.ClickUtils
import net.integr.utilities.game.interaction.RotationUtils
import net.integr.utilities.game.pausers.CombatPauser
import net.integr.utilities.game.rotationfake.RotationLocker

class KillAuraModule : Module("Killaura", "Automatically attacks entities around you", "killaura", listOf(Filter.Combat)) {
    init {
        settings
            .add(CyclerSetting("Mode: ", "The base mode to use", "mode", mutableListOf("Normal", "1.8", "Wand")))
            .add(CyclerSetting("Wand Type: ", "The wand mode to use", "wandMode", mutableListOf("Leftclick", "Rightclick")))
            .add(CyclerSetting("Attack Mode: ", "The mode to attack in", "attackMode", mutableListOf("Single", "Switch", "Multi")))
            .add(CyclerSetting("Rotations: ", "The way to rotate in", "rotations", mutableListOf("Off", "Instant", "Lerp")))
            .add(SliderSetting("Range: ", "The maximum allowed range", "range", 1.0, 7.0))
            .add(BooleanSetting("Critical", "Automatically do a Critical hit", "critical"))
            .add(CyclerSetting("Target: ", "The entities to target", "target", mutableListOf("All", "Players", "Mobs")))
    }

    override fun onDisable() {
        RotationLocker.unLock()
        Variables.target = null
    }


    @EventListen
    fun onPreTick(event: PreTickEvent) {
        if (CombatPauser.isPaused()) return

        val mode = settings.getById<CyclerSetting>("mode")!!.getElement()
        val wandMode = settings.getById<CyclerSetting>("wandMode")!!.getElement()
        val attackMode = settings.getById<CyclerSetting>("attackMode")!!.getElement()
        val rotations = settings.getById<CyclerSetting>("rotations")!!.getElement()
        val range = settings.getById<SliderSetting>("range")!!.getSetValue()
        val critical = settings.getById<BooleanSetting>("critical")!!.isEnabled()
        val target = settings.getById<CyclerSetting>("target")!!.getElement()

        val entities = EntityFinder.getAuraEntities(if (mode == "Wand") 90.0 else range, target, mode == "Wand")

        when (mode) {
            "Normal" -> {
                when (attackMode) {
                    "Single" -> {
                        if (entities.isNotEmpty()) {
                            val entity = entities.first()
                            if (AttackUtils.canAttack()) AttackUtils.attack(entity, critical)
                            Variables.target = entity

                            when (rotations) {
                                "Instant" -> {
                                    RotationLocker.lock()
                                    RotationUtils.lookAt(entity.eyePos)
                                }

                                "Lerp" -> {
                                    RotationLocker.lock()
                                    RotationUtils.lookAt(CoordinateUtils.getLerpedEntityPos(entity, Onyx.MC.tickDelta))
                                }

                                else -> RotationLocker.unLock()
                            }
                        } else RotationLocker.unLock()
                    }

                    "Switch" -> {
                        if (entities.isNotEmpty()) {
                            val entity = entities.random()
                            if (AttackUtils.canAttack()) AttackUtils.attack(entity, critical)
                            Variables.target = entity

                            when (rotations) {
                                "Instant" -> {
                                    RotationLocker.lock()
                                    RotationUtils.lookAt(entity.eyePos)
                                }

                                "Lerp" -> {
                                    RotationLocker.lock()
                                    RotationUtils.lookAt(CoordinateUtils.getLerpedEntityPos(entity, Onyx.MC.tickDelta))
                                }

                                else -> RotationLocker.unLock()
                            }
                        } else RotationLocker.unLock()
                    }

                    "Multi" -> {
                        RotationLocker.unLock()
                        if (entities.isNotEmpty()) {
                            if (AttackUtils.canAttack()) {
                                for (entity in entities) {
                                    AttackUtils.attack(entity, critical)
                                    Variables.target = entity

                                    when (rotations) {
                                        "Instant" -> {
                                            RotationLocker.lock()
                                            RotationUtils.lookAt(entity.eyePos)
                                        }

                                        "Lerp" -> {
                                            RotationLocker.lock()
                                            RotationUtils.lookAt(CoordinateUtils.getLerpedEntityPos(entity, Onyx.MC.tickDelta))
                                        }

                                        else -> RotationLocker.unLock()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "1.8" -> {
                when (attackMode) {
                    "Single" -> {
                        if (entities.isNotEmpty()) {
                            val entity = entities.first()
                            if (AttackUtils.canAttack()) AttackUtils.attackWithoutReset(entity, critical)
                            Variables.target = entity

                            when (rotations) {
                                "Instant" -> {
                                    RotationLocker.lock()
                                    RotationUtils.lookAt(entity.eyePos)
                                }

                                "Lerp" -> {
                                    RotationLocker.lock()
                                    RotationUtils.lookAt(CoordinateUtils.getLerpedEntityPos(entity, Onyx.MC.tickDelta))
                                }

                                else -> RotationLocker.unLock()
                            }
                        } else RotationLocker.unLock()
                    }

                    "Switch" -> {
                        if (entities.isNotEmpty()) {
                            val entity = entities.random()
                            if (AttackUtils.canAttack()) AttackUtils.attackWithoutReset(entity, critical)
                            Variables.target = entity

                            when (rotations) {
                                "Instant" -> {
                                    RotationLocker.lock()
                                    RotationUtils.lookAt(entity.eyePos)
                                }

                                "Lerp" -> {
                                    RotationLocker.lock()
                                    RotationUtils.lookAt(CoordinateUtils.getLerpedEntityPos(entity, Onyx.MC.tickDelta))
                                }

                                else -> RotationLocker.unLock()
                            }
                        } else RotationLocker.unLock()
                    }

                    "Multi" -> {
                        RotationLocker.unLock()
                        if (entities.isNotEmpty()) {
                            if (AttackUtils.canAttack()) {
                                for (entity in entities) {
                                    AttackUtils.attackWithoutReset(entity, critical)
                                    Variables.target = entity

                                    when (rotations) {
                                        "Instant" -> {
                                            RotationLocker.lock()
                                            RotationUtils.lookAt(entity.eyePos)
                                        }

                                        "Lerp" -> {
                                            RotationLocker.lock()
                                            RotationUtils.lookAt(CoordinateUtils.getLerpedEntityPos(entity, Onyx.MC.tickDelta))
                                        }

                                        else -> RotationLocker.unLock()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "Wand" -> {
                when (attackMode) {
                    "Single" -> {
                        if (entities.isNotEmpty()) {
                            val entity = entities.first()
                            Variables.target = entity

                            when (rotations) {
                                "Instant" -> {
                                    RotationLocker.lock()
                                    RotationUtils.lookAt(entity.eyePos)
                                }

                                "Lerp" -> {
                                    RotationLocker.lock()
                                    RotationUtils.lookAt(CoordinateUtils.getLerpedEntityPos(entity, Onyx.MC.tickDelta))
                                }

                                else -> RotationLocker.unLock()
                            }

                            if (wandMode == "Rightclick") ClickUtils.rightClick() else ClickUtils.leftClick()
                        } else RotationLocker.unLock()
                    }

                    "Switch" -> {
                        if (entities.isNotEmpty()) {
                            val entity = entities.random()
                            Variables.target = entity

                            when (rotations) {
                                "Instant" -> {
                                    RotationLocker.lock()
                                    RotationUtils.lookAt(entity.eyePos)
                                }

                                "Lerp" -> {
                                    RotationLocker.lock()
                                    RotationUtils.lookAt(CoordinateUtils.getLerpedEntityPos(entity, Onyx.MC.tickDelta))
                                }

                                else -> RotationLocker.unLock()
                            }

                            if (wandMode == "Rightclick") ClickUtils.rightClick() else ClickUtils.leftClick()
                        } else RotationLocker.unLock()
                    }

                    "Multi" -> {
                        if (entities.isNotEmpty()) {
                            if (AttackUtils.canAttack()) {
                                for (entity in entities) {
                                    Variables.target = entity

                                    when (rotations) {
                                        "Instant" -> {
                                            RotationLocker.lock()
                                            RotationUtils.lookAt(entity.eyePos)
                                        }

                                        "Lerp" -> {
                                            RotationLocker.lock()
                                            RotationUtils.lookAt(CoordinateUtils.getLerpedEntityPos(entity, Onyx.MC.tickDelta))
                                        }

                                        else -> RotationLocker.unLock()
                                    }

                                    if (wandMode == "Rightclick") ClickUtils.rightClick() else ClickUtils.leftClick()
                                }
                            }
                        }
                    }
                }
            }
        }

        if (Variables.target != null && !Variables.target!!.isAlive) Variables.target = null
    }
}