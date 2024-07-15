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

@file:Suppress("DuplicatedCode")

package net.integr.rendering.uisystem

import kotlinx.coroutines.Runnable
import net.integr.Onyx
import net.integr.Variables
import net.integr.rendering.RenderingEngine
import net.integr.rendering.uisystem.base.HelixUiElement
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents
import org.lwjgl.glfw.GLFW

@Suppress("MemberVisibilityCanBePrivate")
class IconButton(var xPos: Int, var yPos: Int, var xSize: Int, var ySize: Int, var text: String, var tooltip: String, val action: Runnable) : Drawable, HelixUiElement {

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val colorEnabled = Variables.guiColor

        val x1 = xPos
        val y1 = yPos

        RenderingEngine.Text.draw(context, text, x1 + xSize / 2 - Onyx.MC.textRenderer.getWidth(text) / 2 - 4, y1 + ySize / 2 - 4, colorEnabled)
    }

    override fun renderTooltip(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val x1 = xPos
        val x2 = xPos + xSize
        val y1 = yPos
        val y2 = yPos + ySize


        if (mouseX in (x1 + 1)..<x2 && mouseY > y1 && mouseY < y2) {
            val explainXSize: Int = Onyx.MC.textRenderer.getWidth(tooltip) + 30
            val explainingBox = Box(xPos, yPos, explainXSize, ySize, tooltip, true)

            explainingBox.xPos = mouseX + 10
            explainingBox.yPos = mouseY
            explainingBox.render(context, mouseX, mouseY, delta)
        }
    }

    override fun onClick(mouseX: Double, mouseY: Double, button: Int) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            val x1 = xPos
            val x2 = xPos + xSize
            val y1 = yPos
            val y2 = yPos + ySize


            if (mouseX.toInt() in (x1 + 1)..<x2 && mouseY > y1 && mouseY < y2) {
                action.run()
                Onyx.MC.soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F))
            }
        }
    }

    override fun onRelease(mouseX: Double, mouseY: Double, button: Int) {
        // Do Nothing
    }

    override fun onKey(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        // Do Nothing
        return false
    }

    override fun update(xPos: Int, yPos: Int): IconButton {
        this.xPos = xPos
        this.yPos = yPos

        return this
    }
}