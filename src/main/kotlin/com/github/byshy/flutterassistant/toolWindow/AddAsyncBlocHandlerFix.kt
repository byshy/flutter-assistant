package com.github.byshy.flutterassistant.toolWindow

import com.jetbrains.lang.dart.psi.DartClass

class AddAsyncBlocHandlerFix(dartClass: DartClass) : AbstractBlocHandlerFix(dartClass) {
    override fun getFamilyName() = "Add BLoC async handler for unused event"

    override fun generateHandlerCode(eventClassName: String): String {
        return """
            on<$eventClassName>(_on$eventClassName)
        """.trimIndent()
    }

    override fun generateHandlerMethod(eventClassName: String): String {
        return """
            Future<void> _on$eventClassName($eventClassName event, emit) async {
                // TODO: Implement async event handling logic
            }
        """.trimIndent()
    }
}
