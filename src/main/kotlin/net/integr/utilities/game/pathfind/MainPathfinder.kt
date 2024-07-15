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

@file:Suppress("DuplicatedCode")

package net.integr.utilities.game.pathfind

import net.integr.Onyx
import net.minecraft.block.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.math.max
import kotlin.math.min

class MainPathFinder {
    @Suppress("SameParameterValue")
    private fun isValid(x: Int, y: Int, z: Int, checkGround: Boolean): Boolean {
        val block1 = BlockPos(x, y, z)
        val block2 = BlockPos(x, y + 1, z)
        val block3 = BlockPos(x, y - 1, z)
        return !isNotPassable(block1) && !isNotPassable(block2) && (isNotPassable(block3) || !checkGround) && canWalkOn(
            block3
        )
    }

    private fun isNotPassable(block: BlockPos): Boolean {
        val b: Block = Onyx.MC.world!!.getBlockState(BlockPos(block.x, block.y, block.z)).block

        return b.defaultState.isFullCube(Onyx.MC.world!!.getChunkAsView(Onyx.MC.player!!.chunkPos.x, Onyx.MC.player!!.chunkPos.x), Onyx.MC.player!!.blockPos)
            || b is SlabBlock
            || b is StairsBlock
            || b is CactusBlock
            || b is ChestBlock
            || b is EnderChestBlock
            || b is SkullBlock
            || b is PaneBlock
            || b is FenceBlock
            || b is WallBlock
            || b is PistonHeadBlock
            || b is PistonExtensionBlock
            || b is PistonBlock
            || b is StainedGlassBlock
            || b is TrapdoorBlock
            || b is EndPortalFrameBlock
            || b is EndPortalBlock
            || b is BedBlock
            || b is CobwebBlock
            || b is BarrierBlock
            || b is LadderBlock
            || b is CarpetBlock
            || b is DirtPathBlock
    }

    private fun canWalkOn(block: BlockPos): Boolean {
        return Onyx.MC.world!!.getBlockState(BlockPos(block.x, block.y, block.z))
            .block !is FenceBlock && Onyx.MC.world!!.getBlockState(BlockPos(block.x, block.y, block.z))
            .block !is WallBlock
    }

    private fun canPassThrough(pos: BlockPos): Boolean {
        val block: Block = Onyx.MC.world!!.getBlockState(BlockPos(pos.x, pos.y, pos.z)).block
        return block.defaultState.isAir || block is PlantBlock || block is VineBlock || block === Blocks.LADDER || block === Blocks.WATER || block is SignBlock
    }

    fun computePath(topFromI: Vec3d, to: Vec3d): ArrayList<Vec3d> {
        var topFrom = topFromI
        if (!canPassThrough(BlockPos.ofFloored(topFrom.x, topFrom.y, topFrom.z))) {
            topFrom = topFrom.add(0.0, 1.0, 0.0)
        }

        val pathfinder: SubPathFinder = SubPathFinder(topFrom, to)
        var lastLoc: Vec3d? = null
        var lastDashLoc: Vec3d? = null
        val path = ArrayList<Vec3d>()
        val pathFinderPath: ArrayList<Vec3d> = pathfinder.computePath()

        for ((i, pathElm) in pathFinderPath.withIndex()) {
            if (i == 0 || i == pathFinderPath.size - 1) {
                if (lastLoc != null) {
                    path.add(lastLoc.add(0.5, 0.0, 0.5))
                }
                path.add(pathElm.add(0.5, 0.0, 0.5))
                lastDashLoc = pathElm
            } else {
                var canContinue = true
                if (pathElm.squaredDistanceTo(lastDashLoc) > 5 * 5) {
                    canContinue = false
                } else {
                    val smallX = min(lastDashLoc!!.getX(), pathElm.getX())
                    val smallY = min(lastDashLoc.getY(), pathElm.getY())
                    val smallZ = min(lastDashLoc.getZ(), pathElm.getZ())
                    val bigX = max(lastDashLoc.getX(), pathElm.getX())
                    val bigY = max(lastDashLoc.getY(), pathElm.getY())
                    val bigZ = max(lastDashLoc.getZ(), pathElm.getZ())
                    var x = smallX.toInt()
                    block1@ while (x <= bigX) {
                        var y2 = smallY.toInt()
                        while (y2 <= bigY) {
                            var z = smallZ.toInt()
                            while (z <= bigZ) {
                                if (!isValid(x, y2, z, false)) {
                                    canContinue = false
                                    break@block1
                                }
                                ++z
                            }
                            ++y2
                        }
                        ++x
                    }
                }

                if (!canContinue) {
                    path.add(lastLoc!!.add(0.5, 0.0, 0.5))
                    lastDashLoc = lastLoc
                }
            }
            lastLoc = pathElm
        }
        return path
    }
}
