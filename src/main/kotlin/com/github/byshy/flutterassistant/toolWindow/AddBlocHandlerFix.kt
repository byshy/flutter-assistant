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
        val blocParentDirectory = dartFile.virtualFile.parent
        val blocVirtualFile = blocParentDirectory?.findChild(blocFileNameWithExtension) ?: return

        // Locate the BLoC class in the adjacent file
        val blocDartFile = PsiManager.getInstance(project).findFile(blocVirtualFile) as? DartFile ?: return
        val blocDartClass = PsiTreeUtil.findChildOfType(blocDartFile, DartClass::class.java) ?: return

        // Find the constructor for the BLoC class
        val blocConstructor = PsiTreeUtil.findChildrenOfType(blocDartClass, DartMethodDeclaration::class.java)
            .firstOrNull { it.name == blocDartClass.name } ?: return

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

        // Create the handler invocation statement (for the constructor body)
        val constructorStatement = DartElementGenerator.createStatementFromText(project, newHandlerCode) ?: return

        // Create a method declaration statement (for the class body)
        val methodDeclaration = DartElementGenerator.createStatementFromText(project, newHandlerMethod) ?: return

        val semicolon = DartElementGenerator.createStatementFromText(project, ";") ?: return

        // Check if the constructor has a body
        val constructorBody = blocConstructor.functionBody ?: return

        //Get the Full constructor block
        val constructorBodyBlock = constructorBody.children[0]

        /**
         * The offset here refers to where the content of the constructor is located at in the [constructorBodyBlock.children]
         * The 2 refers to the list having 2 entries after the content of the constructor
         * The 2 elements are the closing curly braces and the new line character
         */
        val constructorBodyContentOffset = 2

        //Get the constructor body content.
        val constructorBodyContent =
            constructorBodyBlock.children[constructorBodyBlock.children.lastIndex - constructorBodyContentOffset]

        //Get the class body content.
        val classBodyContent = blocDartClass.children[blocDartClass.children.lastIndex].children[0]

        constructorBodyContent.add(constructorStatement).add(semicolon)

        classBodyContent.add(methodDeclaration)
    }
}

