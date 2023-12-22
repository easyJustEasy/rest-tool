package org.smartdot.idea.plugins.toolWindow.ctrlPanel

import com.intellij.ui.*
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import org.bouncycastle.asn1.x500.style.RFC4519Style.o
import org.smartdot.idea.plugins.bo.ApiBO
import java.awt.BorderLayout
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.event.ListSelectionListener


class CtrlPanel() : JBPanel<JBPanel<*>>() {
    private var list: JBList<ApiBO?>
    private val defaultListModel = DefaultListModel<ApiBO>();
    private val btn: JButton = JButton("刷新")

    init {
        list = JBList<ApiBO?>(defaultListModel)
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.installCellRenderer {
            val label = it?.let { it1 -> JBLabel(it1.url) }
            label!!
        }
//        ListSpeedSearch<Any>(list)
        val panel = JPanel()
        panel.setLayout(BorderLayout())
        panel.add(JBScrollPane(list), BorderLayout.CENTER)
        panel.add(btn, BorderLayout.SOUTH)
        setLayout(BorderLayout())

        add(panel, BorderLayout.CENTER)

    }

    fun addElement(s: Collection<ApiBO>) {
        s.forEach {
            defaultListModel.addElement(it)
        }

    }

    fun remove() {
        defaultListModel.clear()

    }

    fun reload(l: ActionListener) {
        btn.addActionListener(l)
    }

    fun select(l: ListSelectionListener) {
        list.addListSelectionListener(l)
    }

    fun getSelectValue(): ApiBO {
        return list.selectedValue!!
    }
}