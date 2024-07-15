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

package net.integr.modules.impl

import net.integr.Onyx
import net.integr.Variables
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.ModuleManager
import net.integr.modules.management.UiModule
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.modules.management.settings.impl.KeyBindSetting
import net.integr.rendering.RenderingEngine
import net.minecraft.client.gui.DrawContext

class HacklistModule : UiModule("Hacklist", "Shows your active modules", "hackList", 20, 100, mutableListOf(Filter.Render)) {
    init {
        settings.add(BooleanSetting("Background", "Renders a background to the hacklist", "background"))
    }

    override fun render(context: DrawContext, originX: Int, originY: Int, delta: Float) {
        val modules = ModuleManager.getEnabledModules().sortedWith(ModuleComparer())

        val background = settings.getById<BooleanSetting>("background")!!.isEnabled()
        var counter = 0

        if (originX-20/2 >= context.scaledWindowWidth/2) {
            // Right
            if (originY - 100 / 2 >= context.scaledWindowHeight / 2) {
                // Bottom
                for (module in modules) {
                    val mn = module.displayName + (if (module.settings.getById<KeyBindSetting>("bind")!!.isBound()) " [" + module.settings.getById<KeyBindSetting>("bind")!!.getKeyChar()!!.uppercase() + "]" else "")
                    if (background) {
                        RenderingEngine.TwoDimensional.fill(originX+5+100f-(Onyx.MC.textRenderer.getWidth(mn))-3, originY+5+5-counter.toFloat()-1, originX+5+100f, originY+5+5-counter+15f, Variables.guiColor, context.matrices.peek().positionMatrix)
                        RenderingEngine.TwoDimensional.fill(originX+5+100f-(Onyx.MC.textRenderer.getWidth(mn))-2, originY+5+5-counter.toFloat(), originX+5+100f, originY+5+5-counter+15f, Variables.guiBack, context.matrices.peek().positionMatrix)
                        RenderingEngine.Text.draw(context, mn, originX+5+100-(Onyx.MC.textRenderer.getWidth(mn)), originY+5+5-counter+4, Variables.guiColor)

                        counter += 15
                    } else {
                        RenderingEngine.Text.draw(context, mn, originX+5+100-(Onyx.MC.textRenderer.getWidth(mn)), originY+5+5-counter+5, Variables.guiColor)
                        counter += 10
                    }
                }
            } else {
                // Top
                for (module in modules) {
                    val mn = module.displayName + (if (module.settings.getById<KeyBindSetting>("bind")!!.isBound()) " [" + module.settings.getById<KeyBindSetting>("bind")!!.getKeyChar()!!.uppercase() + "]" else "")
                    if (background) {
                        RenderingEngine.TwoDimensional.fill(originX+5+100f-(Onyx.MC.textRenderer.getWidth(mn))-3, originY-5+counter.toFloat(), originX+5+100f, originY-5+counter+16f, Variables.guiColor, context.matrices.peek().positionMatrix)
                        RenderingEngine.TwoDimensional.fill(originX+5+100f-(Onyx.MC.textRenderer.getWidth(mn))-2, originY-5+counter.toFloat(), originX+5+100f, originY-5+counter+15f, Variables.guiBack, context.matrices.peek().positionMatrix)
                        RenderingEngine.Text.draw(context, mn, originX+5+100-(Onyx.MC.textRenderer.getWidth(mn)), originY-5+counter+4, Variables.guiColor)

                        counter += 15
                    } else {
                        RenderingEngine.Text.draw(context, mn, originX+5+100-(Onyx.MC.textRenderer.getWidth(mn)), originY-5+counter, Variables.guiColor)
                        counter += 10
                    }
                }
            }
        } else {
            // Left
            if (originY - 100 / 2 >= context.scaledWindowHeight / 2) {
                // Bottom
                for (module in modules) {
                    val mn = module.displayName + (if (module.settings.getById<KeyBindSetting>("bind")!!.isBound()) " [" + module.settings.getById<KeyBindSetting>("bind")!!.getKeyChar()!!.uppercase() + "]" else "")
                    if (background) {
                        RenderingEngine.TwoDimensional.fill(originX-5f-2, originY+5+5-counter.toFloat()-1, originX-5f+Onyx.MC.textRenderer.getWidth(mn)+2+1, originY+5+5-counter+15f, Variables.guiColor, context.matrices.peek().positionMatrix)
                        RenderingEngine.TwoDimensional.fill(originX-5f-2, originY+5+5-counter.toFloat(), originX-5f+Onyx.MC.textRenderer.getWidth(mn)+2, originY+5+5-counter+15f, Variables.guiBack, context.matrices.peek().positionMatrix)
                        RenderingEngine.Text.draw(context, mn, originX-5, originY+5+5-counter+4, Variables.guiColor)

                        counter += 15
                    } else {
                        RenderingEngine.Text.draw(context, mn, originX-5, originY+5+5-counter+5, Variables.guiColor)
                        counter += 10
                    }
                }
            } else {
                // Top
                for (module in modules) {
                    val mn = module.displayName + (if (module.settings.getById<KeyBindSetting>("bind")!!.isBound()) " [" + module.settings.getById<KeyBindSetting>("bind")!!.getKeyChar()!!.uppercase() + "]" else "")
                    if (background) {
                        RenderingEngine.TwoDimensional.fill(originX-5f-2, originY-5+counter.toFloat(), originX-5f+Onyx.MC.textRenderer.getWidth(mn)+2+1, originY-5+counter+16f, Variables.guiColor, context.matrices.peek().positionMatrix)
                        RenderingEngine.TwoDimensional.fill(originX-5f-2, originY-5+counter.toFloat(), originX-5f+Onyx.MC.textRenderer.getWidth(mn)+2, originY-5+counter+15f, Variables.guiBack, context.matrices.peek().positionMatrix)
                        RenderingEngine.Text.draw(context, mn, originX-5, originY-5+counter+4, Variables.guiColor)

                        counter += 15
                    } else {
                        RenderingEngine.Text.draw(context, mn, originX-5, originY-5+counter, Variables.guiColor)
                        counter += 10
                    }
                }
            }
        }


    }

    class ModuleComparer : Comparator<Module> {
        override fun compare(o1: Module, o2: Module): Int {
            val l1 = Onyx.MC.textRenderer.getWidth(o1.displayName + (if (o1.settings.getById<KeyBindSetting>("bind")!!.isBound()) " [" + o1.settings.getById<KeyBindSetting>("bind")!!.getKeyChar()!!.uppercase() + "]" else ""))
            val l2 = Onyx.MC.textRenderer.getWidth(o2.displayName + (if (o2.settings.getById<KeyBindSetting>("bind")!!.isBound()) " [" + o2.settings.getById<KeyBindSetting>("bind")!!.getKeyChar()!!.uppercase() + "]" else ""))

            if (l1 < l2) return 1
            if (l1 > l2) return -1

            return 0
        }

    }
}