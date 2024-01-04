package org.smartdot.idea.plugins.toolWindow.bottomPanel

import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBPanel
import com.intellij.ui.tabs.TabInfo
import com.intellij.ui.tabs.impl.JBTabsImpl
import org.apache.commons.lang3.StringUtils
import org.smartdot.idea.plugins.toolWindow.commpont.JsonEditor
import java.awt.BorderLayout


class BottomPanel(project: Project) : JBPanel<JBPanel<*>>() {
    private var bodyTxt: EditorTextField
    private var responseTxt: EditorTextField
    private var cookieTxt: EditorTextField
    private var headersTxt: EditorTextField
    private var tabs: JBTabsImpl

    init {
        layout = BorderLayout(0,0)
        tabs = JBTabsImpl(project)

        bodyTxt = addEditor("body",project)

        headersTxt = addEditor("header",project)

        cookieTxt = addEditor("cookie",project)

        responseTxt = addEditor("response",project)

        add(tabs.getComponent(),BorderLayout.CENTER)
    }

    private fun addEditor(title: String,project:Project): EditorTextField {
        val editor = JsonEditor(project)
        editor.text = ""
        val bodyTab = TabInfo(editor)
        bodyTab.setText(title)
        tabs.addTab(bodyTab)
        return editor
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

    fun setResponse(txt: String) {
        responseTxt.text = formatJson(txt)
        tabs.select(tabs.getTabAt(3),true)
    }

    private fun formatJson(txt: String): String {
        if (JSONUtil.isTypeJSON(txt)) {
            return JSONUtil.toJsonPrettyStr(txt)
        }
        return txt
    }

    fun setBody(param: JSONObject) {
        bodyTxt.text = param.toStringPretty()
    }
}