package org.http4k.mcp.internal

import org.http4k.client.Http4kSseClient
import org.http4k.client.ReconnectionMode
import org.http4k.client.chunkedSseSequence
import org.http4k.core.ContentType.Companion.APPLICATION_JSON
import org.http4k.core.ContentType.Companion.TEXT_EVENT_STREAM
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Uri
import org.http4k.core.with
import org.http4k.lens.Header
import org.http4k.lens.accept
import org.http4k.lens.contentType
import org.http4k.sse.SseMessage
import java.io.Reader
import java.io.Writer
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

/**
 * Connect to the HTTP endpoint, constructing the request using the passed function
 */
fun pipeHttpStreaming(
    input: Reader,
    output: Writer,
    uri: Uri,
    http: HttpHandler,
    reconnectionMode: ReconnectionMode
) {
    val sessionIdLens = Header.optional("mcp-session-id")

    val sessionId = AtomicReference<String>()

    thread {
        input.buffered().lineSequence().forEach { next ->
            runCatching {
                val response = http(
                    Request(POST, uri)
                        .accept(TEXT_EVENT_STREAM)
                        .contentType(APPLICATION_JSON)
                        .with(sessionIdLens of sessionId.get())
                        .body(next)
                )

                sessionId.set(sessionIdLens(response))

                response.body.stream.chunkedSseSequence()
                    .filterIsInstance<SseMessage.Event>()
                    .filter { it.event == "message" }
                    .forEach {
                        http.startNotificationStream(uri, sessionId.get(), reconnectionMode, output)

                        with(output) {
                            write("${it.data}\n")
                            flush()
                        }
                    }
            }.onFailure { it.printStackTrace(System.err) }
        }
    }
}


private fun (HttpHandler).startNotificationStream(
    uri: Uri,
    sessionId: String,
    reconnectionMode: ReconnectionMode,
    output: Writer
) {
    thread(isDaemon = true) {
        Http4kSseClient(Request(GET, uri).header("mcp-session-id", sessionId), this, reconnectionMode)
            .received()
            .filterIsInstance<SseMessage.Event>()
            .filter { it.event == "message" }
            .forEach {
                with(output) {
                    write("${it.data}\n")
                    flush()
                }
            }
    }
}
