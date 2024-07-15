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
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import java.util.*
import kotlin.collections.ArrayList

class SubPathFinder(startVec3Path: Vec3d, endVec3Path: Vec3d) {
    private val startVec3Path: Vec3d = startVec3Path.add(0.0, 0.0, 0.0).floorAlongAxes(EnumSet.of(Direction.Axis.X, Direction.Axis.Y, Direction.Axis.Z))
    private val endVec3Path: Vec3d = endVec3Path.add(0.0, 0.0, 0.0).floorAlongAxes(EnumSet.of(Direction.Axis.X, Direction.Axis.Y, Direction.Axis.Z))
    private var path: ArrayList<Vec3d> = ArrayList()
    private val pathHubs: ArrayList<Node> = ArrayList()
    private val workingPathHubList: ArrayList<Node> = ArrayList()

    @JvmOverloads
    fun computePath(loops: Int = 1000, depth: Int = 4): ArrayList<Vec3d> {
        path.clear()
        workingPathHubList.clear()

        val initPath = ArrayList<Vec3d>()
        initPath.add(this.startVec3Path)

        workingPathHubList.add(
            Node(this.startVec3Path, initPath, startVec3Path.squaredDistanceTo(this.endVec3Path), 0.0, 0.0)
        )

        outer@ for (i in 0 until loops) {
            workingPathHubList.sortWith(CompareNode())

            if (workingPathHubList.isEmpty()) {
                break
            }

            for ((j, pathHub) in ArrayList<Node>(this.workingPathHubList).withIndex()) {
                var loc2: Vec3d

                if (j + 1 > depth) {
                    continue@outer
                }

                workingPathHubList.remove(pathHub)
                pathHubs.add(pathHub)

                for (direction in directions) {
                    val loc: Vec3d = pathHub.loc.add(direction)
                        .floorAlongAxes(EnumSet.of(Direction.Axis.X, Direction.Axis.Y, Direction.Axis.Z))
                    if (isValid(loc, false) && this.putNode(pathHub, loc, 0.0)) {
                        break@outer
                    }
                }

                val loc1: Vec3d = pathHub.loc.add(0.0, 1.0, 0.0).floorAlongAxes(EnumSet.of(Direction.Axis.X, Direction.Axis.Y, Direction.Axis.Z))
                if (isValid(loc1, false) && this.putNode(pathHub, loc1, 0.0) || isValid(pathHub.loc.add(0.0, -1.0, 0.0).floorAlongAxes(EnumSet.of(Direction.Axis.X, Direction.Axis.Y, Direction.Axis.Z)).also { loc2 = it }, false) && this.putNode(pathHub, loc2, 0.0)
                ) {
                    break@outer
                }
            }
        }

        pathHubs.sortWith(CompareNode())
        return pathHubs[0].pathway
    }

    private fun doesNodeExistAt(loc: Vec3d): Node? {
        for (pathHub in this.pathHubs) {
            if (pathHub.loc.getX() != loc.getX() || pathHub.loc.getY() != loc.getY() || pathHub.loc.getZ() != loc.getZ()) {
                continue
            }
            return pathHub
        }

        for (pathHub in this.workingPathHubList) {
            if (pathHub.loc.getX() != loc.getX() || pathHub.loc.getY() != loc.getY() || pathHub.loc.getZ() != loc.getZ()) {
                continue
            }
            return pathHub
        }
        return null
    }

    @Suppress("SameParameterValue")
    private fun putNode(parent: Node?, loc: Vec3d, cost: Double): Boolean {
        val existingPathHub = this.doesNodeExistAt(loc)
        var totalCost = cost

        if (parent != null) {
            totalCost += parent.maxCost
        }

        if (existingPathHub == null) {
            if ((loc.getX() == endVec3Path.getX() && loc.getY() == endVec3Path.getY()) && loc.getZ() == endVec3Path.getZ() || loc.squaredDistanceTo(this.endVec3Path) <= 1) {
                path.clear()
                this.path = parent?.pathway ?: ArrayList()
                path.add(loc)
                return true
            }

            val path: ArrayList<Vec3d> = if (parent != null) ArrayList(parent.pathway) else ArrayList()
            path.add(loc)
            workingPathHubList.add(Node(loc, path, loc.squaredDistanceTo(this.endVec3Path), cost, totalCost))
        } else if (existingPathHub.currentCost > cost) {
            val path: ArrayList<Vec3d> = if (parent != null) ArrayList(parent.pathway) else ArrayList()
            path.add(loc)
            existingPathHub.loc = loc
            existingPathHub.pathway = path
            existingPathHub.sqDist = loc.squaredDistanceTo(this.endVec3Path)
            existingPathHub.currentCost = cost
            existingPathHub.maxCost = totalCost
        }
        return false
    }

    class CompareNode : Comparator<Node> {
        override fun compare(o1: Node, o2: Node): Int {
            return (o1.sqDist + o1.maxCost - (o2.sqDist + o2.maxCost)).toInt()
        }
    }

    companion object {
        private val directions =
            arrayOf(Vec3d(1.0, 0.0, 0.0), Vec3d(-1.0, 0.0, 0.0), Vec3d(0.0, 0.0, 1.0), Vec3d(0.0, 0.0, -1.0))

        fun isValid(loc: Vec3d, checkGround: Boolean): Boolean {
            return isValid(
                loc.getX().toInt(), loc.getY().toInt(), loc.getZ().toInt(),
                checkGround
            )
        }

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
    }
}