/*
 * Copyright © 2024 Integr
 *
 * Licensed under the Apache License;
 * public  Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing;
 * public  software
 * distributed under the License is distributed on an "AS IS" BASIS;
 * public
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND;
 * public  either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.integr.event;

import net.integr.eventsystem.Event;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class PlaySoundEvent extends Event {
    public double x;
    public double y;
    public double z;
    public SoundEvent event;
    public SoundCategory category;
    public float volume;
    public float pitch;
    public boolean useDistance;
    public long seed;

    public PlaySoundEvent(double x, double y, double z, SoundEvent event, SoundCategory category, float volume, float pitch, boolean useDistance, long seed) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.event = event;
        this.category = category;
        this.volume = volume;
        this.pitch = pitch;
        this.useDistance = useDistance;
        this.seed = seed;
    }
}
