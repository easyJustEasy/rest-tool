package org.smartdot.idea.plugins.toolWindow

import cn.hutool.core.exceptions.ExceptionUtil
import cn.hutool.json.JSONUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.content.ContentFactory
import org.smartdot.idea.plugins.Bundle
import org.smartdot.idea.plugins.bo.RequestBO
import org.smartdot.idea.plugins.services.ProjectService
import org.smartdot.idea.plugins.toolWindow.bottomPanel.BottomPanel
import org.smartdot.idea.plugins.toolWindow.topPanel.TopPanel
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.*
import javax.swing.border.LineBorder


class ToolWindowFactory : ToolWindowFactory {


    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        println(project.basePath)
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)

    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {
        private val service = toolWindow.project.service<ProjectService>()
        fun getContent() = JBPanel<JBPanel<*>>().apply {

            val topPanel: TopPanel = TopPanel()
            val bottomPanel: BottomPanel = BottomPanel()
            topPanel.addSendAction {
                try {
                    val urlMethod = topPanel.getUrlMethod()
                    val url = topPanel.getUrl()
                    val body = bottomPanel.getBody()
                    val header = bottomPanel.getHeaders()
                    val cookie = bottomPanel.getCookies()
                    val res = service.request(RequestBO(urlMethod, url, body, header, cookie))
                    bottomPanel.setResponse(res)
                } catch (e: Exception) {
                    bottomPanel.setResponse(ExceptionUtil.getMessage(e))
                }
            }
//            setBorder(LineBorder(JBColor.RED));
//            topPanel.border = LineBorder(JBColor.RED)
//            bottomPanel.border = LineBorder(JBColor.RED)
            val layout = BorderLayout()
            setLayout(layout)
            add(topPanel, BorderLayout.NORTH)
            add(bottomPanel, BorderLayout.CENTER)
        }

    }
}
