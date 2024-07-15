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

package net.integr.event;

import net.integr.eventsystem.Event;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;

public class PlaceBlockEvent extends Event {
    public ItemPlacementContext context;
    public BlockState state;

    public PlaceBlockEvent(ItemPlacementContext context, BlockState state) {
        this.context = context;
        this.state = state;
    }
}
