package org.smartdot.idea.plugins.toolWindow.restPanel

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import org.smartdot.idea.plugins.toolWindow.apiPanel.ApiPanel
import org.smartdot.idea.plugins.toolWindow.restPanel.components.BottomPanel
import org.smartdot.idea.plugins.toolWindow.restPanel.components.SendPanel
import java.awt.BorderLayout

class RestPanel(
    project: Project,
    private var apiPanel: ApiPanel?,
) : JBPanel<JBPanel<*>>() {
    private var sendPanel: SendPanel
    private var bottomPanel: BottomPanel

    init {
        layout = BorderLayout(0, 0)
        bottomPanel = createBottomPanel(project)
        sendPanel = SendPanel(project, bottomPanel, apiPanel)
        add(sendPanel, BorderLayout.NORTH)
        add(bottomPanel, BorderLayout.CENTER)
    }

    private fun createBottomPanel(project: Project): BottomPanel = BottomPanel(project)

    fun updateApi(
        url: String,
        method: String,
        param: String?,
    ) {
        sendPanel.updateApi(url, method)
        bottomPanel.updateApi(param)
    }

    fun reportError(message: String?) {
        bottomPanel.reportError(message)
    }
}
