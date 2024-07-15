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
import net.integr.modules.filters.Filter
import net.integr.modules.management.ModuleManager
import net.integr.rendering.uisystem.*
import net.integr.rendering.uisystem.base.HelixUiElement
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.lwjgl.glfw.GLFW
import kotlin.math.max
import kotlin.math.min

class MenuScreen : Screen(Text.literal("Onyx Menu")) {
    companion object {
        val INSTANCE: Screen = MenuScreen()
    }

    private val layout = UiLayout()

    private var preInitSizeY = 0
    private var preInitSizeX = 5 + 200 + 5 + 200 + 5 + 200 + 5

    private var backgroundBox: Box? = null
    private var actionRowBox: Box? = null
    private var filterRowBox: Box? = null

    private var searchBar: Box? = null
    private var search = ""
    private var shiftDown = false

    private var moveButton: IconButton? = null
    private var settingsButton: IconButton? = null

    private var selectedFilters: MutableList<Filter> = mutableListOf()
    private var filters: MutableList<PosWrapper> = mutableListOf()

    private val modules: MutableList<PosWrapper> = mutableListOf()

    private var currentScrollPaneY = 0

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        preInitSizeY = (height/1.8).toInt()

        backgroundBox!!.ySize = preInitSizeY

        backgroundBox!!.update(width / 2 - preInitSizeX / 2, height / 2 - preInitSizeY / 2).render(context, mouseX, mouseY, delta)
        actionRowBox!!.update(width / 2 - preInitSizeX / 2, height / 2 - preInitSizeY / 2 - 45 - 40).render(context, mouseX, mouseY, delta)
        filterRowBox!!.update(width / 2 - preInitSizeX / 2, height / 2 - preInitSizeY / 2 - 45).render(context, mouseX, mouseY, delta)

        moveButton!!.update(width / 2 + preInitSizeX / 2 - 19, height / 2 - preInitSizeY / 2 - 45 - 40).render(context, mouseX, mouseY, delta)
        settingsButton!!.update(width / 2 + preInitSizeX / 2 - 43, height / 2 - preInitSizeY / 2 - 45 - 40).render(context, mouseX, mouseY, delta)

        searchBar!!.update(width / 2 + preInitSizeX / 2 - 155, height / 2 - preInitSizeY / 2 - 45 + 5).render(context, mouseX, mouseY, delta)

        reloadList()

        for (f in filters) {
            f.element.update(width / 2 - preInitSizeX / 2 + f.xO, height / 2 - preInitSizeY / 2 + f.yO).render(context, mouseX, mouseY, delta)
        }

        outer@ for (l in modules) {
            for (f in selectedFilters) if (!l.filters.contains(f)) {
                layout.lock(l.element)
                layout.mark(l.element)
                continue@outer
            }

            if (!matchesSearchingPredicate(search, l.searchingTags)) {
                layout.lock(l.element)
                layout.mark(l.element)
                continue@outer
            }

            val cTop = height / 2 - preInitSizeY / 2 + l.yO - currentScrollPaneY
            val cBottom = height / 2 - preInitSizeY / 2 + l.yO + (l.element as ModuleButton).ySize - currentScrollPaneY

            if (cBottom < height / 2 - preInitSizeY / 2+5 || cTop > height / 2 - preInitSizeY / 2 + backgroundBox!!.ySize-5) {
                layout.lock(l.element)
            } else {
                layout.unLock(l.element)
                layout.unMark(l.element)
            }

            context.enableScissor(width / 2 - preInitSizeX / 2+5, height / 2 - preInitSizeY / 2+5, width / 2 - preInitSizeX / 2 + backgroundBox!!.xSize-5, height / 2 - preInitSizeY / 2 + backgroundBox!!.ySize-5)
            l.element.update(width / 2 - preInitSizeX / 2 + l.xO, height / 2 - preInitSizeY / 2 + l.yO - currentScrollPaneY).render(context, mouseX, mouseY, delta)
            context.disableScissor()
        }

        for (f in filters) f.element.renderTooltip(context, mouseX, mouseY, delta)

        outer@ for (l in modules) {
            for (f in selectedFilters) if (!l.filters.contains(f)) continue@outer

            if (!matchesSearchingPredicate(search, l.searchingTags)) continue@outer

            val cTop = height / 2 - preInitSizeY / 2 + l.yO - currentScrollPaneY
            val cBottom = height / 2 - preInitSizeY / 2 + l.yO + (l.element as ModuleButton).ySize - currentScrollPaneY

            if (cBottom < height / 2 - preInitSizeY / 2+5 || cTop > height / 2 - preInitSizeY / 2 + backgroundBox!!.ySize-5) {
                continue@outer
            }

            l.element.renderTooltip(context, mouseX, mouseY, delta)

        }

