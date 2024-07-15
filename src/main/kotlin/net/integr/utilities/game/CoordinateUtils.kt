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

package net.integr.utilities.game

import net.integr.Onyx
import net.integr.utilities.game.entity.EntityFinder
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.entity.decoration.EndCrystalEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.sqrt

class CoordinateUtils {
    companion object {
        fun getLowestX(blocks: ArrayList<Vec3d>): Double {
            var lowestX = 999999999.0
            for (v in blocks) {
                if (v.getX() < lowestX) {
                    lowestX = v.getX()
                }
            }
            return lowestX
        }

        fun getLowestY(blocks: ArrayList<Vec3d>): Double {
            var lowestY = 999999999.0
            for (v in blocks) {
                if (v.getY() < lowestY) {
                    lowestY = v.getY()
                }
            }
            return lowestY
        }

        fun getLowestZ(blocks: ArrayList<Vec3d>): Double {
            var lowestZ = 999999999.0
            for (v in blocks) {
                if (v.getZ() < lowestZ) {
                    lowestZ = v.getZ()
                }
            }
            return lowestZ
        }

        fun getHighestX(blocks: ArrayList<Vec3d>): Double {
            var maxX = -999999999.0
            for (v in blocks) {
                if (v.getX() > maxX) {
                    maxX = v.getX()
                }
            }
            return maxX
        }

        fun getHighestY(blocks: ArrayList<Vec3d>): Double {
            var maxY = -999999999.0
            for (v in blocks) {
                if (v.getY() > maxY) {
                    maxY = v.getY()
                }
            }
            return maxY
        }

        fun getHighestZ(blocks: ArrayList<Vec3d>): Double {
            var maxZ = -999999999.0
            for (v in blocks) {
                if (v.getZ() > maxZ) {
                    maxZ = v.getZ()
                }
            }
            return maxZ
        }

        fun getLerpedEntityPos(e: Entity, partialTicks: Float): Vec3d {
            if (e.isRemoved) return e.pos

            val x = MathHelper.lerp(partialTicks.toDouble(), e.lastRenderX, e.x)
            val y = MathHelper.lerp(partialTicks.toDouble(), e.lastRenderY, e.y)
            val z = MathHelper.lerp(partialTicks.toDouble(), e.lastRenderZ, e.z)
            return Vec3d(x, y, z)
        }

        fun getEntityBox(e: Entity): Box {
            return Box(-(e.boundingBox.lengthX/2), 0.0, -(e.boundingBox.lengthZ/2), (e.boundingBox.lengthX/2), e.boundingBox.lengthY, (e.boundingBox.lengthZ/2))
        }

        fun getGridAroundEntity(e: Entity, radius: Int, checkPlayerAccessible: Boolean, accessRange: Double): List<BlockPos> {
            val returnList: MutableList<BlockPos> = mutableListOf()

            for (i in (-radius..radius)) {
                for (j in (-radius..radius)) {
                    for (k in (-radius..radius)) {
                        val bp = BlockPos(e.blockX + i, e.blockY + j, e.blockZ + k)

                        if (checkPlayerAccessible) {
                            if (distanceTo(bp.toCenterPos()) <= accessRange) returnList += bp
                        } else returnList += bp

                    }
                }
            }

            return returnList
        }

        fun distanceTo(block: Vec3d): Double {
            val d: Double = Onyx.MC.player!!.x - block.x
            val e: Double = Onyx.MC.player!!.y - block.y
            val f: Double = Onyx.MC.player!!.z - block.z
            return sqrt(d * d + e * e + f * f)
        }

        fun distanceBetween(block1: Vec3d, block2: Vec3d): Double {
            val d: Double = block1.x - block2.x
            val e: Double = block1.y - block2.y
            val f: Double = block1.z - block2.z
            return sqrt(d * d + e * e + f * f)
        }

        fun positionIsBlockedByEntity(bp: BlockPos): Boolean {
            val closest = EntityFinder.getClosestAround(bp.toCenterPos()) ?: return false

            return Box(bp).intersects(closest.boundingBox)
        }

        fun crystalIsBlockedByEntity(bp: BlockPos): Boolean {
            val closest = EntityFinder.getClosestAround(bp.toCenterPos()) ?: return false

            if (closest is PlayerEntity) return Box(bp).intersects(closest.boundingBox)

            return EndCrystalEntity(Onyx.MC.world, bp.toCenterPos().x, bp.toCenterPos().y, bp.toCenterPos().z).boundingBox.intersects(closest.boundingBox)
        }

        fun lerpPositionBetween(bp1: Vec3d, bp2: Vec3d, partialTicks: Float): Vec3d {
            val x = MathHelper.lerp(partialTicks.toDouble(), bp1.x, bp2.x)
            val y = MathHelper.lerp(partialTicks.toDouble(), bp1.y, bp2.y)
            val z = MathHelper.lerp(partialTicks.toDouble(), bp1.z, bp2.z)
            return Vec3d(x, y, z)
        }

        fun getDirectionOfBlockToBlock(bp1: BlockPos, bp2: BlockPos): Direction {
            return Direction.fromVector(bp2.x - bp1.x, bp2.y - bp1.y, bp2.z - bp1.z)!!
        }

        fun getDirectionOfBlockToBlockAsVector(bp1: BlockPos, bp2: BlockPos): BlockPos {
            return BlockPos(bp2.x - bp1.x, bp2.y - bp1.y, bp2.z - bp1.z)
        }
    }
}