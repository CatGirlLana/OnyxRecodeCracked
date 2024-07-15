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

@file:Suppress("SameParameterValue", "unused")

package net.integr.utilities.game.entity

import net.integr.Onyx
import net.integr.utilities.game.CoordinateUtils
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.DamageUtil
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.BlockView
import net.minecraft.world.Difficulty
import java.util.function.BiFunction
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min


class DamageUtil {
    companion object {
        fun crystalDamage(target: LivingEntity, crystal: Vec3d, predictMovement: Boolean, obsidianPos: BlockPos): Float {
            return overridingExplosionDamage(target, crystal, 12f, predictMovement, obsidianPos, Blocks.OBSIDIAN.defaultState)
        }

        private fun calculateReductions(damageI: Float, entity: LivingEntity, damageSource: DamageSource): Float {
            var damage = damageI
            if (damageSource.isScaledWithDifficulty) {
                when (Onyx.MC.world!!.difficulty) {
                    Difficulty.EASY -> damage = min((damage / 2 + 1).toDouble(), damage.toDouble()).toFloat()
                    Difficulty.HARD -> damage *= 1.5f
                    else -> {}
                }
            }

            damage = DamageUtil.getDamageLeft(damage, getArmor(entity), entity.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).toFloat())

            damage = resistanceReduction(entity, damage)

            damage = protectionReduction(entity, damage, damageSource)

            return max(damage.toDouble(), 0.0).toFloat()
        }

        private fun getArmor(entity: LivingEntity): Float {
            return floor(entity.getAttributeValue(EntityAttributes.GENERIC_ARMOR)).toFloat()
        }

        private fun protectionReduction(player: LivingEntity, damage: Float, source: DamageSource): Float {
            val protLevel = EnchantmentHelper.getProtectionAmount(player.armorItems, source)
            return DamageUtil.getInflictedDamage(damage, protLevel.toFloat())
        }

        private fun resistanceReduction(player: LivingEntity, damageI: Float): Float {
            var damage = damageI
            val resistance = player.getStatusEffect(StatusEffects.RESISTANCE)
            if (resistance != null) {
                val lvl = resistance.amplifier + 1
                damage *= (1 - (lvl * 0.2f))
            }

            return max(damage.toDouble(), 0.0).toFloat()
        }

        private fun overridingExplosionDamage(target: LivingEntity, explosionPos: Vec3d, power: Float, predictMovement: Boolean, overridePos: BlockPos, overrideState: BlockState): Float {
            return explosionDamage(target, explosionPos, power, predictMovement, getOverridingHitFactory(overridePos, overrideState))
        }


        @Suppress("UNUSED_PARAMETER")
        private fun explosionDamage(target: LivingEntity, targetPos: Vec3d, targetBox: Box, explosionPos: Vec3d, power: Float, raycastFactory: RaycastFactory?): Float {
            val modDistance: Double = CoordinateUtils.distanceBetween(
                Vec3d(targetPos.x, targetPos.y, targetPos.z),
                Vec3d(explosionPos.x, explosionPos.y, explosionPos.z)
            )
            if (modDistance > power) return 0f

            val exposure = getExposure(explosionPos, targetBox, raycastFactory!!).toDouble()
            val impact = (1 - (modDistance / power)) * exposure
            val damage = ((impact * impact + impact) / 2 * 7 * 12 + 1).toInt().toFloat()

            return /*calculateReductions(damage, target, Onyx.MC.world!!.damageSources.explosion(null))*/ damage
        }

        private fun getOverridingHitFactory(overridePos: BlockPos, overrideState: BlockState): RaycastFactory {
            return RaycastFactory { context: ExposureRaycastContext, blockPos: BlockPos ->
                val blockState: BlockState
                if (blockPos == overridePos) blockState = overrideState
                else {
                    blockState = Onyx.MC.world!!.getBlockState(blockPos)
                    if (blockState.block.blastResistance < 600) return@RaycastFactory null
                }
                blockState.getCollisionShape(Onyx.MC.world!!, blockPos).raycast(context.start, context.end, blockPos)
            }
        }

        private fun explosionDamage(target: LivingEntity?, explosionPos: Vec3d, power: Float, predictMovement: Boolean, raycastFactory: RaycastFactory): Float {
            if (target == null) return 0f

            val position = if (predictMovement) target.pos.add(target.velocity) else target.pos

            var box = target.boundingBox
            if (predictMovement) box = box.offset(target.velocity)

            return explosionDamage(target, position, box, explosionPos, power, raycastFactory)
        }

        private fun getExposure(source: Vec3d, box: Box, raycastFactory: RaycastFactory): Float {
            val xDiff: Double = box.maxX - box.minX
            val yDiff: Double = box.maxY - box.minY
            val zDiff: Double = box.maxZ - box.minZ

            var xStep = 1 / (xDiff * 2 + 1)
            var yStep = 1 / (yDiff * 2 + 1)
            var zStep = 1 / (zDiff * 2 + 1)

            if (xStep > 0 && yStep > 0 && zStep > 0) {
                var misses = 0
                var hits = 0

                val xOffset = (1 - floor(1 / xStep) * xStep) * 0.5
                val zOffset = (1 - floor(1 / zStep) * zStep) * 0.5

                xStep *= xDiff
                yStep *= yDiff
                zStep *= zDiff

                val startX: Double = box.minX + xOffset
                val startY: Double = box.minY
                val startZ: Double = box.minZ + zOffset
                val endX: Double = box.maxX + xOffset
                val endY: Double = box.maxY
                val endZ: Double = box.maxZ + zOffset

                var x = startX
                while (x <= endX) {
                    var y = startY
                    while (y <= endY) {
                        var z = startZ
                        while (z <= endZ) {
                            val position = Vec3d(x, y, z)

                            if (raycast(ExposureRaycastContext(position, source), raycastFactory) == null) misses++

                            hits++
                            z += zStep
                        }
                        y += yStep
                    }
                    x += xStep
                }

                return misses.toFloat() / hits
            }

            return 0f
        }

        private fun raycast(context: ExposureRaycastContext, raycastFactory: RaycastFactory): BlockHitResult? {
            return BlockView.raycast(context.start, context.end, context, raycastFactory) { null }
        }

        @JvmRecord
        data class ExposureRaycastContext(val start: Vec3d, val end: Vec3d)

        @FunctionalInterface
        fun interface RaycastFactory : BiFunction<ExposureRaycastContext, BlockPos, BlockHitResult?>
    }
}