package org.http4k.mcp.ui

import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.error.LoaderException
import io.pebbletemplates.pebble.loader.ClasspathLoader
import org.http4k.template.TemplateRenderer
import org.http4k.template.ViewModel
import org.http4k.template.ViewNotFound
import views.ViewModelWithMap
import java.io.StringWriter

fun Http4kMcpDesktopTemplateRenderer(): TemplateRenderer {
    val loader = ClasspathLoader(ClassLoader.getSystemClassLoader())
    loader.prefix = null
    val engine = PebbleEngine.Builder().loader(loader).build()
    return object : TemplateRenderer {
        override fun invoke(viewModel: ViewModel): String = try {
            val writer = StringWriter()
            engine.getTemplate(viewModel.template() + ".peb").evaluate(writer, viewModel as ViewModelWithMap)
            writer.toString()
        } catch (e: LoaderException) {
            throw ViewNotFound(viewModel)
        }
    }
}
