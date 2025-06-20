package org.http4k.mcp.internal

import org.http4k.ai.mcp.client.DiscoveredMcpOAuth
import org.http4k.core.Filter
import org.http4k.core.NoOp
import org.http4k.filter.ClientFilters
import org.http4k.lens.Header
import org.http4k.mcp.McpOptions

fun McpClientSecurityFilter(options: McpOptions): Filter = with(options) {
    when {
        apiKey != null -> ClientFilters.ApiKeyAuth(Header.required(apiKeyHeader) of apiKey!!)
        bearerToken != null -> ClientFilters.BearerAuth(bearerToken!!)
        basicAuth != null -> ClientFilters.BasicAuth(basicAuth!!)
        oauthClientCredentials != null -> ClientFilters.DiscoveredMcpOAuth(oauthClientCredentials!!, oauthScopes)

        else -> Filter.NoOp
    }
}
