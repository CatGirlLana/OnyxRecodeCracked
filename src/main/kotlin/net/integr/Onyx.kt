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

package net.integr

import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.loader.api.FabricLoader
import net.integr.commands.CommandRegistry
import net.integr.discord.PresenceHandler
import net.integr.event.*
import net.integr.eventsystem.EventListen
import net.integr.eventsystem.EventSystem
import net.integr.eventsystem.Priority
import net.integr.friendsystem.FriendStorage
import net.integr.modules.management.ModuleManager
import net.integr.modules.management.settings.impl.CyclerSetting
import net.integr.modules.management.settings.impl.KeyBindSetting
import net.integr.rendering.RenderingEngine
import net.integr.rendering.screens.GameScreen
import net.integr.rendering.screens.MenuScreen
import net.integr.rendering.uisystem.Box
import net.integr.utilities.LogUtils
import net.integr.utilities.game.highlight.HighlightHandler
import net.integr.utilities.game.rotationfake.RotationFaker
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.util.InputUtil
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.resource.InputSupplier
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Formatting
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWImage
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.util.function.Consumer


class Onyx : ClientModInitializer, ModInitializer {

    companion object {
        val MC: MinecraftClient = MinecraftClient.getInstance()
        val LOGGER: Logger = LoggerFactory.getLogger("Onyx")
        val VERSION: String = FabricLoader.getInstance().getModContainer("onyx").get().metadata.version.toString()
        val CONFIG: Path = Path.of(FabricLoader.getInstance().configDir.toString().substringBeforeLast("config") + "onyx")

        const val DISCORD_ID = "1246381680084910100"

        var openKey: KeyBinding? = null
    }

    private var startTime = 0L

    private var branding: Box = Box(4, 49, 110, 40, null, textCentered = false, outlined = true)

    private var iconSet = false

    private var lastSaveTime = System.currentTimeMillis()

    private fun attemptSave() {
        if (System.currentTimeMillis() - lastSaveTime < 120000) return
        lastSaveTime = System.currentTimeMillis()

        ModuleManager.save()
        FriendStorage.save()

        LogUtils.sendLog("Saved your settings [Periodic]!")
    }

    override fun onInitialize() {
        openKey = KeyBindingHelper.registerKeyBinding(KeyBinding("key.onyx.openmenu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.onyx"))

        startTime = System.currentTimeMillis()
        LogUtils.sendLog("Onyx startup...")

        ModuleManager.init()

        EventSystem.register(this)

        EventSystem.register(GameScreen.INSTANCE)
        EventSystem.register(RotationFaker.INSTANCE)
        EventSystem.register(HighlightHandler.INSTANCE)

        PresenceHandler.start()
        CommandRegistry.init()

        FriendStorage.load()

        LogUtils.sendLog("Onyx started [${System.currentTimeMillis() - startTime}ms]")

    }

    @EventListen(Priority.FIRST)
    fun onStop(event: ClientEndEvent) {
        ModuleManager.save()
        PresenceHandler.stop()
        FriendStorage.save()

        LogUtils.sendLog("Saved your settings [Game Stopped]!")
    }

    @EventListen(Priority.FIRST)
    fun onItemPick(event: DoItemPickEvent) {
        val target = MC.crosshairTarget

        if (target != null && target.type == HitResult.Type.ENTITY) {
            val entity = (target as EntityHitResult).entity

            if (entity != null && entity is PlayerEntity) {
                FriendStorage.handleMiddleClickFriend(entity)
                event.cancel()
            }
        }
    }

