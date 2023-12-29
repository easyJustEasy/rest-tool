package org.smartdot.idea.plugins.toolWindow

import cn.hutool.core.exceptions.ExceptionUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import org.apache.commons.lang3.StringUtils
import org.smartdot.idea.plugins.bo.RequestBO
import org.smartdot.idea.plugins.services.ApiScanService
import org.smartdot.idea.plugins.services.ProjectService
import org.smartdot.idea.plugins.toolWindow.bottomPanel.BottomPanel
import org.smartdot.idea.plugins.toolWindow.ctrlPanel.CtrlPanel
import org.smartdot.idea.plugins.toolWindow.topPanel.TopPanel
import java.awt.BorderLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
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
        private val dir = toolWindow.project.basePath
        fun getContent() = JBPanel<JBPanel<*>>().apply {
            val ctrlPanel = CtrlPanel()
            val topPanel = TopPanel()
            val bottomPanel = BottomPanel()
            println("dir is :" + dir)
            val doScan = dir?.let { apiService.doScan(it) }
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
            if (doScan != null) {
                ctrlPanel.initApis(doScan)
            }
            ctrlPanel.reload {
                println("reloading ")
                ctrlPanel.remove()
                val ds = dir?.let { it1 -> apiService.doScan(it1) }
                if (ds != null) {
                    ctrlPanel.initApis(ds)
                }
            }
            ctrlPanel.select {
                val select = ctrlPanel.getSelectValue()
                if(select!=null&&StringUtils.isNotBlank(select.url)) {
                    val url = select.url
                    val port = ctrlPanel.getPort()
                    topPanel.setUrl("http://" + apiService.wrapUrl("localhost:" + port + "/" + url))
                    topPanel.setMethod(select.method)
                    bottomPanel.setBody(select.param)
                }

            }
            ctrlPanel.doSearch(object : KeyAdapter() {
                override fun keyReleased(e: KeyEvent?) {
                   ctrlPanel.filterResult()
                }
            })
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
            add(split, BorderLayout.CENTER)
        }

    }
}
