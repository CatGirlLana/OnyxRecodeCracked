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
import net.integr.modules.filters.Filter
import net.integr.modules.management.UiModule
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.rendering.RenderingEngine
import net.integr.rendering.uisystem.Box
import net.minecraft.client.gui.DrawContext

class ArmorHudModule : UiModule("Armor Hud", "Render your armor durability on screen", "armorHud", 85, 60, listOf(Filter.Render)) {
    init {
        settings.add(BooleanSetting("Use Percentage", "Changes the rendering to be % instead of the default rendering", "percent"))
    }

    private val background = Box(0, 0, 60, 85, null, false, outlined = true)

    override fun render(context: DrawContext, originX: Int, originY: Int, delta: Float) {
        val helmetItem = Onyx.MC.player!!.inventory.armor[3]
        val chestPlateItem = Onyx.MC.player!!.inventory.armor[2]
        val leggingsItem = Onyx.MC.player!!.inventory.armor[1]
        val bootsItem = Onyx.MC.player!!.inventory.armor[0]

        if (settings.getById<BooleanSetting>("percent")!!.isEnabled()) {
            background.xSize = 60
            super.width = 60
            background.update(originX, originY).render(context, 0, 0, delta)

            context.drawItem(helmetItem, originX + 5, originY+5)
            context.drawItem(chestPlateItem, originX + 5, originY + 25)
            context.drawItem(leggingsItem, originX + 5, originY + 45)
            context.drawItem(bootsItem, originX + 5, originY + 65)

            val helmetPercent = if (helmetItem.maxDamage != 0) (helmetItem.maxDamage.toDouble() - helmetItem.damage.toDouble())  / (helmetItem.maxDamage.toDouble()) * 100.0 else 0
            val chestPlatePercent = if (chestPlateItem.maxDamage != 0) (chestPlateItem.maxDamage.toDouble() - chestPlateItem.damage.toDouble()) / (chestPlateItem.maxDamage.toDouble()) * 100.0 else 0
            val leggingsPercent = if (leggingsItem.maxDamage != 0) (leggingsItem.maxDamage.toDouble() - leggingsItem.damage.toDouble()) / (leggingsItem.maxDamage.toDouble()) * 100.0 else 0
            val bootsPercent = if (bootsItem.maxDamage != 0) (bootsItem.maxDamage.toDouble() - bootsItem.damage.toDouble()) / (bootsItem.maxDamage.toDouble()) * 100.0 else 0

            RenderingEngine.Text.draw(context, "${if (helmetPercent.toInt() > 0) helmetPercent.toInt() else "- "}%", originX+25, originY+5+4, Variables.guiColor)
            RenderingEngine.Text.draw(context, "${if (chestPlatePercent.toInt() > 0) chestPlatePercent.toInt() else "- "}%", originX+25, originY+25+4, Variables.guiColor)
            RenderingEngine.Text.draw(context, "${if (leggingsPercent.toInt() > 0) leggingsPercent.toInt() else "- "}%", originX+25, originY+45+4, Variables.guiColor)
            RenderingEngine.Text.draw(context, "${if (bootsPercent.toInt() > 0) bootsPercent.toInt() else "- "}%", originX+25, originY+65+4, Variables.guiColor)
        } else {
            background.xSize = 75
            super.width = 75
            background.update(originX, originY).render(context, 0, 0, delta)

            context.drawItem(helmetItem, originX + 5, originY+5)
            context.drawItem(chestPlateItem, originX + 5, originY + 25)
            context.drawItem(leggingsItem, originX + 5, originY + 45)
            context.drawItem(bootsItem, originX + 5, originY + 65)


            RenderingEngine.Text.draw(context, if (helmetItem.maxDamage > 0) "${helmetItem.maxDamage - helmetItem.damage}/${helmetItem.maxDamage}" else "-", originX+25, originY+5+4, Variables.guiColor)
            RenderingEngine.Text.draw(context, if (chestPlateItem.maxDamage > 0) "${chestPlateItem.maxDamage - chestPlateItem.damage}/${chestPlateItem.maxDamage}" else "-", originX+25, originY+25+4, Variables.guiColor)
            RenderingEngine.Text.draw(context, if (leggingsItem.maxDamage > 0) "${leggingsItem.maxDamage - leggingsItem.damage}/${leggingsItem.maxDamage}" else "-", originX+25, originY+45+4, Variables.guiColor)
            RenderingEngine.Text.draw(context, if (bootsItem.maxDamage > 0) "${bootsItem.maxDamage - bootsItem.damage}/${bootsItem.maxDamage}" else "-", originX+25, originY+65+4, Variables.guiColor)
        }
    }
}