package org.http4k.mcp.internal

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.http4k.client.ReconnectionMode.Disconnect
import org.http4k.core.ContentType.Companion.APPLICATION_JSON
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.lens.contentType
import org.http4k.sse.SseMessage
import org.junit.jupiter.api.Test
import java.io.StringWriter
import java.util.concurrent.atomic.AtomicReference

class PipeHttpStreamingTrafficTest {

    @Test
    fun `pipes input and output to correct place`() {
        val inputMessages = listOf(
            "hello",
            "world",
        )
        val output = StringWriter()
        val sentToHttp = mutableListOf<String>()

        val expectedList = listOf(
            "data1",
            "data2"
        )
        val sessionId = AtomicReference<String>()

        val responses = expectedList.iterator()

        pipeHttpStreaming(
            inputMessages.joinToString("\n").reader(),
            output,
            Uri.of("http://host/mcp"),
            { req: Request ->
                when (req.method) {
                    GET -> {
                        sessionId.set(req.header("mcp-session-id"))
                        Response(OK)
                    }

                    else -> {
                        sentToHttp += req.bodyString()
                        Response(OK)
                            .contentType(APPLICATION_JSON)
                            .header("mcp-session-id", "foobar")
                            .body(SseMessage.Event("message", responses.next()).toMessage())
                    }
                }
            },
            Disconnect
        )

        Thread.sleep(1000)

        assertThat(sentToHttp.toList(), equalTo(inputMessages))

        assertThat(
            output.toString().trimEnd().split("\n"),
            equalTo(expectedList)
        )
        assertThat(sessionId.get(), equalTo("foobar"))
    }
}