    @EventListen(Priority.FIRST)
    fun onTickPre(event: UnsafePreTickEvent) {
        if (MC.window != null) {
            MC.window.setTitle("Onyx | Cracked by CatGirlLana [$VERSION] :)")

            if (!iconSet) {
                try {
                    setIcon("assets/onyx/onyx.png")
                } catch (e: NoSuchFileException) {
                    LogUtils.sendLog("Could not locate Icon... Returning to default!")
                }
                iconSet = true
            }
        }

        if (openKey!!.wasPressed()) {
            MC.setScreen(MenuScreen.INSTANCE)
        }

        when (Settings.INSTANCE.settings.getById<CyclerSetting>("theme")!!.getElement()) {
            "Dark" -> Variables.guiBack = Color(25, 24, 24).rgb
            "Light" -> Variables.guiBack = Color(217, 217, 217).rgb
        }

        when (Settings.INSTANCE.settings.getById<CyclerSetting>("accent")!!.getElement()) {
            "Rgb" -> updateAccent(rgb = true)
            "Orange" -> updateAccent(value = Color(249, 141, 0).rgb)
            "Red" -> updateAccent(value = Color(194, 0, 6).rgb)
            "Green" -> updateAccent(value = Color(0, 175, 10).rgb)
            "Yellow" -> updateAccent(value = Color(187, 170, 0).rgb)
            "Pink" -> updateAccent(value = Color(213, 141, 203).rgb)
            "Purple" -> updateAccent(value = Color(129, 0, 249).rgb)
            "Blue" -> updateAccent(value = Color(0, 87, 249).rgb)
        }

        if (Variables.guiColorIsRgb) {
            val rgbColor = RenderingEngine.Misc.getRainbowColor()
            Variables.guiColor = Color(rgbColor[0], rgbColor[1], rgbColor[2]).rgb
        }

        PresenceHandler.setDynamically()

        attemptSave()
    }

    private fun updateAccent(value: Int = 0, rgb: Boolean = false) {
        Variables.guiColor = value
        Variables.guiColorIsRgb = rgb
    }

    @EventListen
    fun onRenderTitleScreen(event: RenderTitleScreenEvent) {
        branding.render(event.context, event.mouseX, event.mouseY, event.delta)

        RenderingEngine.Text.draw(event.context, Formatting.BOLD.toString() + "Onyx | Cracked by CatGirlLana", 8, 55, Variables.guiColor)
        RenderingEngine.Text.draw(event.context, "by Integr", 8, 65, Variables.guiColor)
        RenderingEngine.Text.draw(event.context, "v$VERSION :)", 8, 75, Variables.guiColor)
    }

    @EventListen
    fun onKey(event: KeyEvent) {
        if (MC.player != null && MC.currentScreen == null && event.action == GLFW.GLFW_PRESS)  {
            for (m in ModuleManager.modules) {
                if (m.settings.getById<KeyBindSetting>("bind")!!.getSetBind() == event.key) {
                    m.toggle()
                    MC.soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F))
                }

                m.onKeyEvent(event)
            }
        }
    }

    override fun onInitializeClient() {

    }

    @Throws(IOException::class)
    fun setIcon(path: String) {
        RenderSystem.assertInInitPhase()
        when (val i = GLFW.glfwGetPlatform()) {
            393217, 393220 -> {
                val list = listOf(InputSupplier { this::class.java.getClassLoader().getResourceAsStream(path)})
                val list2: MutableList<ByteBuffer?> = ArrayList(list.size)

                try {
                    val memoryStack = MemoryStack.stackPush()

                    try {
                        val buffer = GLFWImage.malloc(list.size, memoryStack)

                        var j = 0
                        while (j < list.size) {
                            val nativeImage = NativeImage.read((list[j] as InputSupplier<*>).get() as InputStream)

                            try {
                                val byteBuffer = MemoryUtil.memAlloc(nativeImage!!.width * nativeImage.height * 4)
                                list2.add(byteBuffer)
                                byteBuffer.asIntBuffer().put(nativeImage.copyPixelsRgba())
                                buffer.position(j)
                                buffer.width(nativeImage.width)
                                buffer.height(nativeImage.height)
                                buffer.pixels(byteBuffer)
                            } catch (var20: Throwable) {
                                if (nativeImage != null) {
                                    try {
                                        nativeImage.close()
                                    } catch (var19: Throwable) {
                                        var20.addSuppressed(var19)
                                    }
                                }

                                throw var20
                            }

                            nativeImage.close()
                            ++j
                        }

                        GLFW.glfwSetWindowIcon(MC.window.handle, buffer.position(0))
                    } catch (var21: Throwable) {
                        try {
                            memoryStack.close()
                        } catch (var18: Throwable) {
                            var21.addSuppressed(var18)
                        }

                        throw var21
                    }
                    memoryStack.close()

                } finally {
                    list2.forEach(Consumer { ptr: ByteBuffer? ->
                        MemoryUtil.memFree(
                            ptr
                        )
                    })
                }
            }
            393219, 393221 -> {}
            else -> LOGGER.warn("Not setting icon for unrecognized platform: {}", i)
        }
    }
}