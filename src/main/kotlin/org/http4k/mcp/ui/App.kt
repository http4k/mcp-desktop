package org.http4k.mcp.ui

import org.http4k.core.PolyHandler
import org.http4k.core.then
import org.http4k.filter.ServerFilters.CatchAll
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.poly
import org.http4k.routing.routes
import org.http4k.routing.static

fun App(): PolyHandler {
    val renderer = Http4kMcpDesktopTemplateRenderer()

    return poly(
        CatchAll().then(
            routes(
                static(Classpath("public")),
                Ui(renderer),
            )
        )
    )
}

