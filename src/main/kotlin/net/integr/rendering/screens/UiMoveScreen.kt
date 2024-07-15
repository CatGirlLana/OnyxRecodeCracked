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

package net.integr.rendering.screens

import net.integr.Onyx
import net.integr.modules.management.ModuleManager
import net.integr.modules.management.UiModule
import net.integr.rendering.uisystem.MovableBox
import net.integr.rendering.uisystem.UiLayout
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class UiMoveScreen : Screen(Text.literal("Onyx Ui-Move Screen")){
    companion object {
        val INSTANCE: Screen = UiMoveScreen()
    }

    private val layout = UiLayout()

    private val boxes: MutableList<BoxWrapper> = mutableListOf()

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        for (w in boxes) {
            val box = w.box
            val module = w.module

            box.ySize = module.height
            box.xSize = module.width
            box.render(context, mouseX, mouseY, delta)
            module.updatePosition(box.xPos, box.yPos)
        }
    }

    init {
        for (m in ModuleManager.getUiModules()) {
            val module = m as UiModule

            val box = layout.add(module.getPlaceholderBox()) as MovableBox

            boxes.add(BoxWrapper(box, module))
        }
    }

    override fun renderBackground(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        // Do Nothing
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        layout.onClick(mouseX, mouseY, button)
        return true
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        layout.onRelease(mouseX, mouseY, button)
        return true
    }

    override fun close() {
        Onyx.MC.setScreen(MenuScreen.INSTANCE)
    }

    data class BoxWrapper(val box: MovableBox, val module: UiModule)

}