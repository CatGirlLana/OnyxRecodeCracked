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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class RenderEntityEvent extends Event {
    public Entity entity;
    public double cameraX;
    public double cameraY;
    public double cameraZ;
    public float tickDelta;
    public MatrixStack matrices;
    public VertexConsumerProvider vertexConsumers;

    public RenderEntityEvent(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        this.entity          = entity;
        this.cameraX         = cameraX;
        this.cameraY         = cameraY;
        this.cameraZ         = cameraZ;
        this.tickDelta       = tickDelta;
        this.matrices        = matrices;
        this.vertexConsumers = vertexConsumers;
    }
}
