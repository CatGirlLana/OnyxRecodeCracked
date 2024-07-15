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

package net.integr.utilities.game.notification

import net.integr.Onyx
import net.integr.modules.impl.NotificationModule
import net.integr.modules.management.ModuleManager
import net.integr.utilities.LogUtils
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents
import java.util.concurrent.atomic.AtomicInteger

class NotificationHandler {
    companion object {
        var notifications: List<Pair<String, AtomicInteger>> = mutableListOf()

        fun notify(string: String) {
            if (ModuleManager.getByClass<NotificationModule>()!!.isEnabled()) notifications += Pair(string, AtomicInteger(200))
            else LogUtils.sendChatLog(string)
        }

        fun ping() {
            Onyx.MC.soundManager.play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_BELL, 1.0F))
        }
    }
}