package com.zhaopin.cpms.idea.plugins.toolWindow.bottomPanel

import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.JTextArea


class BottomPanel() : JBPanel<JBPanel<*>>() {
    private var bodyTxt: JTextArea
    private var responseTxt: JTextArea
    private var headersTxt: JTextArea
    private var bottomPanel: JBTabbedPane = JBTabbedPane()

    init {
        val layout = FlowLayout()
        layout.alignment = FlowLayout.LEFT
        setLayout(layout)
        bodyTxt = JTextArea()
        val bodyPanel: JBScrollPane = JBScrollPane(bodyTxt)
        bodyTxt.text = ""
        setPanel(bodyPanel)
        bottomPanel.addTab("body", bodyPanel)
        headersTxt = JTextArea()
        val headerPanel: JBScrollPane = JBScrollPane(headersTxt)
        headersTxt.text = ""
        setPanel(headerPanel)
        bottomPanel.addTab("head", headerPanel)
        responseTxt = JTextArea()
        val responsePanel: JBScrollPane = JBScrollPane(responseTxt)
        responseTxt.text = ""
        setPanel(responsePanel)
        bottomPanel.addTab("response", responsePanel)

        add(bottomPanel)
    }

    fun setPanel(panel: JBScrollPane) {
        panel.setPreferredSize(Dimension(800, 600))
        panel.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }

    fun getBody(): String {
        return bodyTxt.text;
    }

    fun getResponse(): String {
        return responseTxt.text;
    }

    fun setResponse(txt: String) {
        responseTxt.text = formatJson(txt);
        bottomPanel.setSelectedComponent(bottomPanel.getComponentAt(2))
    }

    private fun formatJson(txt: String): String? {
        if (JSONUtil.isTypeJSON(txt)) {
            return JSONUtil.toJsonPrettyStr(txt)
        }
        return txt
    }

    fun getHeaders(): String {
        return headersTxt.text;
    }
}