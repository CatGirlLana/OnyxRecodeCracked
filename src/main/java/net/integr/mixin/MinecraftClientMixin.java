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

import net.integr.Onyx;
import net.integr.event.*;
import net.integr.eventsystem.EventSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void onTickPre(CallbackInfo ci) {
        PreTickEvent e = new PreTickEvent();
        UnsafePreTickEvent eu = new UnsafePreTickEvent();
        if (Onyx.Companion.getMC().player != null) EventSystem.Companion.post(e);

        EventSystem.Companion.post(eu);

        if (e.isCancelled()) ci.cancel();
    }

    @Inject(method = "tick", at = @At("TAIL"), cancellable = true)
    public void onTickPost(CallbackInfo ci) {
        PostTickEvent e = new PostTickEvent();
        UnsafePostTickEvent eu = new UnsafePostTickEvent();
        if (Onyx.Companion.getMC().player != null) EventSystem.Companion.post(e);
        EventSystem.Companion.post(eu);

        if (e.isCancelled()) ci.cancel();
    }

    @Inject(method = "stop", at = @At("HEAD"))
    public void onStop(CallbackInfo ci) {
        EventSystem.Companion.post(new ClientEndEvent());
    }

    @Inject(method = "cleanUpAfterCrash", at = @At("HEAD"))
    public void onCrash(CallbackInfo ci) {
        EventSystem.Companion.post(new ClientEndEvent());
    }

    @Inject(method = "joinWorld", at = @At("HEAD"))
    public void onJoinWorld(ClientWorld world, CallbackInfo ci) {
        EventSystem.Companion.post(new JoinWorldEvent(world));
    }

    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    public void onItemUse(CallbackInfo ci) {
        ItemUseEvent e = new ItemUseEvent();
        EventSystem.Companion.post(e);

        if (e.isCancelled()) ci.cancel();
    }

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    public void onAttack(CallbackInfoReturnable<Boolean> cir) {
        AttackEvent e = new AttackEvent();
        EventSystem.Companion.post(e);

        if (e.isCancelled()) cir.setReturnValue(false);
    }

    @Inject(method = "doItemPick", at = @At("HEAD"), cancellable = true)
    private void onDoItemPick(CallbackInfo ci) {
        DoItemPickEvent e = new DoItemPickEvent();
        EventSystem.Companion.post(e);

        if (e.isCancelled()) ci.cancel();
    }

    @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
    private void entityHasOutline(Entity entity, CallbackInfoReturnable<Boolean> ci) {
        EntityHasOutlineEvent e = new EntityHasOutlineEvent(entity);
        EventSystem.Companion.post(e);

        if (e.getCallback() != null) {
            ci.setReturnValue((Boolean) e.getCallback());
        } else if (e.isCancelled()) ci.cancel();
    }
}
