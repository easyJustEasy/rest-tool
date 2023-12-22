package org.smartdot.idea.plugins.toolWindow.ctrlPanel

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.*
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import org.smartdot.idea.plugins.bo.ApiBO
import org.smartdot.idea.plugins.consts.ProjectConsts
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionListener
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.border.LineBorder


class CtrlPanel() : JBPanel<JBPanel<*>>() {
    private var list: JBList<Any?>
    private val defaultListModel = DefaultListModel<Any>();
    private val btn: JButton = JButton("刷新")

    init {
        list = JBList<Any?>(defaultListModel)
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.
//        ListSpeedSearch<Any>(list)
        val panel = JPanel()
        panel.setLayout(BorderLayout())
        panel.add(JBScrollPane(list), BorderLayout.CENTER)
        panel.add(btn, BorderLayout.SOUTH)
        setLayout(BorderLayout())

        add(panel,BorderLayout.CENTER)

    }

    fun addElement(s: Collection<ApiBO>) {
        s.forEach {
            defaultListModel.addElement(it.url)
        }

    }

    fun remove() {
        defaultListModel.clear()

    }

    fun reload(l: ActionListener) {
        btn.addActionListener(l)
    }

}