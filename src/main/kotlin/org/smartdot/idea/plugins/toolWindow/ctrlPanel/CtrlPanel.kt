package org.smartdot.idea.plugins.toolWindow.ctrlPanel

import com.intellij.icons.AllIcons
import com.intellij.openapi.observable.util.whenFocusLost
import com.intellij.ui.ListSpeedSearch
import com.intellij.ui.components.*
import org.apache.commons.lang.StringUtils
import org.smartdot.idea.plugins.bo.ApiBO
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.*
import javax.swing.*
import javax.swing.event.ListSelectionListener


class CtrlPanel() : JBPanel<JBPanel<*>>() {
    private var list: JBList<ApiBO?>
    private val defaultListModel = DefaultListModel<ApiBO>();
    private val reloadBtn: JButton = JButton("", AllIcons.Actions.Refresh)
    private val configBtn: JButton = JButton("", AllIcons.Actions.InlayGear)
    private val searchBtn: JButton = JButton("", AllIcons.Actions.Search)
    private val port: JBTextField = JBTextField()
    private val search: JBTextField = JBTextField()
    private val allApis: HashSet<ApiBO> = HashSet<ApiBO>()
    private var isTipped:Boolean=false

    init {
        list = JBList<ApiBO?>(defaultListModel)
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
        configBtn.addActionListener{
            if(!isTipped){
                JOptionPane.showMessageDialog(this, "请输入端口号")
                isTipped = true
            }
            port.isVisible = true
            configBtn.isVisible=false
            port.grabFocus()
        }
        panel.add(configBtn)
        port.toolTipText = "端口"
        port.isVisible=false
        port.whenFocusLost {
            port.isVisible = false
            configBtn.isVisible=true
        }
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
        search.addMouseListener(object :MouseAdapter(){
            override fun mouseExited(e: MouseEvent?) {
                searchBtn.isVisible = true
                search.isVisible = false
            }
        })
        panel.add(search)
        return panel
    }

    fun addElement(s: Collection<ApiBO>) {
        s.forEach {
            defaultListModel.addElement(it)
        }
    }

    fun initApis(s: Collection<ApiBO>) {
        allApis.addAll(s)
        addElement(s)
    }

    fun remove() {
        defaultListModel.clear()
    }

    fun reload(l: ActionListener) {
        reloadBtn.addActionListener(l)
    }

    fun select(l: ListSelectionListener) {
        list.addListSelectionListener(l)
    }

    fun getSelectValue(): ApiBO? {
        if (list.selectedValue == null) {
            return null;
        }
        return list.selectedValue!!
    }

    fun getPort(): Int {
        if (StringUtils.isNotEmpty(port.text)) {
            try {
                return Integer.parseInt(port.text)
            }catch (e:NumberFormatException){
                JOptionPane.showMessageDialog(this, "端口号只能是数字")
            }

        }
        return 8080
    }

    fun getSearch(): String {

        return search.text
    }

    fun doSearch(l: KeyAdapter) {
        search.addKeyListener(l)
    }

    fun filterResult() {
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
}


