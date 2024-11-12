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
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

class UnusedBlocEventInspection : LocalInspectionTool() {

    private val log = Logger.getInstance(UnusedBlocEventInspection::class.java)

    override fun getDisplayName(): String {
        return "Detect unused BLoC event"
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element !is DartClass) return

                val dartFile = element.containingFile as? DartFile ?: return
                if (!FileUtils.isEventFile(dartFile.name, dartFile.text)) return

                if (!isUsedBlocEvent(element)) {
                    holder.registerProblem(
                        element.nameIdentifier ?: element,
                        "Unused BLoC event detected",
                        AddBlocHandlerFix(element)
                    )
                }
            }
        }
    }

    private fun isUsedBlocEvent(dartClass: DartClass): Boolean {
        val dartFile = dartClass.containingFile as? DartFile ?: run {
            log.warn("Failed to cast containing file to DartFile for ${dartClass.name}")
            return false
        }

        val project = dartClass.project
        val blocFileNameWithExtension = FileUtils.getEventBLoCFile(dartFile.name)
        val blocVirtualFile = dartFile.virtualFile.parent?.findChild(blocFileNameWithExtension) ?: run {
            log.warn("BLoC file not found for event file: ${dartFile.name}")
            return false
        }

        return hasDirectReference(dartClass, project, blocVirtualFile) || hasReferenceInAdjacentFile(
            dartClass,
            project,
            blocVirtualFile
        )
    }

    private fun hasDirectReference(
        dartClass: DartClass,
        project: Project,
        blocVirtualFile: com.intellij.openapi.vfs.VirtualFile
    ): Boolean {
        val references = ReferencesSearch.search(dartClass, GlobalSearchScope.fileScope(project, blocVirtualFile))
        return references.findFirst() != null
    }

    private fun hasReferenceInAdjacentFile(
        dartClass: DartClass,
        project: Project,
        blocVirtualFile: com.intellij.openapi.vfs.VirtualFile
    ): Boolean {
        val adjacentDartFile = PsiManager.getInstance(project).findFile(blocVirtualFile) as? DartFile ?: run {
            log.warn("Failed to find DartFile for BLoC file: $blocVirtualFile")
            return false
        }
        return PsiTreeUtil.findChildrenOfType(adjacentDartFile, DartReferenceExpression::class.java)
            .any { it.text == dartClass.name }
    }
}
