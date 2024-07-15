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

@file:Suppress("DuplicatedCode", "unused", "MemberVisibilityCanBePrivate")

package net.integr.rendering

import com.mojang.blaze3d.systems.RenderSystem
import net.integr.Onyx
import net.integr.Settings
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.utilities.game.CoordinateUtils
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.*
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class RenderingEngine {
    class ThreeDimensional {
        companion object {
            fun circle(position: Vec3d, precision: Double, radius: Double, offsetMid: Float, fill: Boolean, matrixStack: MatrixStack, color: Int) {
                var pos = position
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glEnable(GL11.GL_CULL_FACE)
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                matrixStack.push()
                val camPos: BlockPos = Onyx.MC.gameRenderer.camera.blockPos
                val regionX = camPos.x
                val regionZ = camPos.z

                applyRegionalRenderOffset(matrixStack, regionX, regionZ)
                pos = pos.subtract(regionX.toDouble(), 0.0, regionZ.toDouble())

                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                val col = Color(color)

                RenderSystem.setShaderColor(
                    col.red.toFloat() / 255f,
                    col.green.toFloat() / 255f,
                    col.blue.toFloat() / 255f,
                    0.5f
                )
                val matrix = matrixStack.peek().positionMatrix
                val tesselator = RenderSystem.renderThreadTesselator()
                val bufferBuilder = tesselator.buffer

                /* Start Drawing */
                bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION)

                var prevX = cos(0.0) * radius
                var prevZ = sin(0.0) * radius

                var angle = 0.0
                while (angle <= 2 * Math.PI) {
                    val x = cos(angle) * radius
                    val z = sin(angle) * radius

                    bufferBuilder.vertex(matrix, pos.getX().toFloat() + prevX.toFloat(), pos.getY().toFloat(), pos.getZ().toFloat() + prevZ.toFloat()).next()
                    bufferBuilder.vertex(matrix, pos.getX().toFloat() + x.toFloat(), pos.getY().toFloat(), pos.getZ().toFloat() + z.toFloat()).next()

                    prevX = x
                    prevZ = z

                    angle += precision
                }

                bufferBuilder.vertex(matrix, pos.getX().toFloat() + cos(0.0).toFloat() * radius.toFloat(), pos.getY().toFloat(), pos.getZ().toFloat() + sin(0.0).toFloat() * radius.toFloat()).next()
                bufferBuilder.vertex(matrix, pos.getX().toFloat() + prevX.toFloat(), pos.getY().toFloat(), pos.getZ().toFloat() + prevZ.toFloat()).next()

                if (fill) {
                    tesselator.draw()

                    prevX = cos(0.0) * radius
                    prevZ = sin(0.0) * radius

                    GL11.glDisable(GL11.GL_CULL_FACE)

                    RenderSystem.setShaderColor(
                        col.red.toFloat() / 255f,
                        col.green.toFloat() / 255f,
                        col.blue.toFloat() / 255f,
                        0.3f
                    )

                    bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION)

                    angle = 0.0
                    while (angle <= 2 * Math.PI) {
                        val x = cos(angle) * radius
                        val z = sin(angle) * radius

                        bufferBuilder.vertex(matrix, pos.getX().toFloat() + prevX.toFloat(), pos.getY().toFloat(), pos.getZ().toFloat() + prevZ.toFloat()).next()
                        bufferBuilder.vertex(matrix, pos.getX().toFloat() + x.toFloat(), pos.getY().toFloat(), pos.getZ().toFloat() + z.toFloat()).next()
                        bufferBuilder.vertex(matrix, pos.getX().toFloat(), pos.getY().toFloat() + offsetMid, pos.getZ().toFloat()).next()

                        prevX = x
                        prevZ = z

                        angle += precision
                    }

                    bufferBuilder.vertex(matrix, pos.getX().toFloat() + cos(0.0).toFloat() * radius.toFloat(), pos.getY().toFloat(), pos.getZ().toFloat() + sin(0.0).toFloat() * radius.toFloat()).next()
                    bufferBuilder.vertex(matrix, pos.getX().toFloat() + prevX.toFloat(), pos.getY().toFloat(), pos.getZ().toFloat() + prevZ.toFloat()).next()
                    bufferBuilder.vertex(matrix, pos.getX().toFloat(), pos.getY().toFloat() + offsetMid, pos.getZ().toFloat()).next()
                }

                /* End Drawing */
                tesselator.draw()

                matrixStack.pop()
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glDisable(GL11.GL_BLEND)
                GL11.glEnable(GL11.GL_CULL_FACE)
            }

            fun line(position1: Vec3d, position2: Vec3d, matrixStack: MatrixStack, color: Int) {
                var pos1 = position1
                var pos2 = position2

                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glEnable(GL11.GL_CULL_FACE)
                GL11.glDisable(GL11.GL_DEPTH_TEST)

                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                matrixStack.push()

                val camPos: BlockPos = Onyx.MC.gameRenderer.camera.blockPos
                val regionX = camPos.x
                val regionZ = camPos.z

                applyRegionalRenderOffset(matrixStack, regionX, regionZ)
                pos1 = pos1.subtract(regionX.toDouble(), 0.0, regionZ.toDouble())
                pos2 = pos2.subtract(regionX.toDouble(), 0.0, regionZ.toDouble())

                val col = Color(color)
                RenderSystem.setShaderColor(
                    col.red.toFloat() / 255f,
                    col.green.toFloat() / 255f,
                    col.blue.toFloat() / 255f,
                    1f
                )
                val matrix = matrixStack.peek().positionMatrix
                val tesselator = RenderSystem.renderThreadTesselator()
                val bufferBuilder = tesselator.buffer

                /* Start Drawing */
                bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION)
                bufferBuilder.vertex(matrix, pos1.getX().toFloat(), pos1.getY().toFloat(), pos1.getZ().toFloat()).next()
                bufferBuilder.vertex(matrix, pos2.getX().toFloat(), pos2.getY().toFloat(), pos2.getZ().toFloat()).next()

                /* End Drawing */
                tesselator.draw()

                matrixStack.pop()
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glDisable(GL11.GL_BLEND)
            }

            fun outlinedBox(block: Vec3d, matrixStack: MatrixStack, color: Int) {
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glEnable(GL11.GL_CULL_FACE)
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                matrixStack.push()
                val bb = Box(-0.5, 0.0, -0.5, 0.5, 1.0, 0.5)
                val camPos: BlockPos = Onyx.MC.gameRenderer.camera.blockPos
                val regionX = camPos.x
                val regionZ = camPos.z

                applyRegionalRenderOffset(matrixStack, regionX, regionZ)
                matrixStack.translate(block.getX() - regionX, block.getY(), block.getZ() - regionZ)
                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                val col = Color(color)

                val tesselator = RenderSystem.renderThreadTesselator()
                val bufferBuilder = tesselator.buffer

                bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION)
                RenderSystem.setShaderColor(
                    col.red.toFloat() / 255f,
                    col.green.toFloat() / 255f,
                    col.blue.toFloat() / 255f,
                    1f
                )
                boxLines(bb, matrixStack, tesselator)
                tesselator.draw()

                matrixStack.pop()
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glDisable(GL11.GL_BLEND)
            }

            fun sideBox(block: Vec3d, matrixStack: MatrixStack, color: Int) {
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glEnable(GL11.GL_CULL_FACE)
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                matrixStack.push()
                val bb = Box(-0.5, 0.0, -0.5, 0.5, 1.0, 0.5)
                val camPos: BlockPos = Onyx.MC.gameRenderer.camera.blockPos
                val regionX = camPos.x
                val regionZ = camPos.z

                applyRegionalRenderOffset(matrixStack, regionX, regionZ)
                matrixStack.translate(block.getX() - regionX, block.getY(), block.getZ() - regionZ)
                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                val col = Color(color)

                val tesselator = RenderSystem.renderThreadTesselator()
                val bufferBuilder = tesselator.buffer

                bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
                RenderSystem.setShaderColor(
                    col.red.toFloat() / 255f,
                    col.green.toFloat() / 255f,
                    col.blue.toFloat() / 255f,
                    0.3f
                )
                boxFill(bb, matrixStack, tesselator)
                tesselator.draw()

                matrixStack.pop()
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glDisable(GL11.GL_BLEND)
            }


            fun box(block: Vec3d, matrixStack: MatrixStack, color: Int) {
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glEnable(GL11.GL_CULL_FACE)
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                matrixStack.push()
                val bb = Box(-0.5, 0.0, -0.5, 0.5, 1.0, 0.5)
                val camPos: BlockPos = Onyx.MC.gameRenderer.camera.blockPos
                val regionX = camPos.x
                val regionZ = camPos.z

                applyRegionalRenderOffset(matrixStack, regionX, regionZ)
                matrixStack.translate(block.getX() - regionX, block.getY(), block.getZ() - regionZ)
                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                val col = Color(color)

                val tesselator = RenderSystem.renderThreadTesselator()
                val bufferBuilder = tesselator.buffer

                bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION)
                RenderSystem.setShaderColor(
                    col.red.toFloat() / 255f,
                    col.green.toFloat() / 255f,
                    col.blue.toFloat() / 255f,
                    1f
                )
                boxLines(bb, matrixStack, tesselator)
                tesselator.draw()

                bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
                RenderSystem.setShaderColor(
                    col.red.toFloat() / 255f,
                    col.green.toFloat() / 255f,
                    col.blue.toFloat() / 255f,
                    0.3f
                )
                boxFill(bb, matrixStack, tesselator)
                tesselator.draw()

                matrixStack.pop()
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glDisable(GL11.GL_BLEND)
            }

            fun box(box: Box, block: Vec3d, matrixStack: MatrixStack, color: Int) {
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glEnable(GL11.GL_CULL_FACE)
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                matrixStack.push()
                val camPos: BlockPos = Onyx.MC.gameRenderer.camera.blockPos
                val regionX = camPos.x
                val regionZ = camPos.z

                applyRegionalRenderOffset(matrixStack, regionX, regionZ)
                matrixStack.translate(block.getX() - regionX, block.getY(), block.getZ() - regionZ)
                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                val col = Color(color)

                val tesselator = RenderSystem.renderThreadTesselator()
                val bufferBuilder = tesselator.buffer

                bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION)
                RenderSystem.setShaderColor(
                    col.red.toFloat() / 255f,
                    col.green.toFloat() / 255f,
                    col.blue.toFloat() / 255f,
                    1f
                )
                boxLines(box, matrixStack, tesselator)
                tesselator.draw()

                bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
                RenderSystem.setShaderColor(
                    col.red.toFloat() / 255f,
                    col.green.toFloat() / 255f,
                    col.blue.toFloat() / 255f,
                    0.3f
                )
                boxFill(box, matrixStack, tesselator)
                tesselator.draw()

                matrixStack.pop()
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glDisable(GL11.GL_BLEND)
            }

            fun sideBox(box: Box, block: Vec3d, matrixStack: MatrixStack, color: Int) {
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glEnable(GL11.GL_CULL_FACE)
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                matrixStack.push()
                val camPos: BlockPos = Onyx.MC.gameRenderer.camera.blockPos
                val regionX = camPos.x
                val regionZ = camPos.z

                applyRegionalRenderOffset(matrixStack, regionX, regionZ)
                matrixStack.translate(block.getX() - regionX, block.getY(), block.getZ() - regionZ)
                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                val col = Color(color)

                val tesselator = RenderSystem.renderThreadTesselator()
                val bufferBuilder = tesselator.buffer

                bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
                RenderSystem.setShaderColor(
                    col.red.toFloat() / 255f,
                    col.green.toFloat() / 255f,
                    col.blue.toFloat() / 255f,
                    0.3f
                )
                boxFill(box, matrixStack, tesselator)
                tesselator.draw()

                matrixStack.pop()
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glDisable(GL11.GL_BLEND)
            }

            fun outlinedBox(box: Box, block: Vec3d, matrixStack: MatrixStack, color: Int) {
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glEnable(GL11.GL_CULL_FACE)
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                matrixStack.push()
                val camPos: BlockPos = Onyx.MC.gameRenderer.camera.blockPos
                val regionX = camPos.x
                val regionZ = camPos.z

                applyRegionalRenderOffset(matrixStack, regionX, regionZ)
                matrixStack.translate(block.getX() - regionX, block.getY(), block.getZ() - regionZ)
                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                val col = Color(color)

                val tesselator = RenderSystem.renderThreadTesselator()
                val bufferBuilder = tesselator.buffer

                bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION)
                RenderSystem.setShaderColor(
                    col.red.toFloat() / 255f,
                    col.green.toFloat() / 255f,
                    col.blue.toFloat() / 255f,
                    1f
                )
                boxLines(box, matrixStack, tesselator)
                tesselator.draw()

                matrixStack.pop()
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glDisable(GL11.GL_BLEND)
            }

            fun multiBlock(blocks: ArrayList<Vec3d>, matrixStack: MatrixStack, color: Int) {
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glEnable(GL11.GL_CULL_FACE)
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                matrixStack.push()
                val camPos: BlockPos = Onyx.MC.gameRenderer.camera.blockPos
                val regionX = camPos.x
                val regionZ = camPos.z

                applyRegionalRenderOffset(matrixStack, regionX, regionZ)

                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                val col = Color(color)

                val tesselator = RenderSystem.renderThreadTesselator()
                val bufferBuilder = tesselator.buffer

                val sizeX: Double = abs(CoordinateUtils.getHighestX(blocks) - CoordinateUtils.getLowestX(blocks))
                val sizeY: Double = abs(CoordinateUtils.getHighestY(blocks) - CoordinateUtils.getLowestY(blocks)) + 1
                val sizeZ: Double = abs(CoordinateUtils.getHighestZ(blocks) - CoordinateUtils.getLowestZ(blocks))

                val bb = Box(-(sizeX / 2 + 0.5), 0.0, -(sizeZ / 2 + 0.5), sizeX / 2 + 0.5, sizeY, sizeZ / 2 + 0.5)

                val center = Vec3d(
                    CoordinateUtils.getLowestX(blocks) + (sizeX / 2),
                    CoordinateUtils.getLowestY(blocks) + (sizeY / 2),
                    CoordinateUtils.getLowestZ(blocks) + (sizeZ / 2)
                )

                matrixStack.translate(center.getX() - regionX, center.getY() - sizeY / 2, center.getZ() - regionZ)

                RenderSystem.setShaderColor(
                    col.red.toFloat() / 255f,
                    col.green.toFloat() / 255f,
                    col.blue.toFloat() / 255f,
                    0.5f
                )

                bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION)
                boxLines(bb, matrixStack, tesselator)

                tesselator.draw()

                RenderSystem.setShaderColor(
                    col.red.toFloat() / 255f,
                    col.green.toFloat() / 255f,
                    col.blue.toFloat() / 255f,
                    0.03f
                )

                bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
                boxFill(bb, matrixStack, tesselator)

                tesselator.draw()

                matrixStack.pop()
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glDisable(GL11.GL_BLEND)
            }

            fun floorQuad(block: Vec3d, matrixStack: MatrixStack, color: Int) {
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glEnable(GL11.GL_CULL_FACE)
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                matrixStack.push()
                val bb = Box(-0.5, 0.0, -0.5, 0.5, 0.0, 0.5)
                val camPos: BlockPos = Onyx.MC.gameRenderer.camera.blockPos
                val regionX = camPos.x
                val regionZ = camPos.z

                applyRegionalRenderOffset(matrixStack, regionX, regionZ)
                matrixStack.translate(block.getX() - regionX, block.getY() - 0.5, block.getZ() - regionZ)
                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                val col = Color(color)

                val tesselator = RenderSystem.renderThreadTesselator()
                val bufferBuilder = tesselator.buffer

                bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION)
                RenderSystem.setShaderColor(
                    col.red.toFloat() / 255f,
                    col.green.toFloat() / 255f,
                    col.blue.toFloat() / 255f,
                    1f
                )
                boxLines(bb, matrixStack, tesselator)
                tesselator.draw()

                bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
                RenderSystem.setShaderColor(
                    col.red.toFloat() / 255f,
                    col.green.toFloat() / 255f,
                    col.blue.toFloat() / 255f,
                    0.3f
                )
                boxFill(bb, matrixStack, tesselator)
                tesselator.draw()

                matrixStack.pop()
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glDisable(GL11.GL_BLEND)
            }

            private fun applyRegionalRenderOffset(matrixStack: MatrixStack, regionX: Int, regionZ: Int) {
                val camPos: Vec3d = Onyx.MC.gameRenderer.camera.pos
                matrixStack.translate(regionX - camPos.x, -camPos.y, regionZ - camPos.z)
            }

            private fun boxLines(bb: Box, matrixStack: MatrixStack, tesselator: Tessellator) {
                val matrix = matrixStack.peek().positionMatrix
                val bufferBuilder = tesselator.buffer

                /* Start Drawing */
                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()).next()

                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()).next()

                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()).next()

                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()).next()

                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()).next()

                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()).next()

                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()).next()

                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()).next()

                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()).next()

                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()).next()

                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()).next()

                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()).next()

                /* End Drawing */
            }

            private fun boxFill(bb: Box, matrixStack: MatrixStack, tesselator: Tessellator) {
                val matrix = matrixStack.peek().positionMatrix
                val bufferBuilder = tesselator.buffer
                RenderSystem.setShader { GameRenderer.getPositionProgram() }


                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()).next()

                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()).next()

                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()).next()

                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()).next()

                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.maxX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()).next()

                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()).next()
                bufferBuilder.vertex(matrix, bb.minX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()).next()
            }
        }
    }

    class TwoDimensional {
        companion object {
            fun line(xp1: Float, yp1: Float, xp2: Float, yp2: Float, color: Int, matrix4f: Matrix4f) {
                var x1 = xp1
                var y1 = yp1
                var x2 = xp2
                var y2 = yp2
                GL11.glDisable(GL11.GL_DEPTH_TEST)

                var i: Float
                if (x1 < x2) {
                    i = x1
                    x1 = x2
                    x2 = i
                }

                if (y1 < y2) {
                    i = y1
                    y1 = y2
                    y2 = i
                }

                val tesselator = RenderSystem.renderThreadTesselator()
                val bufferBuilder = tesselator.buffer

                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                val col = Color(color)

                RenderSystem.setShaderColor(
                    col.red.toFloat() / 255f,
                    col.green.toFloat() / 255f,
                    col.blue.toFloat() / 255f,
                    0.5f
                )

                bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION)


                bufferBuilder.vertex(matrix4f, x1, y1, 0F).next()
                bufferBuilder.vertex(matrix4f, x2, y2, 0F).next()


                tesselator.draw()
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
            }

            private fun fillRoundM4f(xp1: Float, yp1: Float, xp2: Float, yp2: Float, color: Int, colorOutline: Int, precision: Float, radius: Float, matrix4f: Matrix4f) {
                var x1 = xp1
                var y1 = yp1
                var x2 = xp2
                var y2 = yp2

                GL11.glEnable(GL11.GL_BLEND)
                //GL11.glDisable(GL11.GL_DEPTH_TEST)
                GL11.glEnable(GL11.GL_LINE_SMOOTH)
                GL11.glEnable(GL11.GL_POLYGON_SMOOTH)

                var i: Float
                if (x1 < x2) {
                    i = x1
                    x1 = x2
                    x2 = i
                }

                if (y1 < y2) {
                    i = y1
                    y1 = y2
                    y2 = i
                }

                val tesselator = RenderSystem.renderThreadTesselator()
                val bufferBuilder = tesselator.buffer

                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)

                val colOut = Color(colorOutline)
                RenderSystem.setShaderColor(
                    colOut.red.toFloat() / 255f,
                    colOut.green.toFloat() / 255f,
                    colOut.blue.toFloat() / 255f,
                    colOut.alpha.toFloat() / 255f
                )

                var angle = 0.0
                while (angle <= 2.0 * Math.PI) {
                    val x = cos(angle).toFloat() * radius
                    val y = sin(angle).toFloat() * radius
                    bufferBuilder.vertex(matrix4f, x1+1.2F - radius + x, y1+1.2F - radius + y, 0f).next()
                    bufferBuilder.vertex(matrix4f, x1+1.2F - radius + x, y2-1.2F + radius - y, 0f).next()
                    bufferBuilder.vertex(matrix4f, x2-1.2F + radius - x, y2-1.2F + radius - y, 0f).next()
                    bufferBuilder.vertex(matrix4f, x2-1.2F + radius - x, y1+1.2F - radius + y, 0f).next()
                    angle += precision.toDouble()
                }
                tesselator.draw()
                bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)

                val col = Color(color)
                RenderSystem.setShaderColor(
                    col.red.toFloat() / 255f,
                    col.green.toFloat() / 255f,
                    col.blue.toFloat() / 255f,
                    col.alpha.toFloat() / 255f
                )

                angle = 0.0
                while (angle <= 2.0 * Math.PI) {
                    val x = cos(angle).toFloat() * radius
                    val y = sin(angle).toFloat() * radius
                    bufferBuilder.vertex(matrix4f, x1 - radius + x, y1 - radius + y, 0f).next()
                    bufferBuilder.vertex(matrix4f, x1 - radius + x, y2 + radius - y, 0f).next()
                    bufferBuilder.vertex(matrix4f, x2 + radius - x, y2 + radius - y, 0f).next()
                    bufferBuilder.vertex(matrix4f, x2 + radius - x, y1 - radius + y, 0f).next()
                    angle += precision.toDouble()
                }


                tesselator.draw()
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
                GL11.glDisable(GL11.GL_LINE_SMOOTH)
                GL11.glDisable(GL11.GL_POLYGON_SMOOTH)
                //GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glDisable(GL11.GL_BLEND)

            }

            private fun fillRoundNoOutlineM4f(xp1: Float, yp1: Float, xp2: Float, yp2: Float, color: Int, precision: Float, radius: Float, matrix4f: Matrix4f) {
                var x1 = xp1
                var y1 = yp1
                var x2 = xp2
                var y2 = yp2

                GL11.glEnable(GL11.GL_BLEND)
                //GL11.glDisable(GL11.GL_DEPTH_TEST)
                GL11.glEnable(GL11.GL_LINE_SMOOTH)
                GL11.glEnable(GL11.GL_POLYGON_SMOOTH)

                var i: Float
                if (x1 < x2) {
                    i = x1
                    x1 = x2
                    x2 = i
                }

                if (y1 < y2) {
                    i = y1
                    y1 = y2
                    y2 = i
                }

                val tesselator = RenderSystem.renderThreadTesselator()
                val bufferBuilder = tesselator.buffer

                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)

                val col = Color(color)
                RenderSystem.setShaderColor(
                    col.red.toFloat() / 255f,
                    col.green.toFloat() / 255f,
                    col.blue.toFloat() / 255f,
                    col.alpha.toFloat() / 255f
                )

                var angle = 0.0
                while (angle <= 2.0 * Math.PI) {
                    val x = cos(angle).toFloat() * radius
                    val y = sin(angle).toFloat() * radius
                    bufferBuilder.vertex(matrix4f, x1 - radius + x, y1 - radius + y, 0f).next()
                    bufferBuilder.vertex(matrix4f, x1 - radius + x, y2 + radius - y, 0f).next()
                    bufferBuilder.vertex(matrix4f, x2 + radius - x, y2 + radius - y, 0f).next()
                    bufferBuilder.vertex(matrix4f, x2 + radius - x, y1 - radius + y, 0f).next()
                    angle += precision.toDouble()
                }


                tesselator.draw()
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
                GL11.glDisable(GL11.GL_LINE_SMOOTH)
                GL11.glDisable(GL11.GL_POLYGON_SMOOTH)
                //GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glDisable(GL11.GL_BLEND)

            }

            fun fill(xp1: Float, yp1: Float, xp2: Float, yp2: Float, color: Int, matrix4f: Matrix4f?) {
                var x1 = xp1
                var y1 = yp1
                var x2 = xp2
                var y2 = yp2
                GL11.glDisable(GL11.GL_DEPTH_TEST)

                var i: Float
                if (x1 < x2) {
                    i = x1
                    x1 = x2
                    x2 = i
                }

                if (y1 < y2) {
                    i = y1
                    y1 = y2
                    y2 = i
                }

                val tesselator = RenderSystem.renderThreadTesselator()
                val bufferBuilder = tesselator.buffer

                RenderSystem.setShader { GameRenderer.getPositionProgram() }
                val col = Color(color)
                RenderSystem.setShaderColor(
                    col.red.toFloat() / 255f,
                    col.green.toFloat() / 255f,
                    col.blue.toFloat() / 255f,
                    0.5f
                )

                bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)


                bufferBuilder.vertex(matrix4f, x1, y1, 0F).next()
                bufferBuilder.vertex(matrix4f, x1, y2, 0F).next()
                bufferBuilder.vertex(matrix4f, x2, y2, 0F).next()
                bufferBuilder.vertex(matrix4f, x2, y1, 0F).next()


                tesselator.draw()
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
            }

            fun fillRoundNoOutline(xp1: Float, yp1: Float, xp2: Float, yp2: Float, color: Int, context: DrawContext, precision: Float, radius: Float) {
                var x1 = xp1
                var y1 = yp1
                var x2 = xp2
                var y2 = yp2

                val matrix4f = context.matrices.peek().positionMatrix
                GL11.glEnable(GL11.GL_POLYGON_SMOOTH)
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE)
                GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST)

                var i: Float
                if (x1 < x2) {
                    i = x1
                    x1 = x2
                    x2 = i
                }

                if (y1 < y2) {
                    i = y1
                    y1 = y2
                    y2 = i
                }

                val f = ColorHelper.Argb.getAlpha(color).toFloat() / 255.0f
                val g = ColorHelper.Argb.getRed(color).toFloat() / 255.0f
                val h = ColorHelper.Argb.getGreen(color).toFloat() / 255.0f
                val j = ColorHelper.Argb.getBlue(color).toFloat() / 255.0f
                val vertexConsumer = context.vertexConsumers.getBuffer(RenderLayer.getGui())

                var angle = 0.0
                while (angle <= 2.0 * Math.PI) {
                    val x = cos(angle).toFloat() * radius
                    val y = sin(angle).toFloat() * radius
                    vertexConsumer.vertex(matrix4f, x1 - radius + x, y1 - radius + y, 0f).color(g, h, j, f).next()
                    vertexConsumer.vertex(matrix4f, x1 - radius + x, y2 + radius - y, 0f).color(g, h, j, f).next()
                    vertexConsumer.vertex(matrix4f, x2 + radius - x, y2 + radius - y, 0f).color(g, h, j, f).next()
                    vertexConsumer.vertex(matrix4f, x2 + radius - x, y1 - radius + y, 0f).color(g, h, j, f).next()
                    angle += precision.toDouble()
                }
                context.draw()

                GL11.glDisable(GL11.GL_POLYGON_SMOOTH)
                GL11.glDisable(GL11.GL_BLEND)

            }

            fun fillRound(xp1: Float, yp1: Float, xp2: Float, yp2: Float, color: Int, outlineColor: Int, context: DrawContext, precision: Float, radius: Float, glow: Boolean = true) {
                if (glow && Settings.INSTANCE.settings.getById<BooleanSetting>("glow")!!.enabled) {
                    for (i in (2..32) step 4) {
                        val v = 1+i/10
                        val tColor = Color(outlineColor)
                        val colorC = Color(tColor.red, tColor.green, tColor.blue, 4)

                        this.fillRoundNoOutline(xp1-v, yp1-v, xp2+v, yp2+v, colorC.rgb, context, precision, radius)
                    }
                }

                this.fillRoundNoOutline(xp1-1.1f, yp1-1.1f, xp2+1.1f, yp2+1.1f, outlineColor, context, precision, radius)

                this.fillRoundNoOutline(xp1, yp1, xp2, yp2, color, context, precision, radius)
            }
        }
    }

    class Misc {
        companion object {
            fun getRainbowColor(): FloatArray {
                val x = System.currentTimeMillis() % 2000 / 1000f
                val pi = Math.PI.toFloat()

                val rainbow = FloatArray(3)
                rainbow[0] = 0.5f + 0.5f * MathHelper.sin(x * pi)
                rainbow[1] = 0.5f + 0.5f * MathHelper.sin((x + 4f / 3f) * pi)
                rainbow[2] = 0.5f + 0.5f * MathHelper.sin((x + 8f / 3f) * pi)
                return rainbow
            }
        }
    }

    class Text {
        companion object {
            fun draw(context: DrawContext, text: String, x: Int, y: Int, color: Int) {
                context.drawText(Onyx.MC.textRenderer, text, x, y, color, false)
            }
        }
    }
}