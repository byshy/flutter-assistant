package com.github.byshy.flutterassistant.toolWindow

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.jetbrains.lang.dart.psi.DartClass

class BatchAddBlocHandlerFix(
    private val allIssues: List<DartClass>
) : LocalQuickFix {

    override fun getFamilyName() = "Add BLoC handlers for all unused events"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        for (event in allIssues) {
            AddBlocHandlerFix(event).applyFix(project, descriptor)
        }
    }
}
