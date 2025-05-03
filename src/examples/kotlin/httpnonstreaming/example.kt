package httpnonstreaming

import org.http4k.mcp.Http4kMcpDesktop
import org.http4k.mcp.ToolResponse
import org.http4k.mcp.model.Content
import org.http4k.mcp.model.McpEntity
import org.http4k.mcp.model.Tool
import org.http4k.mcp.protocol.ServerMetaData
import org.http4k.mcp.protocol.Version
import org.http4k.routing.bind
import org.http4k.routing.mcpHttpNonStreaming
import org.http4k.server.JettyLoom
import org.http4k.server.asServer
import java.time.Instant

fun main() {
    val mcpServer = mcpHttpNonStreaming(
        ServerMetaData(McpEntity.of("http4k mcp server"), Version.of("0.1.0")),
        Tool("time", "Get the current time") bind { ToolResponse.Ok(listOf(Content.Text(Instant.now().toString()))) }
    )

    mcpServer
        .asServer(JettyLoom(3001))
        .start()

    Http4kMcpDesktop.main("--transport", "http-nonstream", "--url", "http://localhost:3001/mcp", "--debug")

    println("""Now paste the MCP JSON-RPC requests into the console eg. {"jsonrpc":"2.0","method":"tools/call","params":{"name":"time","arguments":{},"_meta":{}}}""")
}
