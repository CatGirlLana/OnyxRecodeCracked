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

package net.integr.modules.management

import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder

class ModuleLoader {
    companion object {
        fun load(): List<Class<*>> {
            val l: MutableList<Class<*>> = mutableListOf()

            val reflections = Reflections(
                ConfigurationBuilder()
                    .forPackage("net.integr.modules.impl")
                    .filterInputsBy(FilterBuilder().includePackage("net.integr.modules.impl"))
            )

            reflections.getSubTypesOf(Module::class.java).forEach {
                if (it != UiModule::class.java) l += it
            }

            return l
        }
    }
}