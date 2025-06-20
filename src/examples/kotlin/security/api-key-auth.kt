package security

import org.http4k.ai.mcp.ToolResponse
import org.http4k.ai.mcp.model.Content
import org.http4k.ai.mcp.model.McpEntity
import org.http4k.ai.mcp.model.Tool
import org.http4k.ai.mcp.protocol.ServerMetaData
import org.http4k.ai.mcp.protocol.Version
import org.http4k.ai.mcp.server.security.ApiKeyMcpSecurity
import org.http4k.filter.debugMcp
import org.http4k.lens.Header
import org.http4k.mcp.Http4kMcpDesktop
import org.http4k.routing.bind
import org.http4k.routing.mcpSse
import org.http4k.server.JettyLoom
import org.http4k.server.asServer
import java.time.Instant

fun main() {
    val secureMcpServer = mcpSse(
        ServerMetaData(McpEntity.of("foo"), Version.of("bar")),
        ApiKeyMcpSecurity(Header.required("X-API-key")) {
            it == "foobar"
        },
        Tool("time", "Get the current time") bind { ToolResponse.Ok(listOf(Content.Text(Instant.now().toString()))) }
    )

    secureMcpServer.debugMcp(System.err).asServer(JettyLoom(3001)).start()

    Http4kMcpDesktop.main(
        "--url", "http://localhost:3001/sse",
        "--apiKey", "foobar"
    )
}
