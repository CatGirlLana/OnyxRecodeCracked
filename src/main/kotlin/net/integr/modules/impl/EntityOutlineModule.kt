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

import net.integr.Variables
import net.integr.event.EntityHasOutlineEvent
import net.integr.event.RenderEntityEvent
import net.integr.eventsystem.EventListen
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.minecraft.client.render.OutlineVertexConsumerProvider
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import java.awt.Color

class EntityOutlineModule : Module("Entity Outline", "Draw Entity Outlines", "entityOutline", mutableListOf(Filter.Render)) {
    @EventListen
    fun onRenderEntity(event: RenderEntityEvent) {
        if (event.vertexConsumers is OutlineVertexConsumerProvider) {
            if (event.entity is LivingEntity) {
                if ((event.entity as LivingEntity).hurtTime > 0) {
                    (event.vertexConsumers as OutlineVertexConsumerProvider).setColor(224, 36, 7, 255);
                } else {
                    val c = Color(Variables.guiColor)
                    (event.vertexConsumers as OutlineVertexConsumerProvider).setColor(c.red, c.green, c.blue, 255);
                }
            } else {
                val c = Color(Variables.guiColor)
                (event.vertexConsumers as OutlineVertexConsumerProvider).setColor(c.red, c.green, c.blue, 255);
            }
        }
    }

    @EventListen
    fun onEntityHasOutline(event: EntityHasOutlineEvent) {
        val et = event.entity.type
        if (et != EntityType.ARMOR_STAND && et != EntityType.ITEM_FRAME && et != EntityType.GLOW_ITEM_FRAME) {
            event.callback = true
        }
    }
}