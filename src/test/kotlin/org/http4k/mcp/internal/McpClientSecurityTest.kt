package org.http4k.mcp.internal

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.http4k.base64Encode
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.Uri
import org.http4k.core.WwwAuthenticate
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.debug
import org.http4k.hamkrest.hasBody
import org.http4k.hamkrest.hasHeader
import org.http4k.hamkrest.hasStatus
import org.http4k.lens.Header
import org.http4k.mcp.McpOptions
import org.http4k.routing.bind
import org.http4k.routing.orElse
import org.http4k.routing.routes
import org.http4k.security.ResponseType.Code
import org.http4k.security.oauth.metadata.AuthMethod.client_secret_basic
import org.http4k.security.oauth.metadata.ResourceMetadata
import org.http4k.security.oauth.metadata.ServerMetadata
import org.http4k.security.oauth.server.AuthorizationServerWellKnown
import org.http4k.security.oauth.server.ResourceServerWellKnown
import org.junit.jupiter.api.Test

class McpClientSecurityTest {

    @Test
    fun `no security`() {
        assertSecurity { Response(OK) }
    }

    @Test
    fun `basic auth`() {
        assertSecurity("--basicAuth", "123:321") {
            assertThat(it.header("Authorization"), equalTo("Basic " + "123:321".base64Encode()))
            Response(OK)
        }
    }

    @Test
    fun `api key`() {
        assertSecurity("--apiKey", "12345") {
            assertThat(it.header("X-API-Key"), equalTo("12345"))
            Response(OK)
        }

        assertSecurity("--apiKey", "12345", "--apiKeyHeader", "foobar") {
            assertThat(it.header("foobar"), equalTo("12345"))
            Response(OK)
        }
    }

    @Test
    fun `bearer auth`() {
        assertSecurity("--bearerToken", "12345") {
            assertThat(it.header("Authorization"), equalTo("Bearer 12345"))
            Response(OK)
        }
    }

    @Test
    fun `oauth protected resource auth`() {
        assertSecurity(
            "--oauthClientCredentials", "client:secret", next = routes(
                AuthorizationServerWellKnown(
                    ServerMetadata(
                        "http://mcp",
                        Uri.of("/auth"),
                        Uri.of("/token"),
                        listOf(client_secret_basic),
                        listOf("RS256"),
                        listOf(Code)
                    )
                ),
                ResourceServerWellKnown(ResourceMetadata(Uri.of("http://mcp"), listOf(Uri.of("http://localhost")))),
                "/token" bind {
                    assertThat(it, hasBody("grant_type=client_credentials&client_id=client&client_secret=secret&resource=http%3A%2F%2Fmcp"))
                    Response(OK).body("12345")
                },
                orElse bind { req: Request ->
                    if (req.header("Authorization") == null) {
                        Response(UNAUTHORIZED).with(
                            Header.WWW_AUTHENTICATE of WwwAuthenticate(
                                "Bearer",
                                mapOf("resource_metadata" to "foobar"),
                            )
                        )
                    } else {
                        assertThat(req, hasHeader("Authorization", "Bearer 12345"))
                        Response(OK)
                    }
                }
            )
        )
    }

    private fun assertSecurity(vararg args: String, next: HttpHandler) {
        val filter = McpClientSecurityFilter(McpOptions(args.toList().toTypedArray()))

        assertThat(filter.then(next.debug())(Request(GET, Uri.of("http://mcp"))), hasStatus(OK))
    }
}
