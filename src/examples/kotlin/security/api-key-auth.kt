package security

import org.http4k.filter.debug
import org.http4k.lens.Header
import org.http4k.mcp.Http4kMcpDesktop
import org.http4k.mcp.ToolResponse
import org.http4k.mcp.model.Content
import org.http4k.mcp.model.McpEntity
import org.http4k.mcp.model.Tool
import org.http4k.mcp.protocol.ServerMetaData
import org.http4k.mcp.protocol.Version
import org.http4k.mcp.server.security.ApiKeyMcpSecurity
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

    secureMcpServer.debug(System.err, true).asServer(JettyLoom(3001)).start()

    Http4kMcpDesktop.main(
        "--url", "http://localhost:3001/sse",
        "--apiKey", "foobar"
    )
}
