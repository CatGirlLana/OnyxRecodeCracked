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

package net.integr.utilities.game.interaction

import net.integr.Onyx
import net.integr.utilities.game.highlight.Highlighter
import net.minecraft.block.BlockState
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.effect.StatusEffectUtil
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShapes
import kotlin.math.pow


class BlockUtil {
    companion object {
        fun placeBlock(pos: BlockPos, rotate: Boolean, silent: Boolean, render: Boolean = true) {
            val hitResult = BlockHitResult(pos.toCenterPos(), Direction.DOWN, pos, false)

            if (rotate) {
                val rotationVec = getLookArea(pos)

                if (rotationVec != null) RotationUtils.lookAt(rotationVec) else RotationUtils.lookAt(pos.toCenterPos())
            }

            Onyx.MC.interactionManager!!.interactBlock(Onyx.MC.player, Hand.MAIN_HAND, hitResult)
            Onyx.MC.interactionManager!!.interactItem(Onyx.MC.player, Hand.MAIN_HAND)

            if (render) Highlighter.renderBlock(pos)

            if (!silent) {
                Onyx.MC.player!!.swingHand(Hand.MAIN_HAND)
            }
        }

        fun getLookArea(pos: BlockPos): Vec3d? {
            val eyesPos = Onyx.MC.player!!.eyePos

            for (side in Direction.entries) {
                val neighbor: BlockPos = pos.offset(side)
                val side2 = side.opposite

                if (eyesPos.squaredDistanceTo(Vec3d.ofCenter(pos)) >= eyesPos.squaredDistanceTo(Vec3d.ofCenter(neighbor))) continue
                if (!canBeClicked(neighbor)) continue

                val hitVec = Vec3d.ofCenter(neighbor).add(Vec3d.of(side2.vector).multiply(0.5))
                if (eyesPos.squaredDistanceTo(hitVec) > 18.0625) continue

                return hitVec
            }

            return null
        }

        fun canBeClicked(pos: BlockPos): Boolean {
            return Onyx.MC.world!!.getBlockState(pos).getOutlineShape(Onyx.MC.world, pos) != VoxelShapes.empty()
        }

        fun canBreak(blockPos: BlockPos?, state: BlockState): Boolean {
            if (!Onyx.MC.player!!.isCreative && state.getHardness(Onyx.MC.world, blockPos) < 0) return false
            return state.getOutlineShape(Onyx.MC.world, blockPos) !== VoxelShapes.empty()
        }

        fun canBreak(blockPos: BlockPos?): Boolean {
            return canBreak(blockPos, Onyx.MC.world!!.getBlockState(blockPos))
        }

        fun minePosition(pos: BlockPos, swing: Boolean, render: Boolean) {
            sendMine(pos)
            if (swing) Onyx.MC.player!!.swingHand(Hand.MAIN_HAND)
            if (render) Highlighter.renderBlockOutlined(pos.toCenterPos().add(0.0, -0.5, 0.0), 30)
        }

        fun minePositionStart(pos: BlockPos, swing: Boolean, render: Boolean) {
            sendStartMine(pos)
            if (swing) Onyx.MC.player!!.swingHand(Hand.MAIN_HAND)
            if (render) Highlighter.renderBlockOutlined(pos.toCenterPos().add(0.0, -0.5, 0.0), 30)
        }

        fun sendMine(pos: BlockPos?) {
            val conn = Onyx.MC.player!!.networkHandler.connection

            val startBreak: Packet<*> = PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP)
            val endBreak: Packet<*> = PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP)

            conn.send(startBreak, null)
            conn.send(endBreak, null)
        }

        fun sendStartMine(pos: BlockPos?) {
            val conn = Onyx.MC.player!!.networkHandler.connection

            val startBreak: Packet<*> = PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP)

            conn.send(startBreak, null)
        }

        private fun getTime(pos: BlockPos, slot: Int, speedMod: Boolean): Float {
            val state: BlockState = Onyx.MC.world!!.getBlockState(pos)
            val f = state.getHardness(Onyx.MC.world!!, pos)
            if (f == -1.0f) {
                return 0.0f
            } else {
                val i = (if (!state.isToolRequired || Onyx.MC.player!!.inventory.getStack(slot).isSuitableFor(state)) 30 else 100).toFloat()
                return getSpeed(state, slot, speedMod) / f / i
            }
        }

        fun getMineTicks(pos: BlockPos, slot: Int, speedMod: Boolean): Float {
            return if (slot == -1) slot.toFloat() else (1 / (getTime(pos, slot, speedMod)))
        }

        private fun getSpeed(state: BlockState, slot: Int, speedMod: Boolean): Float {
            val stack: ItemStack = Onyx.MC.player!!.inventory.getStack(slot)
            var f: Float = Onyx.MC.player!!.inventory.getStack(slot).getMiningSpeedMultiplier(state)
            if (f > 1.0) {
                val i = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack)
                if (i > 0 && !stack.isEmpty) f += (i * i + 1).toFloat()
            }

            if (!speedMod) return f


            if (StatusEffectUtil.hasHaste(Onyx.MC.player!!)) {
                f *= (1.0 + (StatusEffectUtil.getHasteAmplifier(Onyx.MC.player!!) + 1).toFloat() * 0.2f).toFloat()
            }
            if (Onyx.MC.player!!.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
                f *= 0.3f.pow(Onyx.MC.player!!.getStatusEffect(StatusEffects.MINING_FATIGUE)!!.amplifier + 1f)
            }


            if (Onyx.MC.player!!.isSubmergedInWater && !EnchantmentHelper.hasAquaAffinity(Onyx.MC.player!!)) {
                f /= 5.0.toFloat()
            }

            if (!Onyx.MC.player!!.isOnGround) {
                f /= 5.0.toFloat()
            }

            return f
        }
    }
}