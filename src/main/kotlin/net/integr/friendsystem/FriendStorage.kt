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

@file:Suppress("unused")

package net.integr.friendsystem

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import net.integr.Onyx
import net.integr.utilities.LogUtils
import net.integr.utilities.game.notification.NotificationHandler
import net.minecraft.entity.player.PlayerEntity
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

class FriendStorage {
    companion object {
        private var friends: List<String> = listOf()

        private fun loadJson(json: String) {
            this.friends = Gson().fromJson(json, JsonArray::class.java).map { it.asString }
        }

        fun save() {
            val json = GsonBuilder().setPrettyPrinting().create().toJson(friends)
            try {
                Files.createDirectories(Path.of(Onyx.CONFIG.toString() + "\\friends"))
                Files.write(Path.of(Onyx.CONFIG.toString() + "\\friends\\friends.json"), json.toByteArray())
            } catch (e: IOException) {
                LogUtils.sendLog("Could not save friends!")
                e.printStackTrace()
            }
        }

        private fun exists(): Boolean {
            return Path.of(Onyx.CONFIG.toString() + "\\friends\\friends.json").exists()
        }

        fun load() {
            if (exists()) {
                val json = Files.readString(Path.of(Onyx.CONFIG.toString() + "\\friends\\friends.json"))
                loadJson(json)
            } else save()

            LogUtils.sendLog("Loaded ${friends.size} friends from saved file!")
        }

        fun insert(player: PlayerEntity) {
            val name = player.gameProfile.name
            if (!friends.contains(name)) friends += name
        }

        fun remove(player: PlayerEntity) {
            val name = player.gameProfile.name
            if (friends.contains(name)) friends -= name
        }

        fun contains(player: PlayerEntity): Boolean {
            val name = player.gameProfile.name
            return friends.contains(name)
        }

        fun handleMiddleClickFriend(player: PlayerEntity) {
            val name = player.gameProfile.name

            if (friends.contains(name)) {
                friends -= name
                NotificationHandler.notify("Removed $name as a Friend")
            } else {
                friends += name
                NotificationHandler.notify("Added $name as a Friend")
            }
        }
    }
}