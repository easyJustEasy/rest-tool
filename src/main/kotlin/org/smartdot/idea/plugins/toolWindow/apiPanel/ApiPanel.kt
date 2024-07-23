package org.smartdot.idea.plugins.toolWindow.apiPanel

import cn.hutool.core.exceptions.ExceptionUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.components.service
import com.intellij.openapi.observable.util.whenFocusLost
import com.intellij.openapi.observable.util.whenKeyReleased
import com.intellij.openapi.project.Project
import com.intellij.ui.components.*
import org.apache.commons.lang3.StringUtils
import org.smartdot.idea.plugins.bo.ApiBO
import org.smartdot.idea.plugins.services.ApiScanService
import org.smartdot.idea.plugins.toolWindow.restPanel.RestPanel
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*
import kotlin.concurrent.schedule


class ApiPanel(project: Project) : JBPanel<JBPanel<*>>() {
    private val defaultListModel = DefaultListModel<ApiBO>()
    private val port = JBTextField()
    private val allApis: HashSet<ApiBO> = HashSet()
    private var loading: Boolean = false
    private var path: String = project.basePath.toString()
    private var apiService: ApiScanService = project.service<ApiScanService>()
    private lateinit var restPanel: RestPanel

    init {
        setLayout(BorderLayout(0, 0))
        val toolBar = createToolBar()
        val list = createListPanel()
        add(toolBar, BorderLayout.NORTH)
        add(list, BorderLayout.CENTER)
    }

    private fun createListPanel(): JBScrollPane {
        val list = JBList(defaultListModel)
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        list.installCellRenderer {
            val label = it?.let { it1 -> JBLabel(it1.url) }
            label!!
        }
        list.addListSelectionListener {
            val select = getSelectValue(list)
            if (select != null && StringUtils.isNotBlank(select.url)) {
                val url = select.url
                val port = getPort()
                restPanel.updateApi("http://" + apiService.wrapUrl("localhost:$port/$url"), select.method, select.param)
            }
        }
        return JBScrollPane(list)
    }

    private fun createToolBar(): JBPanel<JBPanel<*>> {
        val panel = JBPanel<JBPanel<*>>()
        val layout = FlowLayout()
        layout.alignment = FlowLayout.LEFT
        panel.setLayout(layout)
        createReloadBtn(panel)
        createPort(panel)
        createSearch(panel)
        return panel
    }

    private fun createSearch(panel: JBPanel<JBPanel<*>>) {
        val searchBtn = JButton("", AllIcons.Actions.Search)
        val search = JBTextField()
        searchBtn.toolTipText = "搜索"
        searchBtn.preferredSize = Dimension(30, 30)
        searchBtn.isVisible = true
        searchBtn.addActionListener {
            searchBtn.isVisible = false
            search.isVisible = true
            search.grabFocus()
        }
        panel.add(searchBtn)
        search.columns = 50
        search.toolTipText = "搜索"
        search.isVisible = false
        search.whenFocusLost {
            val timer = java.util.Timer()
            timer.schedule(3000) {
                if(search.isFocusOwner){
                    timer.cancel()
                }else{
                    searchBtn.isVisible = true
                    search.isVisible = false
                }

            }
        }
        search.whenKeyReleased {
            filterResult(search)
        }
        panel.add(search)
    }

    private fun createPort(panel: JBPanel<JBPanel<*>>) {
        val configBtn = JButton("", AllIcons.Actions.InlayGear)
        port.toolTipText = "端口"
        port.isVisible = false
        port.whenFocusLost {
            val timer = java.util.Timer()
            timer.schedule(3000) {
                if(port.isFocusOwner){
                    timer.cancel()
                }else{
                    port.isVisible = false
                    configBtn.isVisible = true
                }

            }
        }
        configBtn.toolTipText = "修改端口"
        configBtn.preferredSize = Dimension(30, 30)
        configBtn.addActionListener {
            port.isVisible = true
            configBtn.isVisible = false
            port.grabFocus()
        }
        panel.add(port)
        panel.add(configBtn)
    }


    private fun createReloadBtn(panel: JBPanel<JBPanel<*>>): JButton {
        val reloadBtn = JButton("", AllIcons.Actions.Refresh)
        reloadBtn.preferredSize = Dimension(30, 30)
        reloadBtn.toolTipText = "刷新"
        reloadBtn.addActionListener {
            remove()
            initApis()
        }
        panel.add(reloadBtn)
        return reloadBtn
    }

    private fun addElement(s: Collection<ApiBO>) {
        s.sortedBy { e -> e.url }.forEach {
            defaultListModel.addElement(it)
        }
    }


    private fun initApis() {
        if (loading) {
            return
        }
        if (StringUtils.isNotBlank(path)) {
            loading = true
            try {
                val doScan = path.let { apiService.doScan(it) }
                allApis.addAll(doScan)
                addElement(doScan)
            }catch (e:Exception){
                println(e.stackTraceToString())
                restPanel.reportError(ExceptionUtil.getMessage(e))
            }
            loading = false
        }

    }

    private fun remove() {
        defaultListModel.clear()
    }


    private fun getSelectValue(list: JBList<ApiBO>): ApiBO? {
        if (list.selectedValue == null) {
            return null
        }
        return list.selectedValue!!
    }

    private fun getPort(): Int {
        if (StringUtils.isNotEmpty(port.text)) {
            try {
                return Integer.parseInt(port.text)
            } catch (e: NumberFormatException) {
                JOptionPane.showMessageDialog(this, "端口号只能是数字")
            }

        }
        return 8080
    }


    private fun filterResult(search: JTextField) {
        val searchTxt = search.text
        val apis = ArrayList<ApiBO>()
        for (element in allApis) {
            if (StringUtils.isBlank(searchTxt) || StringUtils.containsIgnoreCase(element.url, searchTxt)) {
                apis.add(element)
            }
        }
        remove()
        addElement(apis)
    }

    fun updateCache(apiBO: ApiBO) {
        for (element in allApis) {
            if (StringUtils.contains(apiBO.url, element.url) && StringUtils.equals(element.method, apiBO.method)) {
                element.param = apiBO.param
            }
        }
        for (element in defaultListModel.elements()) {
            if (StringUtils.contains(apiBO.url, element.url) && StringUtils.equals(element.method, apiBO.method)) {
                element.param = apiBO.param
            }
        }
    }

    fun setRestPanel(restPanel: RestPanel) {
        this.restPanel = restPanel
    }
}


