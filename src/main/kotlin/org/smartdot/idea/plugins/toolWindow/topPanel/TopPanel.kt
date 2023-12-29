package org.smartdot.idea.plugins.toolWindow.topPanel

import cn.hutool.core.exceptions.ExceptionUtil
import cn.hutool.json.JSONObject
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBPanel
import org.smartdot.idea.plugins.Bundle
import org.smartdot.idea.plugins.bo.ApiBO
import org.smartdot.idea.plugins.bo.RequestBO
import org.smartdot.idea.plugins.services.ApiScanService
import org.smartdot.idea.plugins.services.ProjectService
import org.smartdot.idea.plugins.toolWindow.bottomPanel.BottomPanel
import org.smartdot.idea.plugins.toolWindow.ctrlPanel.CtrlPanel
import java.awt.FlowLayout
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JTextField


class TopPanel() : JBPanel<JBPanel<*>>() {
    private lateinit var select: ComboBox<String>
    private lateinit var urlInput: JTextField
    private lateinit var sendBtn: JButton
    private lateinit var projectService: ProjectService
    private lateinit var bottomPanel: BottomPanel
    private lateinit var ctrlPanel: CtrlPanel
    fun init(bp: BottomPanel, service: ProjectService) {
        bottomPanel = bp
        projectService = service
        val layout = FlowLayout()
        layout.alignment = FlowLayout.LEFT
        setLayout(layout)
        ComboBox<String>().also { select = it }
        select.addItem(Bundle.message("methodGet"))
        select.addItem(Bundle.message("methodPostForm"))
        select.addItem(Bundle.message("methodPostJson"))
        add(select)
        urlInput = JTextField(52)
        add(urlInput)
        sendBtn = JButton(Bundle.message("sendBtn"))
        addSendAction()
        add(sendBtn)
    }

    fun getUrl(): String {
        return urlInput.text;
    }

    fun getUrlMethod(): String {
        return select.selectedItem?.toString() ?: "";
    }

    fun setCtrlPanel(ct: CtrlPanel) {
        ctrlPanel = ct
    }

    fun addSendAction() {
        sendBtn.addActionListener {
            val urlMethod = getUrlMethod()
            val url = getUrl()
            val body = bottomPanel.getBody()
            val header = bottomPanel.getHeaders()
            val cookie = bottomPanel.getCookies()
            ctrlPanel.updateCache(ApiBO(url, JSONObject(body), urlMethod))
            try {
                val res = projectService.request(RequestBO(urlMethod, url, body, header, cookie))
                bottomPanel.setResponse(res)
            } catch (e: Exception) {
                bottomPanel.setResponse(ExceptionUtil.getMessage(e))
            }
        }
    }

    fun setUrl(s: String) {
        urlInput.text = s
    }

    fun setMethod(method: String) {
        select.selectedItem = method

    }
}