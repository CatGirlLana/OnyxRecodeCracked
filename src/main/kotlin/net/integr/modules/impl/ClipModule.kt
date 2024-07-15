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
import net.integr.event.KeyEvent
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.KeyBindSetting
import net.integr.utilities.game.highlight.Highlighter
import net.integr.utilities.game.notification.NotificationHandler
import org.lwjgl.glfw.GLFW

class ClipModule : Module("Clip", "Teleports you a set amount of blocks forward", "clip", mutableListOf(Filter.Move), isExecuted = true) {
    init {
        settings
            .add(KeyBindSetting("Increase: ", "The key to increase the distance", "increase"))
            .add(KeyBindSetting("Decrease: ", "The key to decrease the distance", "decrease"))
    }

    private var distance = 0.0

    override fun onKeyEvent(event: KeyEvent) {
        if (event.action == GLFW.GLFW_PRESS) {
            if (event.key == settings.getById<KeyBindSetting>("increase")!!.bind) {
                distance++;
                NotificationHandler.notify("Clip distance: $distance")
            }
            else if (event.key == settings.getById<KeyBindSetting>("decrease")!!.bind) {
                distance--;
                NotificationHandler.notify("Clip distance: $distance")
            }
        }
    }

    override fun onEnable() {
        val target = Onyx.MC.player!!.pos.add(Onyx.MC.player!!.rotationVector.multiply(distance))

        Highlighter.renderLine(Onyx.MC.player!!.pos.add(0.0, 0.5, 0.0), target.add(0.0, 0.5, 0.0), time = 320)
        Highlighter.renderBlock(Onyx.MC.player!!.pos, 320)
        Highlighter.renderBlock(target, 320)

        Onyx.MC.player!!.setPosition(target)
    }
}