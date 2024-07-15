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
import net.integr.utilities.game.notification.NotificationHandler
import net.minecraft.client.gui.DrawContext

class NotificationModule : UiModule("Notifications", "Renders various notifications on your HUD", "notifications", 20, 200, mutableListOf(Filter.Render)) {
    override fun render(context: DrawContext, originX: Int, originY: Int, delta: Float) {
        val notifications = NotificationHandler.notifications

        var counter = 0

        if (originX-20/2 >= context.scaledWindowWidth/2) {
            // Right
            if (originY-100/2 >= context.scaledWindowHeight/2) {
                // Bottom
                for (notification in notifications) {
                    Box((originX+200)-(Onyx.MC.textRenderer.getWidth(notification.first)+20), originY-counter, Onyx.MC.textRenderer.getWidth(notification.first)+20, 20, notification.first, true).render(context, 0, 0, delta)

                    notification.second.set(notification.second.get()-1)

                    counter += 30
                }
            } else {
                //Top
                for (notification in notifications) {
                    Box((originX+200)-(Onyx.MC.textRenderer.getWidth(notification.first)+20), originY+counter, Onyx.MC.textRenderer.getWidth(notification.first)+20, 20, notification.first, true).render(context, 0, 0, delta)

                    notification.second.set(notification.second.get()-1)

                    counter += 30
                }
            }
        } else {
            // Left
            if (originY-100/2 >= context.scaledWindowHeight/2) {
                // Bottom
                for (notification in notifications) {
                    Box(originX, originY-counter, Onyx.MC.textRenderer.getWidth(notification.first)+20, 20, notification.first, true).render(context, 0, 0, delta)

                    notification.second.set(notification.second.get()-1)

                    counter += 30
                }
            } else {
                //Top
                for (notification in notifications) {
                    Box(originX, originY+counter, Onyx.MC.textRenderer.getWidth(notification.first)+20, 20, notification.first, true).render(context, 0, 0, delta)

                    notification.second.set(notification.second.get()-1)

                    counter += 30
                }
            }
        }

        NotificationHandler.notifications = NotificationHandler.notifications.filter { it.second.get() > 0 }
    }
}