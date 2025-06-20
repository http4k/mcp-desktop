package org.http4k.mcp.internal

import org.http4k.client.JavaHttpClient
import org.http4k.core.BodyMode.Stream
import org.http4k.core.Filter
import org.http4k.core.then
import org.http4k.filter.ClientFilters.Cookies
import org.http4k.filter.ClientFilters.FollowRedirects
import org.http4k.filter.cookie.BasicCookieStorage
import java.time.Clock

fun McpDesktopHttpClient(clock: Clock, security: Filter) = FollowRedirects()
    .then(Cookies(clock, BasicCookieStorage()))
    .then(security)
    .then(JavaHttpClient(responseBodyMode = Stream))
