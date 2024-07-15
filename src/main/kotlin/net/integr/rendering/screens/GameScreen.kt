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
import net.integr.Settings
import net.integr.event.UiRenderEvent
import net.integr.eventsystem.EventListen
import net.integr.modules.management.ModuleManager
import net.integr.modules.management.UiModule
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.rendering.uisystem.Box

class GameScreen {
    companion object {
        val INSTANCE = GameScreen()
    }

    private var branding: Box = Box(5, 5, 70, 20, "Onyx v${Onyx.VERSION}", textCentered = false, outlined = true)


    @EventListen
    fun onRenderUi(event: UiRenderEvent) {
        val context = event.context
        val delta = event.tickDelta

        for (m in ModuleManager.getUiModules()) {
            val module = m as UiModule
            if (m.isEnabled()) module.runRender(context, delta)
        }

        if (Settings.INSTANCE.settings.getById<BooleanSetting>("watermark")!!.isEnabled()) branding.render(context, 0, 0, delta)
    }
}