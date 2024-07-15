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

@file:Suppress("unused", "DuplicatedCode", "SameParameterValue", "UNUSED_PARAMETER")

package net.integr.modules.impl

import net.integr.Onyx
import net.integr.Variables
import net.integr.event.PreTickEvent
import net.integr.eventsystem.EventListen
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.modules.management.settings.impl.CyclerSetting
import net.integr.modules.management.settings.impl.SliderSetting
import net.integr.utilities.game.*
import net.integr.utilities.game.command.CommandUtils
import net.integr.utilities.game.entity.DamageUtil
import net.integr.utilities.game.entity.EntityFinder
import net.integr.utilities.game.highlight.Highlighter
import net.integr.utilities.game.interaction.AttackUtils
import net.integr.utilities.game.interaction.BlockUtil
import net.integr.utilities.game.interaction.ClickUtils
import net.integr.utilities.game.interaction.RotationUtils
import net.integr.utilities.game.inventory.InvUtils
import net.integr.utilities.game.inventory.ToolUtils
import net.integr.utilities.game.pausers.CombatPauser
import net.integr.utilities.game.rotationfake.RotationLocker
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3i
import net.minecraft.world.EmptyBlockView
import java.util.concurrent.atomic.AtomicInteger


class CrystalAuraModule : Module("Crystal Aura", "Automatically places crystals", "crystalAura", mutableListOf(Filter.Combat)) {
    init {
        settings
            .add(SliderSetting("Amount: ", "Crystal Amount", "amount", 1.0, 4.0))
            .add(SliderSetting("Max Active: ", "Maximum amount of crystals active at once", "maxActive", 1.0, 10.0))
            .add(SliderSetting("Min Damage: ", "Minimum damage amount", "minDamage", 0.0, 120.0))
            .add(SliderSetting("Self Max Damage: ", "Maximum self damage amount", "selfMaxDamage", 0.0, 120.0))
            .add(SliderSetting("Entity Radius: ", "The Radius around the targeted entity", "entityRadius", 1.0, 7.0))
            .add(BooleanSetting("Support", "Allow placing support", "support"))
            .add(BooleanSetting("Mine", "Attempt to mine blocks if needed", "mine"))
            .add(SliderSetting("Mining Tool Dura Min: ", "The minimum allowed durability to be left", "minDura", 0.0, 100.0))
            .add(SliderSetting("Max Mine Ticks: ", "The maximum allowed ticks to mine", "mineTicks", 10.0, 200.0))
            .add(BooleanSetting("Sight", "Check if the player can see the entity", "see"))
            .add(SliderSetting("Range: ", "The maximum allowed range for entities", "range", 1.0, 16.0))
            .add(SliderSetting("Place Range: ", "The maximum allowed range for placing", "placeRange", 1.0, 7.0))
            .add(BooleanSetting("Rotate", "Enable rotations if wanted", "rotate"))
            .add(CyclerSetting("Target: ", "The entities to target", "target", mutableListOf("All", "Players", "Mobs")))
            .add(SliderSetting("Delay: ", "The ticks to wait after each cycle", "delay", 0.0, 5.0))
            .add(SliderSetting("Break Delay: ", "The ticks to wait before breaking each crystal", "breakDelay", 0.0, 25.0))
            .add(BooleanSetting("Swing", "Swing the hand when placing", "swing"))
            .add(BooleanSetting("ReDupe", "Automatically Dupes resources on DupeAnarchy", "redupe"))
            .add(BooleanSetting("Pause On Use", "Automatically Pauses on Item use", "pauseOnUse"))

    }

    private var lastGrid: MutableMap<BlockPos, Double> = mutableMapOf()
    private var timer = 0
    private var reDupeTimer = 0
    private var crystals: List<Pair<BlockPos, AtomicInteger>> = mutableListOf()

    private var minePosition: BlockPos? = null
    private var mineTicks = 0
    private var facePlaceMining = false
    private var facePlaceCrystalCounter = 0

