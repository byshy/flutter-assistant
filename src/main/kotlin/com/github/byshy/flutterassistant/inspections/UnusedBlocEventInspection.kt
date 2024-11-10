package com.github.byshy.flutterassistant.inspections

import com.github.byshy.flutterassistant.toolWindow.AddBlocHandlerFix
import com.github.byshy.flutterassistant.utils.FileUtils
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartFile
import com.jetbrains.lang.dart.psi.DartReferenceExpression

class UnusedBlocEventInspection : LocalInspectionTool() {
    override fun getDisplayName(): String {
        return "Detect unused BLoC event"
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                val isADartClass = element is DartClass

                if (isADartClass) {
                    val dartFile = (element as DartClass).containingFile as? DartFile ?: return
                    val isEventFile = FileUtils.isEventFile(dartFile.name, dartFile.text)

                    if (isEventFile) {
                        val isNotUsed = !isUsedBlocEvent(element)

                        if (isNotUsed) {
                            /**
                             * We must pass the [element] manually to the [AddBlocHandlerFix] since taking it from the
                             * descriptor can't be cast into a [DartClass]
                            */
                            holder.registerProblem(
                                element.nameIdentifier ?: element,
                                "Unused BLoC event detected",
                                AddBlocHandlerFix(element)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun isUsedBlocEvent(dartClass: DartClass): Boolean {
        val dartFile = dartClass.containingFile as? DartFile ?: return false
        val project = dartClass.project
        val blocFileNameWithExtension = FileUtils.getEventBLoCFile(dartFile.name)
        val classDirectory = dartFile.virtualFile.parent
        val blocDartFile = classDirectory.findChild(blocFileNameWithExtension) ?: return false

        // Search for references directly
        val references = ReferencesSearch.search(dartClass, GlobalSearchScope.fileScope(project, blocDartFile))
        if (references.findFirst() != null) return true

        // Alternative search for DartReferenceExpression if direct references are not found
        val adjacentDartFile = PsiManager.getInstance(project).findFile(blocDartFile) as? DartFile ?: return false
        return PsiTreeUtil.findChildrenOfType(adjacentDartFile, DartReferenceExpression::class.java)
            .any { it.text == dartClass.name }
    }
}