        moveButton!!.renderTooltip(context, mouseX, mouseY, delta)
        settingsButton!!.renderTooltip(context, mouseX, mouseY, delta)
    }

    private fun getXFromIndex(index: Int): Int {
        return when (index) {
            0 -> 5
            1 -> 5 + 200 + 5
            2 -> 5 + 200 + 5 + 200 + 5
            else -> 1
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

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        currentScrollPaneY -= verticalAmount.toInt() * 10

        if (currentScrollPaneY <= 0) currentScrollPaneY = 0

        currentScrollPaneY = min(currentScrollPaneY, max(0, (ModuleManager.modules.count { matchesSearchingPredicate(search, it.getSearchingTags()) } / 3+2) * 25 - preInitSizeY))
        return true
    }

    init {
        preInitSizeY = height/2

        var currY = 5
        var mCount = 0

        for (m in ModuleManager.modules) {
            val b = layout.add(ModuleButton(width / 2 - preInitSizeX / 2 + getXFromIndex(ModuleManager.modules.indexOf(m) % 3), height / 2 - preInitSizeY / 2 + currY, 200, 20, m.displayName, true, m.toolTip, false, m) {
                search = ""
                currentScrollPaneY = 0
                searchBar!!.text = "Search modules..."
            })

            modules += PosWrapper(b, getXFromIndex(ModuleManager.modules.indexOf(m) % 3), currY, m.filters, m.getSearchingTags())

            if (mCount == 2) {
                currY += 20 + 5
                mCount = 0
            } else mCount++
        }

        var currX = 5

        for (f in Filter.entries) {
            val fil = layout.add(ToggleButton(width / 2 - preInitSizeX / 2 + currX, height / 2 - preInitSizeY / 2 - 45 - 5, 65, 20, " " + f.name, true, "Select a filter", true) {
                if (it.enabled) selectedFilters += f else selectedFilters -= f
            })

            filters += PosWrapper(fil, currX, -40)

            currX += 70
        }

        backgroundBox = layout.add(Box(width / 2 - preInitSizeX / 2, height / 2 - preInitSizeY / 2, preInitSizeX, preInitSizeY, null, false)) as Box

        actionRowBox = layout.add(Box(width / 2 - preInitSizeX / 2, height / 2 - preInitSizeY / 2 - 45 - 40, preInitSizeX, 20, "Onyx", false)) as Box
        filterRowBox = layout.add(Box(width / 2 - preInitSizeX / 2, height / 2 - preInitSizeY / 2 - 45, preInitSizeX, 30, null, false)) as Box

        searchBar = layout.add(Box(width / 2 + preInitSizeX / 2 - 155, height / 2 - preInitSizeY / 2 - 45 + 5, 150, 20, "" + Formatting.ITALIC + "Search modules...", false, innerIsDisabled = true)) as Box

        moveButton = layout.add(IconButton(width / 2 + preInitSizeX / 2 - 19, height / 2 - preInitSizeY / 2 - 45 - 40, 20, 20, "⌂", "Move the UI elements") {
            Onyx.MC.setScreen(UiMoveScreen.INSTANCE)
        }) as IconButton

        settingsButton = layout.add(IconButton(width / 2 + preInitSizeX / 2 - 43, height / 2 - preInitSizeY / 2 - 45 - 40, 20, 20, "≡", "Open the general settings") {
            Onyx.MC.setScreen(SettingsScreen())
        }) as IconButton
    }

    data class PosWrapper(
        val element: HelixUiElement,
        var xO: Int,
        var yO: Int,
        val filters: List<Filter> = listOf(),
        val searchingTags: List<String> = listOf()
    )

    private fun reloadList() {
        var currY = 5
        var mCount = 0

        for (pw in modules) {
            if (layout.isMarked(pw.element)) continue

            pw.xO = getXFromIndex(modules.filter { !layout.isMarked(it.element) }.indexOf(pw) % 3)
            pw.yO = currY

            if (mCount == 2) {
                currY += 20 + 5
                mCount = 0
            } else mCount++
        }
    }

    private fun matchesSearchingPredicate(search: String, tags: List<String>): Boolean {
        for (t in tags) {
            for (ta in tags) {
                if (ta.contains(search.lowercase().filter { it != ' ' })) return true
            }
        }

        return false
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        when (keyCode) {
            GLFW.GLFW_KEY_BACKSPACE -> {
                if (search.isNotEmpty()) search = search.substring(0, search.length - 1)
            }

            GLFW.GLFW_KEY_RIGHT_SHIFT -> {
                shiftDown = true
            }

            GLFW.GLFW_KEY_LEFT_SHIFT -> {
                shiftDown = true
            }

            GLFW.GLFW_KEY_ESCAPE -> {
                search = ""
                currentScrollPaneY = 0
            }

            GLFW.GLFW_KEY_SPACE -> {
                search += " "
            }

            else -> {
                if (shiftDown) {
                    val kn = GLFW.glfwGetKeyName(keyCode, scanCode)
                    if (kn != null) search += kn.uppercase()
                } else {
                    val kn = GLFW.glfwGetKeyName(keyCode, scanCode)
                    if (kn != null) search += kn
                }
            }
        }

        if (search == "") {
            searchBar!!.text = "" + Formatting.ITALIC + "Search modules..."
        } else {
            searchBar!!.text = search
        }

        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        when (keyCode) {
            GLFW.GLFW_KEY_RIGHT_SHIFT -> {
                shiftDown = false
            }
            GLFW.GLFW_KEY_LEFT_SHIFT -> {
                shiftDown = false
            }
        }

        return super.keyReleased(keyCode, scanCode, modifiers)
    }
}