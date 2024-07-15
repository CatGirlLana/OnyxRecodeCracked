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
import net.integr.event.AttackEvent
import net.integr.event.SendPacketEvent
import net.integr.eventsystem.EventListen
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.ModuleManager
import net.integr.modules.management.settings.impl.BooleanSetting
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.entity.decoration.EndCrystalEntity
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.*
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d


class CrystalOptimizerModule : Module("Optimizer", "Increases crystal speed by removing crystals clientside after they were hit", "optimizer", listOf(Filter.Combat)) {
    init {
        settings.add(BooleanSetting("Mine-Block", "Blocks mining obsidian while holding a crystal in experimental mode", "mineBlock"))
    }

    @EventListen
    fun onPacket(event: SendPacketEvent) {
        if (ModuleManager.getByClass<CrystalAuraModule>()!!.isEnabled()) return

        val packet = event.packet

        if (packet is PlayerInteractEntityC2SPacket) {
            val hitResult = Onyx.MC.crosshairTarget
            if (hitResult != null) {
                if (hitResult.type == HitResult.Type.ENTITY) {
                    if ((hitResult as EntityHitResult).entity is EndCrystalEntity) {
                        packet.handle(EntityHandler())
                    }
                }
            }
        }
    }

    @EventListen
    fun onAttack(event: AttackEvent) {
        if (ModuleManager.getByClass<CrystalAuraModule>()!!.isEnabled()) return

        if (settings.getById<BooleanSetting>("mineBlock")!!.isEnabled()) {
            val hitResult = Onyx.MC.crosshairTarget
            if (hitResult!!.type == HitResult.Type.BLOCK) {
                val block = Onyx.MC.world!!.getBlockState((hitResult as BlockHitResult).blockPos).block

                if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK) {
                    if (Onyx.MC.player!!.inventory.mainHandStack.item == Items.END_CRYSTAL) Onyx.MC.options.attackKey.isPressed = false
                }
            }
        }
    }

    class EntityHandler : PlayerInteractEntityC2SPacket.Handler {
        override fun interact(hand: Hand) {}
        override fun interactAt(hand: Hand, pos: Vec3d) {}

        override fun attack() {
            val hitResult = Onyx.MC.crosshairTarget
            if (hitResult != null) {
                if (hitResult.type == HitResult.Type.ENTITY) {
                    val entityHitResult = hitResult as EntityHitResult
                    val entity = entityHitResult.entity

                    if (entity is EndCrystalEntity) {
                        val weakness = Onyx.MC.player!!.getStatusEffect(StatusEffects.WEAKNESS)
                        val strength = Onyx.MC.player!!.getStatusEffect(StatusEffects.STRENGTH)

                        if (weakness != null && (strength == null || strength.amplifier <= weakness.amplifier) && !isTool(Onyx.MC.player!!.mainHandStack)) {
                            return
                        }

                        entity.kill()
                        entity.setRemoved(Entity.RemovalReason.KILLED)
                        entity.onRemoved()
                    }
                }

            }
        }

        private fun isTool(itemStack: ItemStack): Boolean {
            if (itemStack.item is ToolItem && itemStack.item !is HoeItem) {
                val material = (itemStack.item as ToolItem).material
                return material == ToolMaterials.DIAMOND || material == ToolMaterials.NETHERITE
            } else {
                return false
            }
        }
    }
}