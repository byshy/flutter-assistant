package com.github.byshy.flutterassistant.inspections

import com.github.byshy.flutterassistant.toolWindow.AddAsyncBlocHandlerFix
import com.github.byshy.flutterassistant.toolWindow.AddBlocHandlerFix
import com.github.byshy.flutterassistant.utils.FileUtils
import com.github.byshy.flutterassistant.utils.UnusedBlocEventUtils
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartFile

class UnusedBlocEventInspection : LocalInspectionTool() {

    override fun getDisplayName(): String {
        return "Detect unused BLoC event"
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                analyzeAndRegisterProblems(element, holder)
            }
        }
    }

    private fun analyzeAndRegisterProblems(
        element: PsiElement,
        holder: ProblemsHolder
    ) {
        if (element !is DartClass) return

        val dartFile = element.containingFile as? DartFile ?: return
        if (!FileUtils.isEventFile(dartFile.name, dartFile.text)) return

        if (UnusedBlocEventUtils.isBlocEventSubclass(element, dartFile) && !UnusedBlocEventUtils.isUsedBlocEvent(element)) {
            holder.registerProblem(
                element.nameIdentifier ?: element,
                "Unused BLoC event detected",
                AddAsyncBlocHandlerFix(element),
                AddBlocHandlerFix(element),
            )
        }
    }
}
