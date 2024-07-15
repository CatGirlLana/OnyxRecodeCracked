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

package net.integr.utilities.game.pathfind

import net.integr.Onyx
import net.integr.Variables
import net.integr.utilities.game.highlight.Highlighter
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext

class Path(val path: List<Vec3d>) {
    fun render(color: Int = Variables.guiColor, time: Int = 1) {
        var pre = path[0]

        for (p in path.subList(0, path.size)) {
            Highlighter.renderLine(pre, p, color, time = time)
            pre = p
        }
    }

    fun iterate(execute: (Vec3d, Vec3d) -> Unit) {
        var pre = path[0]

        for (p in path.subList(0, path.size)) {
            execute(pre, p)
            pre = p
        }
    }

    fun iterateAndRender(color: Int = Variables.guiColor, time: Int = 1, execute: (Vec3d, Vec3d) -> Unit) {
        var pre = path[0]

        for (p in path.subList(0, path.size)) {
            Highlighter.renderLine(pre, p, color, time = time)
            execute(pre, p)
            pre = p
        }
    }

    fun smoothened(): Path {
        val smoothedPath: ArrayList<Vec3d> = arrayListOf(path[0])
        var lastTravelPos = path[0]

        while (lastTravelPos != path[path.size - 1]) {
            var tempPos = lastTravelPos
            for (it in path.indexOf(lastTravelPos)..<path.size) {
                if (canTravel(lastTravelPos, path[it])) {
                    tempPos = path[it]
                }
            }

            lastTravelPos = tempPos
            smoothedPath.add(lastTravelPos)
        }

        return Path(smoothedPath)
    }

    fun reversed(): Path {
        return Path(path.reversed())
    }

    private fun canTravel(from: Vec3d, to: Vec3d): Boolean {
        val h: HitResult = Onyx.MC.world!!.raycast(RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, Onyx.MC.player))
        val h2: HitResult = Onyx.MC.world!!.raycast(RaycastContext(from.add(0.0, 1.0, 0.0), to.add(0.0, 1.0, 0.0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, Onyx.MC.player))

        return h.type != HitResult.Type.BLOCK && h2.type != HitResult.Type.BLOCK
    }
}