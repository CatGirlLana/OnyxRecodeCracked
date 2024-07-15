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

import net.integr.event.SendChatMessageEvent
import net.integr.event.SendCommandEvent
import net.integr.eventsystem.EventListen
import net.integr.eventsystem.Priority
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.BooleanSetting
import net.integr.utilities.LogUtils
import net.integr.utilities.Evaluator
import java.awt.Color


class ChatFormatModule : Module("Chat Format", "Chat formatter (Details on the Modrinth page)", "chatFormat", listOf(Filter.Util)){
    init {
        settings
            .add(BooleanSetting("Gradients", "Enable the gradient generator formatting option", "gradients"))
            .add(BooleanSetting("Math", "Enable the math solver formatting option", "math"))
    }

    @EventListen(Priority.FIRST)
    fun onChatMessage(event: SendChatMessageEvent) {
        val m = replaceInText(event.message)
        if (m.length < 269 && m.isNotEmpty()) event.callback = m else LogUtils.sendChatLog("Message to Long/Short!")
    }

    @EventListen(Priority.FIRST)
    fun onCommand(event: SendCommandEvent) {
        val m = replaceInText(event.command)
        if (m.length < 269 && m.isNotEmpty()) event.callback = m else LogUtils.sendChatLog("Message to Long/Short!")
    }

    private fun replaceInText(messageIn: String): String {
        var message = messageIn

        if (settings.getById<BooleanSetting>("math")!!.isEnabled()) {
            Regex("(<solve>)[^<]*(<solve>)").findAll(messageIn).forEach { exp ->
                val text = exp.value.substring(exp.value.indexOf('>')+1..<exp.value.lastIndexOf('<'))

                var result = ""

                try {
                    result = Evaluator.evaluate(text)
                } catch (e: Exception) {
                    LogUtils.sendChatLog("Invalid Operation!")
                    e.printStackTrace()
                }

                message = message.replaceRange(exp.range.first, exp.range.last+1, result)
            }
        }

        if (settings.getById<BooleanSetting>("gradients")!!.isEnabled()) {
            Regex("(<.*:([lonm]*)#[0-9a-f]{6}>)[^<]*(<#[0-9a-f]{6}>)").findAll(messageIn).forEach { exp ->
                var modText = ""
                val mods = exp.value.substring(exp.value.indexOf(':')+1..<exp.value.indexOf('#'))
                val fChar = exp.value.substring(1..<exp.value.indexOf(':'))

                mods.forEach {
                    modText += "&$it"
                }

                val c1 = exp.value.substring(exp.value.indexOf('<')+1+mods.count()+fChar.count()+1..<exp.value.indexOf('>'))
                val c2 = exp.value.substring(exp.value.lastIndexOf('<')+1..<exp.value.lastIndexOf('>'))

                val text = exp.value.substring(exp.value.indexOf('>')+1..<exp.value.lastIndexOf('<'))

                val hText = grad(Color.decode(c1), Color.decode(c2), text, fChar, modText)
                message = message.replaceRange(exp.range.first, exp.range.last+1, hText)
            }
        }

        return message
    }

    private fun grad(color1: Color, color2: Color, word: String, fChar: String, modifiers: String): String {
        val r1 = color1.red
        val g1 = color1.green
        val b1 = color1.blue
        val r2 = color2.red
        val g2 = color2.green
        val b2 = color2.blue

        val unitVector = intArrayOf(r2 - r1, g2 - g1, b2 - b1)
        val r = unitVector[0].toDouble() / word.length
        val g = unitVector[1].toDouble() / word.length
        val b = unitVector[2].toDouble() / word.length

        val values = intArrayOf(r1, g1, b1)
        val output: MutableList<String> = ArrayList()
        for (i in word.indices) {
            if (word[i] == ' ') {
                output += " "

                values[0] = (values[0] + r).toInt()
                values[1] = (values[1] + g).toInt()
                values[2] = (values[2] + b).toInt()
            } else {
                output += "$fChar#" + componentToHex(values[0]) + componentToHex(values[1]) + componentToHex(values[2]) + modifiers + word[i]

                values[0] = (values[0] + r).toInt()
                values[1] = (values[1] + g).toInt()
                values[2] = (values[2] + b).toInt()
            }
        }

        return output.joinToString(separator = "")
    }

    private fun componentToHex(c: Int): String {
        val hex = Integer.toHexString(c)
        return if (hex.length == 1) "0$hex" else hex
    }
}