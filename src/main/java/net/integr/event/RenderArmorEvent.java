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

package net.integr.event;

import net.integr.eventsystem.Event;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;

public class RenderArmorEvent extends Event {
    public MatrixStack matrices;
    public VertexConsumerProvider vertexConsumers;
    public Entity entity;
    public EquipmentSlot armorSlot;
    public int light;
    public BipedEntityModel<?> model;

    public RenderArmorEvent(BipedEntityModel<?> model, int light, EquipmentSlot armorSlot, Entity entity, VertexConsumerProvider vertexConsumers, MatrixStack matrices) {
        this.model           = model;
        this.light           = light;
        this.armorSlot       = armorSlot;
        this.entity          = entity;
        this.vertexConsumers = vertexConsumers;
        this.matrices        = matrices;
    }
}
