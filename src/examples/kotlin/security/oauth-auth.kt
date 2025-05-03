package security

import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.filter.debug
import org.http4k.mcp.Http4kMcpDesktop
import org.http4k.mcp.model.McpEntity
import org.http4k.mcp.protocol.ServerMetaData
import org.http4k.mcp.protocol.Version
import org.http4k.mcp.server.security.OAuthMcpSecurity
import org.http4k.routing.bind
import org.http4k.routing.mcpHttpStreaming
import org.http4k.routing.routes
import org.http4k.security.ResponseType.Code
import org.http4k.security.oauth.metadata.AuthMethod.client_secret_basic
import org.http4k.security.oauth.metadata.ServerMetadata
import org.http4k.security.oauth.server.AuthorizationServerWellKnown
import org.http4k.server.JettyLoom
import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun main() {

    // This is a mock OAuth2 server that returns a token for any request to /token
    val oauthServer = routes(
        "/token" bind { request ->
            Response(OK).body("token")
        },
        AuthorizationServerWellKnown(
            ServerMetadata(
                "http://localhost",
                Uri.of("/auth"),
                Uri.of("/token"),
                listOf(client_secret_basic),
                listOf("RS256"),
                listOf(Code)
            )
        )
    ).asServer(SunHttp(8080)).start()

    val secureMcpServer = mcpHttpStreaming(
        ServerMetaData(McpEntity.of("foo"), Version.of("bar")),
        OAuthMcpSecurity(Uri.of("https://localhost:${oauthServer.port()}")) {
            it == "token"
        })

    secureMcpServer.debug(System.err, true).asServer(JettyLoom(3001)).start()
    Http4kMcpDesktop.main(
        "--url", "http://localhost:3001/mcp",
        "--oauthClientCredentials", "foo:bar"
    )
}
