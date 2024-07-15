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

package net.integr.mixin;

import net.integr.event.PostMoveEvent;
import net.integr.event.PreMoveEvent;
import net.integr.event.PushOutOfBlocksEvent;
import net.integr.event.SwingHandEvent;
import net.integr.eventsystem.EventSystem;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Inject(method = "swingHand", at = @At("HEAD"), cancellable = true)
    public void swingHand(Hand hand, CallbackInfo ci) {
        SwingHandEvent e = new SwingHandEvent(hand);
        EventSystem.Companion.post(e);

        if (e.isCancelled()) {
            ci.cancel();
        }
    }
    @Inject(at = @At("HEAD"), method = "sendMovementPackets()V", cancellable = true)
    private void onSendMovementPacketsPre(CallbackInfo ci) {
        PreMoveEvent e = new PreMoveEvent();
        EventSystem.Companion.post(e);

        if (e.isCancelled()) ci.cancel();
    }

    @Inject(at = @At("TAIL"), method = "sendMovementPackets()V", cancellable = true)
    private void onSendMovementPacketsPost(CallbackInfo ci) {
        PostMoveEvent e = new PostMoveEvent();
        EventSystem.Companion.post(e);

        if (e.isCancelled()) ci.cancel();
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void onPushOutOfBlocks(double x, double d, CallbackInfo info) {
        PushOutOfBlocksEvent e = new PushOutOfBlocksEvent(x, d);
        EventSystem.Companion.post(e);

        if (e.isCancelled()) info.cancel();
    }

}
