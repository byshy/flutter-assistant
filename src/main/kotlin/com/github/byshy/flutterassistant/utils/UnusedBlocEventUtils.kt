package com.github.byshy.flutterassistant.utils

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartFile
import com.jetbrains.lang.dart.psi.DartReferenceExpression
import com.intellij.psi.util.PsiTreeUtil

object UnusedBlocEventUtils {

    private val log = Logger.getInstance(UnusedBlocEventUtils::class.java)

    fun isBlocEventSubclass(dartClass: DartClass, dartFile: DartFile): Boolean {
        val blocName = FileUtils.getBlocName(dartFile.name)
        return dartClass.superClass?.text == "${blocName}Event"
    }

    fun isUsedBlocEvent(dartClass: DartClass): Boolean {
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
