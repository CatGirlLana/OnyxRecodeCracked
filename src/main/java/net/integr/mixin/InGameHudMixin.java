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
import net.integr.event.UiRenderEvent;
import net.integr.eventsystem.EventSystem;
import net.integr.modules.impl.HotbarModule;
import net.integr.modules.management.ModuleManager;
import net.integr.modules.management.settings.impl.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow @Nullable private Text overlayMessage;
    @Unique Text uniqueOverlayMessage;
    @Shadow private int overlayRemaining;
    @Unique int uniqueOverlayRemaining;
    @Shadow private int scaledHeight;
    @Shadow private int scaledWidth;
    @Shadow @Final private MinecraftClient client;
    @Shadow private boolean overlayTinted;
    @Unique boolean uniqueOverlayTinted;
    @Shadow private void drawTextBackground(DrawContext context, TextRenderer textRenderer, int yOffset, int width, int color) {}
    @Shadow private int heldItemTooltipFade;
    @Shadow private ItemStack currentStack;
    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void onRender(DrawContext context, float tickDelta, CallbackInfo ci) {
        UiRenderEvent e = new UiRenderEvent(context, tickDelta);
        EventSystem.Companion.post(e);

        if (e.isCancelled()) ci.cancel();

        if (Objects.requireNonNull(ModuleManager.Companion.getByClass(HotbarModule.class)).isEnabled() && ((BooleanSetting) Objects.requireNonNull(Objects.requireNonNull(ModuleManager.Companion.getByClass(HotbarModule.class)).getSettings().getById("locked"))).isEnabled()) {
            if (overlayMessage != null) {
                uniqueOverlayMessage = overlayMessage;
            }

            if(uniqueOverlayRemaining == 0) {
                uniqueOverlayMessage = overlayMessage;
            }

            uniqueOverlayRemaining = overlayRemaining;
            uniqueOverlayTinted = overlayTinted;
            overlayMessage = null;
            int m;
            int n;
            float h;
            int l;

            if (uniqueOverlayMessage != null && uniqueOverlayRemaining > 0) {
                client.getProfiler().push("overlayMessage");
                h = (float)uniqueOverlayRemaining - tickDelta;
                l = (int)(h * 255.0F / 20.0F);
                if (l > 255) {
                    l = 255;
                }

                if (l > 8) {
                    context.getMatrices().push();
                    context.getMatrices().translate((float)(scaledWidth / 2), (float)(scaledHeight - 68), 0.0F);
                    int k = 16777215;
                    if (uniqueOverlayTinted) {
                        k = MathHelper.hsvToRgb(h / 50.0F, 0.7F, 0.6F) & 16777215;
                    }

                    m = l << 24 & -16777216;
                    n = Onyx.Companion.getMC().textRenderer.getWidth(uniqueOverlayMessage);
                    drawTextBackground(context, Onyx.Companion.getMC().textRenderer, -59, n, 16777215 | m);
                    context.drawTextWithShadow(Onyx.Companion.getMC().textRenderer, uniqueOverlayMessage, -n / 2, -59, k | m);
                    context.getMatrices().pop();
                }

                client.getProfiler().pop();
            }
        }
    }

    @Inject(method = "renderHealthBar", at = @At("HEAD"), cancellable = true)
    public void renderHealthBar(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        if (Objects.requireNonNull(ModuleManager.Companion.getByClass(HotbarModule.class)).isEnabled() && ((BooleanSetting) Objects.requireNonNull(Objects.requireNonNull(ModuleManager.Companion.getByClass(HotbarModule.class)).getSettings().getById("locked"))).isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
    public void renderStatusBars(DrawContext context, CallbackInfo ci) {
        if (Objects.requireNonNull(ModuleManager.Companion.getByClass(HotbarModule.class)).isEnabled() && ((BooleanSetting) Objects.requireNonNull(Objects.requireNonNull(ModuleManager.Companion.getByClass(HotbarModule.class)).getSettings().getById("locked"))).isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderHeldItemTooltip", at = @At("HEAD"), cancellable = true)
    public void renderHeldItemTooltip(DrawContext context, CallbackInfo ci) {
        if (Objects.requireNonNull(ModuleManager.Companion.getByClass(HotbarModule.class)).isEnabled() && ((BooleanSetting) Objects.requireNonNull(Objects.requireNonNull(ModuleManager.Companion.getByClass(HotbarModule.class)).getSettings().getById("locked"))).isEnabled()) {
            ci.cancel();

            this.client.getProfiler().push("selectedItemName");
            if (this.heldItemTooltipFade > 0 && !this.currentStack.isEmpty()) {
                MutableText mutableText = Text.empty().append(this.currentStack.getName()).formatted(this.currentStack.getRarity().formatting);
                if (currentStack.hasCustomName()) {
                    mutableText.formatted(Formatting.ITALIC);
                }

                int i = getTextRenderer().getWidth(mutableText);
                int j = (this.scaledWidth - i) / 2;
                int k = this.scaledHeight - 59;
                assert this.client.interactionManager != null;
                if (!this.client.interactionManager.hasStatusBars()) {
                    k += 14;
                }

                int l = (int)((float)heldItemTooltipFade * 256.0F / 10.0F);
                if (l > 255) {
                    l = 255;
                }

                if (l > 0) {
                    int var10001 = j - 2;
                    int var10002 = k - 2;
                    int var10003 = j + i + 2;
                    Objects.requireNonNull(this.getTextRenderer());
                    context.fill(var10001, var10002, var10003, k + 9 + 2-50, this.client.options.getTextBackgroundColor(0));
                    context.drawTextWithShadow(this.getTextRenderer(), mutableText, j, k-50, 16777215 + (l << 24));
                }
            }

            this.client.getProfiler().pop();
        }
    }
}
