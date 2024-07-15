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

package net.integr.modules.impl

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.systems.VertexSorter
import net.integr.Onyx
import net.integr.Variables
import net.integr.event.RenderWorldEvent
import net.integr.event.UiRenderEvent
import net.integr.event.UpdateWorldRenderEvent
import net.integr.eventsystem.EventListen
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.modules.management.settings.impl.SliderSetting
import net.integr.rendering.RenderingEngine
import net.integr.rendering.uisystem.Slider
import net.integr.utilities.game.CoordinateUtils
import net.minecraft.block.entity.*
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.option.Perspective
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.*
import net.minecraft.world.chunk.WorldChunk
import org.joml.Matrix4f
import org.joml.Vector3d
import org.joml.Vector3f
import org.joml.Vector4f
import java.util.*
import java.util.stream.Stream
import kotlin.math.abs

class EspModule : Module("Esp", "See trough walls", "esp", mutableListOf(Filter.Render)) {
    init {
        settings
            .add(BooleanSetting("Self", "Show the Esp on yourself", "self"))
            .add(BooleanSetting("Items", "See items trough walls", "items"))
            .add(BooleanSetting("Mobs", "See mobs trough walls", "mobs"))
            .add(BooleanSetting("Containers", "See containers trough walls", "containers"))
            .add(BooleanSetting("Tracers", "Render lines", "tracers"))
            .add(SliderSetting("Expand: ", "Expand the entity boxes by this amount", "expand", 0.0, 1.0, 0.2))
            .add(BooleanSetting("Extra Data", "Renders extra info next to the boxes", "data"))
    }

    private var vertexSorter: VertexSorter? = null
    private val pos1 = Vector3d()
    private val pos2 = Vector3d()
    private val pos = Vector3d()

    private var positions: HashMap<Vec3d, String> = HashMap()

    @EventListen
    fun onUpdateWorldRender(event: UpdateWorldRenderEvent) {
        model = Matrix4f(event.matrices.peek().positionMatrix)
        projection = event.matrix4f
        val pos = Onyx.MC.gameRenderer.camera.pos
        cameraNegated.set(Vector3d(pos.x, pos.y, pos.z))
        cameraNegated.negate()

        windowScale = Onyx.MC.window.calculateScaleFactor(1, false).toDouble()
    }

    private fun unscaledProjection() {
        vertexSorter = RenderSystem.getVertexSorting()
        RenderSystem.setProjectionMatrix(
            Matrix4f().setOrtho(
                0f,
                Onyx.MC.window.framebufferWidth.toFloat(),
                Onyx.MC.window.framebufferHeight.toFloat(), 0f, 1000f, 21000f
            ), VertexSorter.BY_Z
        )
    }

    private fun scaledProjection() {
        RenderSystem.setProjectionMatrix(
            Matrix4f().setOrtho(
                0f,
                (Onyx.MC.window.framebufferWidth / Onyx.MC.window.scaleFactor).toFloat(),
                (Onyx.MC.window.framebufferHeight / Onyx.MC.window.scaleFactor).toFloat(), 0f, 1000f, 21000f
            ), vertexSorter
        )
    }

    @EventListen
    fun onRenderTick(event: RenderWorldEvent) {
        if (settings.getById<BooleanSetting>("tracers")!!.isEnabled() && Onyx.MC.options.perspective == Perspective.FIRST_PERSON) {
            val pos = Vector3f(0f, 0f, 1f)

            if (Onyx.MC.options.bobView.value) {
                val bobViewMatrices = MatrixStack()

                bobView(bobViewMatrices)
                pos.mulPosition(bobViewMatrices.peek().positionMatrix.invert())
            }

            val center = Vec3d(pos.x.toDouble(), -pos.y.toDouble(), pos.z.toDouble())
                .rotateX(-Math.toRadians(Onyx.MC.gameRenderer.camera.pitch.toDouble()).toFloat())
                .rotateY(-Math.toRadians(Onyx.MC.gameRenderer.camera.yaw.toDouble()).toFloat())
                .add(Onyx.MC.gameRenderer.camera.pos)

            for (e in Onyx.MC.world!!.entities) {
                if (e.type != EntityType.PLAYER) continue
                if (e == Onyx.MC.cameraEntity) continue

                RenderingEngine.ThreeDimensional.line(center, CoordinateUtils.getLerpedEntityPos(e, event.tickDelta).add(0.0, e.height / 2.0, 0.0), event.matrices, Variables.guiColor)
            }
        }
    }

