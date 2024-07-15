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

import net.integr.Onyx
import net.integr.Variables
import net.integr.rendering.RenderingEngine
import net.integr.rendering.uisystem.base.HelixUiElement
import net.integr.utilities.round
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.MathHelper
import org.jetbrains.annotations.Nullable
import org.lwjgl.glfw.GLFW

@Suppress("MemberVisibilityCanBePrivate")
class Slider(var xPos: Int, var yPos: Int, var xSize: Int, var ySize: Int, @Nullable var text: String?, var textCentered: Boolean, var tooltip: String, val min: Double, var max: Double) : Drawable, HelixUiElement {
    private var dragging = false
    var fillX = 0
    var text2 = text

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val color = Variables.guiColor
        val textColor = Variables.guiBack
        val disabledColor = Variables.guiDisabled
        val x1 = xPos
        val x2 = xPos + xSize
        val y1 = yPos
        val y2 = yPos + ySize

        if (dragging) {
            fillX = mouseX - x1
            text2 = text + valueFromFillX().round(1)
        }

        RenderingEngine.TwoDimensional.fillRoundNoOutline(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), disabledColor, context, 0.05f, 9f)
        if (valueFromFillX().round(1) > min) RenderingEngine.TwoDimensional.fillRoundNoOutline(x1.toFloat(), y1.toFloat(), MathHelper.clamp(x1 + fillX, x1+20, x2).toFloat(), y2.toFloat(), color, context, 0.05f, 9f)

        if (text != null) {
            if (textCentered) {
                RenderingEngine.Text.draw(context, text2!!, x1 + xSize / 2 - Onyx.MC.textRenderer.getWidth(text2) / 2 - 4, y1 + ySize / 2 - 4, textColor)
            } else {
                RenderingEngine.Text.draw(context, text2!!, x1 + 2, y1 + ySize / 2 - 4, textColor)
            }
        }
    }

    fun setValue(value: Double) {
        fillX = xFillFromValue(value)
        text2 = text + valueFromFillX().round(1)
    }

    fun valueFromFillX(): Double {
        val lowerBound = min // -2
        val upperBound = max // +2

        val sectionCount = xSize // 200

        val valueRange = upperBound - lowerBound // 4

        val sectionValue = valueRange/sectionCount //

        return MathHelper.clamp(fillX * sectionValue + min, min, max)
    }

    fun xFillFromValue(value: Double): Int {
        val lowerBound = min
        val upperBound = max

        val sectionCount = xSize

        val valueRange = upperBound - lowerBound

        val sectionValue = valueRange/sectionCount

        return ((value - min) / sectionValue).toInt()
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
                dragging = true
                Onyx.MC.soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F))
            }
        }
    }

    override fun onRelease(mouseX: Double, mouseY: Double, button: Int) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (dragging) {
                dragging = false
            }
        }
    }

    override fun onKey(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        // Do Nothing
        return false
    }

    override fun update(xPos: Int, yPos: Int): Slider {
        this.xPos = xPos
        this.yPos = yPos

        return this
    }
}