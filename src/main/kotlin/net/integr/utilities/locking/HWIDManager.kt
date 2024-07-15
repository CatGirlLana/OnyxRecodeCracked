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

package net.integr.utilities.locking

import com.google.common.hash.Hashing
import net.integr.utilities.http.HttpSystem
import oshi.SystemInfo
import oshi.software.os.OperatingSystem


class HWIDManager {
    companion object {
        fun getHwid(): String {
            return hashData(getData())
        }

        fun isAuthed(hwid: String): Boolean {
            val res = HttpSystem
                .get("https://ceptea.xyz/onyx/checkhwid/$hwid")
                .sendString()

            val authed = res.toBoolean()
            return authed
        }

        private fun getData(): String {
            val systemInfo = SystemInfo()
            val operatingSystem: OperatingSystem = systemInfo.operatingSystem
            val hardwareAbstractionLayer = systemInfo.hardware
            val centralProcessor = hardwareAbstractionLayer.processor
            val computerSystem = hardwareAbstractionLayer.computerSystem

            val vendor: String = operatingSystem.manufacturer
            val processorSerialNumber = computerSystem.serialNumber
            val processorIdentifier: String = centralProcessor.processorIdentifier.processorID
            val processors = centralProcessor.logicalProcessorCount

            val delimiter = "#"

            return vendor + delimiter + processorSerialNumber + delimiter + processorIdentifier + delimiter + processors
        }

        private fun hashData(data: String): String {
            return Hashing.sha512().hashString(data, Charsets.UTF_8).toString()
        }
    }
}