package org.smartdot.idea.plugins.listeners

import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.wm.IdeFrame

internal class ApplicationActivationListener :
    ApplicationActivationListener {

    override fun applicationActivated(ideFrame: IdeFrame) {
        println("action "+ideFrame.project)
    }
}
