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

import net.integr.modules.impl.CrystalTweaksModule;
import net.integr.modules.management.ModuleManager;
import net.integr.modules.management.settings.impl.SliderSetting;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Objects;

@Mixin(EndCrystalEntityRenderer.class)
public class EndCrystalEntityRendererMixin {
    @ModifyArgs(method = "render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V", ordinal = 0))
    private void modifyScale(Args args) {
        CrystalTweaksModule module = (CrystalTweaksModule) ModuleManager.Companion.getByClass(CrystalTweaksModule.class);
        if (module == null || !module.isEnabled()) return;

        float scale = ((SliderSetting) Objects.requireNonNull(module.getSettings().getById("scale"))).getSetValueAsFloat();

        args.set(0, 2.0F * scale);
        args.set(1, 2.0F * scale);
        args.set(2, 2.0F * scale);
    }

    @Redirect(method = "render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EndCrystalEntityRenderer;getYOffset(Lnet/minecraft/entity/decoration/EndCrystalEntity;F)F"))
    private float getYOff(EndCrystalEntity crystal, float tickDelta) {
        CrystalTweaksModule module = (CrystalTweaksModule) ModuleManager.Companion.getByClass(CrystalTweaksModule.class);
        if (module == null || !module.isEnabled()) return EndCrystalEntityRenderer.getYOffset(crystal, tickDelta);

        float bounce = ((SliderSetting) Objects.requireNonNull(module.getSettings().getById("bounce"))).getSetValueAsFloat();

        float f = (float) crystal.endCrystalAge + tickDelta;
        float g = MathHelper.sin(f * 0.2F) / 2.0F + 0.5F;
        g = ((g * g + g) * 0.4F * bounce);
        return g - 1.4F;
    }

    @ModifyArgs(method = "render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/RotationAxis;rotationDegrees(F)Lorg/joml/Quaternionf;"))
    private void modifySpeed(Args args) {
        CrystalTweaksModule module = (CrystalTweaksModule) ModuleManager.Companion.getByClass(CrystalTweaksModule.class);
        if (module == null || !module.isEnabled()) return;

        float speed = ((SliderSetting) Objects.requireNonNull(module.getSettings().getById("speed"))).getSetValueAsFloat();


        args.set(0, ((float) args.get(0)) * speed);
    }
}
