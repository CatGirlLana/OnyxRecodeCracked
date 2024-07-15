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

@file:Suppress("DuplicatedCode", "unused")

package net.integr.utilities.game.entity

import net.integr.Onyx
import net.integr.friendsystem.FriendStorage
import net.integr.utilities.game.CoordinateUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.EndCrystalEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext

class EntityFinder {
    companion object {
        fun getAuraEntities(range: Double, target: String, checkSee: Boolean = false): List<LivingEntity> {
            val entities = Onyx.MC.world!!.entities
                .asSequence()
                .filterIsInstance<LivingEntity>()
                .filter {
                    it.type != EntityType.ITEM_FRAME &&
                    it.type != EntityType.ARMOR_STAND &&
                    it.type != EntityType.GLOW_ITEM_FRAME &&
                    it.type != EntityType.PAINTING &&
                    it.type != EntityType.MINECART &&
                    it.type != EntityType.TNT_MINECART &&
                    it.type != EntityType.CHEST_MINECART &&
                    it.type != EntityType.HOPPER_MINECART &&
                    it.type != EntityType.FURNACE_MINECART &&
                    it.type != EntityType.COMMAND_BLOCK_MINECART &&
                    it.type != EntityType.SPAWNER_MINECART &&
                    it != Onyx.MC.player && it.isAlive
                }
                .filter { Onyx.MC.player!!.distanceTo(it) <= range }
                .filter { if (checkSee) Onyx.MC.player!!.canSee(it) else true }
                .filter { if (it is PlayerEntity) !FriendStorage.contains(it) else true }
                .toList()

            var newEntities = listOf<LivingEntity>()
            when (target) {
                "All" -> newEntities = entities
                "Mobs" -> newEntities = entities.filter { it.type != EntityType.PLAYER }
                "Players" -> newEntities = entities.filter { it.type == EntityType.PLAYER }
            }

            return newEntities.sortedBy { Onyx.MC.player!!.distanceTo(it) }
        }

        fun getClosestAuraEntity(range: Double, target: String, checkSee: Boolean = false): LivingEntity? {
            val entities = Onyx.MC.world!!.entities
                .asSequence()
                .filterIsInstance<LivingEntity>()
                .filter {
                    it.type != EntityType.ITEM_FRAME &&
                    it.type != EntityType.ARMOR_STAND &&
                    it.type != EntityType.GLOW_ITEM_FRAME &&
                    it.type != EntityType.PAINTING &&
                    it.type != EntityType.MINECART &&
                    it.type != EntityType.TNT_MINECART &&
                    it.type != EntityType.CHEST_MINECART &&
                    it.type != EntityType.HOPPER_MINECART &&
                    it.type != EntityType.FURNACE_MINECART &&
                    it.type != EntityType.COMMAND_BLOCK_MINECART &&
                    it.type != EntityType.SPAWNER_MINECART &&
                    it != Onyx.MC.player && it.isAlive
                }
                .filter { Onyx.MC.player!!.distanceTo(it) <= range }
                .filter { if (it is PlayerEntity) !FriendStorage.contains(it) else true }
                .filter { if (checkSee) Onyx.MC.player!!.canSee(it) else true }
                .toList()

            var newEntities = listOf<LivingEntity>()
            when (target) {
                "All" -> newEntities = entities
                "Mobs" -> newEntities = entities.filter { it.type != EntityType.PLAYER }
                "Players" -> newEntities = entities.filter { it.type == EntityType.PLAYER }
            }

            return newEntities.minByOrNull { Onyx.MC.player!!.distanceTo(it) }
        }

        fun getClosestAround(pos: Vec3d): Entity? {
            return Onyx.MC.world!!.entities.minByOrNull { CoordinateUtils.distanceBetween(pos, it.pos) }
        }

        fun getClosestCrystalAround(pos: Vec3d): EndCrystalEntity? {
            return Onyx.MC.world!!.entities.filterIsInstance<EndCrystalEntity>().filter { CoordinateUtils.distanceBetween(
                pos,
                it.pos
            ) < 0.7 }.minByOrNull { CoordinateUtils.distanceBetween(pos, it.pos) }
        }

        fun getRandomEntity(): Entity? {
            return if (Onyx.MC.world!!.entities.any { it != Onyx.MC.player }) Onyx.MC.world!!.entities.filter { it != Onyx.MC.player }.random() else return null
        }

        fun getStaredAtEntity(range: Double): LivingEntity? {
            val entities = Onyx.MC.world!!.entities
                .asSequence()
                .filterIsInstance<LivingEntity>()
                .filter {
                    it.type != EntityType.ITEM_FRAME &&
                    it.type != EntityType.ARMOR_STAND &&
                    it.type != EntityType.GLOW_ITEM_FRAME &&
                    it.type != EntityType.PAINTING &&
                    it.type != EntityType.MINECART &&
                    it.type != EntityType.TNT_MINECART &&
                    it.type != EntityType.CHEST_MINECART &&
                    it.type != EntityType.HOPPER_MINECART &&
                    it.type != EntityType.FURNACE_MINECART &&
                    it.type != EntityType.COMMAND_BLOCK_MINECART &&
                    it.type != EntityType.SPAWNER_MINECART &&
                    it != Onyx.MC.player && it.isAlive
                }
                .filter { Onyx.MC.player!!.distanceTo(it) <= range }
                .filter { Onyx.MC.player!!.canSee(it) }
                .toList()

            entities.forEach {
                if (playerStaringAt(it)) {
                    return it
                }
            }

            return null
        }

        fun playerStaringAt(entity: Entity): Boolean {
            val rotationVector = Onyx.MC.cameraEntity!!.getRotationVec(1.0f).normalize()
            var vec3d2 = Vec3d(entity.x - Onyx.MC.cameraEntity!!.x, entity.eyeY - Onyx.MC.cameraEntity!!.eyeY, entity.z - Onyx.MC.cameraEntity!!.z)

            val d = vec3d2.length()
            vec3d2 = vec3d2.normalize()
            val e = rotationVector.dotProduct(vec3d2)

            return if (e > 1.0 - 0.025 / d) entityCanSeeEntity(Onyx.MC.cameraEntity!!, entity) else false
        }

        fun entityCanSeeEntity(toInspect: Entity, entity: Entity): Boolean {
            if (entity.world != toInspect.world) {
                return false
            } else {
                val vec3d = Vec3d(toInspect.x, toInspect.eyeY, toInspect.z)
                val vec3d2 = Vec3d(entity.x, entity.eyeY, entity.z)
                return if (vec3d2.distanceTo(vec3d) > 128.0) {
                    false
                } else {
                    toInspect.world.raycast(RaycastContext(vec3d, vec3d2, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, toInspect)).type == HitResult.Type.MISS
                }
            }
        }
    }
}