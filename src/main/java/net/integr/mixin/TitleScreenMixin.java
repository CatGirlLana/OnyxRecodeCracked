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

import net.integr.event.RenderTitleScreenEvent;
import net.integr.eventsystem.EventSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

	@Shadow @Nullable private SplashTextRenderer splashText;

	@Inject(at = @At("TAIL"), method = "init")
	private void initTitleScreen(CallbackInfo ci) {
		splashText = null;
	}

	@Inject(at = @At("TAIL"), method = "render", cancellable = true)
	private void renderTitleScreen(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		RenderTitleScreenEvent e = new RenderTitleScreenEvent(context, mouseX, mouseY, delta);
		EventSystem.Companion.post(e);

		if (e.isCancelled()) ci.cancel();
	}
}