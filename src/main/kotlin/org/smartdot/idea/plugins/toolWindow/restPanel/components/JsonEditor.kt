package org.smartdot.idea.plugins.toolWindow.restPanel.components

import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.json.JsonFileType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.ui.EditorTextField
import com.intellij.util.LocalTimeCounter
import com.intellij.util.ui.JBUI
import org.smartdot.idea.plugins.Bundle.message
import javax.swing.border.Border

class JsonEditor(
    project: Project,
    fileType: FileType = TEXT_FILE_TYPE,
) : EditorTextField(null, project, fileType, false, false) {
    fun setText(
        text: String?,
        fileType: FileType,
    ) {
        super.setFileType(fileType)
        val document = createDocument(text, fileType)
        setDocument(document)
        val psiFile =
            PsiDocumentManager.getInstance(project).getPsiFile(
                document!!,
            )
        if (psiFile != null) {
            WriteCommandAction.runWriteCommandAction(
                project,
            ) { CodeStyleManager.getInstance(project).reformat(psiFile) }
        }
    }

    override fun setFileType(fileType: FileType) {
        setNewDocumentAndFileType(fileType, createDocument(getText(), fileType))
    }

    override fun createDocument(): Document = createDocument(null, fileType)!!

    private fun initOneLineMode(editor: EditorEx) {
        editor.isOneLineMode = false
        editor.colorsScheme = editor.createBoundColorSchemeDelegate(null)
        editor.settings.isCaretRowShown = false
    }

    override fun createEditor(): EditorEx {
        val editor = super.createEditor()
        initOneLineMode(editor)
        setupTextFieldEditor(editor)
        return editor
    }

    override fun repaint(
        tm: Long,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) {
        super.repaint(tm, x, y, width, height)
        if (editor is EditorEx) {
            initOneLineMode((editor as EditorEx?)!!)
        }
    }

    override fun setBorder(border: Border?) {
        super.setBorder(JBUI.Borders.empty())
    }

    private fun createDocument(
        text: String?,
        fileType: FileType,
    ): Document? {
        val factory = PsiFileFactory.getInstance(project)
        val stamp = LocalTimeCounter.currentTime()
        val psiFile =
            factory.createFileFromText(
                message("plugin.name"),
                fileType,
                text ?: "",
                stamp,
                true,
                false,
            )
        return PsiDocumentManager.getInstance(project).getDocument(psiFile)
    }

    companion object {
        /**
         * 文本格式
         */
        val TEXT_FILE_TYPE: FileType = FileTypes.PLAIN_TEXT

        /**
         * json格式
         */
        val JSON_FILE_TYPE: FileType = JsonFileType.INSTANCE

        /**
         * html格式
         */
        val HTML_FILE_TYPE: FileType = HtmlFileType.INSTANCE

        /**
         * xml格式
         */
        val XML_FILE_TYPE: FileType = XmlFileType.INSTANCE

        fun setupTextFieldEditor(editor: EditorEx) {
            val settings = editor.settings
            settings.isFoldingOutlineShown = true
            settings.isLineNumbersShown = true
            settings.isIndentGuidesShown = true
            editor.setHorizontalScrollbarVisible(true)
            editor.setVerticalScrollbarVisible(true)
        }
    }
}
