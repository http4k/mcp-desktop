package env

import org.http4k.mcp.Http4kMcpDesktop
import org.http4k.mcp.ToolRequest
import org.http4k.mcp.ToolResponse.Ok
import org.http4k.mcp.model.Tool
import org.http4k.mcp.protocol.ServerMetaData
import org.http4k.routing.bind
import org.http4k.routing.mcpJsonRpc
import org.http4k.server.Helidon
import org.http4k.server.asServer
import java.time.Instant

fun main() {

    mcpJsonRpc(
        ServerMetaData("entity", "0.1.0"),
        Tool("time", "Get the current time") bind { r: ToolRequest ->
            Ok(Instant.now().toString())
        }
    )
        .asServer(Helidon(3001))
        .start()

    Http4kMcpDesktop.main("--transport", "jsonrpc", "--url", "http://localhost:3001/mcp")
}
