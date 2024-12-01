package com.github.byshy.flutterassistant.inspections

import com.github.byshy.flutterassistant.toolWindow.BatchAddBlocHandlerFix
import com.github.byshy.flutterassistant.utils.FileUtils
import com.github.byshy.flutterassistant.utils.UnusedBlocEventUtils
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartFile

class UnusedBatchBlocEventInspection : LocalInspectionTool() {

    override fun getDisplayName(): String {
        return "Detect unused BLoC event"
    }

    override fun runForWholeFile(): Boolean {
        return true
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor> {
        val problemsHolder = ProblemsHolder(manager, file, isOnTheFly)

        file.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                analyzeAndRegisterProblems(element, problemsHolder)
            }
        })

        return problemsHolder.resultsArray
    }

    private fun analyzeAndRegisterProblems(
        element: PsiElement,
        holder: ProblemsHolder
    ) {
        if (element !is DartFile) return

        if (!FileUtils.isEventFile(element.name, element.text)) return

        val dartClasses: List<DartClass> = PsiTreeUtil.findChildrenOfType(element, DartClass::class.java).toList()

        val dartHighlightedClasses: MutableList<DartClass> = mutableListOf()

        for (dartClass in dartClasses) {
            val isBlocEventSubclass = UnusedBlocEventUtils.isBlocEventSubclass(dartClass, element)
            val isUsedBlocEvent = !UnusedBlocEventUtils.isUsedBlocEvent(dartClass)

            if (isBlocEventSubclass && isUsedBlocEvent) {
                dartHighlightedClasses.add(dartClass)
            }
        }

        for (dartClass in dartHighlightedClasses) {
            holder.registerProblem(
                dartClass.nameIdentifier ?: element,
                "Unused BLoC event detected",
                BatchAddBlocHandlerFix(dartHighlightedClasses),
            )
        }
    }
}
