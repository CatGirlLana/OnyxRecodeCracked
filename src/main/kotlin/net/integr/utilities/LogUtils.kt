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

package net.integr.utilities

import net.integr.Onyx
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class LogUtils {
    companion object {
        fun sendChatLog(str: String) {
            Onyx.MC.player!!.sendMessage(Text.literal("" + Formatting.LIGHT_PURPLE + "Onyx" + Formatting.DARK_GRAY + " > " + Formatting.GRAY + str))
        }

        fun getChatLog(str: String): Text {
            return Text.literal("" + Formatting.LIGHT_PURPLE + "Onyx" + Formatting.DARK_GRAY + " > " + Formatting.GRAY + str)
        }

        fun sendLog(str: String) {
            Onyx.LOGGER.info("[Onyx] > $str")
        }

        fun assistantMessage(str: String) {
            Onyx.MC.player!!.sendMessage(Text.literal("" + Formatting.LIGHT_PURPLE + "Onyx" + Formatting.DARK_GRAY + " > " + Formatting.DARK_PURPLE + "Assistant" + Formatting.DARK_GRAY + " > " + Formatting.GRAY + str))
        }
    }
}