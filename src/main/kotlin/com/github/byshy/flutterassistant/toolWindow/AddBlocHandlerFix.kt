package com.github.byshy.flutterassistant.toolWindow

import com.jetbrains.lang.dart.psi.DartClass

class AddBlocHandlerFix(dartClass: DartClass) : AbstractBlocHandlerFix(dartClass) {
    override fun getFamilyName() = "Add BLoC synchronous handler for unused event"

    override fun generateHandlerCode(eventClassName: String): String {
        return """
            on<$eventClassName>(_on$eventClassName)
        """.trimIndent()
    }

    override fun generateHandlerMethod(eventClassName: String): String {
        return """
            void _on$eventClassName($eventClassName event, emit) {
                // TODO: Implement event handling logic
            }
        """.trimIndent()
    }
}
