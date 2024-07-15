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

package net.integr.rendering.screens

import net.integr.Onyx
import net.integr.modules.management.Module
import net.integr.modules.management.ModuleManager
import net.integr.modules.management.settings.Setting
import net.integr.rendering.uisystem.Box
import net.integr.rendering.uisystem.IconButton
import net.integr.rendering.uisystem.UiLayout
import net.integr.rendering.uisystem.base.HelixUiElement
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import kotlin.math.max
import kotlin.math.min

class ModuleScreen(mod: Module) : Screen(Text.literal("${mod.displayName} Config")) {
    private val layout = UiLayout()

    private var backgroundBox: Box? = null
    private var titleBox: Box? = null
    private var closeButton: IconButton? = null

    private var preInitSizeY = 5
    private var preInitSizeX = 5 + 200 + 5

    private val settings: MutableList<PosWrapper> = mutableListOf()

    private var currentScrollPaneY = 0

    private var mod: Module? = null

    init {
        this.mod = mod
        preInitSizeY = min((height/1.8).toInt(), (mod.settings.options.count()) * 25) + 5

        var currY = 5

        for (s in mod.settings.options) {
            val b = layout.add(s.getUiElement()!!)

            s.getUiElement()

            settings += PosWrapper(b, s, currY)

            currY += 20 + 5
        }

        titleBox = layout.add(Box(width/2-preInitSizeX/2, height/2-preInitSizeY/2-45, preInitSizeX, 20, mod.displayName, false, outlined = true)) as Box

        backgroundBox = layout.add(Box(width/2-preInitSizeX/2, height/2-preInitSizeY/2, preInitSizeX, preInitSizeY, null, false)) as Box

        closeButton = layout.add(IconButton(width/2+preInitSizeX/2-15,height/2-preInitSizeY/2-45, 19, 19, "x", "Return to the main gui") {
            Onyx.MC.setScreen(MenuScreen.INSTANCE)
        }) as IconButton
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        preInitSizeY = min((height/1.8).toInt(), (mod!!.settings.options.count()) * 25) + 5

        backgroundBox!!.ySize = preInitSizeY
        backgroundBox!!.update(width/2-preInitSizeX/2, height/2-preInitSizeY/2).render(context, mouseX, mouseY, delta)
        titleBox!!.update(width/2-preInitSizeX/2, height/2-preInitSizeY/2-45).render(context, mouseX, mouseY, delta)
        closeButton!!.update(width/2+preInitSizeX/2-15,height/2-preInitSizeY/2-45).render(context, mouseX, mouseY, delta)

        for (s in settings) {
            val cTop = height / 2 - preInitSizeY / 2 + s.yO - currentScrollPaneY
            val cBottom = height / 2 - preInitSizeY / 2 + s.yO + 25 - currentScrollPaneY

            if (cBottom < height / 2 - preInitSizeY / 2+5 || cTop > height / 2 - preInitSizeY / 2 + backgroundBox!!.ySize-5) {
                layout.lock(s.element)
            } else {
                layout.unLock(s.element)
            }

            context.enableScissor(width / 2 - preInitSizeX / 2+5, height / 2 - preInitSizeY / 2+5, width / 2 - preInitSizeX / 2 + backgroundBox!!.xSize-5, height / 2 - preInitSizeY / 2 + backgroundBox!!.ySize-5)
            s.element.update(width/2-preInitSizeX/2+5, height/2-preInitSizeY/2 + s.yO - currentScrollPaneY).render(context, mouseX, mouseY, delta)
            context.disableScissor()

            if (s.setting != null) s.setting.onUpdate(s.element)
        }

        outer@for (s in settings) {
            val cTop = height / 2 - preInitSizeY / 2 + s.yO - currentScrollPaneY
            val cBottom = height / 2 - preInitSizeY / 2 + s.yO + 25 - currentScrollPaneY

            if (cBottom < height / 2 - preInitSizeY / 2+5 || cTop > height / 2 - preInitSizeY / 2 + backgroundBox!!.ySize-5) {
                continue@outer
            }

            s.element.renderTooltip(context, mouseX, mouseY, delta)
        }

        closeButton!!.renderTooltip(context, mouseX, mouseY, delta)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        currentScrollPaneY -= verticalAmount.toInt() * 10

        if (currentScrollPaneY <= 0) currentScrollPaneY = 0

        currentScrollPaneY = min(currentScrollPaneY, max(0, ((settings.count()) * 25 - preInitSizeY)) + 5)
        return true
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

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val callback = layout.onKey(keyCode, scanCode, modifiers)
        return if (callback) true else super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun close() {
        Onyx.MC.setScreen(MenuScreen.INSTANCE)
    }

    data class PosWrapper(val element: HelixUiElement, val setting: Setting?, val yO: Int)
}