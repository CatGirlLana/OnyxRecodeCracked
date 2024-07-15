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
import net.integr.Settings
import net.integr.modules.management.settings.Setting
import net.integr.rendering.uisystem.Box
import net.integr.rendering.uisystem.IconButton
import net.integr.rendering.uisystem.UiLayout
import net.integr.rendering.uisystem.base.HelixUiElement
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class SettingsScreen : Screen(Text.literal("Settings")) {
    private val layout = UiLayout()

    private var backgroundBox: Box? = null
    private var titleBox: Box? = null
    private var closeButton: IconButton? = null

    private var preInitSizeY = 5
    private var preInitSizeX = 5 + 200 + 5

    private val settings: MutableList<PosWrapper> = mutableListOf()

    init {
        preInitSizeY += Settings.INSTANCE.settings.options.count() * 25

        var currY = 5

        for (s in Settings.INSTANCE.settings.options) {
            val b = layout.add(s.getUiElement()!!)

            s.getUiElement()

            settings += PosWrapper(b, s, currY)

            currY += 20 + 5
        }

        titleBox = layout.add(Box(width/2-preInitSizeX/2, height/2-preInitSizeY/2-45, preInitSizeX, 20, "Settings", false, outlined = true)) as Box

        backgroundBox = layout.add(Box(width/2-preInitSizeX/2, height/2-preInitSizeY/2, preInitSizeX, preInitSizeY, null, false)) as Box

        closeButton = layout.add(IconButton(width/2+preInitSizeX/2-15,height/2-preInitSizeY/2-45, 19, 19, "x", "Return to the main gui") {
            Onyx.MC.setScreen(MenuScreen.INSTANCE)
        }) as IconButton
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        backgroundBox!!.update(width/2-preInitSizeX/2, height/2-preInitSizeY/2).render(context, mouseX, mouseY, delta)
        titleBox!!.update(width/2-preInitSizeX/2, height/2-preInitSizeY/2-45).render(context, mouseX, mouseY, delta)
        closeButton!!.update(width/2+preInitSizeX/2-15,height/2-preInitSizeY/2-45).render(context, mouseX, mouseY, delta)

        for (s in settings) {
            s.element.update(width/2-preInitSizeX/2+5, height/2-preInitSizeY/2 + s.yO).render(context, mouseX, mouseY, delta)
            if (s.setting != null) s.setting.onUpdate(s.element)
        }

        for (s in settings) s.element.renderTooltip(context, mouseX, mouseY, delta)

        closeButton!!.renderTooltip(context, mouseX, mouseY, delta)
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