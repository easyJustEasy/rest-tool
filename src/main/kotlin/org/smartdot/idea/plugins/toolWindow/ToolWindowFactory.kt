package org.smartdot.idea.plugins.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import org.smartdot.idea.plugins.toolWindow.apiPanel.ApiPanel
import org.smartdot.idea.plugins.toolWindow.restPanel.RestPanel
import java.awt.BorderLayout

class ToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow,
    ) {
        val myToolWindow = MyToolWindow(toolWindow)

        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(
        toolWindow: ToolWindow,
    ) {
        private val project = toolWindow.project

        fun getContent() =
            JBPanel<JBPanel<*>>().apply {
                val apiPanel = ApiPanel(project)
                val restPanel = RestPanel(project, apiPanel)
                apiPanel.setRestPanel(restPanel)
                val split = JBSplitter(true, "", 0.5F)
                split.firstComponent = apiPanel
                split.secondComponent = restPanel
                setLayout(BorderLayout(0, 0))
                add(split, BorderLayout.CENTER)
            }
    }
}
