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

package net.integr.utilities.game.interaction

import net.integr.Onyx
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult

class ClickUtils {
    companion object {
        fun rightClick() {
            Onyx.MC.interactionManager!!.interactItem(Onyx.MC.player, Hand.MAIN_HAND)
        }

        fun rightClickBlock() {
            val t = Onyx.MC.crosshairTarget
            if (t != null && t.type == HitResult.Type.BLOCK && t is BlockHitResult) Onyx.MC.interactionManager!!.interactBlock(Onyx.MC.player, Hand.MAIN_HAND, Onyx.MC.crosshairTarget as BlockHitResult)
            Onyx.MC.interactionManager!!.interactItem(Onyx.MC.player, Hand.MAIN_HAND)
        }

        fun leftClick() {
            Onyx.MC.player!!.swingHand(Hand.MAIN_HAND)
        }
    }
}