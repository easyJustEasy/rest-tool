package org.smartdot.idea.plugins.toolWindow

import cn.hutool.core.exceptions.ExceptionUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import org.smartdot.idea.plugins.bo.RequestBO
import org.smartdot.idea.plugins.services.ApiScanService
import org.smartdot.idea.plugins.services.ProjectService
import org.smartdot.idea.plugins.toolWindow.bottomPanel.BottomPanel
import org.smartdot.idea.plugins.toolWindow.ctrlPanel.CtrlPanel
import org.smartdot.idea.plugins.toolWindow.topPanel.TopPanel
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JSplitPane
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
        private val apiService = toolWindow.project.service<ApiScanService>()
        fun getContent() = JBPanel<JBPanel<*>>().apply {
            val ctrlPanel: CtrlPanel = CtrlPanel()
            val topPanel: TopPanel = TopPanel()
            val bottomPanel: BottomPanel = BottomPanel()
            val doScan = apiService.doScan("E:\\work\\project\\java-it-cpms-p-project")
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
            ctrlPanel.addElement(doScan)
            ctrlPanel.reload {
                println("reloading ")
                ctrlPanel.remove()
                val ds = apiService.doScan("E:\\work\\project\\java-it-cpms-p-project")
                ctrlPanel.addElement(ds)

            }
            setBorder(LineBorder(JBColor.RED));
            topPanel.border = LineBorder(JBColor.RED)
            bottomPanel.border = LineBorder(JBColor.RED)
            ctrlPanel.border = LineBorder(JBColor.RED)
            val split = JSplitPane(JSplitPane.VERTICAL_SPLIT)
            split.add(ctrlPanel)
            val bt = JBPanel<JBPanel<*>>()
            bt.layout = BorderLayout()
            bt.add(topPanel, BorderLayout.NORTH)
            bt.add(bottomPanel, BorderLayout.CENTER)
            split.add(bt)
            setLayout(BorderLayout())
            add(split,BorderLayout.CENTER)
        }

    }
}
