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

package net.integr.eventsystem

import net.integr.utilities.LogUtils
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

class EventSystem {
    companion object {
        private var receivers: MutableMap<KCallable<*>, Any> = mutableMapOf()

        fun register(klass: KClass<*>) {
            for (m in klass.members) {
                if (m.hasAnnotation<EventListen>()) {
                    receivers[m] = klass.createInstance()
                }
            }
        }

        fun register(klass: KClass<*>, instance: Any) {
            for (m in klass.members) {
                if (m.hasAnnotation<EventListen>()) {
                    receivers[m] = instance
                }
            }
        }

        fun register(instance: Any) {
            for (m in instance::class.members) {
                if (m.hasAnnotation<EventListen>()) {
                    receivers[m] = instance
                }
            }
        }

        fun unRegister(klass: KClass<*>) {
            for (m in klass.members) {
                if (m.hasAnnotation<EventListen>()) {
                    receivers.remove(m)
                }
            }
        }

        fun unRegister(instance: Any) {
            for (m in instance::class.members) {
                if (m.hasAnnotation<EventListen>()) {
                    receivers.remove(m)
                }
            }
        }

        fun post(event: Event): Any? {
            try {
                for (m in receivers.keys.sortedBy { it.findAnnotation<EventListen>()?.prio }) {
                    if (m.parameters.size > 2) throw IllegalArgumentException("Only one parameter is allowed at ${m.javaClass.declaringClass.kotlin.simpleName}.${m.name}()")

                    if (m.parameters[1].type.toString().substringAfterLast('.') == event::class.simpleName) {
                        try {
                            m.call(receivers[m], event)
                        } catch (ex: InvocationTargetException) {
                            LogUtils.sendLog("Fatal Error during event!")
                            ex.targetException.printStackTrace()
                        }
                        if (event.isCancelled()) break
                    }
                }

                return event.getCallback()
            } catch (_: Exception) {}

            return null
        }
    }
}