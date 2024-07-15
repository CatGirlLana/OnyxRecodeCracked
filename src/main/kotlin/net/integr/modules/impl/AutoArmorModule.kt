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
import net.integr.utilities.game.inventory.InvUtils
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import java.util.*

class AutoArmorModule : Module("Auto Armor", "Automatically equips armor for you", "autoArmor", mutableListOf(Filter.Util)) {
    @Suppress("UNUSED_PARAMETER")
    @EventListen
    fun onTick(event: PreTickEvent) {
        doAutoArmor()
    }

    private fun doAutoArmor() {
        if (Onyx.MC.currentScreen is HandledScreen<*> && Onyx.MC.currentScreen !is InventoryScreen) return

        val player: ClientPlayerEntity = Onyx.MC.player!!
        val inventory = player.inventory

        val bestArmorSlots = IntArray(4)
        val bestArmorValues = IntArray(4)

        for (type in 0..3) {
            bestArmorSlots[type] = -1

            val stack = inventory.getArmorStack(type)
            if (stack.isEmpty || stack.item !is ArmorItem) continue

            val item = stack.item as ArmorItem
            bestArmorValues[type] = getArmorValue(item, stack)
        }


        for (slot in 0..35) {
            val stack = inventory.getStack(slot)

            if (stack.isEmpty || stack.item !is ArmorItem) continue

            val item = stack.item as ArmorItem
            val armorType = item.slotType.entitySlotId
            val armorValue = getArmorValue(item, stack)

            if (armorValue > bestArmorValues[armorType]) {
                bestArmorSlots[armorType] = slot
                bestArmorValues[armorType] = armorValue
            }
        }

        val types = ArrayList(mutableListOf(0, 1, 2, 3))
        types.shuffle()

        for (type in types) {
            var slot = bestArmorSlots[type]
            if (slot == -1) continue

            val oldArmor = inventory.getArmorStack(type)
            if (!oldArmor.isEmpty && inventory.emptySlot == -1) continue

            if (slot < 9) slot += 36

            if (!oldArmor.isEmpty) InvUtils.quickMove(9 - type)
            InvUtils.quickMove(slot)

            break
        }
    }

    private fun getArmorValue(item: ArmorItem, stack: ItemStack): Int {
        val armorPoints = item.protection
        val prtPoints: Int
        val armorToughness = item.toughness.toInt()
        val armorType = item.material.getProtection(ArmorItem.Type.LEGGINGS)

        val protection = Enchantments.PROTECTION
        val prtLvl = EnchantmentHelper.getLevel(protection, stack)

        val player: ClientPlayerEntity = Onyx.MC.player!!
        val dmgSource = player.damageSources.playerAttack(player)
        prtPoints = protection.getProtectionAmount(prtLvl, dmgSource)


        return armorPoints * 5 + prtPoints * 3 + armorToughness + armorType
    }
}