    private var minePre = -1

    override fun onDisable() {
        Variables.target = null
        Highlighter.clearCrystal()

        RotationLocker.unLock()

        minePosition = null
        mineTicks = 0
        facePlaceMining = false

        facePlaceCrystalCounter = 0
    }

    @EventListen
    fun onTick(event: PreTickEvent) {
        if (timer > 0) {
            timer--
            return
        }

        if (settings.getById<BooleanSetting>("pauseOnUse")!!.isEnabled() && Onyx.MC.player!!.isUsingItem) return
        if (CombatPauser.isPaused()) return

        val obsSlot = findSupport()
        val crystalSlot = findCrystal()

        val amount = settings.getById<SliderSetting>("amount")!!.getSetValue().toInt()
        val maxActive = settings.getById<SliderSetting>("maxActive")!!.getSetValue().toInt()
        val delay = settings.getById<SliderSetting>("delay")!!.getSetValue().toInt()
        val breakDelay = settings.getById<SliderSetting>("breakDelay")!!.getSetValue().toInt()
        val entityRadius = settings.getById<SliderSetting>("entityRadius")!!.getSetValue()
        val support = settings.getById<BooleanSetting>("support")!!.isEnabled()
        val minDmg = settings.getById<SliderSetting>("minDamage")!!.getSetValue()
        val selfMaxDamage = settings.getById<SliderSetting>("selfMaxDamage")!!.getSetValue()
        val see = settings.getById<BooleanSetting>("see")!!.isEnabled()
        val rotate = settings.getById<BooleanSetting>("rotate")!!.isEnabled()
        val range = settings.getById<SliderSetting>("range")!!.getSetValue()
        val placeRange = settings.getById<SliderSetting>("placeRange")!!.getSetValue()
        val target = settings.getById<CyclerSetting>("target")!!.getElement()
        val mine = settings.getById<BooleanSetting>("mine")!!.isEnabled()
        val minDura = settings.getById<SliderSetting>("minDura")!!.getSetValue()
        val mineTicksMax = settings.getById<SliderSetting>("mineTicks")!!.getSetValue()
        val redupe = settings.getById<BooleanSetting>("redupe")!!.isEnabled()
        val swing = settings.getById<BooleanSetting>("swing")!!.isEnabled()

        val entity = EntityFinder.getClosestAuraEntity(range, target, see)

        if (entity != null) {
            if (reDupeTimer > 0) {
                reDupeTimer--
            }

            val shouldRedupe = redupe && InvUtils.countInHotbar(Items.END_CRYSTAL) <= 32 || redupe && InvUtils.countInHotbar(Items.OBSIDIAN) <= 32

            if (minePosition != null) {
                if (minePre != -1) {
                    InvUtils.selectSlotPacket(minePre)
                    minePre = -1
                }

                if (facePlaceMining) {
                    val bestTool = ToolUtils.getBestSlotForBlock(minePosition!!, maxBreakPercent = minDura.toInt())
                    minePre = InvUtils.getSelectedSlot()
                    InvUtils.selectSlotPacket(bestTool)

                    Onyx.MC.networkHandler!!.sendPacket(PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, minePosition, Direction.UP))
                    Highlighter.renderBlock(minePosition!!.toCenterPos().add(0.0, -0.5, 0.0), 30)
                    placeCrystal(getOptimalFacePlacePosition(minePosition!!, entity.blockPos), entity, rotate, obsSlot, crystalSlot, delay, mine, placeRange, shouldRedupe, swing)

                    val hasCrystal = EntityFinder.getClosestCrystalAround(minePosition!!.toCenterPos()) != null

                    if (hasCrystal) facePlaceCrystalCounter++

                    if (!shouldFacePlaceMine(entity, placeRange) && CoordinateUtils.distanceBetween(entity.pos, minePosition!!.toCenterPos()) > 2 || facePlaceCrystalCounter > 15) {
                        minePosition = null
                        mineTicks = 0
                        facePlaceMining = false
                        facePlaceCrystalCounter = 0
                    }
                } else {
                    if (mineTicks < mineTicksMax.toInt() && !isMinedAir(minePosition!!)) {
                        mineTicks++
                        val bestTool = ToolUtils.getBestSlotForBlock(minePosition!!, maxBreakPercent = minDura.toInt())
                        minePre = InvUtils.getSelectedSlot()
                        InvUtils.selectSlotPacket(bestTool)

                        mineBlock(minePosition!!)
                    } else mineTicks = 0

                    if (isMinedAir(minePosition!!) || mineTicks >= mineTicksMax.toInt()) {
                        minePosition = null
                        mineTicks = 0
                    }

                    return
                }
            }

            crystals = crystals.filter { it.second.get() > -1 }

            for (position in crystals) {
                if (position.second.get() > 0) {
                    position.second.set(position.second.get()-1)
                    continue
                }

                breakOnly(position.first)
                position.second.set(-1)
            }

            if (isPhased(entity) && canMine(entity.blockPos, placeRange)) {
                mineBlock(entity.blockPos)
                return
            }

            if (crystals.count() < maxActive && !facePlaceMining) {
                if (crystalSlot != -1) {
                    loadNextGrid(entity, entityRadius.toInt(), true, placeRange, minDmg, selfMaxDamage, (support && obsSlot != -1))

                    val positions = getBest(amount)

                    if (positions.isNotEmpty()) {
                        for (position in positions.distinct()) {
                            placeOnly(position, entity, rotate, obsSlot, crystalSlot, breakDelay, mine, placeRange, shouldRedupe, swing)
                            timer = delay
                        }
                    } else if (mine) runFacePlaceMining(entity, placeRange)

                    Variables.target = entity
                }
            }

        } else RotationLocker.unLock()

