package org.smartdot.idea.plugins.toolWindow.ctrlPanel

import com.intellij.icons.AllIcons
import com.intellij.openapi.observable.util.whenKeyReleased
import com.intellij.ui.ListSpeedSearch
import com.intellij.ui.components.*
import org.apache.commons.lang.StringUtils
import org.smartdot.idea.plugins.bo.ApiBO
import org.smartdot.idea.plugins.services.ApiScanService
import org.smartdot.idea.plugins.toolWindow.bottomPanel.BottomPanel
import org.smartdot.idea.plugins.toolWindow.topPanel.TopPanel
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.*
import javax.swing.*


class CtrlPanel : JBPanel<JBPanel<*>>() {
    private lateinit var list: JBList<ApiBO?>
    private val defaultListModel = DefaultListModel<ApiBO>()
    private val reloadBtn: JButton = JButton("", AllIcons.Actions.Refresh)
    private val configBtn: JButton = JButton("", AllIcons.Actions.InlayGear)
    private val searchBtn: JButton = JButton("", AllIcons.Actions.Search)
    private val port: JBTextField = JBTextField()
    private val search: JBTextField = JBTextField()
    private val allApis: HashSet<ApiBO> = HashSet()
    private var isTipped: Boolean = false
    private lateinit var dir: String
    private lateinit var apiService: ApiScanService
    private lateinit var topPanel: TopPanel
    private lateinit var bottomPanel: BottomPanel
    fun init(top: TopPanel, bottom: BottomPanel, path: String?, service: ApiScanService) {
        if (path != null) {
            dir = path
        }
        topPanel = top
        bottomPanel = bottom
        apiService = service
        list = JBList(defaultListModel)
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        list.installCellRenderer {
            val label = it?.let { it1 -> JBLabel(it1.url) }
            label!!
        }
        ListSpeedSearch<ApiBO>(list)
        val panel = JBPanel<JBPanel<*>>()
        panel.setLayout(BorderLayout())
        val toolBar = createToolBar()
        panel.add(toolBar, BorderLayout.NORTH)
        panel.add(JBScrollPane(list), BorderLayout.CENTER)
        setLayout(BorderLayout())
        add(panel, BorderLayout.CENTER)
        initApis()

    }

    private fun createToolBar(): JBPanel<JBPanel<*>> {
        val panel = JBPanel<JBPanel<*>>()
        val layout = FlowLayout()
        layout.alignment = FlowLayout.LEFT
        panel.setLayout(layout)
        reloadBtn.preferredSize = Dimension(30, 30)
        reloadBtn.toolTipText = "刷新"
        panel.add(reloadBtn)
        configBtn.toolTipText = "设置"
        configBtn.preferredSize = Dimension(30, 30)
        configBtn.addActionListener {
            if (!isTipped) {
                JOptionPane.showMessageDialog(this, "请输入端口号")
                isTipped = true
            }
            port.isVisible = true
            configBtn.isVisible = false
            port.grabFocus()
        }
        panel.add(configBtn)
        port.toolTipText = "端口"
        port.isVisible = false
        port.addMouseListener(object : MouseAdapter() {
            override fun mouseExited(e: MouseEvent?) {
            port.isVisible = false
            configBtn.isVisible = true
        }})
        panel.add(port)

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
        search.addMouseListener(object : MouseAdapter() {
            override fun mouseExited(e: MouseEvent?) {
                searchBtn.isVisible = true
                search.isVisible = false
            }
        })
        panel.add(search)
        return panel
    }

    private fun addElement(s: Collection<ApiBO>) {
        s.sortedBy { e->e.url }.forEach {
            defaultListModel.addElement(it)
        }
    }

    private fun initApis() {
        val doScan = dir.let { apiService.doScan(it) }
        allApis.addAll(doScan)
        addElement(doScan)
        reload()
        select()
        doSearch()
    }

    private fun remove() {
        defaultListModel.clear()
    }

    private fun reload() {
        reloadBtn.addActionListener {
            println("reloading ")
            remove()
            initApis()
        }
    }

    private fun select() {
        list.addListSelectionListener {
            val select = getSelectValue()
            if (select != null && org.apache.commons.lang3.StringUtils.isNotBlank(select.url)) {
                val url = select.url
                val port = getPort()
                topPanel.setUrl("http://" + apiService.wrapUrl("localhost:$port/$url"))
                topPanel.setMethod(select.method)
                bottomPanel.setBody(select.param)
            }
        }
    }

    private fun getSelectValue(): ApiBO? {
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

    private fun getSearch(): String {

        return search.text
    }

    private fun doSearch() {
        search.whenKeyReleased {
            filterResult()
        }
    }

    private fun filterResult() {
        val searchTxt = getSearch()
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
            if (StringUtils.contains(apiBO.url,element.url)&&StringUtils.equals(element.method,apiBO.method)) {
                element.param=apiBO.param
            }
        }
        for (element in defaultListModel.elements()) {
            if (StringUtils.contains(apiBO.url,element.url)&&StringUtils.equals(element.method,apiBO.method)) {
                element.param=apiBO.param
            }
        }
    }
}


