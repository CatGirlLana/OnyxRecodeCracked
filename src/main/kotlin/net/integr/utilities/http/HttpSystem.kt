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

package net.integr.utilities.http

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sun.net.httpserver.Authenticator.Failure
import java.awt.Window
import java.io.IOException
import java.io.InputStream
import java.net.Proxy
import java.net.URI
import java.net.URISyntaxException
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.util.*
import java.util.stream.Stream
import javax.annotation.Nullable


object HttpSystem {
    const val SUCCESS: Int = 200
    const val BAD_REQUEST: Int = 400
    const val UNAUTHORIZED: Int = 401
    const val FORBIDDEN: Int = 403
    const val NOT_FOUND: Int = 404

    private val CLIENT: HttpClient = HttpClient.newHttpClient()

    private val GSON: Gson = GsonBuilder().create()

    fun get(url: String): Request {
        return Request(Method.GET, url)
    }

    fun post(url: String): Request {
        return Request(Method.POST, url)
    }

    enum class Method {
        GET,
        POST
    }

    class Request(method: Method, url: String) {
        private var builder: HttpRequest.Builder? = null
        private var method: Method? = null

        init {
            try {
                this.builder = HttpRequest.newBuilder().uri(URI(url)).header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36"
                )
                this.method = method
            } catch (e: URISyntaxException) {
                throw IllegalArgumentException(e)
            }
        }

        fun header(name: String?, value: String?): Request {
            builder!!.header(name, value)

            return this
        }

        fun bearer(token: String): Request {
            builder!!.header("Authorization", "Bearer $token")

            return this
        }

        fun bodyString(string: String?): Request {
            builder!!.header("Content-Type", "text/plain")
            builder!!.method(method!!.name, HttpRequest.BodyPublishers.ofString(string))
            method = null

            return this
        }

        fun bodyForm(string: String?): Request {
            builder!!.header("Content-Type", "application/x-www-form-urlencoded")
            builder!!.method(method!!.name, HttpRequest.BodyPublishers.ofString(string))
            method = null

            return this
        }

        fun bodyJson(string: String?): Request {
            builder!!.header("Content-Type", "application/json")
            builder!!.method(method!!.name, HttpRequest.BodyPublishers.ofString(string))
            method = null

            return this
        }

        fun bodyJson(`object`: Any?): Request {
            builder!!.header("Content-Type", "application/json")
            builder!!.method(method!!.name, HttpRequest.BodyPublishers.ofString(GSON.toJson(`object`)))
            method = null

            return this
        }

        private fun <T> sendResponse(accept: String, responseBodyHandler: HttpResponse.BodyHandler<T>): HttpResponse<T>? {
            builder!!.header("Accept", accept)
            if (method != null) builder!!.method(method!!.name, HttpRequest.BodyPublishers.noBody())

            val request: HttpRequest = builder!!.build()

            try {
                return CLIENT.send(request, responseBodyHandler)
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            } catch (e: InterruptedException) {
                e.printStackTrace()
                return null
            }
        }

        @Nullable
        private fun <T> send(accept: String, responseBodyHandler: HttpResponse.BodyHandler<T>): T? {
            val res: HttpResponse<T> = sendResponse(accept, responseBodyHandler) ?: return null
            return if (res.statusCode() == 200) res.body() else null
        }

        fun send() {
            send("*/*", BodyHandlers.discarding())
        }

        fun sendResponse(): HttpResponse<Void> {
            return sendResponse("*/*", BodyHandlers.discarding())!!
        }

        @Nullable
        fun sendInputStream(): InputStream {
            return send("*/*", BodyHandlers.ofInputStream())!!
        }

        fun sendInputStreamResponse(): HttpResponse<InputStream> {
            return sendResponse("*/*", BodyHandlers.ofInputStream())!!
        }

        @Nullable
        fun sendString(): String? {
            return send("*/*", BodyHandlers.ofString())
        }

        fun sendStringResponse(): HttpResponse<String> {
            return sendResponse("*/*", BodyHandlers.ofString())!!
        }

        @Nullable
        fun sendLines(): Stream<String> {
            return send("*/*", BodyHandlers.ofLines())!!
        }

        fun sendLinesResponse(): HttpResponse<Stream<String>> {
            return sendResponse("*/*", HttpResponse.BodyHandlers.ofLines())!!
        }
    }
}