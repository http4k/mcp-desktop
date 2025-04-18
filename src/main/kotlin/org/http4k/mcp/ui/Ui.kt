package org.http4k.mcp.ui

import org.http4k.core.Body
import org.http4k.core.ContentType.Companion.TEXT_HTML
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.datastarFragments
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.orElse
import org.http4k.routing.routes
import org.http4k.template.DatastarFragmentRenderer
import org.http4k.template.TemplateRenderer
import org.http4k.template.viewModel
import views.Index

fun Ui(renderer: TemplateRenderer): RoutingHttpHandler {

    val body = Body.viewModel(renderer, TEXT_HTML).toLens()

    val fragments = DatastarFragmentRenderer(renderer)

    return routes(
        "/foo" bind {
            Response(OK)
            Response(OK).datastarFragments(fragments(Index(mapOf("foo" to "bar"))))
        },
        orElse bind {
            Response(OK)
            Response(OK).with(body of Index(mapOf("foo" to "bar")))
        }
    )
}
