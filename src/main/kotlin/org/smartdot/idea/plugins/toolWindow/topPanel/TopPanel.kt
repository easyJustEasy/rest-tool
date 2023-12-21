package org.smartdot.idea.plugins.toolWindow.topPanel

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBPanel
import org.smartdot.idea.plugins.Bundle
import java.awt.FlowLayout
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JTextField


class TopPanel() : JBPanel<JBPanel<*>>() {
    private  var select: ComboBox<String>
    private  var urlInput: JTextField
    private  var sendBtn: JButton

    init {
        val layout = FlowLayout()
        layout.alignment = FlowLayout.LEFT
        setLayout(layout)
        ComboBox<String>().also { select = it }
        select.addItem(Bundle.message("methodGet"))
        select.addItem(Bundle.message("methodPostForm"))
        select.addItem(Bundle.message("methodPostJson"))
        add(select)
        urlInput = JTextField(52)
        add(urlInput)
        sendBtn = JButton(Bundle.message("sendBtn"))
        add(sendBtn)
    }

    fun getUrl(): String {
        return urlInput.text;
    }

    fun getUrlMethod(): String {
        return select.selectedItem?.toString() ?: "";
    }

    fun addSendAction(l: ActionListener) {
        sendBtn.addActionListener(l)
    }
}