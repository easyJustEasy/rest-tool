package org.smartdot.idea.plugins.toolWindow.restPanel.components

import cn.hutool.core.exceptions.ExceptionUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBPanel
import org.smartdot.idea.plugins.Bundle
import org.smartdot.idea.plugins.bo.ApiBO
import org.smartdot.idea.plugins.services.RequestService
import org.smartdot.idea.plugins.toolWindow.apiPanel.ApiPanel
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JTextField

class SendPanel(
    project: Project,
    bottomPanel: BottomPanel,
    apiPanel: ApiPanel?,
) : JBPanel<JBPanel<*>>() {
    private val projectService = project.service<RequestService>()
    private val select: ComboBox<String>
    private val urlInput: JTextField

    init {
        val layout = BorderLayout(0, 0)
        setLayout(layout)
        select = ComboBox<String>()
        select.addItem(Bundle.message("methodGet"))
        select.addItem(Bundle.message("methodPostForm"))
        select.addItem(Bundle.message("methodPostJson"))
        select.selectedItem = Bundle.message("methodGet")
        add(select, BorderLayout.WEST)
        urlInput = JTextField(45)
        add(urlInput)
        val sendBtn = addSendAction(select, urlInput, bottomPanel, apiPanel)
        add(sendBtn, BorderLayout.EAST)
    }

    private fun addSendAction(
        select: ComboBox<String>,
        urlInput: JTextField,
        bottomPanel: BottomPanel,
        apiPanel: ApiPanel?,
    ): JButton {
        val sendBtn = JButton(Bundle.message("sendBtn"))
        sendBtn.addActionListener {
            val urlMethod = select.selectedItem?.toString() ?: ""
            val url = urlInput.text
            val requestBO = bottomPanel.getRequestBo(urlMethod, url)
            apiPanel?.updateCache(ApiBO(url, requestBO.params, urlMethod))
            try {
                val res = projectService.request(requestBO)
                bottomPanel.setResponse(res)
            } catch (e: Exception) {
                bottomPanel.setResponse(ExceptionUtil.getMessage(e))
            }
        }
        return sendBtn
    }

    fun updateApi(
        url: String,
        method: String,
    ) {
        select.selectedItem = method
        urlInput.text = url
    }
}
