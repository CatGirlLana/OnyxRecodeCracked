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

package net.integr.rendering.uisystem

import net.integr.rendering.uisystem.base.HelixUiElement
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

class UiLayout {
    private val l: MutableMap<HelixUiElement, Pair<AtomicBoolean, AtomicBoolean>> = mutableMapOf()

    fun add(obj: HelixUiElement): HelixUiElement {
        l[obj] = AtomicBoolean(false) to AtomicBoolean(false)

        return obj
    }

    fun clear() {
        l.clear()
    }

    fun isLocked(obj: HelixUiElement): Boolean {
        return l[obj]!!.first.get()
    }

    fun isMarked(obj: HelixUiElement): Boolean {
        return l[obj]!!.second.get()
    }

    fun onClick(mouseX: Double, mouseY: Double, button: Int) {
        for ((e, lo) in l) {
            if (lo.first.get()) continue
            e.onClick(mouseX, mouseY, button)
        }
    }

    fun onRelease(mouseX: Double, mouseY: Double, button: Int) {
        for ((e, lo) in l) {
            if (lo.first.get()) continue
            e.onRelease(mouseX, mouseY, button)
        }
    }

    fun onKey(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        for ((e, lo) in l) {
            if (lo.first.get()) continue
            if (e.onKey(keyCode, scanCode, modifiers)) return true
        }

        return false
    }

    fun removeAll(klass: KClass<*>) {
        for (e in l.keys) {
            if (e::class == klass) l.remove(e)
        }
    }

    fun lock(obj: HelixUiElement) {
        l[obj]!!.first.set(true)
    }

    fun unLock(obj: HelixUiElement) {
        l[obj]!!.first.set(false)
    }

    fun mark(obj: HelixUiElement) {
        l[obj]!!.second.set(true)
    }

    fun unMark(obj: HelixUiElement) {
        l[obj]!!.second.set(false)
    }

    fun lockAll(klass: KClass<*>) {
        for (e in l.keys) {
            if (l::class == klass) lock(e)
        }
    }

    fun unLockAll(klass: KClass<*>) {
        for (e in l.keys) {
            if (l::class == klass) unLock(e)
        }
    }
}