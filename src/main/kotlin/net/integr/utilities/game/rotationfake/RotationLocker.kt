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

package net.integr.utilities.game.rotationfake

class RotationLocker {
    companion object {
        private var rotationLocked: Boolean = false
        var fakeYaw: Float = 0f
        var fakePitch: Float = 0f

        fun lock() {
            rotationLocked = true
        }

        fun unLock() {
            rotationLocked = false
        }

        fun isLocked(): Boolean = rotationLocked

    }
}