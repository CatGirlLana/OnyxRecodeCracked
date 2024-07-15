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

@file:Suppress("unused", "DuplicatedCode", "UNUSED_PARAMETER")

package net.integr.modules.impl

import net.integr.Onyx
import net.integr.event.PreTickEvent
import net.integr.eventsystem.EventListen
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.SliderSetting
import net.integr.utilities.game.notification.NotificationHandler

class ArmorDurabilityNotifierModule : Module("Armor Durability Notifier", "Notifies you when your armor is low on durability", "armorDurabilityNotifier", mutableListOf(Filter.Util)) {
    init {
        settings.add(SliderSetting("Piece Dura: ", "Minimum percentage of durability for a piece to be repaired", "minDura", 1.0, 100.0))
    }

    private var timer = 0;

    @EventListen
    fun onTick(event: PreTickEvent) {
        if (timer > 0) {
            timer--
            return
        }

        val helmetItem = Onyx.MC.player!!.inventory.armor[3]
        val chestPlateItem = Onyx.MC.player!!.inventory.armor[2]
        val leggingsItem = Onyx.MC.player!!.inventory.armor[1]
        val bootsItem = Onyx.MC.player!!.inventory.armor[0]

        val helmetPercent = if (helmetItem.maxDamage != 0) (helmetItem.maxDamage.toDouble() - helmetItem.damage.toDouble()) / (helmetItem.maxDamage.toDouble()) * 100.0 else Double.MAX_VALUE
        val chestPlatePercent = if (chestPlateItem.maxDamage != 0) (chestPlateItem.maxDamage.toDouble() - chestPlateItem.damage.toDouble()) / (chestPlateItem.maxDamage.toDouble()) * 100.0 else Double.MAX_VALUE
        val leggingsPercent = if (leggingsItem.maxDamage != 0) (leggingsItem.maxDamage.toDouble() - leggingsItem.damage.toDouble()) / (leggingsItem.maxDamage.toDouble()) * 100.0 else Double.MAX_VALUE
        val bootsPercent = if (bootsItem.maxDamage != 0) (bootsItem.maxDamage.toDouble() - bootsItem.damage.toDouble()) / (bootsItem.maxDamage.toDouble()) * 100.0 else Double.MAX_VALUE

        val min = settings.getById<SliderSetting>("minDura")!!.getSetValue()

        if (helmetPercent < min || chestPlatePercent < min || leggingsPercent < min || bootsPercent < min) {
            NotificationHandler.notify("You should repair your Armor!")
            NotificationHandler.ping()
        }

        timer = 60
    }
}