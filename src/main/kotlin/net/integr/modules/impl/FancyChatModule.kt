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

@file:Suppress("SpellCheckingInspection", "unused")

package net.integr.modules.impl

import net.integr.event.SendChatMessageEvent
import net.integr.eventsystem.EventListen
import net.integr.eventsystem.Priority
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module
import net.integr.modules.management.settings.impl.CyclerSetting
import kotlin.random.Random

class FancyChatModule : Module("Fancy Chat", "Modify your chat font", "fancyChat", mutableListOf(Filter.Util)) {
    init {
        settings.add(CyclerSetting("Mode: ", "The font/charset/mode to use", "mode", mutableListOf("Small", "Thin", "Thick", "Script1", "Script2", "UwU", "Annoy", "CAPS")))
    }

    private val alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

    private val small = "ᵃ ᵇ ᶜ ᵈ ᵉ ᶠ ᵍ ʰ ⁱ ʲ ᵏ ˡ ᵐ ⁿ ᵒ ᵖ q ʳ ˢ ᵗ ᵘ ᵛ ʷ ˣ ʸ ᶻ ᴬ ᴮ ᶜ ᴰ ᴱ ᶠ ᴳ ᴴ ᴵ ᴶ ᴷ ᴸ ᴹ ᴺ ᴼ ᴾ Q ᴿ ˢ ᵀ ᵁ ⱽ ᵂ ˣ ʸ ᶻ"
    private val thin = "ａ ｂ ｃ ｄ ｅ ｆ ｇ ｈ ｉ ｊ ｋ ｌ ｍ ｎ ｏ ｐ ｑ ｒ ｓ ｔ ｕ ｖ ｗ ｘ ｙ ｚ Ａ Ｂ Ｃ Ｄ Ｅ Ｆ Ｇ Ｈ Ｉ Ｊ Ｋ Ｌ Ｍ Ｎ Ｏ Ｐ Ｑ Ｒ Ｓ Ｔ Ｕ Ｖ Ｗ Ｘ Ｙ Ｚ"
    private val thick = "𝕒 𝕓 𝕔 𝕕 𝕖 𝕗 𝕘 𝕙 𝕚 𝕛 𝕜 𝕝 𝕞 𝕟 𝕠 𝕡 𝕢 𝕣 𝕤 𝕥 𝕦 𝕧 𝕨 𝕩 𝕪 𝕫 𝔸 𝔹 ℂ 𝔻 𝔼 𝔽 𝔾 ℍ 𝕀 𝕁 𝕂 𝕃 𝕄 ℕ 𝕆 ℙ ℚ ℝ 𝕊 𝕋 𝕌 𝕍 𝕎 𝕏 𝕐 ℤ"
    private val script1 = "𝖆 𝖇 𝖈 𝖉 𝖊 𝖋 𝖌 𝖍 𝖎 𝖏 𝖐 𝖑 𝖒 𝖓 𝖔 𝖕 𝖖 𝖗 𝖘 𝖙 𝖚 𝖛 𝖜 𝖝 𝖞 𝖟 𝕬 𝕭 𝕮 𝕯 𝕰 𝕱 𝕲 𝕳 𝕴 𝕵 𝕶 𝕷 𝕸 𝕹 𝕺 𝕻 𝕼 𝕽 𝕾 𝕿 𝖀 𝖁 𝖂 𝖃 𝖄 𝖅"
    private val script2 = "𝒶 𝒷 𝒸 𝒹 𝑒 𝒻 𝑔 𝒽 𝒾 𝒿 𝓀 𝓁 𝓂 𝓃 𝑜 𝓅 𝓆 𝓇 𝓈 𝓉 𝓊 𝓋 𝓌 𝓍 𝓎 𝓏 𝒜 𝐵 𝒞 𝒟 𝐸 𝐹 𝒢 𝐻 𝐼 𝒥 𝒦 𝐿 𝑀 𝒩 𝒪 𝒫 𝒬 𝑅 𝒮 𝒯 𝒰 𝒱 𝒲 𝒳 𝒴 𝒵"

    private val vowels = arrayOf("a", "e", "i", "o", "u")

    // Credits @PlayingMao61105
    private val uwuVariations = arrayOf("UwU", "nya~", ":3", "OwO", "*blushes*", "*stuttering*", "rawrrr", "( ˶ˆ꒳ˆ˵ )", "*moans*", "*gets a boner*", "Breed me~", "Daddy~", "*cums cutely*")

    @EventListen(Priority.LAST)
    fun onChatMessage(event: SendChatMessageEvent) {
        val mode = settings.getById<CyclerSetting>("mode")!!.getElement()

        val oldMsg = if (event.callback != null) event.callback.toString() else event.message

        when (mode) {
            "Small" -> event.callback = replaceChars(oldMsg, small.split(' '))
            "Thin" -> event.callback = replaceChars(oldMsg, thin.split(' '))
            "Thick" -> event.callback = replaceChars(oldMsg, thick.split(' '))
            "Script1" -> event.callback = replaceChars(oldMsg, script1.split(' '))
            "Script2" -> event.callback = replaceChars(oldMsg, script2.split(' '))
            "UwU" -> event.callback = uwuIfy(oldMsg)
            "Annoy" -> event.callback = annoyIfy(oldMsg)
            "CAPS" -> event.callback = oldMsg.uppercase()
        }
    }

    private fun annoyIfy(str: String): String {
        var output = ""

        for (c in str) {
            output += if (Random.nextBoolean()) {if (c.isUpperCase()) c.lowercase() else c.uppercase()} else c
        }

        return output
    }

    private fun uwuIfy(str: String): String {
        var output = vowelIfy(str)
        output = stutterIfy(output)
        output += "..."

        for (i in (0..Random.nextInt(0, 3))) {
            output += " ${uwuVariations.random()}"
        }

        return output
    }

    private fun stutterIfy(str: String): String {
        var output = ""

        for (i in str.indices) {
            if (Random.nextInt(0, 2) == 0 && i-1 >= 0 && str[i-1] == ' ') {
                output += "${str[i]}- "
            }

            output += str[i]
        }

        return output
    }

    private fun vowelIfy(str: String): String {
        var output = ""

        for (c in str) {
            if (Random.nextInt(0, 5) == 0 && c.isLowerCase() && vowels.contains(c.lowercase())) {
                output += "w"
            }

            output += c
        }

        return output
    }

    private fun replaceChars(str: String, font: List<String>): String {
        var copy = str
        for (c in alphabet.indices) {
            copy = copy.replace(alphabet[c].toString(), font[c])
        }

        return copy
    }
}