    private fun bobView(matrices: MatrixStack) {
        val cameraEntity = Onyx.MC.getCameraEntity()

        if (cameraEntity is PlayerEntity) {
            val f = Onyx.MC.tickDelta
            val g = cameraEntity.horizontalSpeed - cameraEntity.prevHorizontalSpeed
            val h = -(cameraEntity.horizontalSpeed + g * f)
            val i: Float = MathHelper.lerp(f, cameraEntity.prevStrideDistance, cameraEntity.strideDistance)

            matrices.translate(-(MathHelper.sin(h * 3.1415927f) * i * 0.5), abs((MathHelper.cos(h * 3.1415927f) * i).toDouble()), 0.0)
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin(h * 3.1415927f) * i * 3))
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((abs((MathHelper.cos(h * 3.1415927f - 0.2f) * i).toDouble()) * 5).toFloat()))
        }
    }

    @EventListen
    fun onRenderUI(event: UiRenderEvent) {
        unscaledProjection()

        for (e in Onyx.MC.world!!.entities) {
            if (e.type == EntityType.ITEM_FRAME) continue
            if (e.type == EntityType.GLOW_ITEM_FRAME) continue
            if (e.type == EntityType.ARMOR_STAND) continue
            if (e.type == EntityType.END_CRYSTAL) continue

            if (!settings.getById<BooleanSetting>("mobs")!!.isEnabled() && e.type != EntityType.ITEM && e.type != EntityType.PLAYER) continue
            if (!settings.getById<BooleanSetting>("items")!!.isEnabled() && e.type == EntityType.ITEM) continue

            if (e == Onyx.MC.player!! && !settings.getById<BooleanSetting>("self")!!.isEnabled()) continue

            val box = e.boundingBox.expand(settings.getById<SliderSetting>("expand")!!.getSetValue())

            val x = MathHelper.lerp(event.tickDelta.toDouble(), e.lastRenderX, e.x) - e.x
            val y = MathHelper.lerp(event.tickDelta.toDouble(), e.lastRenderY, e.y) - e.y
            val z = MathHelper.lerp(event.tickDelta.toDouble(), e.lastRenderZ, e.z) - e.z

            // Check corners
            pos1[Double.MAX_VALUE, Double.MAX_VALUE] = Double.MAX_VALUE
            pos2[0.0, 0.0] = 0.0

            //     Bottom
            if (checkCorner(box.minX + x, box.minY + y, box.minZ + z, pos1, pos2)) continue
            if (checkCorner(box.maxX + x, box.minY + y, box.minZ + z, pos1, pos2)) continue
            if (checkCorner(box.minX + x, box.minY + y, box.maxZ + z, pos1, pos2)) continue
            if (checkCorner(box.maxX + x, box.minY + y, box.maxZ + z, pos1, pos2)) continue

            //     Top
            if (checkCorner(box.minX + x, box.maxY + y, box.minZ + z, pos1, pos2)) continue
            if (checkCorner(box.maxX + x, box.maxY + y, box.minZ + z, pos1, pos2)) continue
            if (checkCorner(box.minX + x, box.maxY + y, box.maxZ + z, pos1, pos2)) continue
            if (checkCorner(box.maxX + x, box.maxY + y, box.maxZ + z, pos1, pos2)) continue


            RenderingEngine.TwoDimensional.line(pos1.x.toFloat(), pos1.y.toFloat(), pos1.x.toFloat(), pos2.y.toFloat(), Variables.guiColor, event.context.matrices.peek().positionMatrix)
            RenderingEngine.TwoDimensional.line(pos2.x.toFloat(), pos1.y.toFloat(), pos2.x.toFloat(), pos2.y.toFloat(), Variables.guiColor, event.context.matrices.peek().positionMatrix)
            RenderingEngine.TwoDimensional.line(pos1.x.toFloat(), pos1.y.toFloat(), pos2.x.toFloat(), pos1.y.toFloat(), Variables.guiColor, event.context.matrices.peek().positionMatrix)
            RenderingEngine.TwoDimensional.line(pos1.x.toFloat(), pos2.y.toFloat(), pos2.x.toFloat(), pos2.y.toFloat(), Variables.guiColor, event.context.matrices.peek().positionMatrix)
            val name = if (e is PlayerEntity) {
                if (e.distanceTo(Onyx.MC.cameraEntity) >= 30) {
                    e.gameProfile.name
                } else ""
            } else e.type.name.string

            Onyx.MC.textRenderer.draw(name, pos1.x.toFloat(), pos1.y.toFloat() - 10f, Variables.guiColor, false, event.context.matrices.peek().positionMatrix, event.context.vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, 10)

            if (e is LivingEntity && Onyx.MC.cameraEntity!!.distanceTo(e) < 30 && settings.getById<BooleanSetting>("data")!!.isEnabled() && e != Onyx.MC.player) {
                RenderingEngine.TwoDimensional.fillRound((pos1.x + (pos2.x - pos1.x) + 18).toFloat(), pos1.y.toFloat() - 2, (pos1.x + (pos2.x - pos1.x) + 122f).toFloat(), pos1.y.toFloat() + 22f+24f, Variables.guiBack, Variables.guiColor,  event.context, 0.05f, 9f)

                val hp = Slider((pos1.x + (pos2.x - pos1.x) + 20).toInt(), pos1.y.toInt(), 100, 20, "Health: ", false, "", 0.0, e.maxHealth.toDouble() + e.maxAbsorption.toDouble())
                hp.setValue(e.health.toDouble() + e.absorptionAmount.toDouble())
                hp.render(event.context, 0, 0, 2F)

                val ar = Slider((pos1.x + (pos2.x - pos1.x) + 20).toInt(), pos1.y.toInt() + 24, 100, 20, "Armor: ", false, "", 0.0, 20.0)
                ar.setValue(e.armor.toDouble())
                ar.render(event.context, 0, 0, 1F)
            }

            event.context.draw()
        }

        if (settings.getById<BooleanSetting>("containers")!!.isEnabled()) {
            prepChestEsp()

            for (v in positions.keys) {
                val box = Box(BlockPos.ofFloored(v))

                val x = 0.0
                val y = 0.0
                val z = 0.0

                // Check corners
                pos1[Double.MAX_VALUE, Double.MAX_VALUE] = Double.MAX_VALUE
                pos2[0.0, 0.0] = 0.0

                //     Bottom
                if (checkCorner(box.minX + x, box.minY + y, box.minZ + z, pos1, pos2)) continue
                if (checkCorner(box.maxX + x, box.minY + y, box.minZ + z, pos1, pos2)) continue
                if (checkCorner(box.minX + x, box.minY + y, box.maxZ + z, pos1, pos2)) continue
                if (checkCorner(box.maxX + x, box.minY + y, box.maxZ + z, pos1, pos2)) continue

                //     Top
                if (checkCorner(box.minX + x, box.maxY + y, box.minZ + z, pos1, pos2)) continue
                if (checkCorner(box.maxX + x, box.maxY + y, box.minZ + z, pos1, pos2)) continue
                if (checkCorner(box.minX + x, box.maxY + y, box.maxZ + z, pos1, pos2)) continue
                if (checkCorner(box.maxX + x, box.maxY + y, box.maxZ + z, pos1, pos2)) continue


                RenderingEngine.TwoDimensional.line(pos1.x.toFloat(), pos1.y.toFloat(), pos1.x.toFloat(), pos2.y.toFloat(), Variables.guiColor, event.context.matrices.peek().positionMatrix)
                RenderingEngine.TwoDimensional.line(pos2.x.toFloat(), pos1.y.toFloat(), pos2.x.toFloat(), pos2.y.toFloat(), Variables.guiColor, event.context.matrices.peek().positionMatrix)
                RenderingEngine.TwoDimensional.line(pos1.x.toFloat(), pos1.y.toFloat(), pos2.x.toFloat(), pos1.y.toFloat(), Variables.guiColor, event.context.matrices.peek().positionMatrix)
                RenderingEngine.TwoDimensional.line(pos1.x.toFloat(), pos2.y.toFloat(), pos2.x.toFloat(), pos2.y.toFloat(), Variables.guiColor, event.context.matrices.peek().positionMatrix)

                Onyx.MC.textRenderer.draw(positions[v], pos1.x.toFloat(), pos1.y.toFloat() - 10f, Variables.guiColor, false, event.context.matrices.peek().positionMatrix, event.context.vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, 10)
                event.context.draw()
            }
        }

        scaledProjection()
    }

    private fun checkCorner(x: Double, y: Double, z: Double, min: Vector3d, max: Vector3d): Boolean {
        pos.set(x, y, z)
        if (!to2D(pos)) return true

        // Check Min
        if (pos.x < min.x) min.x = pos.x
        if (pos.y < min.y) min.y = pos.y
        if (pos.z < min.z) min.z = pos.z

        // Check Max
        if (pos.x > max.x) max.x = pos.x
        if (pos.y > max.y) max.y = pos.y
        if (pos.z > max.z) max.z = pos.z

        return false
    }

    private val vec4: Vector4f = Vector4f()
    private val mmMat4: Vector4f = Vector4f()
    private val pmMat4: Vector4f = Vector4f()
    private val cameraNegated: Vector3d = Vector3d()
    private var model: Matrix4f? = null
    private var projection: Matrix4f? = null
    private var windowScale: Double = 0.0

    private fun to2D(pos: Vector3d): Boolean {
        vec4[cameraNegated.x + pos.x, cameraNegated.y + pos.y, cameraNegated.z + pos.z] = 1.0

        vec4.mul(model, mmMat4)
        mmMat4.mul(projection, pmMat4)

        val behind = pmMat4.w <= 0f

        if (behind) return false

        toScreen(pmMat4)
        val x: Double = (pmMat4.x * Onyx.MC.window.framebufferWidth).toDouble()
        val y: Double = (pmMat4.y * Onyx.MC.window.framebufferHeight).toDouble()

        if (java.lang.Double.isInfinite(x) || java.lang.Double.isInfinite(y)) return false

        pos[x / windowScale, Onyx.MC.window.framebufferHeight - y / windowScale] =
            (pmMat4.z).toDouble()
        return true
    }

    private fun toScreen(vec: Vector4f) {
        val newW = 1.0f / vec.w * 0.5f

        vec.x = vec.x * newW + 0.5f
        vec.y = vec.y * newW + 0.5f
        vec.z = vec.z * newW + 0.5f
        vec.w = newW
    }

    private fun prepChestEsp() {
        positions.clear()
        if (settings.getById<BooleanSetting>("containers")!!.isEnabled()) {
            for (e in getLoadedBlockEntities().toList()) {
                when (e) {
                    is TrappedChestBlockEntity -> positions[e.getPos().toCenterPos().add(0.0, -0.5, 0.0)] = "Trapped Chest"
                    is ChestBlockEntity -> positions[e.getPos().toCenterPos().add(0.0, -0.5, 0.0)] = "Chest"
                    is EnderChestBlockEntity -> positions[e.getPos().toCenterPos().add(0.0, -0.5, 0.0)] = "Ender Chest"
                    is ShulkerBoxBlockEntity -> positions[e.getPos().toCenterPos().add(0.0, -0.5, 0.0)] = "Shulker Box"
                    is BarrelBlockEntity -> positions[e.getPos().toCenterPos().add(0.0, -0.5, 0.0)] = "Barrel"
                }
            }
        }
    }


    private fun getLoadedBlockEntities(): Stream<BlockEntity> {
        return getLoadedChunks().flatMap { chunk: WorldChunk -> chunk.blockEntities.values.stream() }
    }

    private fun getLoadedChunks(): Stream<WorldChunk> {
        val radius = 2.coerceAtLeast(Onyx.MC.options.clampedViewDistance) + 3
        val diameter = radius * 2 + 1

        val center: ChunkPos = Onyx.MC.player!!.chunkPos
        val min = ChunkPos(center.x - radius, center.z - radius)
        val max = ChunkPos(center.x + radius, center.z + radius)

        val stream: Stream<WorldChunk> = Stream.iterate(min) { pos: ChunkPos ->
            var x = pos.x
            var z = pos.z
            x++
            if (x > max.x) {
                x = min.x
                z++
            }
            check(z <= max.z) { "Stream limit didn't work." }
            ChunkPos(x, z)
        }.limit((diameter * diameter).toLong()).filter { c: ChunkPos -> Onyx.MC.world!!.isChunkLoaded(c.x, c.z) }.map { c: ChunkPos -> Onyx.MC.world!!.getChunk(c.x, c.z) }.filter { obj: Any? -> Objects.nonNull(obj) }
        return stream
    }
}