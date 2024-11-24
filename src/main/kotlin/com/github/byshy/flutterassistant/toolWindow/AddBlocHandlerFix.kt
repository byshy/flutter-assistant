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
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiParserFacade

class AddBlocHandlerFix(private val dartClass: DartClass) : LocalQuickFix {

    companion object {
        private const val CONSTRUCTOR_BODY_CONTENT_OFFSET = 2
        private const val SEMICOLON = ";"
    }

    private val log = Logger.getInstance(AddBlocHandlerFix::class.java)

    override fun getFamilyName() = "Add BLoC handler for unused event"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val dartFile = dartClass.containingFile as? DartFile ?: run {
            log.warn("Failed to cast containing file to DartFile")
            return
        }

        val blocVirtualFile = findBlocVirtualFile(dartFile) ?: return
        val blocDartClass = findBlocDartClass(project, blocVirtualFile) ?: return

        val blocConstructor = findBlocConstructor(blocDartClass) ?: return

        val eventClassName = dartClass.name ?: run {
            log.warn("Event class name is null for $dartClass")
            return
        }

        val newHandlerCode = generateHandlerCode(eventClassName)
        val newHandlerMethod = generateHandlerMethod(eventClassName)

        addHandlerToBloc(project, blocConstructor, blocDartClass, newHandlerCode, newHandlerMethod)
    }

    private fun findBlocVirtualFile(dartFile: DartFile): com.intellij.openapi.vfs.VirtualFile? {
        val blocFileNameWithExtension = FileUtils.getEventBLoCFile(dartFile.name)
        val blocParentDirectory = dartFile.virtualFile.parent
        return blocParentDirectory?.findChild(blocFileNameWithExtension).also {
            if (it == null) log.warn("BLoC file not found for event: ${dartFile.name}")
        }
    }

    private fun findBlocDartClass(project: Project, blocVirtualFile: com.intellij.openapi.vfs.VirtualFile): DartClass? {
        val blocDartFile = PsiManager.getInstance(project).findFile(blocVirtualFile) as? DartFile ?: run {
            log.warn("Failed to find DartFile for BLoC file")
            return null
        }
        return PsiTreeUtil.findChildOfType(blocDartFile, DartClass::class.java).also {
            if (it == null) log.warn("BLoC Dart class not found in file: $blocDartFile")
        }
    }

    private fun findBlocConstructor(blocDartClass: DartClass): DartMethodDeclaration? {
        return PsiTreeUtil.findChildrenOfType(blocDartClass, DartMethodDeclaration::class.java)
            .firstOrNull { it.name == blocDartClass.name }.also {
                if (it == null) log.warn("Constructor not found for BLoC class: ${blocDartClass.name}")
            }
    }

    private fun generateHandlerCode(eventClassName: String): String {
        return """
            on<$eventClassName>(_on$eventClassName)
        """.trimIndent()
    }

    private fun generateHandlerMethod(eventClassName: String): String {
        return """
            void _on$eventClassName($eventClassName event, emit) {
                // TODO: Implement event handling logic
            }
        """.trimIndent()
    }

    private fun addHandlerToBloc(
            project: Project,
            blocConstructor: DartMethodDeclaration,
            blocDartClass: DartClass,
            handlerCode: String,
            handlerMethod: String
    ) {
        val constructorStatement = DartElementGenerator.createStatementFromText(project, handlerCode) ?: return
        val methodDeclaration = DartElementGenerator.createStatementFromText(project, handlerMethod) ?: return
        val semicolon = DartElementGenerator.createStatementFromText(project, SEMICOLON) ?: return
        val emptyBlock = DartElementGenerator.createStatementFromText(project, "{\n}") ?: return
        val whiteSpace = PsiParserFacade.getInstance(project).createWhiteSpaceFromText(" ")

        // Get the class body and add the new handler method
        val classBodyContent = blocDartClass.children[blocDartClass.children.lastIndex].children[0]
        classBodyContent.add(methodDeclaration)

        // Check if the constructor has a body and get its content
        val constructorBody = blocConstructor.functionBody

        if (constructorBody == null) {
            if (blocConstructor.lastChild.text.equals(SEMICOLON)) {
                blocConstructor.lastChild.delete()
            }

            blocConstructor.add(whiteSpace)
            blocConstructor.add(emptyBlock)
            blocConstructor.lastChild.children[0].add(constructorStatement).add(semicolon)
            return
        }

        val constructorBodyBlock = constructorBody.children.getOrNull(0) ?: run {
            log.warn("Constructor body block is null for ${blocConstructor.name}")
            return
        }

        var offset = CONSTRUCTOR_BODY_CONTENT_OFFSET
        if (constructorBodyBlock.children.lastIndex <= 3) {
            offset = 1
        }

        val constructorBodyContent = constructorBodyBlock.children.getOrNull(
                constructorBodyBlock.children.lastIndex - offset
        ) ?: run {
            log.warn("Failed to access constructor body content for ${blocConstructor.name}")
            return
        }

        // Add the handler invocation statement to the constructor body
        constructorBodyContent.add(constructorStatement).add(semicolon)

    }
}
