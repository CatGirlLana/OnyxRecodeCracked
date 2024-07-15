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

package net.integr.utilities.game.highlight

import net.integr.Variables
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.concurrent.atomic.AtomicInteger

class Highlighter {
    companion object {
        var blocks: List<Pair<Vec3d, AtomicInteger>> = mutableListOf()
        var blocksOutlined: List<Pair<Vec3d, AtomicInteger>> = mutableListOf()

        var crystal: Vec3d? = null

        var lines: List<Pair<Pair<Pair<Vec3d, Vec3d>, Int>, AtomicInteger>> = mutableListOf()

        fun renderLine(pos1: Vec3d, pos2: Vec3d, color: Int = Variables.guiColor, time: Int = 20) {
            lines += ((pos1 to pos2) to color) to AtomicInteger(time)
        }

        fun renderCrystal(pos: Vec3d) {
            crystal = pos
        }

        fun clearCrystal() {
            crystal = null
        }

        fun renderBlock(pos: Vec3d, time: Int = 20) {
            blocks += pos to AtomicInteger(time)
        }

        fun renderBlockOutlined(pos: Vec3d, time: Int = 20) {
            blocksOutlined += pos to AtomicInteger(time)
        }

        fun renderBlock(pos: BlockPos, time: Int = 20) {
            blocks += pos.toCenterPos().add(0.0, -0.5, 0.0) to AtomicInteger(time)
        }
    }
}