package com.zhaopin.cpms.idea.plugins.toolWindow

import cn.hutool.core.exceptions.ExceptionUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.content.ContentFactory
import com.zhaopin.cpms.idea.plugins.Bundle
import com.zhaopin.cpms.idea.plugins.bo.RequestBO
import com.zhaopin.cpms.idea.plugins.services.ProjectService
import com.zhaopin.cpms.idea.plugins.toolWindow.bottomPanel.BottomPanel
import com.zhaopin.cpms.idea.plugins.toolWindow.topPanel.TopPanel
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.*
import javax.swing.border.LineBorder


class ToolWindowFactory : ToolWindowFactory {


    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
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
                    val res = service.request(RequestBO(urlMethod, url, body,header))
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
