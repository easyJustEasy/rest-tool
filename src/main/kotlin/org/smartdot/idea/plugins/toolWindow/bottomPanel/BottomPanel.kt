package org.smartdot.idea.plugins.toolWindow.bottomPanel

import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import org.apache.commons.lang3.StringUtils
import org.smartdot.idea.plugins.consts.ProjectConsts
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.JTextArea


class BottomPanel() : JBPanel<JBPanel<*>>() {
    private var bodyTxt: JTextArea
    private var responseTxt: JTextArea
    private var cookieTxt: JTextArea
    private var headersTxt: JTextArea
    private var bottomPanel: JBTabbedPane = JBTabbedPane()

    init {
        val layout = FlowLayout()
        layout.alignment = FlowLayout.LEFT
        setLayout(layout)
        bodyTxt = JTextArea()
        bodyTxt.setLineWrap(true);
        bodyTxt.setWrapStyleWord(true);
        val bodyPanel = JBScrollPane(bodyTxt)
        bodyTxt.text = ""
        setPanel(bodyPanel)
        bottomPanel.addTab("body", bodyPanel)


        headersTxt = JTextArea()
        headersTxt.setLineWrap(true);
        headersTxt.setWrapStyleWord(true);
        val headerPanel = JBScrollPane(headersTxt)
        headersTxt.text = ""
        setPanel(headerPanel)
        bottomPanel.addTab("header", headerPanel)

        cookieTxt = JTextArea()
        cookieTxt.setLineWrap(true);
        cookieTxt.setWrapStyleWord(true);
        val cookiePanel = JBScrollPane(cookieTxt)
        cookieTxt.text = ""
        setPanel(cookiePanel)
        bottomPanel.addTab("cookie", cookiePanel)

        responseTxt = JTextArea()
        responseTxt.setLineWrap(true);
        responseTxt.setWrapStyleWord(true);
        val responsePanel = JBScrollPane(responseTxt)
        responseTxt.text = ""
        setPanel(responsePanel)
        bottomPanel.addTab("response", responsePanel)



        add(bottomPanel)
    }

    fun setPanel(panel: JBScrollPane) {
        panel.setPreferredSize(Dimension(ProjectConsts.w, ProjectConsts.h))
        panel.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }

    fun getBody(): String {
        val txt = bodyTxt.text
        if (JSONUtil.isTypeJSON(txt)) {
            return txt
        }
        if (StringUtils.isBlank(txt)) {
            return StringUtils.EMPTY
        }
        throw RuntimeException("body 不是一个json")
    }


    fun getHeaders(): String {
        val txt = headersTxt.text
        if (StringUtils.isBlank(txt)) {
            return StringUtils.EMPTY
        }
        if (JSONUtil.isTypeJSON(txt)) {
            return txt
        }
        throw RuntimeException("header 不是一个json")
    }

    fun getCookies(): String {
        val txt = cookieTxt.text
        if (StringUtils.isBlank(txt)) {
            return StringUtils.EMPTY
        }
        if (JSONUtil.isTypeJSON(txt)) {
            return txt
        }
        throw RuntimeException("cookie 不是一个json")
    }

    fun getResponse(): String {
        return responseTxt.text;
    }

    fun setResponse(txt: String) {
        responseTxt.text = formatJson(txt);
        bottomPanel.setSelectedComponent(bottomPanel.getComponentAt(3))
    }

    private fun formatJson(txt: String): String? {
        if (JSONUtil.isTypeJSON(txt)) {
            return JSONUtil.toJsonPrettyStr(txt)
        }
        return txt
    }

    fun setBody(param: JSONObject) {
        bodyTxt.text = param.toStringPretty()
    }
}