package org.smartdot.idea.plugins.toolWindow.restPanel.components

import cn.hutool.json.JSONUtil
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import com.intellij.ui.tabs.TabInfo
import com.intellij.ui.tabs.impl.JBTabsImpl
import org.apache.commons.lang3.StringUtils
import org.smartdot.idea.plugins.bo.RequestBO
import java.awt.BorderLayout

class BottomPanel(
    project: Project,
) : JBPanel<JBPanel<*>>() {
    private var bodyTxt: JsonEditor
    private var responseTxt: JsonEditor
    private var cookieTxt: JsonEditor
    private var headersTxt: JsonEditor
    private var tabs: JBTabsImpl

    init {
        layout = BorderLayout(0, 0)
        tabs = JBTabsImpl(project)

        bodyTxt = addEditor("body", project)

        headersTxt = addEditor("header", project)

        cookieTxt = addEditor("cookie", project)

        responseTxt = addEditor("response", project)

        add(tabs.component, BorderLayout.CENTER)
    }

    private fun addEditor(
        title: String,
        project: Project,
    ): JsonEditor {
        val editor = JsonEditor(project)
        editor.name = title
        editor.setText("", JsonEditor.TEXT_FILE_TYPE)
        val bodyTab = TabInfo(editor)
        bodyTab.setText(title)
        tabs.addTab(bodyTab)
        return editor
    }

    private fun getEditor(
        editor: JsonEditor,
        name: String,
    ): String {
        val txt = editor.text
        if (StringUtils.isBlank(txt)) {
            return StringUtils.EMPTY
        }
        if (JSONUtil.isTypeJSON(txt)) {
            return txt
        }
        throw RuntimeException(name + " 不是一个json")
    }

    private fun getBody(): String = getEditor(bodyTxt, "body")

    private fun getHeaders(): String = getEditor(headersTxt, "header")

    private fun getCookies(): String = getEditor(cookieTxt, "cookie")

    fun setResponse(txt: String) {
        if (StringUtils.isNotBlank(txt)) {
            responseTxt.setText(txt, JsonEditor.JSON_FILE_TYPE)
        } else {
            responseTxt.setText(txt, JsonEditor.TEXT_FILE_TYPE)
        }
        tabs.select(tabs.getTabAt(3), true)
    }

    fun updateApi(param: String?) {
        if (StringUtils.isNotBlank(param)) {
            bodyTxt.setText(param, JsonEditor.JSON_FILE_TYPE)
        }
    }

    fun getRequestBo(
        method: String,
        url: String,
    ): RequestBO {
        val body = getBody()
        val header = getHeaders()
        val cookie = getCookies()
        return RequestBO(method, url, body, header, cookie)
    }

    fun reportError(message: String?) {
        responseTxt.setText(message, JsonEditor.TEXT_FILE_TYPE)
        tabs.select(tabs.getTabAt(3), true)
    }
}
