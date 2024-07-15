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
import net.integr.rendering.uisystem.Box
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.network.PlayerListEntry

class PingModule : UiModule("Ping", "Renders your Ping", "ping", 22,70, listOf(Filter.Render)) {
    private var background: Box = Box(0, 0, 70, 22, "Ping: ", false)

    override fun render(context: DrawContext, originX: Int, originY: Int, delta: Float) {
        background.text = "Ping: ${getPing()}"
        background.update(originX, originY).render(context, 10, 10, delta)
    }

    private fun getPing(): Int {
        if (Onyx.MC.networkHandler == null) return 0

        val playerListEntry: PlayerListEntry = Onyx.MC.networkHandler!!.getPlayerListEntry(Onyx.MC.player!!.uuid) ?: return 0
        return playerListEntry.latency
    }
}