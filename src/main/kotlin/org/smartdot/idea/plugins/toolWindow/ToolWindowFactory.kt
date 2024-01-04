package org.smartdot.idea.plugins.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import org.smartdot.idea.plugins.services.ApiScanService
import org.smartdot.idea.plugins.services.ProjectService
import org.smartdot.idea.plugins.toolWindow.bottomPanel.BottomPanel
import org.smartdot.idea.plugins.toolWindow.ctrlPanel.CtrlPanel
import org.smartdot.idea.plugins.toolWindow.topPanel.TopPanel
import java.awt.BorderLayout
import javax.swing.JSplitPane


class ToolWindowFactory : ToolWindowFactory {


    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)

        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)

    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {
        private val service = toolWindow.project.service<ProjectService>()
        private val apiService = toolWindow.project.service<ApiScanService>()
        private val dir = toolWindow.project.basePath
        private val project = toolWindow.project
        fun getContent() = JBPanel<JBPanel<*>>().apply {

            val ctrlPanel = CtrlPanel()
            val bottomPanel = BottomPanel(project)
            val topPanel = TopPanel()
            topPanel.init(bottomPanel, service)
            ctrlPanel.init(topPanel, bottomPanel, dir, apiService)
            topPanel.setCtrlPanel(ctrlPanel)

            val split = JBSplitter(true, "", 0.5F)
            split.firstComponent = ctrlPanel
            val bt = JBPanel<JBPanel<*>>()
            bt.layout = BorderLayout()
            bt.add(topPanel, BorderLayout.NORTH)
            bt.add(bottomPanel, BorderLayout.CENTER)
            split.secondComponent = bt
            setLayout(BorderLayout())
            add(split, BorderLayout.CENTER)
        }

    }
}
