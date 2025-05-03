package org.http4k.mcp

import dev.forkhandles.bunting.Bunting
import dev.forkhandles.bunting.InMemoryConfig
import dev.forkhandles.bunting.int
import org.http4k.core.Credentials
import org.http4k.core.Uri
import org.http4k.mcp.TransportMode.`http-stream`
import org.http4k.mcp.TransportMode.valueOf
import java.time.Duration
import java.time.Duration.ZERO

class McpOptions(args: Array<String>) :
    Bunting(
        args,
        "MCP proxy to pipe StdIO to a remote server. Command line options can be found at: https://github.com/http4k/mcp-desktop",
        "http4k-mcp-desktop",
        config = InMemoryConfig()
    ) {

    val transport by option("MCP transport. Choose between ${TransportMode.entries.joinToString(", ")}").map {
        valueOf(it)
    }
        .defaultsTo(`http-stream`)

    val url by option("Base URL of the MCP server to connect to: eg. http://localhost:3001/mcp")
        .map(Uri::of).required()

    val apiKey by option("API key to use to communicate with the server").secret()
    val apiKeyHeader by option("API key header name to use to communicate with the server").defaultsTo("X-Api-key")
    val bearerToken by option("Bearer token to use to communicate with the server").secret()
    val basicAuth by option("Basic Auth credentials to use to communicate with the server in the format: <user>:<password>")
        .map { Credentials(it.substringBefore(":"), it.substringAfter(":")) }
        .secret()

    val oauthScopes by option("OAuth scopes to request").map { it.split(",") }.defaultsTo(listOf())
    val oauthClientCredentials by option("OAuth client credentials to use to communicate with the server in the format: <client>:<secret>")
        .map { Credentials(it.substringBefore(":"), it.substringAfter(":")) }
        .secret()

    val version by switch("Get the version information for the app")

    val reconnectDelay by option("Reconnect delay (in seconds) in case of disconnection. Defaults to 0").int()
        .map { Duration.ofSeconds(it.toLong()) }
        .defaultsTo(ZERO)
}

enum class TransportMode {
    jsonrpc, sse, websocket, `http-stream`, `http-nonstream`
}
