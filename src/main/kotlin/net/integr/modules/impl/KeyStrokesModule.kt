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
import net.integr.modules.filters.Filter
import net.integr.modules.management.UiModule
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.rendering.uisystem.Box
import net.integr.rendering.uisystem.ToggleButton
import net.minecraft.client.gui.DrawContext

class KeyStrokesModule : UiModule("Keystrokes", "Renders your Key Inputs", "keystrokes", 79, 80, listOf(Filter.Render)) {
    init {
        settings.add(BooleanSetting("Spacebar", "Toggle rendering the Spacebar", "spaceBar"))
    }

    private var background: Box = Box(0, 0, 79, 80, null, false)

    private var keyW: ToggleButton = ToggleButton(0, 0, 20, 20, "  W", true, "", false)
    private var keyA: ToggleButton = ToggleButton(0, 0, 20, 20, "  A", true, "", false)
    private var keyS: ToggleButton = ToggleButton(0, 0, 20, 20, "  S", true, "", false)
    private var keyD: ToggleButton = ToggleButton(0, 0, 20, 20, "  D", true, "", false)
    private var keySpace: ToggleButton = ToggleButton(0, 0, 68, 20, "  ━━━━━━━━", true, "", false)

    override fun render(context: DrawContext, originX: Int, originY: Int, delta: Float) {
        keyW.enabled = Onyx.MC.options.forwardKey.isPressed
        keyA.enabled = Onyx.MC.options.leftKey.isPressed
        keyS.enabled = Onyx.MC.options.backKey.isPressed
        keyD.enabled = Onyx.MC.options.rightKey.isPressed
        keySpace.enabled = Onyx.MC.options.jumpKey.isPressed

        val renderSpace = settings.getById<BooleanSetting>("spaceBar")!!.isEnabled()

        if (!renderSpace) {
            background.ySize = 55
            super.height = 55
        } else {
            background.ySize = 80
            super.height = 80
        }
        background.update(originX, originY).render(context, 0, 0, delta)

        keyW.update(originX + 29, originY + 5).render(context, 0, 0, delta)
        keyA.update(originX + 5, originY + 29).render(context, 0, 0, delta)
        keyS.update(originX + 29, originY + 29).render(context, 0, 0, delta)
        keyD.update(originX + 53, originY + 29).render(context, 0, 0, delta)

        if (renderSpace) keySpace.update(originX + 5, originY + 53).render(context, 0, 0, delta)
    }
}