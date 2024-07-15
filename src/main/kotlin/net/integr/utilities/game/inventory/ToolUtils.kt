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

package net.integr.utilities.game.inventory

import net.integr.Onyx
import net.integr.utilities.game.interaction.BlockUtil
import net.minecraft.block.*
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.*
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.math.BlockPos
import java.util.function.Predicate


class ToolUtils {
    companion object {

        private val ORES: List<Block> = listOf(
            Blocks.DIAMOND_ORE,
            Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.GOLD_ORE,
            Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.EMERALD_ORE,
            Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.LAPIS_ORE,
            Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.IRON_ORE,
            Blocks.DEEPSLATE_IRON_ORE,
            Blocks.COAL_ORE,
            Blocks.DEEPSLATE_COAL_ORE,
            Blocks.REDSTONE_ORE,
            Blocks.DEEPSLATE_REDSTONE_ORE,
        )

        fun getBestSlotForBlock(pos: BlockPos, silkEnderChest: Boolean = false, fortuneCrops: Boolean = false, fortuneOverSilkTouch: Boolean = false, maxBreakPercent: Int): Int {
            val blockState: BlockState = Onyx.MC.world!!.getBlockState(pos)
            if (!BlockUtil.canBreak(pos, blockState)) return InvUtils.getSelectedSlot()

            var bestScore = -1.0
            var bestSlot = -1

            for (i in 0..8) {
                val itemStack: ItemStack = Onyx.MC.player!!.inventory.getStack(i)

                val score = getScore(itemStack, blockState, silkEnderChest, fortuneCrops, if (fortuneOverSilkTouch) EnchantPreference.Fortune else EnchantPreference.SilkTouch) { itemStack2 -> !isAlmostBroken(itemStack2!!, maxBreakPercent) }
                if (score < 0) continue

                if (score > bestScore) {
                    bestScore = score
                    bestSlot = i
                }
            }

            return bestSlot
        }

        private fun isAlmostBroken(itemStack: ItemStack, maxBreakPercent: Int): Boolean {
            return (itemStack.maxDamage - itemStack.damage) < (itemStack.maxDamage * maxBreakPercent / 100)
        }


        private fun getScore(itemStack: ItemStack, state: BlockState, silkTouchEnderChest: Boolean, fortuneOre: Boolean, enchantPreference: EnchantPreference, good: Predicate<ItemStack?>): Double {
            if (!good.test(itemStack) || !isTool(itemStack)) return (-1).toDouble()
            if (!itemStack.isSuitableFor(state) && !(itemStack.item is SwordItem && (state.block is BambooBlock || state.block is BambooShootBlock)) && !(itemStack.item is ShearsItem && state.block is LeavesBlock || state.isIn(BlockTags.WOOL))) return (-1).toDouble()

            if (silkTouchEnderChest && state.block === Blocks.ENDER_CHEST && EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, itemStack) == 0) {
                return (-1).toDouble()
            }

            if ((fortuneOre && isFortunable(state.block)) && EnchantmentHelper.getLevel(Enchantments.FORTUNE, itemStack) == 0) {
                return (-1).toDouble()
            }

            var score = 0.0

            score += (itemStack.getMiningSpeedMultiplier(state) * 1000).toDouble()
            score += EnchantmentHelper.getLevel(Enchantments.UNBREAKING, itemStack).toDouble()
            score += EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, itemStack).toDouble()
            score += EnchantmentHelper.getLevel(Enchantments.MENDING, itemStack).toDouble()

            if (enchantPreference == EnchantPreference.Fortune) score += EnchantmentHelper.getLevel(Enchantments.FORTUNE, itemStack).toDouble()
            if (enchantPreference == EnchantPreference.SilkTouch) score += EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, itemStack).toDouble()

            if (itemStack.item is SwordItem) {
                val item = itemStack.item as SwordItem
                if (state.block is BambooBlock || state.block is BambooShootBlock) score += 9000 + (item.getMiningSpeedMultiplier(itemStack, state) * 1000)
            }

            return score
        }

        private fun isTool(item: Item?): Boolean {
            return item is ToolItem || item is ShearsItem
        }

        private fun isTool(itemStack: ItemStack): Boolean {
            return isTool(itemStack.item)
        }


        private fun isFortunable(block: Block): Boolean {
            if (block == Blocks.ANCIENT_DEBRIS) return false
            return ORES.contains(block) || block is CropBlock
        }
        @Suppress("unused")
        enum class EnchantPreference {
            None,
            Fortune,
            SilkTouch
        }
    }
}