        if (Variables.target != null && !Variables.target!!.isAlive) {
            Variables.target = null
            Highlighter.clearCrystal()
        }
    }

    private fun getOptimalFacePlacePosition(placementPos: BlockPos, entityPos: BlockPos): BlockPos {
        val dir = CoordinateUtils.getDirectionOfBlockToBlockAsVector(entityPos, placementPos)

        val extended = placementPos.add(dir)

        return if (Onyx.MC.world!!.getBlockState(extended).isAir) extended else placementPos
    }

    private fun isPhased(entity: Entity): Boolean {
        return Onyx.MC.world!!.getBlockState(entity.blockPos).isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN)
    }

    private fun findSupport(): Int {
        for (i in 0..8) {
            val stack = Onyx.MC.player!!.inventory.getStack(i)

            if (stack!!.item == Items.OBSIDIAN || stack.item == Items.BEDROCK) return i
        }

        return -1
    }

    private fun findCrystal(): Int {
        for (i in 0..8) {
            val stack = Onyx.MC.player!!.inventory.getStack(i)

            if (stack!!.item == Items.END_CRYSTAL) return i
        }

        return -1
    }

    private fun runFacePlaceMining(entity: Entity, maxReach: Double) {
        val pos = entity.blockPos
        val pairs = listOf(Pair(+1, 0), Pair(-1, 0), Pair(0, +1), Pair(0, -1))

        for ((i, j) in pairs) {
            if (canPlaceOn(pos.add(i, -1, j)) && canMine(pos.add(i, 0, j), maxReach) || isMinedAir(pos.add(i, -1, j)) && canMine(pos.add(i, 0, j), maxReach)) {
                mineBlockStart(pos.add(i, 0, j))
                facePlaceMining = true
                return
            }
        }
    }

    private fun shouldFacePlaceMine(entity: Entity, maxReach: Double): Boolean {
        if (hasOpenSpots(entity)) return false

        val pos = entity.blockPos
        val pairs = listOf(Pair(+1, 0), Pair(-1, 0), Pair(0, +1), Pair(0, -1))

        for ((i, j) in pairs) {
            if (canPlaceOn(pos.add(i, -1, j)) && canMine(pos.add(i, 0, j), maxReach) || isMinedAir(pos.add(i, -1, j)) && canMine(pos.add(i, 0, j), maxReach)) {
                return true
            }
        }

        return false
    }

    private fun hasOpenSpots(entity: Entity): Boolean {
        val pos = entity.blockPos
        val pairs = listOf(Pair(+1, 0), Pair(-1, 0), Pair(0, +1), Pair(0, -1))

        for ((i, j) in pairs) {
            if (Onyx.MC.world!!.getBlockState(pos.add(i, 0, j)).isAir) {
                return true
            }
        }

        return false
    }

    private fun placeObsidian(bp: BlockPos, obsSlot: Int, redupe: Boolean) {
        val pre = InvUtils.getSelectedSlot()
        InvUtils.selectSlot(obsSlot)

        if (redupe) {
            InvUtils.selectSlotPacket(obsSlot)
            CommandUtils.sendCommand("dupe 2")
            reDupeTimer = 10
        }

        BlockUtil.placeBlock(bp, rotate = false, silent = true, render = false)
        InvUtils.selectSlot(pre)
    }

    private fun mineBlock(bp: BlockPos) {
        BlockUtil.minePosition(bp, swing = false, render = true)
        minePosition = bp
    }

    private fun mineBlockStart(bp: BlockPos) {
        BlockUtil.minePositionStart(bp, swing = false, render = true)
        minePosition = bp
    }

    private fun placeOnly(bp: BlockPos, pe: LivingEntity, rotate: Boolean, obsSlot: Int, crystalSlot: Int, delay: Int, mine: Boolean, maxReach: Double, redupe: Boolean, swing: Boolean) {
        placeCrystal(bp, pe, rotate, obsSlot, crystalSlot, delay, mine, maxReach, redupe, swing)
    }

    private fun breakOnly(bp: BlockPos) {
        breakCrystal(EntityFinder.getClosestCrystalAround(bp.toCenterPos()))
    }

    private fun placeCrystal(bp: BlockPos, pe: LivingEntity, rotate: Boolean, obsSlot: Int, crystalSlot: Int, delay: Int, mine: Boolean, maxReach: Double, redupe: Boolean, swing: Boolean) {
        val trueRedupe = redupe && reDupeTimer == 0

        if (!canPlaceOn(bp.add(0, -1, 0))) {
            if (Onyx.MC.world!!.getBlockState(bp.add(0, -1, 0)).isAir) {
                placeObsidian(bp.add(0, -1, 0), obsSlot, trueRedupe)
            } else if (mine && canMine(bp.add(0, -1, 0), maxReach)) mineBlock(bp.add(0, -1, 0))
        } else {
            crystals += bp to AtomicInteger(delay)

            val pre = InvUtils.getSelectedSlot()

            val hitResult = BlockHitResult(bp.subtract(Vec3i(0, 1, 0)).toCenterPos(), Direction.UP, bp.subtract(Vec3i(0, 1, 0)), false)

            if (rotate) {
                RotationLocker.lock()
                RotationUtils.lookAt(bp.toCenterPos())
            } else RotationLocker.unLock()


            InvUtils.selectSlot(crystalSlot)
            if (trueRedupe) {
                InvUtils.selectSlotPacket(crystalSlot)
                CommandUtils.sendCommand("dupe 2")
                reDupeTimer = 10
            }

            Onyx.MC.interactionManager!!.interactBlock(Onyx.MC.player, Hand.MAIN_HAND, hitResult)

            if (swing) {
                ClickUtils.rightClick()
                ClickUtils.leftClick()
            }

            InvUtils.selectSlot(pre)

            Highlighter.renderCrystal(bp.toCenterPos().add(0.0, -1.5, 0.0))
            Highlighter.renderLine(bp.toCenterPos().add(0.0, -0.5, 0.0), pe.pos, time = 6)
        }
    }

    private fun breakCrystal(entity: Entity?) {
        if (entity != null) {
            if (entity.isAlive) {
                AttackUtils.attackViaManager(entity)

                entity.kill()
                entity.remove(Entity.RemovalReason.KILLED)
                entity.onRemoved()
            }
        }
    }

    private fun getBest(amt: Int): List<BlockPos> {
        return lastGrid.keys.sortedByDescending { lastGrid[it]!! }.take(amt)
    }

    private fun loadNextGrid(e: LivingEntity, radius: Int, checkPlayerAccessible: Boolean, accessRange: Double, minDamage: Double, selfMaxDamage: Double, support: Boolean) {
        lastGrid = getDamageGrid(e, radius, checkPlayerAccessible, accessRange, minDamage, selfMaxDamage, support)
    }

    private fun getDamageGrid(e: LivingEntity, radius: Int, checkPlayerAccessible: Boolean, accessRange: Double, minDamage: Double, selfMaxDamage: Double, support: Boolean): MutableMap<BlockPos, Double> {
        val returnList: MutableMap<BlockPos, Double> = mutableMapOf()

        val fpPenalty = if (shouldFacePlaceMine(e, accessRange)) 80 else 0

        for (i in (-radius..radius)) {
            for (j in (-radius..radius)) {
                for (k in (-radius..radius)) {
                    val bp = e.blockPos.add(i, j, k)

                    var supportPenalty = 0

                    if (!Onyx.MC.world!!.getBlockState(bp.add(0, -1, 0)).isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN)) {
                        if (!support) continue
                        else supportPenalty = 30
                    }

                    if (!Onyx.MC.world!!.getBlockState(bp).isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN) && Onyx.MC.world!!.getBlockState(bp).block != Blocks.WATER && !CoordinateUtils.crystalIsBlockedByEntity(bp)) {
                        val entityDamage = DamageUtil.crystalDamage(e, bp.toCenterPos().add(0.0, -0.5, 0.0), false, bp.add(0, -1, 0)).toDouble()
                        val selfDamage = DamageUtil.crystalDamage(Onyx.MC.player!!, bp.toCenterPos().add(0.0, -0.5, 0.0), false, bp.add(0, -1, 0)).toDouble()

                        if (checkPlayerAccessible && CoordinateUtils.distanceTo(bp.toCenterPos()) <= accessRange || !checkPlayerAccessible) {
                            val miningPenalty = if (Onyx.MC.world!!.getBlockState(bp.add(0, -1, 0)).isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN) && !canPlaceOn(bp.add(0, -1, 0))) 50 else 0
                            if (entityDamage >= minDamage && selfDamage < selfMaxDamage) {
                                val dmg = entityDamage - miningPenalty - fpPenalty - supportPenalty
                                if (dmg > 0) returnList[bp] = dmg
                            }
                        }
                    }
                }
            }
        }

        return returnList
    }

    private val unbreakable: List<Block> = listOf(
        Blocks.BEDROCK,
        Blocks.REINFORCED_DEEPSLATE,
    )

    private fun canPlaceOn(bp: BlockPos): Boolean {
        return Onyx.MC.world!!.getBlockState(bp).block.equals(Blocks.OBSIDIAN) || Onyx.MC.world!!.getBlockState(bp).block.equals(Blocks.BEDROCK)
    }

    private fun isMined(bp: BlockPos, minDura: Int): Boolean {
        return mineTicks >= BlockUtil.getMineTicks(bp, ToolUtils.getBestSlotForBlock(bp, maxBreakPercent = minDura), true)
    }

    private fun isMinedAir(bp: BlockPos): Boolean {
        return !Onyx.MC.world!!.getBlockState(bp).isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN)
    }

    private fun canMine(bp: BlockPos, maxReach: Double): Boolean {
        return Onyx.MC.world!!.getBlockState(bp).isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN) && !unbreakable.contains(Onyx.MC.world!!.getBlockState(bp).block) && CoordinateUtils.distanceTo(bp.toCenterPos()) < maxReach
    }
}