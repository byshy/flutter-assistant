package com.github.byshy.flutterassistant.toolWindow

import com.github.byshy.flutterassistant.utils.FileUtils
import com.intellij.openapi.project.Project
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartFile
import com.jetbrains.lang.dart.psi.DartMethodDeclaration
import com.jetbrains.lang.dart.util.DartElementGenerator

class AddBlocHandlerFix(private val dartClass: DartClass) : LocalQuickFix {
    override fun getFamilyName() = "Add BLoC handler for unused event"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val dartFile = dartClass.containingFile as? DartFile ?: return

        // Get BLoC file for the event
        val blocFileNameWithExtension = FileUtils.getEventBLoCFile(dartFile.name)
        val classDirectory = dartFile.virtualFile.parent
        val blocDartFile = classDirectory?.findChild(blocFileNameWithExtension) ?: return

        // Locate the BLoC class in the adjacent file
        val blocFile = PsiManager.getInstance(project).findFile(blocDartFile) as? DartFile ?: return
        val blocClass = PsiTreeUtil.findChildOfType(blocFile, DartClass::class.java) ?: return

        // Find the constructor for the BLoC class
        val blocConstructor = PsiTreeUtil.findChildrenOfType(blocClass, DartMethodDeclaration::class.java)
            .firstOrNull { it.name == blocClass.name } ?: return

        // Construct the new handler code for the constructor
        val eventClassName = dartClass.name

        val newHandlerCode = """
        on<$eventClassName>(_on$eventClassName)
    """.trimIndent()

        // Create the new handler method
        val newHandlerMethod = """
        void _on$eventClassName($eventClassName event, emit) {
            // TODO: Implement event handling logic
        }
    """.trimIndent()

        // Check if the constructor has a body
        val functionBody = blocConstructor.functionBody ?: return
        val classBody = blocClass.children ?: return

        // Create the handler invocation statement (for the constructor body)
        val constructorStatement = DartElementGenerator.createStatementFromText(project, newHandlerCode) ?: return
        // Create a method declaration statement (for the class body)
        val methodDeclaration = DartElementGenerator.createStatementFromText(project, newHandlerMethod) ?: return
        val semicolon = DartElementGenerator.createStatementFromText(project, ";") ?: return


        // Add the new handler code to the constructor body after the last statement
        val constructorBodyBlock = functionBody.children.get(0);
        val constructorBodyContent = constructorBodyBlock.children
            .get(constructorBodyBlock.children.lastIndex - 2);

        val classBodyContent = blocClass.children.get(blocClass.children.lastIndex).children.get(0);

        constructorBodyContent.add(constructorStatement).add(semicolon);
        classBodyContent.add(methodDeclaration);
    }
}

