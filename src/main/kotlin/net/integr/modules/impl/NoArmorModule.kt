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
import net.integr.event.RenderArmorEvent
import net.integr.eventsystem.EventListen
import net.integr.modules.filters.Filter
import net.integr.modules.management.Module

class NoArmorModule : Module("No Armor", "Hides your Armor", "noArmor", listOf(Filter.Render)) {
    @EventListen
    fun onRenderArmor(event: RenderArmorEvent) {
        if (event.entity == Onyx.MC.player) event.cancel()
    }
}