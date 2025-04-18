package views

import org.http4k.template.ViewModel

interface ViewModelWithMap : ViewModel, Map<String, Any>
