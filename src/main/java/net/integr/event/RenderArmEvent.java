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
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class RenderArmEvent extends Event {
    public AbstractClientPlayerEntity player;
    public float tickDelta;
    public float pitch;
    public Hand hand;
    public float swingProgress;
    public ItemStack item;
    public float equipProgress;
    public MatrixStack matrices;
    public VertexConsumerProvider vertexConsumers;
    public int light;

    public RenderArmEvent(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        this.player          = player;
        this.tickDelta       = tickDelta;
        this.pitch           = pitch;
        this.hand            = hand;
        this.swingProgress   = swingProgress;
        this.item            = item;
        this.equipProgress   = equipProgress;
        this.matrices        = matrices;
        this.vertexConsumers = vertexConsumers;
        this.light           = light;
    }
}
