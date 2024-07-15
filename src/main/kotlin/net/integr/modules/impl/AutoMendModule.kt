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

@file:Suppress("DuplicatedCode", "unused", "UNUSED_PARAMETER")

package net.integr.modules.impl

import net.integr.Onyx
import net.integr.event.PreTickEvent
import net.integr.eventsystem.EventListen
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.utilities.game.command.CommandUtils
import net.integr.utilities.game.interaction.ClickUtils
import net.integr.utilities.game.interaction.RotationUtils
import net.integr.utilities.game.inventory.InvUtils
import net.integr.utilities.game.pausers.CombatPauser
import net.integr.utilities.game.rotationfake.RotationLocker
import net.minecraft.item.Items

class AutoMendModule : Module("Auto Mend", "Automatically repairs your armor", "autoMend", mutableListOf(Filter.Util)) {
    init {
        settings
            .add(BooleanSetting("Pause Combat", "Pause combat modules when active", "pauseCombat"))
            .add(BooleanSetting("Redupe", "Redupe the item", "redupe"))
    }

    override fun onDisable() {
        RotationLocker.unLock()
        CombatPauser.resume()
    }

    private var redupeCounter = 0

    @EventListen
    fun onTick(event: PreTickEvent) {
        if (redupeCounter > 0) {
            redupeCounter--
        }

        val helmetItem = Onyx.MC.player!!.inventory.armor[3]
        val chestPlateItem = Onyx.MC.player!!.inventory.armor[2]
        val leggingsItem = Onyx.MC.player!!.inventory.armor[1]
        val bootsItem = Onyx.MC.player!!.inventory.armor[0]

        val helmetPercent = if (helmetItem.maxDamage != 0) (helmetItem.maxDamage.toDouble() - helmetItem.damage.toDouble()) / (helmetItem.maxDamage.toDouble()) * 100.0 else Double.MAX_VALUE
        val chestPlatePercent = if (chestPlateItem.maxDamage != 0) (chestPlateItem.maxDamage.toDouble() - chestPlateItem.damage.toDouble()) / (chestPlateItem.maxDamage.toDouble()) * 100.0 else Double.MAX_VALUE
        val leggingsPercent = if (leggingsItem.maxDamage != 0) (leggingsItem.maxDamage.toDouble() - leggingsItem.damage.toDouble()) / (leggingsItem.maxDamage.toDouble()) * 100.0 else Double.MAX_VALUE
        val bootsPercent = if (bootsItem.maxDamage != 0) (bootsItem.maxDamage.toDouble() - bootsItem.damage.toDouble()) / (bootsItem.maxDamage.toDouble()) * 100.0 else Double.MAX_VALUE

        if (helmetPercent >= 100 && chestPlatePercent >= 100 && leggingsPercent >= 100 && bootsPercent >= 100) {
            RotationLocker.unLock()
            CombatPauser.resume()

            disable()
            return
        }

        val pauseCombat = settings.getById<BooleanSetting>("pauseCombat")!!.isEnabled()

        val slot = InvUtils.findInHotbar(Items.EXPERIENCE_BOTTLE)

        if (slot != -1 && !Onyx.MC.world!!.getBlockState(Onyx.MC.player!!.blockPos.down()).isAir) {
            if (pauseCombat) {
                CombatPauser.pause()
            } else CombatPauser.resume()

            val pre = InvUtils.getSelectedSlot()
            InvUtils.selectSlotPacket(slot)
            InvUtils.selectSlot(slot)

            if (redupeCounter == 0) {
                CommandUtils.sendCommand("dupe 2")

                redupeCounter = 20
            }

            RotationLocker.lock()
            RotationUtils.lookAt(Onyx.MC.player!!.pos.add(0.0, -1.0, 0.0), true)

            ClickUtils.rightClick()
            InvUtils.selectSlot(pre)
        } else {
            RotationLocker.unLock()
            CombatPauser.resume()
        }
    }
}